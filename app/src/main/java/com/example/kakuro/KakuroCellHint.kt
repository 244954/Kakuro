package com.example.kakuro

class KakuroCellHint(row: Int, column: Int,val hintRight: Int = 0, val hintDown: Int = 0) : KakuroCell(row, column) {
    override val essential = false
}