package com.example.kakuro.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.kakuro.R
import com.example.kakuro.gamelogic.*

class KakuroBoardView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    private var boardSize = 5 // test
    private var cellSizePixels = 0F

    private var selectedRow = -1
    private var selectedColumn = -1

    private var cells: Array<Array<KakuroCell?>>? = null

    private var listener: KakuroBoardView.OnTouchListener? = null

    private val thickLinePaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.DKGRAY
        strokeWidth = 6F
    }

    private val thinLinePaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#77787a")
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

    private val textPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = Color.BLACK
        textSize = 84F
    }

    private val littleTextPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = Color.parseColor("#77787a")
        textSize = 64F
    }

    private val blankCellPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = Color.parseColor("#000308")
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
        drawText(canvas)
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

    private fun drawText(canvas: Canvas?) {
        cells?.forEach { boardRow->
            boardRow.forEach {
                val row = it!!.row
                val col = it!!.column
                when (it) {
                    is KakuroCellValue -> {
                        val cell = it as KakuroCellValue
                        if ( cell.value != 0) { // don't print zeros
                            val stringValue = cell.value.toString()

                            val textBounds = Rect()
                            textPaint.getTextBounds(stringValue, 0, stringValue.length, textBounds)
                            val textWidth = textPaint.measureText(stringValue)
                            val textHeight = textBounds.height()

                            canvas?.drawText(
                                stringValue, (col * cellSizePixels) + cellSizePixels / 2 - textWidth / 2,
                                (row * cellSizePixels) + cellSizePixels / 2 + textHeight / 2, textPaint
                            )
                        }
                    }
                    is KakuroCellBlank -> {
                        canvas?.drawRect(col * cellSizePixels, row * cellSizePixels, (col + 1) * cellSizePixels, (row + 1) * cellSizePixels, blankCellPaint)
                    }
                    is KakuroCellHint -> {
                        canvas?.drawRect(col * cellSizePixels, row * cellSizePixels, (col + 1) * cellSizePixels, (row + 1) * cellSizePixels, blankCellPaint)
                        canvas?.drawLine(col * cellSizePixels, row * cellSizePixels, (col + 1) * cellSizePixels, (row + 1) * cellSizePixels, thinLinePaint)
                        // jeszcze numerki
                        if (it.hintDown != 0) {
                            val stringValue = it.hintDown.toString()

                            val textBounds = Rect()
                            littleTextPaint.getTextBounds(stringValue, 0, stringValue.length, textBounds)
                            val textWidth = textPaint.measureText(stringValue)
                            val textHeight = textBounds.height()
                            canvas?.drawText(stringValue, (col * cellSizePixels) + cellSizePixels / 4 - textWidth / 2,
                                (row * cellSizePixels) + cellSizePixels * 3 / 4 + textHeight / 2, littleTextPaint)
                        }
                        if (it.hintRight != 0) {
                            val stringValue = it.hintRight.toString()

                            val textBounds = Rect()
                            littleTextPaint.getTextBounds(stringValue, 0, stringValue.length, textBounds)
                            val textWidth = textPaint.measureText(stringValue)
                            val textHeight = textBounds.height()
                            canvas?.drawText(stringValue, (col * cellSizePixels) + cellSizePixels * 3 / 4 - textWidth / 2,
                                (row * cellSizePixels) + cellSizePixels / 4 + textHeight / 2, littleTextPaint)
                        }
                    }
                }
            }
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
        val playerSelectedRow = (y / cellSizePixels).toInt()
        val playerSelectedCol = (x / cellSizePixels).toInt()
        listener?.onCellTouched(playerSelectedRow, playerSelectedCol)
    }

    fun updateSelectedCellUI(row: Int, col: Int) {
        selectedRow = row
        selectedColumn = col
        invalidate()
    }

    fun updateCells(cells: Array<Array<KakuroCell?>>) {
        this.cells = cells
        invalidate()
    }

    fun registerListener(listener: KakuroBoardView.OnTouchListener) {
        this.listener = listener
    }

    interface OnTouchListener {
        fun onCellTouched(row: Int, col: Int)
    }
}