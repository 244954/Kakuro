package com.example.kakuro.gamelogic

class KakuroCellHint(row: Int, column: Int,var hintRight: Int = 0, var hintDown: Int = 0) : KakuroCell(row, column) {
    override val essential = false
}