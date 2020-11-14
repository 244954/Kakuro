package com.example.kakuro.gamelogic

import android.arch.lifecycle.MutableLiveData

class KakuroGame(size: Int, values: Array<Array<Int>>) {

    var selectedCellLiveData = MutableLiveData<Pair<Int, Int>>()
    var cellsLiveData = MutableLiveData<Array<Array<KakuroCell?>>>()

    private var selectedRow = -1
    private var selectedCol = -1

    private var timePassed: Long = 0

    private var board: KakuroBoardModel = KakuroBoardModel(size, values)

    init {
        selectedCellLiveData.postValue(Pair(selectedRow, selectedCol))
        cellsLiveData.postValue(board.board)
    }

    fun handleInput(number: Int) {
        if ( selectedRow == -1 || selectedCol == -1) return

        val cell = board.getCell(selectedRow, selectedCol) as KakuroCellValue
        if ( cell.value == number) {
            cell.value = 0
        }
        else {
            cell.value = number
        }
        updateValidation(selectedRow, selectedCol, board)
        cellsLiveData.postValue(board.board)
    }

    fun solvePuzzle() {
        val solver = KakuroSolver(board)
        solver.solveTrivial()
        cellsLiveData.postValue(board.board)
    }

    fun updateTime(timePassed: Long) {
        this.timePassed = timePassed
    }

    fun getTime() : Long {
        // val elapsedMillis = SystemClock.elapsedRealtime() - chronometerInstance.base // to get time in milis

        return timePassed
    }

    fun updateteSelectedCell(row: Int, col: Int) {
        val cell = board.getCell(row, col)
        if (cell != null && cell.essential) {
            selectedRow = row
            selectedCol = col
        }
        else {
            selectedRow = -1
            selectedCol = -1
        }
        selectedCellLiveData.postValue(Pair(selectedRow, selectedCol))
    }

    private fun updateValidation(row: Int, col: Int, board: KakuroBoardModel) {
        updateRow(row, col, board)
        updateCol(row, col, board)
    }

    private fun updateRow(row: Int, col: Int, board: KakuroBoardModel) {
        val rowHint = board.getRowHint(row, col)?.hintRight
        val rowItems = board.getRow(row, col)
        var sum = 0
        for (i in rowItems) {
            if (i.value == 0) {
                rowItems.forEach {
                    it.wrongRow = false
                }
                return
            }
            sum += i.value
        }
        if (sum != rowHint) {
            rowItems.forEach {
                it.wrongRow = true
            }
        }
        else {
            val values = rowItems.map { it.value }
            if (values.distinct().size != values.size) {
                rowItems.forEach {
                    it.wrongRow = true
                }
            }
            else {
                rowItems.forEach {
                    it.wrongRow = false
                }
            }
        }
    }

    private fun updateCol(row: Int, col: Int, board: KakuroBoardModel) {
        val colHint = board.getColumnHint(row, col)?.hintDown
        val colItems = board.getColumn(row, col)
        var sum = 0
        for (i in colItems) {
            if (i.value == 0) {
                colItems.forEach {
                    it.wrongCol = false
                }
                return
            }
            sum += i.value
        }
        if (sum != colHint) {
            colItems.forEach {
                it.wrongCol = true
            }
        }
        else {
            val values = colItems.map { it.value }
            if (values.distinct().size != values.size) {
                colItems.forEach {
                    it.wrongCol = true
                }
            }
            else {
                colItems.forEach {
                    it.wrongCol = false
                }
            }
        }
    }
}