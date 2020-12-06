package com.example.kakuro.gamelogic

import java.util.*

data class Tile(val id: Int, var value: Int = 0) {
    companion object {
        const val initialValue = 0
    }
} // 0 is initial value, otherwise 1..9

class Tiles() {
    private var tiles: MutableList<Tile>

    init {
        tiles = mutableListOf()
    }

    constructor(tiles: MutableList<Tile>) {
        this.tiles = tiles
    }

    fun addTile(tile: Tile) {
        tiles.add(tile)
    }

    fun copy(): Tiles {
        val newTiles: MutableList<Tile> = mutableListOf()
        for (i in tiles) {
            newTiles.add(i.copy())
        }
        return Tiles(newTiles)
    }

    fun emptyCellsExist(): Boolean {
        for (i in tiles) {
            if (i.value == Tile.initialValue) {
                return true
            }
        }
        return false
    }

    operator fun get(index: Int) = tiles[index]
}

class BacktrackingSolver(private val model: KakuroBoardModel) {
    private val tiles = Tiles()
    private val lookup = SolverLookup()

    init {
        // to do
        tiles.addTile(Tile(0, 0))
        tiles.addTile(Tile(1, 0))
        tiles.addTile(Tile(2, 0))
        tiles.addTile(Tile(3, 0))
        lookup.addElement(0, arrayOf(1), arrayOf(1), 4, arrayOf(2), 3)
        lookup.addElement(1, arrayOf(3), arrayOf(0), 4, arrayOf(3), 12)
        lookup.addElement(2, arrayOf(2), arrayOf(3), 11, arrayOf(0), 3)
        lookup.addElement(3, arrayOf(3, 4, 5, 7, 8, 9), arrayOf(2), 11, arrayOf(1), 12)
    }

    fun solve() {
        val stack: Stack<Tiles> = Stack()
        var currentState = tiles
        var currentCell = currentState[0]
        var currentValue = lookup[currentCell.id]!!.first.min()
        stack.push(currentState)
        while (currentState.emptyCellsExist()) {

        }
    }


}