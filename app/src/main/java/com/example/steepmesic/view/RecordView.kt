package com.example.steepmesic.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.View

class RecordView(context: Context, attrs: AttributeSet)
    : View(context, attrs) {

    companion object {
        const val longNeedleLength = 250f
        const val shortNeedleLength = 100f
        const val longShortNeedleDegree = -30f
        const val longConeLength = 50f
        const val shortConeLength = 25f
        const val needleWidth = 15f
        const val longConeWidth = 30f
        const val shortConeWidth = 50f
        const val armColor = "#888888"
        //起止角度
        const val PLAYING_DEGREE = -15f
        const val PAUSE_DEGREE = -45f
    }

    private fun drawNeedle(canvas: Canvas, degree: Float) {
        val paint = Paint()
        paint.strokeWidth = needleWidth
        paint.color = Color.parseColor(armColor)
        canvas.apply {
            save()
            translate(200f, 100f)
            rotate(degree)
            drawLine(0f, 0f, 0f, longNeedleLength, paint)

            translate(0f, longNeedleLength)
            rotate(longShortNeedleDegree)
            drawLine(0f, 0f, 0f, shortNeedleLength, paint)

            translate(0f, shortNeedleLength)
            paint.strokeWidth = longConeWidth
            drawLine(0f, 0f, 0f, longConeLength, paint)

            translate(0f, longConeLength)
            paint.strokeWidth = shortConeWidth
            drawLine(0f, 0f, 0f, shortConeLength, paint)

            restore()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.apply { drawNeedle(canvas, PLAYING_DEGREE) }
    }

}