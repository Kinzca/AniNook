package io.github.kinzca.aninook

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import io.github.kinzca.aninook.imports.PendingModel
import io.github.kinzca.aninook.imports.PendingModelStore
import io.github.kinzca.aninook.overlay.OverlayState
import io.github.kinzca.aninook.overlay.PetOverlayService
import io.github.kinzca.aninook.ui.HomeScreen
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : ComponentActivity() {
    private var overlayAllowed by mutableStateOf(false)
    private var notificationsAllowed by mutableStateOf(false)
    private var previewActive by mutableStateOf(false)
    private var sizeDp by mutableIntStateOf(160)
    private var pendingModel by mutableStateOf<PendingModel?>(null)
    private var importing by mutableStateOf(false)
    private var feedback by mutableStateOf<String?>(null)
    private val store by lazy { PendingModelStore(File(filesDir, "pending-model")) }
    private val preferences by lazy { getSharedPreferences("pet", MODE_PRIVATE) }

    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) {
        refreshPermissions()
        if (!it) feedback = "通知未开启；你仍可回到 AniNook 停止陪伴。"
    }
    private val openDocument = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) importModel(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        sizeDp = preferences.getInt("sizeDp", 160).coerceIn(96, 240)
        refreshModel()
        setContent {
            val serviceState by OverlayState.state.collectAsState()
            HomeScreen(
                overlayAllowed = overlayAllowed,
                notificationsAllowed = notificationsAllowed,
                serviceState = serviceState,
                previewActive = previewActive,
                sizeDp = sizeDp,
                pendingModel = pendingModel,
                importing = importing,
                feedback = feedback,
                onOverlayPermission = ::requestOverlayPermission,
                onNotificationPermission = {
                    if (Build.VERSION.SDK_INT >= 33) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    else startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, packageName))
                },
                onStart = ::startPet,
                onStop = { stopService(Intent(this, PetOverlayService::class.java)) },
                onSizeChange = { sizeDp = it },
                onSizeFinished = {
                    preferences.edit().putInt("sizeDp", sizeDp).apply()
                    if (OverlayState.state.value.running) startPet()
                },
                onImport = { openDocument.launch(arrayOf("*/*")) },
                onClearImport = {
                    lifecycleScope.launch {
                        try {
                            withContext(Dispatchers.IO) { store.clear() }
                            pendingModel = null
                            feedback = "已移除本地副本，原始文件未改变。"
                        } catch (error: Exception) { handleImportError(error) }
                    }
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        previewActive = true
        refreshPermissions()
        refreshModel()
    }

    override fun onPause() {
        previewActive = false
        super.onPause()
    }

    private fun refreshPermissions() {
        overlayAllowed = Settings.canDrawOverlays(this)
        notificationsAllowed = getSystemService(android.app.NotificationManager::class.java).areNotificationsEnabled()
    }

    private fun requestOverlayPermission() {
        try {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        } catch (_: android.content.ActivityNotFoundException) {
            feedback = "此设备没有提供悬浮权限设置页面，请在系统应用设置中查看。"
        }
    }

    private fun startPet() {
        refreshPermissions()
        if (!overlayAllowed) {
            feedback = "先开启悬浮权限，返回后点击开始陪伴。"
            requestOverlayPermission()
            return
        }
        try {
            ContextCompat.startForegroundService(this, Intent(this, PetOverlayService::class.java))
            feedback = null
        } catch (_: RuntimeException) {
            feedback = "系统暂未允许启动陪伴，请保持应用在前台后重试。"
        }
    }

    private fun refreshModel() {
        lifecycleScope.launch {
            try { pendingModel = withContext(Dispatchers.IO) { store.current() } }
            catch (error: Exception) { handleImportError(error) }
        }
    }

    private fun importModel(uri: Uri) {
        if (importing) return
        importing = true
        lifecycleScope.launch {
            try {
                pendingModel = withContext(Dispatchers.IO) {
                    val name = contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                        ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
                        ?: throw IllegalArgumentException("无法取得文件名，请从设备文件管理器选择模型。")
                    val input = contentResolver.openInputStream(uri)
                        ?: throw java.io.IOException("Cannot open document")
                    input.use { store.import(name, it) }
                }
                feedback = "文件已保存到本机。Inochi2D 引擎尚未接入，暂不能显示此模型。"
            } catch (error: Exception) { handleImportError(error) }
            finally { importing = false }
        }
    }

    private fun handleImportError(error: Exception) {
        if (error is CancellationException) throw error
        feedback = if (error is IllegalArgumentException || error is IllegalStateException) error.message
            else "文件操作未完成，请检查文件是否可读、存储空间是否足够后重试。"
    }
}
