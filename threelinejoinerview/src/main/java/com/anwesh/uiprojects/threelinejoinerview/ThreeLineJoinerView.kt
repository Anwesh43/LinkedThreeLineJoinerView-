package com.anwesh.uiprojects.threelinejoinerview

/**
 * Created by anweshmishra on 30/08/20.
 */

import android.view.View
import android.view.MotionEvent
import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color

val colors : Array<Int> = arrayOf(
        "#bbd123",
        "#9812CC",
        "#14AB21",
        "#445AB",
        "#ddee12"
).map {Color.parseColor(it)}.toTypedArray()
val lines : Int = 3
val scGap : Float = 0.02f / lines
val strokeFactor : Float = 90f
val hFactor : Float = 2.4f
val delay : Long = 20
val backColor : Int = Color.parseColor("#BDBDBD")

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n))* n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()
fun Int.midExpand(n : Int) : Int = (n + 1) / 2 - Math.abs(this - (lines / 2))

fun Canvas.drawMultiJoinerLine(scale : Float, w : Float, h : Float, paint : Paint) {
    val sf : Float = scale.sinify()
    val sf1 : Float = sf.divideScale(0, lines)
    val gap : Float = w / lines
    val hGap : Float = h / ((lines + 1) / 2 * hFactor)
    for (j in 0..(lines - 1)) {
        val i : Int = j.midExpand(lines)
        val yCurr : Float = -hGap * i * sf1
        save()
        translate(gap / 2 + gap * i, h)
        drawLine(0f, 0f, 0f, yCurr, paint)
        if (i != lines - 1) {
            val sfj : Float = sf.divideScale(j + 1, lines)
            val iNext : Int = (j + 1).midExpand(lines)
            val y : Float = -hGap * iNext
            drawLine(0f, yCurr, gap * sfj, yCurr + (y - yCurr) * sfj, paint)
        }
        restore()
    }
}

fun Canvas.drawMJLNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.color = colors[i]
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    drawMultiJoinerLine(scale, w, h, paint)
}

class ThreeLineJoinerView(ctx : Context) : View(ctx) {

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }
}