package com.example.kakuro

class KakuroCellValue(row: Int, column: Int,val value: Int = 0) : KakuroCell(row, column) {
    override val essential = true
}