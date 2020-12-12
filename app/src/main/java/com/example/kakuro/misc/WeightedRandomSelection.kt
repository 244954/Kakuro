package com.example.kakuro.misc

import java.util.*

class WeightedRandomSelection<E> {
    private val map: NavigableMap<Double, E> = sortedMapOf<Double, E>() as NavigableMap<Double, E>
    private val random = Random()
    private var total = 0.0

    fun add(weight: Double, element: E): WeightedRandomSelection<E> {
        if (weight <= 0) {
            return this
        }
        total += weight
        map[total] = element
        return this
    }

    fun next(): E {
        val value = random.nextDouble() * total
        return map.higherEntry(value).value
    }
}