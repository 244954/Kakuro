package com.example.kakuro.gamelogic

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
                    board[i[2]][i[3] - 1] = KakuroCellHint(i[2], i[3] - 1, i[0], 0)
                    for ( x in 0 until i[1]) {
                        board[i[2]][i[3] + x] = KakuroCellValue(i[2], i[3] + x, 0)
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
                        board[i[2] - 1][i[3]] = KakuroCellHint(i[2] - 1, i[3], 0, i[0])
                    }
                    for ( x in 0 until i[1]) {
                        board[i[2] + x][i[3]] = KakuroCellValue(i[2] + x, i[3], 0)
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

    fun getCell(row: Int, col: Int) : KakuroCell? {
        /*
        if (board[row][col] is KakuroCellValue)
        {
            return board[row][col] as KakuroCellValue
        }
        return null
        */
        return if (row in 0 until size && col in 0 until size)
            board[row][col]!!
        else
            null
    }

    fun getRow(row: Int, col: Int) : ArrayList<KakuroCellValue> {

        val cells = ArrayList<KakuroCellValue>()

        if (row in 0 until size && col in 0 until size) {
            val cell = board[row][col]!!
            if (cell is KakuroCellValue) {

                cells.add(cell)
                var currCol = col - 1

                while (currCol > 0 && board[row][currCol] is KakuroCellValue) {
                    cells.add(board[row][currCol]!! as KakuroCellValue)
                    currCol -= 1
                }

                currCol = col + 1

                while (currCol < size && board[row][currCol] is KakuroCellValue) {
                    cells.add(board[row][currCol]!! as KakuroCellValue)
                    currCol += 1
                }
            }
        }
        return cells
    }

    fun getColumn(row: Int, col: Int) : ArrayList<KakuroCellValue> {

        val cells = ArrayList<KakuroCellValue>()

        if (row in 0 until size && col in 0 until size) {
            val cell = board[row][col]!!
            if (cell is KakuroCellValue) {

                cells.add(cell)
                var currRow = row - 1

                while (currRow > 0 && board[currRow][col] is KakuroCellValue) {
                    cells.add(board[currRow][col]!! as KakuroCellValue)
                    currRow -= 1
                }

                currRow = row + 1

                while (currRow < size && board[currRow][col] is KakuroCellValue) {
                    cells.add(board[currRow][col]!! as KakuroCellValue)
                    currRow += 1
                }
            }
        }
        return cells
    }

    fun getAllRowsAndCols(): ArrayList<ArrayList<Pair<Int, Int>>> { // array of all hint coordinates, first is always a pair of <hint,hint>
        val coords = ArrayList<ArrayList<Pair<Int, Int>>>()

        for (row in 0 until size) {
            for (col in 0 until size) {
                val cel = board[row][col]
                if (cel is KakuroCellHint) {
                    if (cel.hintDown != 0) {
                        var newRow = row + 1
                        val newElem = ArrayList<Pair<Int, Int>>()
                        newElem.add(Pair(cel.hintDown, cel.hintDown))
                        while (newRow < size && board[newRow][col] is KakuroCellValue) {
                            newElem.add(Pair(newRow, col))
                            newRow ++
                        }
                        coords.add(newElem)
                    }
                    if (cel.hintRight != 0) {
                        var newCol = col + 1
                        val newElem = ArrayList<Pair<Int, Int>>()
                        newElem.add(Pair(cel.hintRight, cel.hintRight))
                        while (newCol < size && board[row][newCol] is KakuroCellValue) {
                            newElem.add(Pair(row, newCol))
                            newCol ++
                        }
                        coords.add(newElem)
                    }
                }
            }
        }

        return coords
    }

    fun getRowHint(row: Int, col: Int) : KakuroCellHint? {
        if (row in 0 until size && col in 0 until size) {
            var cell = board[row][col]!!
            if (cell is KakuroCellValue) {
                var currCol = col - 1

                while (currCol >= 0) {
                    cell = board[row][currCol]!!
                    if (cell is KakuroCellHint) {
                        return cell
                    }
                    currCol -= 1
                }
            }
        }
        return null
    }

    fun getColumnHint(row: Int, col: Int) : KakuroCellHint? {
        if (row in 0 until size && col in 0 until size) {
            var cell = board[row][col]!!
            if (cell is KakuroCellValue) {
                var currRow = row - 1

                while (currRow >= 0) {
                    cell = board[currRow][col]!!
                    if (cell is KakuroCellHint) {
                        return cell
                    }
                    currRow -= 1
                }
            }
        }
        return null
    }
}