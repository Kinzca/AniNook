package io.github.kinzca.aninook

import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import io.github.kinzca.aninook.overlay.PetOverlayService
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Real Android WindowManager integration on emulator/device; no Inochi2D claim. */
@RunWith(AndroidJUnit4::class)
class OverlaySmokeTest {
    @Test fun petAppearsOutsideAppCanMoveAndStops() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val device = UiDevice.getInstance(instrumentation)
        val selector = By.desc("测试占位角色，点击打招呼，按住拖动")
        device.executeShellCommand("appops set ${context.packageName} SYSTEM_ALERT_WINDOW allow")
        try {
            context.startActivity(Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            assertTrue("App did not open", device.wait(Until.hasObject(By.text("开始陪伴")), 10000))
            device.findObject(By.text("开始陪伴")).click()
            // Wait for the service to attach before leaving the visible activity.
            assertTrue("Foreground service did not start", device.wait(Until.hasObject(By.text("陪伴已开启")), 10000))
            device.pressHome()
            assertTrue("No overlay outside the app", device.wait(Until.hasObject(selector), 10000))
            val before = device.findObject(selector).visibleBounds
            device.drag(before.centerX(), before.centerY(), before.centerX() + 120, before.centerY() + 150, 30)
            device.waitForIdle()
            val after = device.findObject(selector).visibleBounds
            assertTrue("Overlay did not move", before.left != after.left || before.top != after.top)
            context.stopService(Intent(context, PetOverlayService::class.java))
            assertTrue("Overlay remained after stop", device.wait(Until.gone(selector), 10000))
        } finally {
            context.stopService(Intent(context, PetOverlayService::class.java))
            device.executeShellCommand("appops set ${context.packageName} SYSTEM_ALERT_WINDOW default")
        }
    }
}
