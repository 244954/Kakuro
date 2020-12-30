package com.example.kakuro.gamelogic

abstract class KakuroCell(val row: Int,val column: Int) {
    abstract val essential: Boolean

    abstract fun copy(): KakuroCell
}