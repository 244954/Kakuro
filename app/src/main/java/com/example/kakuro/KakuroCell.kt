package com.example.kakuro

abstract class KakuroCell(val row: Int,val column: Int) {
    abstract val essential: Boolean
}