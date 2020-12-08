package com.example.kakuro.gamelogic

import java.util.*

class Tiles {
    private var tiles: MutableMap<Int, Int>

    constructor() {
        tiles = mutableMapOf()
    }

    constructor(tiles: MutableMap<Int, Int>) : this() {
        this.tiles = tiles
    }

    fun addTile(id: Int, value: Int) {
        this.tiles[id] = value
    }

    fun copy(): Tiles {
        val newTiles = tiles.toMutableMap()
        return Tiles(newTiles)
    }

    fun emptyCellsExist(): Boolean {
        for (i in tiles) {
            if (i.value == initialValue) {
                return true
            }
        }
        return false
    }

    fun noDuplicatesForTile(id: Int, lookup: SolverLookup): Boolean {
        val tileRowIds = lookup.getRowTilesIds(id)
        val tileColIds = lookup.getColumnTilesIds(id)
        val rowValuesArr: MutableList<Int> = mutableListOf()
        val colValuesArr: MutableList<Int> = mutableListOf()
        for (i in tileRowIds) {
            if (tiles[i]!! != 0) {
                rowValuesArr.add(tiles[i]!!) // zeros can be duplicated!
            }
        }
        for (i in tileColIds) {
            if (tiles[i]!! != 0) {
                colValuesArr.add(tiles[i]!!)
            }
        }
        if (rowValuesArr.size != rowValuesArr.distinct().size) {
            return false
        }
        if (colValuesArr.size != colValuesArr.distinct().size) {
            return false
        }
        return true
    }

    fun notExceededForTileOrCompleted(id: Int, lookup: SolverLookup): Boolean {
        val tileRowIds = lookup.getRowTilesIds(id)
        val tileColIds = lookup.getColumnTilesIds(id)
        val rowValuesArr: MutableList<Int> = mutableListOf()
        val colValuesArr: MutableList<Int> = mutableListOf()
        var emptyInRow = false
        var emptyInCol = false
        for (i in tileRowIds) {
            rowValuesArr.add(tiles[i]!!)
            if (tiles[i]!! == 0) {
                emptyInRow = true
            }
        }
        for (i in tileColIds) {
            colValuesArr.add(tiles[i]!!)
            if (tiles[i]!! == 0) {
                emptyInCol = true
            }
        }
        if ((rowValuesArr.sum() > lookup.getRowTilesSum(id) && emptyInRow) || (rowValuesArr.sum() != lookup.getRowTilesSum(id) && !emptyInRow)) {
            return false
        }
        if ((colValuesArr.sum() > lookup.getColumnTilesSum(id) && emptyInCol) || (colValuesArr.sum() != lookup.getColumnTilesSum(id) && !emptyInCol)) {
            return false
        }
        return true
    }

    fun underSumForTile(id: Int, lookup: SolverLookup): Boolean {
        val tileRowIds = lookup.getRowTilesIds(id)
        val tileColIds = lookup.getColumnTilesIds(id)
        val rowValuesArr: MutableList<Int> = mutableListOf()
        val colValuesArr: MutableList<Int> = mutableListOf()
        for (i in tileRowIds) {
            rowValuesArr.add(tiles[i]!!)
        }
        for (i in tileColIds) {
            colValuesArr.add(tiles[i]!!)
        }
        if (rowValuesArr.sum() > lookup.getRowTilesSum(id)) {
            return false
        }
        if (colValuesArr.sum() > lookup.getColumnTilesSum(id)) {
            return false
        }
        return true
    }

    fun nextAvailableCellIndex(currId: Int) : Int {
        /*
        for (i in tiles) {
            if (i.value != 0) {
                return i.key
            }
        }
        return 0 // not found

         */
        return currId + 1
    }

    fun prevAvailableIndex(currId: Int) : Int {
        return currId - 1
    }

    operator fun get(index: Int) = tiles[index]
    operator fun set(index: Int, value: Int) {
        tiles[index] = value
    }

    companion object {
        const val initialValue = 0
    }
}

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
        // to do
        /*
        tiles.addTile(0, 0)
        tiles.addTile(1, 0)
        tiles.addTile(2, 0)
        tiles.addTile(3, 0)
        tiles.addTile(4, 0)
        tiles.addTile(5, 0)
        tiles.addTile(6, 0)
        tiles.addTile(7, 0)
        tiles.addTile(8, 0)
        tiles.addTile(9, 0)
        lookup.addElement(0, arrayOf(7, 9), arrayOf(0, 1), 10, arrayOf(0, 2), 16)
        lookup.addElement(1, arrayOf(1, 2, 3), arrayOf(0, 1), 10, arrayOf(1, 3, 5), 6)
        lookup.addElement(2, arrayOf(7), arrayOf(2, 3, 4), 10, arrayOf(0, 2), 16)
        lookup.addElement(3, arrayOf(1, 2, 3), arrayOf(2, 3, 4), 10, arrayOf(1, 3, 5), 6)
        lookup.addElement(4, arrayOf(1, 2, 3), arrayOf(2, 3, 4), 10, arrayOf(4, 6, 8), 6)
        lookup.addElement(5, arrayOf(1, 2, 3), arrayOf(5, 6, 7), 13, arrayOf(1, 3, 5), 6)
        lookup.addElement(6, arrayOf(1, 2, 3), arrayOf(5, 6, 7), 13, arrayOf(4, 6, 8), 6)
        lookup.addElement(7, arrayOf(8, 9), arrayOf(5, 6, 7), 13, arrayOf(7, 9), 17)
        lookup.addElement(8, arrayOf(3), arrayOf(8, 9), 12, arrayOf(4, 6, 8), 6)
        lookup.addElement(9, arrayOf(8, 9), arrayOf(8, 9), 12, arrayOf(7, 9), 17)

         */
        calcAllPossibleValuesAndGiveIds()
        initTiles()
        calcLookup()
    }

    fun solve() {
        val calculatedTiles = backtrackingAlgorhitm()
        applyCalculatedResult(calculatedTiles)
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

    fun backtrackingAlgorhitm(): Tiles {
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

    fun applyCalculatedResult(tiles: Tiles) {
        for (row in 0 until size) {
            for (col in 0 until size) {
                if (idBoard[row][col] >= Tiles.initialValue) {
                    val cell = board[row][col] as KakuroCellValue
                    cell.value = tiles[idBoard[row][col]]!!
                }
            }
        }
    }

    private fun getPossibleValues(row: Int, col: Int): ArrayList<Int> {
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