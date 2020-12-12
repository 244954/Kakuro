package com.example.kakuro.gamelogic

import com.example.kakuro.misc.WeightedRandomSelection
import kotlin.random.Random

class BoardGenerator {

    var size: Int = 0

    fun generate(): Array<Array<KakuroCell?>> {
        val random = Random.nextInt(3)
        val board: Array<Array<KakuroCell?>>?
        board = when (random) {
            0-> {
                empty3x3Board()
            }
            1-> {
                empty5x5Board()
            }
            else-> {
                empty8x8Board()
            }
        }
        fillRandomly(board)
        // correct
        fillHints(board)
        zeroValuesInBoard(board)
        return board
    }

    private fun empty3x3Board(): Array<Array<KakuroCell?>> {
        size = 3
        val row0: Array<KakuroCell?> = arrayOf(KakuroCellBlank(0, 0), KakuroCellHint(0, 1), KakuroCellHint(0, 2))
        val row1: Array<KakuroCell?> = arrayOf(KakuroCellHint(1, 0), KakuroCellValue(1, 1), KakuroCellValue(1, 2))
        val row2: Array<KakuroCell?> = arrayOf(KakuroCellHint(2, 0), KakuroCellValue(2, 1), KakuroCellValue(2, 2))
        return arrayOf(row0, row1, row2)
    }

    private fun empty5x5Board(): Array<Array<KakuroCell?>> {
        size = 5
        var row = 0
        when(Random.nextInt(3)) {
            0 -> { // star formation
                val row0: Array<KakuroCell?> = arrayOf(KakuroCellBlank(row, 0), KakuroCellBlank(row, 1), KakuroCellHint(row, 2), KakuroCellHint(row, 3), KakuroCellBlank(row, 4))
                row = 1
                val row1: Array<KakuroCell?> = arrayOf(KakuroCellBlank(row, 0), KakuroCellHint(row, 1), KakuroCellValue(row, 2), KakuroCellValue(row, 3), KakuroCellHint(row, 4))
                row = 2
                val row2: Array<KakuroCell?> = arrayOf(KakuroCellHint(row, 0), KakuroCellValue(row, 1), KakuroCellValue(row, 2), KakuroCellValue(row, 3), KakuroCellValue(row, 4))
                row = 3
                val row3: Array<KakuroCell?> = arrayOf(KakuroCellHint(row, 0), KakuroCellValue(row, 1), KakuroCellValue(row, 2), KakuroCellValue(row, 3), KakuroCellValue(row, 4))
                row = 4
                val row4: Array<KakuroCell?> = arrayOf(KakuroCellBlank(row, 0), KakuroCellHint(row, 1), KakuroCellValue(row, 2), KakuroCellValue(row, 3), KakuroCellBlank(row, 4))
                return arrayOf(row0, row1, row2, row3, row4)
            }
            1 -> { // stairs formation
                val row0: Array<KakuroCell?> = arrayOf(KakuroCellBlank(row, 0), KakuroCellBlank(row, 1), KakuroCellBlank(row, 2), KakuroCellHint(row, 3), KakuroCellHint(row, 4))
                row = 1
                val row1: Array<KakuroCell?> = arrayOf(KakuroCellBlank(row, 0), KakuroCellBlank(row, 1), KakuroCellHint(row, 2), KakuroCellValue(row, 3), KakuroCellValue(row, 4))
                row = 2
                val row2: Array<KakuroCell?> = arrayOf(KakuroCellBlank(row, 0), KakuroCellHint(row, 1), KakuroCellValue(row, 2), KakuroCellValue(row, 3), KakuroCellValue(row, 4))
                row = 3
                val row3: Array<KakuroCell?> = arrayOf(KakuroCellHint(row, 0), KakuroCellValue(row, 1), KakuroCellValue(row, 2), KakuroCellValue(row, 3), KakuroCellBlank(row, 4))
                row = 4
                val row4: Array<KakuroCell?> = arrayOf(KakuroCellHint(row, 0), KakuroCellValue(row, 1), KakuroCellValue(row, 2), KakuroCellBlank(row, 3), KakuroCellBlank(row, 4))
                return arrayOf(row0, row1, row2, row3, row4)
            }
            else -> { // bricks formation
                val row0: Array<KakuroCell?> = arrayOf(KakuroCellBlank(row, 0), KakuroCellHint(row, 1), KakuroCellHint(row, 2), KakuroCellHint(row, 3), KakuroCellBlank(row, 4))
                row = 1
                val row1: Array<KakuroCell?> = arrayOf(KakuroCellHint(row, 0), KakuroCellValue(row, 1), KakuroCellValue(row, 2), KakuroCellValue(row, 3), KakuroCellBlank(row, 4))
                row = 2
                val row2: Array<KakuroCell?> = arrayOf(KakuroCellHint(row, 0), KakuroCellValue(row, 1), KakuroCellValue(row, 2), KakuroCellValue(row, 3), KakuroCellHint(row, 4))
                row = 3
                val row3: Array<KakuroCell?> = arrayOf(KakuroCellBlank(row, 0), KakuroCellHint(row, 1), KakuroCellValue(row, 2), KakuroCellValue(row, 3), KakuroCellValue(row, 4))
                row = 4
                val row4: Array<KakuroCell?> = arrayOf(KakuroCellBlank(row, 0), KakuroCellHint(row, 1), KakuroCellValue(row, 2), KakuroCellValue(row, 3), KakuroCellValue(row, 4))
                return arrayOf(row0, row1, row2, row3, row4)
            }
        }
    }

