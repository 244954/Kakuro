package com.example.kakuro.gamelogic

import android.arch.lifecycle.MutableLiveData

class KakuroGame {

    var selectedCellLiveData = MutableLiveData<Pair<Int, Int>>()

    private var selectedRow = -1
    private var selectedCol = -1

    init {
        selectedCellLiveData.postValue(Pair(selectedRow, selectedCol))
    }

    fun updateteSelectedCell(row: Int, col: Int) {
        selectedRow = row
        selectedCol = col
        selectedCellLiveData.postValue(Pair(row, col))
    }
}