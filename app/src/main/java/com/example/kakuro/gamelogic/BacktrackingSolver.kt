package com.example.kakuro.gamelogic

import com.example.kakuro.misc.Tiles
import java.util.*

class BacktrackingSolver(private val model: KakuroBoardModel) {

    private val size = model.size
    private val board = model.board // shortcuts
    private val posBoard: Array<Array<ArrayList<Int>>> = Array(size) {
        Array(size) {
            ArrayList<Int>()
        }
    }
    private val idBoard: Array<Array<Int>> = Array(size) {
        Array(size) {
            Tiles.initialValue - 1 // -1
        }
    }

    private val tiles = Tiles()
    private val lookup = SolverLookup()

    init {
        calcAllPossibleValuesAndGiveIds()
        initTiles()
        calcLookup()
    }

    fun solve() {
        if(lookup.valid()) {
            val calculatedTiles = backtrackingAlgorithm()
            applyCalculatedResult(calculatedTiles)
        }
        else {
            // invalid board!
        }
    }

    fun manySolutions(): Boolean {
        if (lookup.valid()) {
            val res = backtrackingAlgorithmMoreSolutions()
            if (res) {
                println("---------More than one solution found!-------")
            }
            else {
                println("-----------Only one solution found!---------")
            }
            return res
        }
        return false
    }

    fun mostDubiousField(): Triple<Int, Int, Int> { // row, col, value
        var maxRow = -1
        var maxCol = -1
        var maxVal = 0
        for (row in 0 until size) {
            for (col in 0 until size) {
                if (posBoard[row][col].isNotEmpty()) {
                    if (posBoard[row][col].size > maxVal) {
                        maxRow = row
                        maxCol = col
                        maxVal = posBoard[row][col].size
                    }
                }
            }
        }
        return Triple(maxRow, maxCol, maxVal)
    }

    private fun calcAllPossibleValuesAndGiveIds() {
        var currId = 0
        for (row in 0 until size) {
            for (col in 0 until size) {
                val cell = board[row][col]
                if (cell is KakuroCellValue) {
                    val pos = getPossibleValues(row, col)
                    pos.sort() // important, we need to go from smallest
                    posBoard[row][col] = pos
                    idBoard[row][col] = currId
                    currId ++
                }
            }
        }
    }

    private fun initTiles() {
        for (row in 0 until size) {
            for (col in 0 until size) {
                if (idBoard[row][col] >= Tiles.initialValue) {
                    tiles.addTile(idBoard[row][col], 0)
                }
            }
        }
    }

    private fun calcLookup() {
        for (row in 0 until size) {
            for (col in 0 until size) {
                if (idBoard[row][col] >= Tiles.initialValue) {
                    val a = posBoard[row][col]
                    val rows = model.getRow(row, col).map { Pair(it.row, it.column) }
                    val cols = model.getColumn(row, col).map { Pair(it.row, it.column) }
                    val indexArrRow = mutableListOf<Int>()
                    val indexArrCol = mutableListOf<Int>()
                    for (i in rows) {
                        indexArrRow.add(idBoard[i.first][i.second])
                    }
                    for (i in cols) {
                        indexArrCol.add(idBoard[i.first][i.second])
                    }
                    lookup.addElement(idBoard[row][col],posBoard[row][col].toTypedArray(),indexArrRow.toTypedArray(), model.getRowHint(row, col)!!.hintRight, indexArrCol.toTypedArray(), model.getColumnHint(row, col)!!.hintDown)
                }
            }
        }
    }

    private fun backtrackingAlgorithmMoreSolutions(): Boolean {
        val stack: Stack<Tiles> = Stack()
        var currentState = tiles
        var currentCellId = 0
        var currentValue = lookup[currentCellId]!!.first.min()
        var numberOfOperations = 0
        var correctSolutions = 0
        stack.push(currentState.copy()) // insert copy
        while (currentState.emptyCellsExist()) {
            currentState[currentCellId] = currentValue!!
            numberOfOperations++
            if (currentState.noDuplicatesForTile(currentCellId, lookup) && currentState.notExceededForTileOrCompleted(currentCellId, lookup)) {
                // remember we have to copy state at some point
                if(currentState.emptyCellsExist()) {
                    stack.push(currentState.copy()) // insert copy
                    currentCellId = currentState.nextAvailableCellIndex(currentCellId)
                    currentValue = lookup[currentCellId]!!.first.min()
                }
                else {
                    correctSolutions ++
                    if (correctSolutions > 1) {
                        return true
                    }
                    if (currentState.lastSolution(lookup)) { // we reached the end
                        return false
                    }
                    currentState = stack.pop()
                    currentCellId = currentState.prevAvailableIndex(currentCellId) // makeshift solution, we process them in order of their placement
                    currentValue = currentState[currentCellId]
                    while (!lookup.hasNextValue(currentCellId, currentValue!!)) {
                        currentState = stack.pop()
                        currentCellId = currentState.prevAvailableIndex(currentCellId)
                        if (currentCellId < 0) {
                            return false
                        }
                        currentValue = currentState[currentCellId]
                    }
                    currentValue = lookup.nextValue(currentCellId, currentValue)
                    if (currentValue == 0) { // it means board had no more solutions
                        return false
                    }

                }
            }
            else if ((currentState.underSumForTile(currentCellId, lookup) || !currentState.noDuplicatesForTile(currentCellId, lookup)) && lookup.hasNextValue(currentCellId, currentValue)) {
                currentState[currentCellId] = Tiles.initialValue // zero it before it gets considered by while loop
                currentValue = lookup.nextValue(currentCellId, currentValue)
            }
            else {
                currentState = stack.pop()
                currentCellId = currentState.prevAvailableIndex(currentCellId) // makeshift solution, we process them in order of their placement
                currentValue = currentState[currentCellId]
                while (!lookup.hasNextValue(currentCellId, currentValue!!)) {
                    currentState = stack.pop()
                    currentCellId = currentState.prevAvailableIndex(currentCellId)
                    if (currentCellId < 0) {
                        return false
                    }
                    currentValue = currentState[currentCellId]
                }
                currentValue = lookup.nextValue(currentCellId, currentValue)
                if (currentValue == 0) { // it means board had no more solutions
                    return false
                }
            }
        }
        return false // technically it should never reach this
    }

