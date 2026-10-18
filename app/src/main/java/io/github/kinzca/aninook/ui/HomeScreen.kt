package io.github.kinzca.aninook.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.github.kinzca.aninook.imports.PendingModel
import io.github.kinzca.aninook.overlay.PetServiceState
import io.github.kinzca.aninook.preview.DiagnosticPetView
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    overlayAllowed: Boolean, notificationsAllowed: Boolean, serviceState: PetServiceState,
    previewActive: Boolean, sizeDp: Int, pendingModel: PendingModel?, importing: Boolean, feedback: String?,
    onOverlayPermission: () -> Unit, onNotificationPermission: () -> Unit,
    onStart: () -> Unit, onStop: () -> Unit, onSizeChange: (Int) -> Unit, onSizeFinished: () -> Unit,
    onImport: () -> Unit, onClearImport: () -> Unit
) {
    MaterialTheme(colorScheme = lightColorScheme(
        primary = Color(0xFF6851A5), onPrimary = Color.White,
        secondaryContainer = Color(0xFFECE5F8), background = Color(0xFFFAF8FD),
        surface = Color(0xFFFFFBFF), onSurface = Color(0xFF302A3C)
    )) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.safeDrawingPadding().fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("AniNook", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                    Text("0.1 · 验证版", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
                Text("让喜欢的角色，留在日常里。", style = MaterialTheme.typography.bodyLarge)
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF0EAF9)), shape = RoundedCornerShape(28.dp)) {
                    Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("一个小小的陪伴角落", style = MaterialTheme.typography.titleMedium)
                        AndroidView(factory = { DiagnosticPetView(it).apply { isClickable = true } },
                            modifier = Modifier.size(220.dp), update = { it.animate = previewActive },
                            onRelease = { it.animate = false })
                        Text("测试占位角色", style = MaterialTheme.typography.titleSmall)
                        Text("点击打招呼 · 开启后可拖动悬浮角色", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(if (serviceState.running) "陪伴已开启" else "准备好，就让它陪着你", style = MaterialTheme.typography.titleLarge)
                    Text("仅角色所在的小窗口接收触摸；窗口里的透明区域也可能挡住下方操作。",
                        style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = onStart, enabled = !serviceState.running, modifier = Modifier.weight(1f)) { Text("开始陪伴") }
                        OutlinedButton(onClick = onStop, enabled = serviceState.running, modifier = Modifier.weight(1f)) { Text("停止陪伴") }
                    }
                    Text("角色大小 · ${sizeDp}dp", style = MaterialTheme.typography.labelLarge)
                    Slider(value = sizeDp.toFloat(), onValueChange = { onSizeChange(it.roundToInt()) },
                        valueRange = 96f..240f, onValueChangeFinished = onSizeFinished)
                }
                Section("系统权限") {
                    PermissionRow("悬浮窗口", if (overlayAllowed) "已允许" else "显示在其他应用上方，需要你手动允许", onOverlayPermission)
                    PermissionRow("陪伴通知", if (notificationsAllowed) "已允许，可从通知停止陪伴" else "未允许，可在应用内停止陪伴", onNotificationPermission)
                    Text("锁屏时隐藏并暂停绘制，解锁后恢复；不会开机自动启动。", style = MaterialTheme.typography.bodySmall)
                }
                Section("你的角色文件") {
                    Text("当前仅保存 .inp / .inx 文件，最多 64 MiB。引擎尚未接入，导入文件不会替换上方占位角色。",
                        style = MaterialTheme.typography.bodyMedium)
                    if (pendingModel != null) {
                        Text(pendingModel.name, fontWeight = FontWeight.SemiBold)
                        Text("${pendingModel.bytes / 1024} KiB · 已保存，待兼容性验证", style = MaterialTheme.typography.bodySmall)
                    } else Text("还没有待验证的模型文件", style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = onImport, enabled = !importing) { Text(if (importing) "正在导入…" else "选择模型文件") }
                        if (pendingModel != null) TextButton(onClick = onClearImport, enabled = !importing) { Text("移除副本") }
                    }
                    Text("文件只保存在此设备，不上传。再次导入会替换本地待验证副本。", style = MaterialTheme.typography.bodySmall)
                }
                (feedback ?: serviceState.message)?.let {
                    Text(it, Modifier.fillMaxWidth().background(Color(0xFFECE5F8), RoundedCornerShape(16.dp)).padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium)
                }
                Text("技术验证版 · 未包含 Inochi2D、社区、广告或账号功能。",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}

@Composable
private fun PermissionRow(title: String, description: String, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(description, style = MaterialTheme.typography.bodySmall)
        }
        TextButton(onClick = onClick) { Text("设置") }
    }
}
