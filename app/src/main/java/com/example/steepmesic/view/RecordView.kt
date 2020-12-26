package com.example.steepmesic.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.example.steepmesic.R

class RecordView(context: Context, attrs: AttributeSet)
    : View(context, attrs) {

    companion object {
        const val longNeedleLength = 200f
        const val shortNeedleLength = 100f
        const val longShortNeedleDegree = -30f
        const val longConeLength = 50f
        const val shortConeLength = 25f
        const val needleWidth = 15f
        const val longConeWidth = 30f
        const val shortConeWidth = 50f
        const val armColor = "#888888"
        //唱针旋转点
        const val bigCircleRadius = 30f
        const val bigCircleColor = "#C0C0C0"
        const val smallCircleRadius = 15f
        const val smallCircleColor = "#8A8A8A"
        const val needleRotateSpeed = 1
        //唱片
        const val picRadius = 200f //唱片半径
        const val ringRadius = 100f //圆环半径
        const val ringColor = "#000000"
        const val discRotateSpeed = 1
        //起止角度
        const val PLAYING_DEGREE = -15f
        const val PAUSE_DEGREE = -45f
    }

    //唱针样式
    private val needlePaint = Paint().apply {
        strokeWidth = needleWidth
        color = Color.parseColor(armColor)
    }

    //唱盘样式
    private val discPaint = Paint().apply { color = Color.parseColor(ringColor) }
    //视图横向中点
    private val xHalf by lazy { measuredWidth / 2f }
    //唱针旋转点y值偏移
    private val yOffset by lazy { measuredHeight / 12f }
    //图片剪裁
    private val clipPath = Path().apply { addCircle(0f, 0f, picRadius, Path.Direction.CW) }
    //图片
    private val bm by lazy { BitmapFactory.decodeResource(resources, R.drawable.cover) }

    var isPlaying = false
        set(value) {field = value}
    var needleRadiusCounter = PAUSE_DEGREE
    private var discRadiusCounter = PAUSE_DEGREE

    //绘制唱针
    private fun drawNeedle(canvas: Canvas?, degree: Float) {
        canvas?.apply {
            save()

            needlePaint.strokeWidth = needleWidth
            translate(xHalf, yOffset)
            rotate(degree)
            drawLine(0f, 0f, 0f, longNeedleLength, needlePaint)

            translate(0f, longNeedleLength)
            rotate(longShortNeedleDegree)
            drawLine(0f, 0f, 0f, shortNeedleLength, needlePaint)

            translate(0f, shortNeedleLength)
            needlePaint.strokeWidth = longConeWidth
            drawLine(0f, 0f, 0f, longConeLength, needlePaint)

            translate(0f, longConeLength)
            needlePaint.strokeWidth = shortConeWidth
            drawLine(0f, 0f, 0f, shortConeLength, needlePaint)

            restore()

            // 两个重叠的圆形，唱针顶部的旋转点
            save()
            translate(xHalf, yOffset)
            needlePaint.setStyle(Paint.Style.FILL);
            needlePaint.setColor(Color.parseColor(bigCircleColor))
            drawCircle(0f, 0f, bigCircleRadius, needlePaint)
            needlePaint.setColor(Color.parseColor(smallCircleColor))
            drawCircle(0f, 0f, smallCircleRadius, needlePaint)
            restore()
        }
    }

    //绘制唱针的动态效果
    private fun drawNeedle(canvas: Canvas?) {
        if (isPlaying) {
            if (needleRadiusCounter < PLAYING_DEGREE) {
                needleRadiusCounter += needleRotateSpeed
            }
        } else {
            if (needleRadiusCounter > PAUSE_DEGREE) {
                needleRadiusCounter -= needleRotateSpeed
            }
        }
        drawNeedle(canvas, needleRadiusCounter)
    }

    //绘制唱盘
    private fun drawDisc(canvas: Canvas?, degree: Float) {
        canvas?.apply {
            save()
            translate(xHalf, yOffset+longNeedleLength+ringRadius+picRadius)
            rotate(degree)
            drawCircle(0f, 0f, picRadius + ringRadius, discPaint)
            clipPath(clipPath)
            drawBitmap(bm, Rect(0, 0, 400, 400),
                    Rect(-200, -200, 400, 400), discPaint)
            restore()
        }
    }

    private fun drawDisc(canvas: Canvas?) {
        if (isPlaying) discRadiusCounter = (discRadiusCounter + discRotateSpeed) % 360
        drawDisc(canvas, discRadiusCounter)
    }

    override fun onDraw(canvas: Canvas?) {
        drawDisc(canvas)
        drawNeedle(canvas)
    }

}