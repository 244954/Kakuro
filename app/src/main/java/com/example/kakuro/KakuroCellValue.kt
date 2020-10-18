package com.example.kakuro

class KakuroCellValue(row: Int, column: Int,var value: Int = 0, var candidates: Array<Int> = emptyArray()) : KakuroCell(row, column) {
    override val essential = true
}