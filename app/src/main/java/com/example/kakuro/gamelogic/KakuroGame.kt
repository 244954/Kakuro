package com.example.kakuro.gamelogic

import androidx.lifecycle.MutableLiveData

class KakuroGame(size: Int) {

    var selectedCellLiveData = MutableLiveData<Pair<Int, Int>>()
    var cellsLiveData = MutableLiveData<Array<Array<KakuroCell?>>>()

    private var selectedRow = -1
    private var selectedCol = -1

    private var timePassed: Long = 0

    private lateinit var board: KakuroBoardModel
    private lateinit var solver: BacktrackingSolver

    constructor(size: Int, values: Array<Array<Int>>) : this(size) { // constructor for simplified representation
        board = KakuroBoardModel(size, values)

        val newBoardForSolver = board.deepCopy()
        newBoardForSolver.cleanBoard()
        solver = BacktrackingSolver(newBoardForSolver)
        solver.solve()

        selectedCellLiveData.postValue(Pair(selectedRow, selectedCol))
        cellsLiveData.postValue(board.board)
    }

    constructor(size: Int, startedBoard: Array<Array<KakuroCell?>>) : this(size) { // constructor for saved games
        board = KakuroBoardModel(size, startedBoard)

        val newBoardForSolver = board.deepCopy()
        newBoardForSolver.cleanBoard()
        solver = BacktrackingSolver(newBoardForSolver)
        solver.solve()

        selectedCellLiveData.postValue(Pair(selectedRow, selectedCol))
        cellsLiveData.postValue(board.board)
    }

    fun isGameFinishedAndHandleInput(number: Int) : Boolean { // returns whether kakuro is finished
        if ( selectedRow == -1 || selectedCol == -1) return false

        val cell = board.getCell(selectedRow, selectedCol) as KakuroCellValue
        if ( cell.value == number) {
            cell.value = 0
        }
        else {
            cell.value = number
        }
        updateValidation(selectedRow, selectedCol, board)
        cellsLiveData.postValue(board.board)
        return board.isFinished()
    }

    fun solvePuzzle() {
        board = solver.getBoard().deepCopy()
        cellsLiveData.postValue(board.board)
    }

    fun clearBoard() {
        board.cleanBoard()
        cellsLiveData.postValue(board.board)
    }

    fun giveHint() {
        val coords = board.cellWithMostFilledNeighbours()
        val value = solver.getCellValue(coords.first, coords.second)
        board.setCell(coords.first, coords.second, value)
        cellsLiveData.postValue(board.board)
    }

    fun updateTime(timePassed: Long) {
        this.timePassed = timePassed
    }

    fun getTime() : Long {
        // val elapsedMillis = SystemClock.elapsedRealtime() - chronometerInstance.base // to get time in milis

        return timePassed
    }

    fun getCell(row: Int, col: Int) : KakuroCell? {
        if (row in 0 until board.size && col in 0 until board.size) {
            return board.getCell(row, col)
        }
        return null
    }

    fun updateteSelectedCell(row: Int, col: Int) {
        val cell = board.getCell(row, col)
        if (cell != null && cell.essential) {
            selectedRow = row
            selectedCol = col
        }
        else {
            selectedRow = -1
            selectedCol = -1
        }
        selectedCellLiveData.postValue(Pair(selectedRow, selectedCol))
    }

    private fun updateValidation(row: Int, col: Int, board: KakuroBoardModel) {
        updateRow(row, col, board)
        updateCol(row, col, board)
    }

    private fun updateRow(row: Int, col: Int, board: KakuroBoardModel) {
        val rowHint = board.getRowHint(row, col)?.hintRight
        val rowItems = board.getRow(row, col)
        val rowValues = rowItems.map { it.value }.toMutableList()

        if (rowValues.contains(0)) {
            rowValues.removeAll(listOf(0))

            if (rowValues.size != rowValues.distinct().size || (rowHint != null && rowValues.sum() > rowHint)) { // something repeats or it's too much
                rowItems.forEach {
                    it.wrongRow = true
                }
            }
            else {
                rowItems.forEach {
                    it.wrongRow = false
                }
            }
            return
        }

        if (rowValues.size != rowValues.distinct().size) { // something repeats, but with no zeros
            rowItems.forEach {
                it.wrongRow = true
            }
            return
        }
        if (rowItems.map { it.value }.sum() != rowHint) { // not exact sum
            rowItems.forEach {
                it.wrongRow = true
            }
            return
        }
        // else
        rowItems.forEach {
            it.wrongRow = false
        }
        return
    }

    private fun updateCol(row: Int, col: Int, board: KakuroBoardModel) {
        val colHint = board.getColumnHint(row, col)?.hintDown
        val colItems = board.getColumn(row, col)
        val colValues = colItems.map { it.value }.toMutableList()

        if (colValues.contains(0)) {
            colValues.removeAll(listOf(0))

            if (colValues.size != colValues.distinct().size || (colHint != null && colValues.sum() > colHint)) { // something repeats or it's too much
                colItems.forEach {
                    it.wrongCol = true
                }
            }
            else {
                colItems.forEach {
                    it.wrongCol = false
                }
            }
            return
        }

        if (colValues.size != colValues.distinct().size) { // something repeats, but with no zeros
            colItems.forEach {
                it.wrongCol = true
            }
            return
        }
        if (colItems.map { it.value }.sum() != colHint) { // not exact sum
            colItems.forEach {
                it.wrongCol = true
            }
            return
        }
        // else
        colItems.forEach {
            it.wrongCol = false
        }
        return
    }
}