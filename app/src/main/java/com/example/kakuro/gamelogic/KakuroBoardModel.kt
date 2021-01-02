package com.example.kakuro.gamelogic

import com.example.kakuro.datahandling.DatabaseHelper

/*
    [how much in a sum, how many cells, row, column, direction (0 - right, 1 - down)]
    [10, 4, 3, 2, 0]
    [3, 2, 3, 2, 1]
 */
class KakuroBoardModel(val size: Int) {

    var board: Array<Array<KakuroCell?>> = Array(size) {
        arrayOfNulls<KakuroCell>(size)
    }

    constructor(size: Int, board: Array<Array<KakuroCell?>>) : this(size) {
        this.board = board
    }

    constructor(size: Int, values: Array<Array<Int>>) : this(size) {

        for (i in values) {
            when (i[4]) {
                0 -> { // goes to the right
                    board[i[2]][i[3] - 1] = KakuroCellHint(i[2], i[3] - 1, i[0], 0)
                    for (x in 0 until i[1]) {
                        board[i[2]][i[3] + x] = KakuroCellValue(i[2], i[3] + x, 0)
                    }
                }
                else -> { // goes down
                    if (board[i[2] - 1][i[3]] is KakuroCellHint) {
                        val cell = board[i[2] - 1][i[3]] as KakuroCellHint
                        cell.hintDown = i[0]
                    } else {
                        board[i[2] - 1][i[3]] = KakuroCellHint(i[2] - 1, i[3], 0, i[0])
                    }
                    for (x in 0 until i[1]) {
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

    fun deepCopy(): KakuroBoardModel {
        val newBoard: Array<Array<KakuroCell?>> = Array(size) {
            arrayOfNulls<KakuroCell>(size)
        }
        for (row in 0 until size) {
            for (col in 0 until size) {
                newBoard[row][col] = board[row][col]?.copy()
            }
        }
        return KakuroBoardModel(size, newBoard)
    }

    fun cleanBoard() {
        for (row in 0 until size) {
            for (col in 0 until size) {
                val cell = board[row][col]!!
                if (cell is KakuroCellValue) {
                    cell.value = 0
                    cell.wrongCol = false
                    cell.wrongRow = false
                    cell.candidates = emptyArray()
                }
            }
        }
    }

    fun checkForMistakenCells(solvedBoard: KakuroBoardModel): Pair<Int, Int>? {
        for (row in 0 until size) {
            for (col in 0 until size) {
                val cell = board[row][col]!!
                val corrCell = solvedBoard.getCell(row, col)
                if (cell is KakuroCellValue && corrCell is KakuroCellValue) {
                    if (cell.value != 0 && cell.value != corrCell.value) {
                        return Pair(row, col)
                    }
                }
            }
        }
        return null
    }

    fun cellWithMostFilledNeighbours(): Pair<Int, Int> {
        var maxRow = -1
        var maxCol = -1
        var maxNeighbours = -1f
        var maxFilledNeighbours = 0f
        for (row in 0 until size) {
            for (col in 0 until size) {
                val cell = board[row][col]!!
                if (cell is KakuroCellValue && cell.value == 0) { // consider empty value cells
                    var neighbours = 0f
                    var filledNeighbours = 0f
                    for (i in -1..1) {
                        for (j in -1..1) { // all neigbouring cells
                            if (row + i in 0 until size // if in range
                                && col + j in 0 until size
                                && board[row + i][col + j] is KakuroCellValue) { // if are also value cells
                                    neighbours += 1f
                                    if ((board[row + i][col + j] as KakuroCellValue).value != 0) { // is filled
                                        filledNeighbours += 1f
                                    }
                            }
                        }
                    }
                    if (neighbours != 0f // can't happen, but just to be safe
                        && filledNeighbours / neighbours >= maxFilledNeighbours / maxNeighbours) {
                            maxRow = row
                            maxCol = col
                            maxNeighbours = neighbours
                            maxFilledNeighbours = filledNeighbours
                    }
                }
            }
        }
        return Pair(maxRow, maxCol)
    }

    fun getCell(row: Int, col: Int): KakuroCell? {
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

    fun getRow(row: Int, col: Int): ArrayList<KakuroCellValue> {

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

    fun getColumn(row: Int, col: Int): ArrayList<KakuroCellValue> {

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
                            newRow++
                        }
                        coords.add(newElem)
                    }
                    if (cel.hintRight != 0) {
                        var newCol = col + 1
                        val newElem = ArrayList<Pair<Int, Int>>()
                        newElem.add(Pair(cel.hintRight, cel.hintRight))
                        while (newCol < size && board[row][newCol] is KakuroCellValue) {
                            newElem.add(Pair(row, newCol))
                            newCol++
                        }
                        coords.add(newElem)
                    }
                }
            }
        }

        return coords
    }

    fun getRowHint(row: Int, col: Int): KakuroCellHint? {
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

    fun getColumnHint(row: Int, col: Int): KakuroCellHint? {
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

    fun getSquare2x2(row: Int, col: Int): Array<Array<KakuroCellValue>>? {
        if (row >= size || col >= size || board[row][col] !is KakuroCellValue) {
            return null
        }
        if (row + 1 >= size || board[row + 1][col] !is KakuroCellValue) {
            return null
        }
        if (col + 1 >= size || board[row][col + 1] !is KakuroCellValue) {
            return null
        }
        if (board[row + 1][col + 1] !is KakuroCellValue) {
            return null
        }
        return arrayOf(
            arrayOf(board[row][col] as KakuroCellValue, board[row][col + 1] as KakuroCellValue),
            arrayOf(
                board[row + 1][col] as KakuroCellValue,
                board[row + 1][col + 1] as KakuroCellValue
            )
        )
    }

    fun getPossibleValues(row: Int, col: Int): ArrayList<Int> {
        val wholeRow = getRow(row, col)
        val wholeCol = getColumn(row, col)
        val possibles = arrayListOf(1, 2, 3, 4, 5, 6, 7, 8, 9)
        for (i in wholeRow) {
            possibles.remove(i.value)
        }
        for (i in wholeCol) {
            possibles.remove(i.value)
        }
        return possibles
    }

    fun isFinished(): Boolean {
        for (row in 0 until size) {
            for (col in 0 until size) {
                val cell = board[row][col]
                if (cell is KakuroCellValue) {
                    if (cell.value == 0 || cell.wrongRow || cell.wrongCol) {
                        return false
                    }
                }
            }
        }
        return true
    }

    fun setCell(row: Int, col: Int, value: Int) {
        if (row in 0 until size && col in 0 until size) {
            val cell = board[row][col]!!
            if (cell is KakuroCellValue) {
                cell.value = value
            }
        }
    }

    fun insertToDb(database: DatabaseHelper) {
        for (row in board.indices) {
            for (col in board.indices) {
                when(val cell = board[row][col]) {
                    is KakuroCellBlank -> {
                        database.insertDataBoard(row, col, 0, 0, 0) // 0 - blank
                    }
                    is KakuroCellValue -> {
                        database.insertDataBoard(row, col, 1, cell.value, 0) // 1 - value
                    }
                    is KakuroCellHint -> {
                        database.insertDataBoard(
                            row,
                            col,
                            2,
                            cell.hintRight,
                            cell.hintDown
                        ) // 2 - hint
                    }
                }
            }
        }
    }

    fun sameRowOrColumn(firstRow: Int, firstCol: Int, secondRow: Int, secondCol: Int): Boolean {
        if (firstRow in 0 until size && firstCol in 0 until size && secondCol in 0 until size && secondRow in 0 until size) {
            if (board[firstRow][firstCol] is KakuroCellValue && board[secondRow][secondCol] is KakuroCellValue) {
                if (getRowHint(firstRow, firstCol) === getRowHint(secondRow, secondCol) ||
                    getColumnHint(firstRow, firstCol) === getColumnHint(secondRow, secondCol)) {
                    return true
                }
            }
        }
        return false
    }

    fun isBoardCorrect(): Boolean{
        var valueCellsCount = 0
        for (row in 0 until size) {
            for (col in 0 until size) {
                if (board[row][col] is KakuroCellValue) {
                    valueCellsCount ++
                    if (getRowHint(row, col) == null || getColumnHint(row, col) == null) {
                        return false
                    }
                }
            }
        }
        return valueCellsCount > 0
    }

    fun recommendedHintsAmount(): Int{
        return when(size) {
            in 0..5 -> {
                1
            }
            in 6..8 -> {
                2
            }
            in 9..10 -> {
                3
            }
            else -> {
                4
            }
        }
    }
}