package com.touchawesome.medicalwheel

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import kotlin.math.*

/**
 * Created by georgi.chakarov on 2/5/18
 */
class Wheel : View, GestureDetector.OnGestureListener {
    override fun onShowPress(e: MotionEvent?) {
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        return false
    }

    override fun onDown(e: MotionEvent?): Boolean {
        return false
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
        val velocity = max(abs(velocityX), abs(velocityY))

        if (abs(velocity) > 10) {
            val animator = ValueAnimator.ofFloat(0f, Math.toRadians(720.0).toFloat()).setDuration(5000)
            animator.addUpdateListener {
                rotateAngle += (lastDirection * 1)
                rotateAngle %= 360
                invalidate()
            }
            animator.start()
            return true
        }
        return false
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        return false
    }

    override fun onLongPress(e: MotionEvent?) {
    }

    private val wheelPaint: Paint = Paint()
    private val borderPaint: Paint = Paint()
    private val centerPoint: PointF = PointF()
    private val ringsCount = 8
    private var rotateAngle = 0f
    private var lastDirection = 0
    private val startMovePoint: PointF = PointF()
    private val endMovePoint: PointF = PointF()
    private val gestureDetector = GestureDetector(context, this)

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        wheelPaint.strokeCap = Paint.Cap.ROUND
        wheelPaint.isAntiAlias = true

        borderPaint.strokeCap = Paint.Cap.ROUND
        borderPaint.isAntiAlias = true
        borderPaint.color = ContextCompat.getColor(context, R.color.borderCircleWhite)
        borderPaint.strokeWidth = 4f
    }

    private fun getRadiusForRing(i: Int): Float {
        return (height - i * getRingWidth()) / 4
    }

    private fun getRingWidth(): Float {
        return height.toFloat() / ringsCount
    }

    private fun magnitude(start: PointF, end: PointF): Float {
        return sqrt(((end.x - start.x) * (end.x - start.x)
                + (end.y - start.y) * (end.y - start.y)).toDouble()).toFloat()
    }

    private fun dotProduct(a: PointF, b: PointF): Float {
        return a.x * b.x + a.y * b.y
    }

    private fun getDirection(a: PointF, b: PointF, c: PointF): Int {
        val face = (a.x * b.y + b.x * c.y + c.x * a.y - b.y * c.x - c.y * a.x - a.y * b.x) / 2
        return when {
            face > 0 -> 1
            face < 0 -> -1
            else -> 0
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        gestureDetector.onTouchEvent(event)

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                startMovePoint.set(event.x, event.y)
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                endMovePoint.set(event.x, event.y)

                val vectorStart = PointF(startMovePoint.x - centerPoint.x, startMovePoint.y - centerPoint.y)
                val vectorEnd = PointF(endMovePoint.x - centerPoint.x, endMovePoint.y - centerPoint.y)
                val magnitudeStart = magnitude(centerPoint, startMovePoint)
                val magnitudeEnd = magnitude(centerPoint, endMovePoint)
                val dotProduct = dotProduct(vectorStart, vectorEnd)

                val angle = acos(dotProduct.toDouble() / (magnitudeStart * magnitudeEnd))
                if (!angle.isNaN()) {
                    lastDirection = getDirection(startMovePoint, centerPoint, endMovePoint)
                    rotateAngle += Math.toDegrees(angle).toFloat() * lastDirection
                }

                startMovePoint.set(endMovePoint)
                invalidate()
                return true
            }
            MotionEvent.ACTION_OUTSIDE, MotionEvent.ACTION_UP -> {
                return performClick()
            }
        }

        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        // apply rotation
        canvas?.rotate(-rotateAngle, centerPoint.x, centerPoint.y)

        // set the center point to the bottom of the screen
        centerPoint.set(width.toFloat() / 2, height.toFloat() / 2)

        // draw the rings
        for (i in 0..ringsCount) {
            when (i) {
                0 -> {
                    wheelPaint.color = ContextCompat.getColor(context, R.color.borderCircleBlue)
                }
                1 -> {
                    wheelPaint.color = ContextCompat.getColor(context, R.color.subBorderCircleBlue)
                }
                else -> {
                    if (i % 2 == 0)
                        wheelPaint.color = ContextCompat.getColor(context, R.color.evenCircleBlue)
                    else
                        wheelPaint.color = ContextCompat.getColor(context, R.color.oddCircleBlue)
                }
            }
            wheelPaint.strokeWidth = getRingWidth()
            canvas?.drawCircle(centerPoint.x, centerPoint.y, getRadiusForRing(i), wheelPaint)
        }

        // draw the sections
        for (i in 0 until 36) {
            val angle: Double = Math.toRadians(10.0 * i)
            val startX = centerPoint.x + sin(angle) * getRadiusForRing(2)
            val startY = centerPoint.y + cos(angle) * getRadiusForRing(2)
            val stopX = centerPoint.x + sin(angle) * getRadiusForRing(1)
            val stopY = centerPoint.y + cos(angle) * getRadiusForRing(1)

            if (i == 0) {
                borderPaint.color = Color.RED
            }
            else {
                borderPaint.color = ContextCompat.getColor(context, R.color.borderCircleWhite)
            }

            canvas?.drawLine(startX.toFloat(), startY.toFloat(), stopX.toFloat(), stopY.toFloat(), borderPaint)
        }

        // revert rotation
        canvas?.rotate(0f)
    }
}
