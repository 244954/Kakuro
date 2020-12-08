package com.example.kakuro.gamelogic

class SolverLookup() {
    // ID -> {[values],<[IDs], Sum>,<[IDs], Sum>}
    private val dictionary: MutableMap<Int, Triple<Array<Int>, Pair<List<Int>, Int>, Pair<List<Int>, Int>>> = mutableMapOf()

    fun addElement(id: Int, values: Array<Int>, tiles1: Array<Int>, sum1: Int, tiles2: Array<Int>, sum2: Int) {
        dictionary[id] = Triple(values, Pair(tiles1.toList(), sum1), Pair(tiles2.toList(), sum2))
    }

    fun getRowTilesIds(id: Int): List<Int> {
        return this[id]!!.second.first
    }

    fun getColumnTilesIds(id: Int): List<Int> {
        return this[id]!!.third.first
    }

    fun getRowTilesSum(id: Int): Int {
        return this[id]!!.second.second
    }

    fun getColumnTilesSum(id: Int): Int {
        return this[id]!!.third.second
    }

    fun hasNextValue(id: Int, prevNum: Int): Boolean {
        if (this[id]!!.first.last() == prevNum) {
            return false
        }
        return true
    }

    fun nextValue(id: Int, prevNum: Int): Int {
        for (i in this[id]!!.first.indices) {
            if (this[id]!!.first[i] == prevNum) {
                return this[id]!!.first[i + 1]
            }
        }
        return 0 // not found
    }

    operator fun get(index: Int) = dictionary[index]
}