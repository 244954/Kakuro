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

    public companion object {
        const val initialValue = 0
    }
}

class BacktrackingSolver() {
    private val tiles = Tiles()
    private val lookup = SolverLookup()

    init {
        // to do
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
    }

    fun solve(): Tiles {
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
            }
        }
        return currentState
    }


}