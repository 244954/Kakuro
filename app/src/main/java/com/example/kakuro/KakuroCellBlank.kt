package com.example.kakuro

class KakuroCellBlank(row: Int, column: Int) : KakuroCell(row, column) {
    override val essential = false
}