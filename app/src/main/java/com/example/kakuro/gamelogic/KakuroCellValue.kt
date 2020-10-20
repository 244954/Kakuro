package com.example.kakuro.gamelogic

class KakuroCellValue(row: Int, column: Int,var value: Int = 0, var candidates: Array<Int> = emptyArray(), var wrongRow: Boolean = false, var wrongCol: Boolean = false) : KakuroCell(row, column) {
    override val essential = true
}