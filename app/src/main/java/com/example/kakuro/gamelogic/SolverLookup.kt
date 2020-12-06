package com.example.kakuro.gamelogic

class SolverLookup() {
    // ID -> {[values],<[IDs], Sum>,<[IDs], Sum>}
    private val dictionary: MutableMap<Int, Triple<Array<Int>, Pair<List<Int>, Int>, Pair<List<Int>, Int>>> = mutableMapOf()

    fun addElement(id: Int, values: Array<Int>, tiles1: Array<Int>, sum1: Int, tiles2: Array<Int>, sum2: Int) {
        dictionary[id] = Triple(values, Pair(tiles1.toList(), sum1), Pair(tiles2.toList(), sum2))
    }

    operator fun get(index: Int) = dictionary[index]
}