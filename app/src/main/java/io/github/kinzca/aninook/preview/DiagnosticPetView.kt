package io.github.kinzca.aninook.preview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.SystemClock
import android.view.View
import kotlin.math.sin

/** Original geometric test mascot. This is deliberately NOT an Inochi2D renderer. */
class DiagnosticPetView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var wavingUntil = 0L
    var animate = false
        set(value) { field = value; invalidate() }
    init {
        contentDescription = "测试占位角色，点击打招呼，按住拖动"
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
    }

    override fun performClick(): Boolean {
        super.performClick()
        wavingUntil = SystemClock.uptimeMillis() + 1600
        announceForAccessibility("你好呀！")
        invalidate()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val now = SystemClock.uptimeMillis()
        val happy = now < wavingUntil
        val blink = animate && now % 4200 < 150
        val scale = minOf(width, height) / 240f
        canvas.save()
        canvas.scale(scale, scale)
        canvas.translate(0f, if (animate) sin(now / 700.0).toFloat() * 3 else 0f)
        paint.style = Paint.Style.FILL
        paint.color = Color.argb(24, 75, 48, 119)
        canvas.drawOval(65f, 213f, 175f, 226f, paint)
        paint.color = Color.rgb(105, 80, 159)
        canvas.drawRoundRect(60f, 40f, 180f, 184f, 58f, 58f, paint)
        paint.color = Color.rgb(224, 214, 251)
        canvas.drawRoundRect(77f, 145f, 163f, 211f, 30f, 30f, paint)
        paint.color = Color.rgb(255, 234, 221)
        canvas.drawOval(73f, 59f, 167f, 159f, paint)
        paint.color = Color.rgb(140, 111, 196)
        val fringe = Path().apply {
            moveTo(64f, 99f); cubicTo(58f, 22f, 184f, 22f, 176f, 105f)
            lineTo(143f, 76f); lineTo(131f, 103f); lineTo(107f, 73f)
            lineTo(86f, 100f); close()
        }
        canvas.drawPath(fringe, paint)
        paint.color = Color.rgb(66, 48, 95)
        if (blink || happy) {
            paint.style = Paint.Style.STROKE; paint.strokeWidth = 4f; paint.strokeCap = Paint.Cap.ROUND
            canvas.drawArc(87f, 108f, 105f, 120f, 190f, 160f, false, paint)
            canvas.drawArc(135f, 108f, 153f, 120f, 190f, 160f, false, paint)
            paint.style = Paint.Style.FILL
        } else {
            canvas.drawOval(93f, 106f, 103f, 123f, paint)
            canvas.drawOval(137f, 106f, 147f, 123f, paint)
            paint.color = Color.WHITE
            canvas.drawCircle(99f, 110f, 2.5f, paint)
            canvas.drawCircle(143f, 110f, 2.5f, paint)
        }
        paint.color = Color.rgb(239, 163, 175)
        canvas.drawOval(80f, 124f, 99f, 132f, paint)
        canvas.drawOval(141f, 124f, 160f, 132f, paint)
        paint.color = Color.rgb(163, 96, 121)
        canvas.drawArc(111f, 126f, 129f, 140f, 0f, 180f, false, paint)
        paint.color = Color.rgb(255, 218, 131)
        canvas.drawCircle(163f, 76f, 9f, paint)
        paint.color = Color.rgb(105, 80, 159)
        canvas.drawRoundRect(85f, 199f, 110f, 220f, 8f, 8f, paint)
        canvas.drawRoundRect(130f, 199f, 155f, 220f, 8f, 8f, paint)
        if (happy) {
            paint.color = Color.rgb(105, 80, 159); paint.textSize = 19f; paint.textAlign = Paint.Align.CENTER
            canvas.drawText("你好呀！", 120f, 27f, paint)
        }
        canvas.restore()
        if (animate && isShown) postInvalidateDelayed(33)
    }
}
