package com.example.kakuro

/*
    [how much in a sum, how many cells, row, column, direction (0 - right, 1 - down)]
    [10, 4, 3, 2, 0]
    [3, 2, 3, 2, 1]
 */
class KakuroBoardModel(val size: Int, values: Array<Array<Int>>) {

    var board: Array<Array<KakuroCell?>> = Array(size) {
        arrayOfNulls<KakuroCell>(size)
    }

    init {
        for (i in values) {
            when (i[4]) {
                0 -> { // goes to the right
                    board[i[2]][i[3] - 1] = KakuroCellHint(i[2],i[3] - 1,i[0],0)
                    for ( x in 0 until i[1]) {
                        board[i[2]][i[3] + x] = KakuroCellValue(i[2],i[3] + x, 0)
                    }
                }
                else -> { // goes down
                    if (board[i[2] - 1][i[3]] is KakuroCellHint)
                    {
                        val cell = board[i[2] - 1][i[3]] as KakuroCellHint
                        cell.hintDown = i[0]
                    }
                    else
                    {
                        board[i[2] - 1][i[3]] = KakuroCellHint(i[2] - 1,i[3],0,i[0])
                    }
                    for ( x in 0 until i[1]) {
                        board[i[2] + x][i[3]] = KakuroCellValue(i[2] + x,i[3], 0)
                    }
                }
            }
        }
        for (i in 0 until size) {
            for (j in 0 until size) {
                board[i][j] = board[i][j] ?: KakuroCellBlank(i, j) // elvis operator
            }
        }
    }
}