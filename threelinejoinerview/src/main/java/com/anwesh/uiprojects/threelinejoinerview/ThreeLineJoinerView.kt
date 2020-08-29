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