    private fun backtrackingAlgorithm(): Tiles {
        val stack: Stack<Tiles> = Stack()
        var currentState = tiles
        var currentCellId = 0
        var currentValue = lookup[currentCellId]!!.first.min()
        var numberOfOperations = 0
        stack.push(currentState.copy()) // insert copy
        while (currentState.emptyCellsExist()) {
            currentState[currentCellId] = currentValue!!
            numberOfOperations++
            if (currentState.noDuplicatesForTile(currentCellId, lookup) && currentState.notExceededForTileOrCompleted(currentCellId, lookup)) {
                stack.push(currentState.copy()) // insert copy
                // remember we have to copy state at some point
                if(currentState.emptyCellsExist()) {
                    currentCellId = currentState.nextAvailableCellIndex(currentCellId)
                }
                currentValue = lookup[currentCellId]!!.first.min()
            }
            else if ((currentState.underSumForTile(currentCellId, lookup) || !currentState.noDuplicatesForTile(currentCellId, lookup)) && lookup.hasNextValue(currentCellId, currentValue)) {
                currentState[currentCellId] = Tiles.initialValue // zero it before it gets considered by while loop
                currentValue = lookup.nextValue(currentCellId, currentValue)
            }
            else {
                currentState = stack.pop()
                currentCellId = currentState.prevAvailableIndex(currentCellId) // makeshift solution, we process them in order of their placement
                currentValue = currentState[currentCellId]
                while (!lookup.hasNextValue(currentCellId, currentValue!!)) {
                    currentState = stack.pop()
                    currentCellId = currentState.prevAvailableIndex(currentCellId)
                    currentValue = currentState[currentCellId]
                }
                currentValue = lookup.nextValue(currentCellId, currentValue)
                if (currentValue == 0) { // it means an error occured or board had no solution
                    return currentState
                }
            }
        }
        return currentState
    }

    private fun applyCalculatedResult(tiles: Tiles) {
        for (row in 0 until size) {
            for (col in 0 until size) {
                if (idBoard[row][col] >= Tiles.initialValue) {
                    val cell = board[row][col] as KakuroCellValue
                    cell.value = tiles[idBoard[row][col]]!!
                }
            }
        }
    }

    fun getPossibleValues(row: Int, col: Int): ArrayList<Int> {
        var possibles = ArrayList<Int>()
        val wholeRow = model.getRow(row, col)
        val wholeCol = model.getColumn(row, col)
        val rowHint = model.getRowHint(row, col)
        val colHint = model.getColumnHint(row, col)

        val rowCombinations = calcCombinations(rowHint!!.hintRight, wholeRow.size)
        for(i in wholeRow) {
            if (i.value != 0) { // it can't be me! Cannot discard my own value
                rowCombinations.removeIf { j -> !j.contains(i.value)}
            }
        }
        val colCombinations = calcCombinations(colHint!!.hintDown, wholeCol.size)
        for(i in wholeCol) {
            if (i.value != 0) {
                colCombinations.removeIf { j -> !j.contains(i.value) }
            }
        }

        val combinations = mergeCombinations(rowCombinations, colCombinations)
        possibles = combinations.toCollection(ArrayList())

        for(i in wholeRow) {
            possibles.remove(i.value)
        }
        for(i in wholeCol) {
            possibles.remove(i.value)
        }


        return possibles
    }

    companion object{
        private fun minsum(length: Int): Int {
            var sum = 0
            for (i in 1..length) {
                sum += i
            }
            return sum
        }

        private fun maxsum(length: Int): Int {
            var sum = 0
            for (i in (10 - length)..9) {
                sum += i
            }
            return sum
        }

        fun calcCombinations(sum: Int, length: Int): ArrayList<Array<Int>> {
            val combinations = ArrayList<Array<Int>>()

            if (sum in 3..45 && length in 2..9 && sum in minsum(length)..maxsum(length)) {
                calcCombinationsRec(length, 0, Array<Int>(length){ 0 }, combinations, sum)
            }

            return combinations
        }

        private fun calcCombinationsRec(length: Int, start: Int, result: Array<Int>, combinations: ArrayList<Array<Int>>, sum: Int) {
            if (length == 0) {
                if (result.sum() == sum) {
                    combinations.add(result.clone())
                }
                return
            }
            for (i in start..(9 - length)) {
                result[result.size - length] = i + 1
                calcCombinationsRec(length - 1, i + 1, result, combinations, sum)
            }
        }

        fun mergeCombinations(arr1: ArrayList<Array<Int>>, arr2: ArrayList<Array<Int>>): Array<Int> {
            var combinations = Array<Int>(0) { 0 }
            var combinations2 = Array<Int>(0) { 0 }

            for (i in arr1) {
                combinations = combinations.union(i.toSet()).toTypedArray()
            }

            for (i in arr2) {
                combinations2 = combinations2.union(i.toSet()).toTypedArray()
            }

            return combinations.intersect(combinations2.toSet()).toTypedArray()
        }
    }

}