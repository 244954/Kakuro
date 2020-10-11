package com.example.kakuro

/*
    [how much in a sum, how many cells, row, column, direction (0 - right, 1 - down)]
    [10, 4, 3, 2, 0]
    [3, 2, 3, 2, 1]
 */
class KakuroBoardModel(val size: Int, values: Array<Array<Int>>) {

    val board: Array<Array<KakuroCell?>> = Array(size) {
        arrayOfNulls<KakuroCell>(size)
    }

    init {

        /*
        for (i in values) {
            when (i[4]) {
                0 -> { // goes to the right

                }
                else -> { // goes down

                }
            }
        }
        */
    }
}