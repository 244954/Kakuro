package com.example.kakuro.misc

import com.example.kakuro.gamelogic.SolverLookup

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