    private fun empty8x8Board(): Array<Array<KakuroCell?>> {
        size = 9
        var row = 0
        when(Random.nextInt(3)) {
            0-> { // virus formation
                val row0: Array<KakuroCell?> = arrayOf(KakuroCellBlank(row, 0), KakuroCellHint( row, 1), KakuroCellHint( row, 2), KakuroCellBlank(row, 3), KakuroCellHint(row, 4), KakuroCellHint(row, 5), KakuroCellBlank(row, 6), KakuroCellHint(row, 7), KakuroCellHint(row, 8))
                row = 1
                val row1: Array<KakuroCell?> = arrayOf(KakuroCellHint(row, 0), KakuroCellValue( row, 1), KakuroCellValue( row, 2), KakuroCellHint(row, 3), KakuroCellValue(row, 4), KakuroCellValue(row, 5), KakuroCellHint(row, 6), KakuroCellValue(row, 7), KakuroCellValue(row, 8))
                row = 2
                val row2: Array<KakuroCell?> = arrayOf(KakuroCellHint(row, 0), KakuroCellValue( row, 1), KakuroCellValue( row, 2), KakuroCellValue(row, 3), KakuroCellValue(row, 4), KakuroCellValue(row, 5), KakuroCellValue(row, 6), KakuroCellValue(row, 7), KakuroCellValue(row, 8))
                row = 3
                val row3: Array<KakuroCell?> = arrayOf(KakuroCellBlank(row, 0), KakuroCellHint( row, 1), KakuroCellHint( row, 2), KakuroCellValue(row, 3), KakuroCellValue(row, 4), KakuroCellValue(row, 5), KakuroCellValue(row, 6), KakuroCellHint(row, 7), KakuroCellHint(row, 8))
                row = 4
                val row4: Array<KakuroCell?> = arrayOf(KakuroCellHint(row, 0), KakuroCellValue( row, 1), KakuroCellValue( row, 2), KakuroCellValue(row, 3), KakuroCellBlank(row, 4), KakuroCellHint(row, 5), KakuroCellValue(row, 6), KakuroCellValue(row, 7), KakuroCellValue(row, 8))
                row = 5
                val row5: Array<KakuroCell?> = arrayOf(KakuroCellHint(row, 0), KakuroCellValue( row, 1), KakuroCellValue( row, 2), KakuroCellValue(row, 3), KakuroCellHint(row, 4), KakuroCellHint(row, 5), KakuroCellValue(row, 6), KakuroCellValue(row, 7), KakuroCellValue(row, 8))
                row = 6
                val row6: Array<KakuroCell?> = arrayOf(KakuroCellBlank(row, 0), KakuroCellHint( row, 1), KakuroCellHint( row, 2), KakuroCellValue(row, 3), KakuroCellValue(row, 4), KakuroCellValue(row, 5), KakuroCellValue(row, 6), KakuroCellHint(row, 7), KakuroCellHint(row, 8))
                row = 7
                val row7: Array<KakuroCell?> = arrayOf(KakuroCellHint(row, 0), KakuroCellValue( row, 1), KakuroCellValue( row, 2), KakuroCellValue(row, 3), KakuroCellValue(row, 4), KakuroCellValue(row, 5), KakuroCellValue(row, 6), KakuroCellValue(row, 7), KakuroCellValue(row, 8))
                row = 8
                val row8: Array<KakuroCell?> = arrayOf(KakuroCellHint(row, 0), KakuroCellValue( row, 1), KakuroCellValue( row, 2), KakuroCellHint(row, 3), KakuroCellValue(row, 4), KakuroCellValue(row, 5), KakuroCellHint(row, 6), KakuroCellValue(row, 7), KakuroCellValue(row, 8))
                return arrayOf(row0, row1, row2, row3, row4, row5, row6, row7, row8)
            }
            else-> { // bird formation
                val row0: Array<KakuroCell?> = arrayOf(KakuroCellBlank(row, 0), KakuroCellHint( row, 1), KakuroCellHint( row, 2), KakuroCellBlank(row, 3), KakuroCellBlank(row, 4), KakuroCellHint(row, 5), KakuroCellHint(row, 6), KakuroCellBlank(row, 7), KakuroCellBlank(row, 8))
                row = 1
                val row1: Array<KakuroCell?> = arrayOf(KakuroCellHint(row, 0), KakuroCellValue( row, 1), KakuroCellValue( row, 2), KakuroCellHint(row, 3), KakuroCellHint(row, 4), KakuroCellValue(row, 5), KakuroCellValue(row, 6), KakuroCellHint(row, 7), KakuroCellHint(row, 8))
                row = 2
                val row2: Array<KakuroCell?> = arrayOf(KakuroCellHint(row, 0), KakuroCellValue( row, 1), KakuroCellValue( row, 2), KakuroCellValue(row, 3), KakuroCellHint(row, 4), KakuroCellValue(row, 5), KakuroCellValue(row, 6), KakuroCellValue(row, 7), KakuroCellValue(row, 8))
                row = 3
                val row3: Array<KakuroCell?> = arrayOf(KakuroCellBlank(row, 0), KakuroCellHint( row, 1), KakuroCellHint( row, 2), KakuroCellValue(row, 3), KakuroCellValue(row, 4), KakuroCellHint(row, 5), KakuroCellValue(row, 6), KakuroCellValue(row, 7), KakuroCellValue(row, 8))
                row = 4
                val row4: Array<KakuroCell?> = arrayOf(KakuroCellHint(row, 0), KakuroCellValue( row, 1), KakuroCellValue( row, 2), KakuroCellHint(row, 3), KakuroCellValue(row, 4), KakuroCellValue(row, 5), KakuroCellValue(row, 6), KakuroCellValue(row, 7), KakuroCellValue(row, 8))
                row = 5
                val row5: Array<KakuroCell?> = arrayOf(KakuroCellHint(row, 0), KakuroCellValue( row, 1), KakuroCellValue( row, 2), KakuroCellValue(row, 3), KakuroCellValue(row, 4), KakuroCellValue(row, 5), KakuroCellHint(row, 6), KakuroCellValue(row, 7), KakuroCellValue(row, 8))
                row = 6
                val row6: Array<KakuroCell?> = arrayOf(KakuroCellHint(row, 0), KakuroCellValue( row, 1), KakuroCellValue( row, 2), KakuroCellValue(row, 3), KakuroCellHint(row, 4), KakuroCellValue(row, 5), KakuroCellValue(row, 6), KakuroCellHint(row, 7), KakuroCellHint(row, 8))
                row = 7
                val row7: Array<KakuroCell?> = arrayOf(KakuroCellHint(row, 0), KakuroCellValue( row, 1), KakuroCellValue( row, 2), KakuroCellValue(row, 3), KakuroCellValue(row, 4), KakuroCellHint(row, 5), KakuroCellValue(row, 6), KakuroCellValue(row, 7), KakuroCellValue(row, 8))
                row = 8
                val row8: Array<KakuroCell?> = arrayOf(KakuroCellBlank(row, 0), KakuroCellBlank( row, 1), KakuroCellHint( row, 2), KakuroCellValue(row, 3), KakuroCellValue(row, 4), KakuroCellBlank(row, 5), KakuroCellHint(row, 6), KakuroCellValue(row, 7), KakuroCellValue(row, 8))
                return arrayOf(row0, row1, row2, row3, row4, row5, row6, row7, row8)
            }
        }
    }

