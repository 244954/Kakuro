package com.example.kakuro

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class KakuroBoardView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    private val linePaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.DKGRAY
        strokeWidth = 4F
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.drawRect(0F, 0F, width.toFloat(), width.toFloat(), linePaint)
    }
}