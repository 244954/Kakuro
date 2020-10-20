package com.example.kakuro.gamelogic

import android.arch.lifecycle.MutableLiveData

class KakuroGame {

    var selectedCellLiveData = MutableLiveData<Pair<Int, Int>>()
    var cellsLiveData = MutableLiveData<Array<Array<KakuroCell?>>>()

    private var selectedRow = -1
    private var selectedCol = -1

    private var board: KakuroBoardModel

    init {
        var kakuroBoardRaw : Array<Array<Int>> = arrayOf(
            arrayOf(8, 2, 1 ,3, 0),
            arrayOf(24, 4, 2, 1 ,0),
            arrayOf(18, 4, 3, 1, 0),
            arrayOf(9, 2, 4, 1, 0),
            arrayOf(7, 3, 2, 1, 1),
            arrayOf(23, 3, 2, 2, 1),
            arrayOf(23, 3, 1, 3, 1),
            arrayOf(6, 3, 1, 4, 1)
        )

        board = KakuroBoardModel(5, kakuroBoardRaw)

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
        // board.getCell(selectedRow, selectedCol)?.value = number
        cellsLiveData.postValue(board.board)

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
}