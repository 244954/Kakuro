package com.example.kakuro.gamelogic

class KakuroCellBlank(row: Int, column: Int) : KakuroCell(row, column) {
    override val essential = false
}