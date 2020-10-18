package com.example.kakuro.gamelogic

class KakuroCellValue(row: Int, column: Int,var value: Int = 0, var candidates: Array<Int> = emptyArray()) : KakuroCell(row, column) {
    override val essential = true
}