    private fun fillRandomly(board: Array<Array<KakuroCell?>>) {
        val model = KakuroBoardModel(size, board)
        val rowIndices = (0 until size).toMutableList()
        val colIndices = (0 until size).toMutableList()
        rowIndices.shuffle()
        for (i in rowIndices) {
            colIndices.shuffle()
            for (j in colIndices) {
                if (model.getCell(i, j) is KakuroCellValue) {
                    val cell = model.getCell(i, j) as KakuroCellValue
                    val row = model.getRow(i, j)
                    val col = model.getColumn(i, j)
                    val possibleValues = mutableListOf(1, 2, 3, 4, 5, 6, 7, 8, 9)
                    for (k in row) {
                        if (k.value in possibleValues) {
                            possibleValues.remove(k.value)
                        }
                    }
                    for (k in col) {
                        if (k.value in possibleValues) {
                            possibleValues.remove(k.value)
                        }
                    }
                    if (possibleValues.size == 0) return // something went wrong
                    val average = possibleValues.sum().toDouble() / possibleValues.size.toDouble()
                    val weightedRandomSelection = WeightedRandomSelection<Int>()
                    var initWeight = 100.0
                    when {
                        average < 5.0 -> { // we want a bigger number
                            for (k in 0 until possibleValues.size) {
                                weightedRandomSelection.add(initWeight, possibleValues.asReversed()[k])
                                if (initWeight > 50.0) {
                                    initWeight *= 3 / 4
                                }
                            }
                        }
                        average > 5.0 -> { // we want a smaller number
                            for (k in 0 until possibleValues.size) {
                                weightedRandomSelection.add(initWeight, possibleValues[k])
                                if (initWeight > 50.0) {
                                    initWeight *= 3 / 4
                                }
                            }
                        }
                        else -> {
                            for (k in 0 until possibleValues.size) {
                                weightedRandomSelection.add(1.0, possibleValues[k])
                            }
                        }
                    }
                    cell.value = weightedRandomSelection.next()
                }
            }
        }
    }

    private fun fillHints(board: Array<Array<KakuroCell?>>) {
        val model = KakuroBoardModel(size, board)
        for (i in 0 until size) {
            for (j in 0 until size) {
                if (model.getCell(i, j) is KakuroCellHint) {
                    val cell = model.getCell(i, j) as KakuroCellHint
                    if (model.getCell(i, j + 1) != null && model.getCell(i, j + 1) is KakuroCellValue) {
                        val row = model.getRow(i, j + 1)
                        var sum = 0
                        for (k in row) {
                            sum += k.value
                        }
                        cell.hintRight = sum
                    }
                    if (model.getCell(i + 1, j) != null && model.getCell(i + 1, j) is KakuroCellValue) {
                        val col = model.getColumn(i + 1, j)
                        var sum = 0
                        for (k in col) {
                            sum += k.value
                        }
                        cell.hintDown = sum
                    }
                }
            }
        }
    }

    private fun zeroValuesInBoard(board: Array<Array<KakuroCell?>>) {
        for (i in 0 until size) {
            for (j in 0 until size) {
                if (board[i][j] is KakuroCellValue) {
                    val cell = board[i][j] as KakuroCellValue
                    cell.value = 0
                }
            }
        }
    }
}