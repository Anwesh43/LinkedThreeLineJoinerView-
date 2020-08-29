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
import android.util.Log

val colors : Array<Int> = arrayOf(
        "#bb3123",
        "#9812CC",
        "#14AB21",
        "#4452AB",
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
    val hGap : Float = h / (((lines + 1) / 2) * hFactor)
    for (j in 0..(lines - 1)) {
        val i : Int = j.midExpand(lines)
        val yCurr : Float = -hGap * i * sf1
        Log.d("yCurr:$j", "$yCurr")
        save()
        translate(gap / 2 + gap * j, h)
        drawLine(0f, 0f, 0f, yCurr, paint)
        if (j != lines - 1) {
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

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            Log.d("scale", "$scale")
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

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class TLJNode(var i : Int, val state : State = State()) {

        private var next : TLJNode? = null
        private var prev : TLJNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = TLJNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawMJLNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : TLJNode {
            var curr : TLJNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class ThreeLineJoiner(var i : Int) {

        private var curr : TLJNode = TLJNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : ThreeLineJoinerView) {

        private val animator : Animator = Animator(view)
        private val tlj : ThreeLineJoiner = ThreeLineJoiner(0)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            tlj.draw(canvas, paint)
            animator.animate {
                tlj.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            tlj.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : ThreeLineJoinerView {
            val view : ThreeLineJoinerView = ThreeLineJoinerView(activity)
            activity.setContentView(view)
            return view
        }
    }
}