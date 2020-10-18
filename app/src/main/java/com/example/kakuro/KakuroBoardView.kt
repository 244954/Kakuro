package com.example.kakuro

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class KakuroBoardView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    private var boardSize = 5 // test
    private var cellSizePixels = 0F

    private var selectedRow = -1
    private var selectedColumn = -1

    private var kakuroBoardRaw : Array<Array<Int>> = arrayOf(
        arrayOf(8, 2, 1 ,3, 0),
        arrayOf(24, 4, 2, 1 ,0),
        arrayOf(18, 4, 3, 1, 0),
        arrayOf(9, 2, 4, 1, 0),
        arrayOf(7, 3, 2, 1, 1),
        arrayOf(23, 3, 2, 2, 1),
        arrayOf(23, 3, 1, 3, 1),
        arrayOf(6, 3, 1, 4, 1)
    )

    private var kakuroBoard = KakuroBoardModel(boardSize, kakuroBoardRaw)

    private val thickLinePaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.DKGRAY
        strokeWidth = 6F
    }

    private val thinLinePaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.DKGRAY
        strokeWidth = 3F
    }

    private val selectedCellPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = Color.parseColor(resources.getString(R.string.selectedCellColor))
    }

    private val errorCellPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = Color.parseColor(resources.getString(R.string.errorCellColor))
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val sizePixels = Math.min(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(sizePixels, sizePixels)
        // assume kakuros are always square
    }

    override fun onDraw(canvas: Canvas?) {
        cellSizePixels = (width / boardSize).toFloat()

        fillCells(canvas)
        drawLines(canvas)
    }

    private fun fillCells(canvas: Canvas?) {
        canvas?.drawRect(selectedColumn * cellSizePixels, selectedRow * cellSizePixels, (selectedColumn + 1) * cellSizePixels, (selectedRow + 1) * cellSizePixels, selectedCellPaint)
    }

    private fun drawLines(canvas: Canvas?) {
        canvas?.drawRect(0F, 0F, width.toFloat(), width.toFloat(), thickLinePaint)
        for (i in 1 until boardSize) {
            canvas?.drawLine(0F, i * cellSizePixels, width.toFloat(), i * cellSizePixels, thinLinePaint)
            canvas?.drawLine(i * cellSizePixels, 0F, i * cellSizePixels, height.toFloat(), thinLinePaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                handleTouchEvent(event.x, event.y)
                true
            }
            else -> false
        }
    }

    private fun handleTouchEvent(x: Float, y: Float) {
        selectedRow = (y / cellSizePixels).toInt()
        selectedColumn = (x / cellSizePixels).toInt()
        invalidate() // draw again
    }
}