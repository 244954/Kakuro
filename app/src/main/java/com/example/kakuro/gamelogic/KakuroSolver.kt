package com.example.kakuro.gamelogic

import org.chocosolver.solver.Model
import org.chocosolver.solver.variables.IntVar

class KakuroSolver(private val model: KakuroBoardModel) {

    private val size = model.size
    private val board = model.board // shortcuts
    private val posBoard: Array<Array<ArrayList<Int>>> = Array(size) {
        Array(size) {
            ArrayList<Int>()
        }
    }
    private val constModelBoard: Array<Array<IntVar?>> = Array(size) {
        arrayOfNulls<IntVar>(size)
    }

    companion object {

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

    fun solveTrivial() {
        val kakuroModel = Model("Kakuro Constraint Problem")
        // Create a variable taking its value in {1, 3} (the value is 1 or 3)
        // val v2 = model.intVar("v2", intArrayOf(1, 3))

        var newSolution = -1
        while (newSolution != 0) {
            newSolution = 0
            for (row in 0 until size) {
                for (col in 0 until size) {
                    val cell = board[row][col]
                    if (cell is KakuroCellValue) {
                        if (cell.value == 0) {
                            val pos = getPossibleValues(row, col)
                            posBoard[row][col] = pos
                            if (pos.size == 1) {
                                cell.value = pos[0]
                                newSolution++
                            }
                        }
                        else { // data already entered
                            posBoard[row][col] = arrayListOf(cell.value)
                            /*
                            val pos = getPossibleValues(row, col)
                            posBoard[row][col] = pos
                            if (pos.size == 1) { // if one solution, override
                                cell.value = pos[0]
                                newSolution++
                            }
                            else {
                                if (cell.value in pos) {
                                    // if legal, do nothing (though it probably should do something)
                                }
                                else {
                                    cell.value = 0  // if illegal, remove
                                }
                            }
                            */
                        }
                    }
                }
            }
        }

        for (row in 0 until size) {
            for (col in 0 until size) {
                if (posBoard[row][col].size > 0) { // > 1 ?
                    constModelBoard[row][col] = kakuroModel.intVar("V_$row$col", posBoard[row][col].toIntArray())
                }
            }
        }
        val hints = model.getAllRowsAndCols()
        for (i in hints) { // probably the dumbest piece of code ever written
            when (i.size) { // behold
                3 -> {
                    constModelBoard[i[1].first][i[1].second]!!.add(constModelBoard[i[2].first][i[2].second])
                        .eq(i[0].first).post()

                    kakuroModel.allDifferent(constModelBoard[i[1].first][i[1].second],
                        constModelBoard[i[2].first][i[2].second]).post()
                }
                4 -> {
                    constModelBoard[i[1].first][i[1].second]!!.add(constModelBoard[i[2].first][i[2].second])
                        .add(constModelBoard[i[3].first][i[3].second])
                        .eq(i[0].first).post()

                    kakuroModel.allDifferent(constModelBoard[i[1].first][i[1].second],
                        constModelBoard[i[2].first][i[2].second],
                        constModelBoard[i[3].first][i[3].second]).post()
                }
                5 -> {
                    constModelBoard[i[1].first][i[1].second]!!.add(constModelBoard[i[2].first][i[2].second])
                        .add(constModelBoard[i[3].first][i[3].second])
                        .add(constModelBoard[i[4].first][i[4].second])
                        .eq(i[0].first).post()

                    kakuroModel.allDifferent(constModelBoard[i[1].first][i[1].second],
                        constModelBoard[i[2].first][i[2].second],
                        constModelBoard[i[3].first][i[3].second],
                        constModelBoard[i[4].first][i[4].second]).post()
                }
                6 -> {
                    constModelBoard[i[1].first][i[1].second]!!.add(constModelBoard[i[2].first][i[2].second])
                        .add(constModelBoard[i[3].first][i[3].second])
                        .add(constModelBoard[i[4].first][i[4].second])
                        .add(constModelBoard[i[5].first][i[5].second])
                        .eq(i[0].first).post()

                    kakuroModel.allDifferent(constModelBoard[i[1].first][i[1].second],
                        constModelBoard[i[2].first][i[2].second],
                        constModelBoard[i[3].first][i[3].second],
                        constModelBoard[i[4].first][i[4].second],
                        constModelBoard[i[5].first][i[5].second]).post()
                }
                7 -> {
                    constModelBoard[i[1].first][i[1].second]!!.add(constModelBoard[i[2].first][i[2].second])
                        .add(constModelBoard[i[3].first][i[3].second])
                        .add(constModelBoard[i[4].first][i[4].second])
                        .add(constModelBoard[i[5].first][i[5].second])
                        .add(constModelBoard[i[6].first][i[6].second])
                        .eq(i[0].first).post()

                    kakuroModel.allDifferent(constModelBoard[i[1].first][i[1].second],
                        constModelBoard[i[2].first][i[2].second],
                        constModelBoard[i[3].first][i[3].second],
                        constModelBoard[i[4].first][i[4].second],
                        constModelBoard[i[5].first][i[5].second],
                        constModelBoard[i[6].first][i[6].second]).post()
                }
                8 -> {
                    constModelBoard[i[1].first][i[1].second]!!.add(constModelBoard[i[2].first][i[2].second])
                        .add(constModelBoard[i[3].first][i[3].second])
                        .add(constModelBoard[i[4].first][i[4].second])
                        .add(constModelBoard[i[5].first][i[5].second])
                        .add(constModelBoard[i[6].first][i[6].second])
                        .add(constModelBoard[i[7].first][i[7].second])
                        .eq(i[0].first).post()

                    kakuroModel.allDifferent(constModelBoard[i[1].first][i[1].second],
                        constModelBoard[i[2].first][i[2].second],
                        constModelBoard[i[3].first][i[3].second],
                        constModelBoard[i[4].first][i[4].second],
                        constModelBoard[i[5].first][i[5].second],
                        constModelBoard[i[6].first][i[6].second],
                        constModelBoard[i[7].first][i[7].second]).post()
                }
                9 -> {
                    constModelBoard[i[1].first][i[1].second]!!.add(constModelBoard[i[2].first][i[2].second])
                        .add(constModelBoard[i[3].first][i[3].second])
                        .add(constModelBoard[i[4].first][i[4].second])
                        .add(constModelBoard[i[5].first][i[5].second])
                        .add(constModelBoard[i[6].first][i[6].second])
                        .add(constModelBoard[i[7].first][i[7].second])
                        .add(constModelBoard[i[8].first][i[8].second])
                        .eq(i[0].first).post()

                    kakuroModel.allDifferent(constModelBoard[i[1].first][i[1].second],
                        constModelBoard[i[2].first][i[2].second],
                        constModelBoard[i[3].first][i[3].second],
                        constModelBoard[i[4].first][i[4].second],
                        constModelBoard[i[5].first][i[5].second],
                        constModelBoard[i[6].first][i[6].second],
                        constModelBoard[i[7].first][i[7].second],
                        constModelBoard[i[8].first][i[8].second]).post()
                }
                10 -> {
                    constModelBoard[i[1].first][i[1].second]!!.add(constModelBoard[i[2].first][i[2].second])
                        .add(constModelBoard[i[3].first][i[3].second])
                        .add(constModelBoard[i[4].first][i[4].second])
                        .add(constModelBoard[i[5].first][i[5].second])
                        .add(constModelBoard[i[6].first][i[6].second])
                        .add(constModelBoard[i[7].first][i[7].second])
                        .add(constModelBoard[i[8].first][i[8].second])
                        .add(constModelBoard[i[9].first][i[9].second])
                        .eq(i[0].first).post()

                    kakuroModel.allDifferent(constModelBoard[i[1].first][i[1].second],
                        constModelBoard[i[2].first][i[2].second],
                        constModelBoard[i[3].first][i[3].second],
                        constModelBoard[i[4].first][i[4].second],
                        constModelBoard[i[5].first][i[5].second],
                        constModelBoard[i[6].first][i[6].second],
                        constModelBoard[i[7].first][i[7].second],
                        constModelBoard[i[8].first][i[8].second],
                        constModelBoard[i[9].first][i[9].second]).post()
                }
            }
        }

        val solution = kakuroModel.getSolver().findSolution()

        for (row in 0 until size) {
            for (col in 0 until size) {
                val cel = board[row][col]
                if (cel is KakuroCellValue && constModelBoard[row][col] != null) {
                    cel.value = constModelBoard[row][col]!!.value
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

        val rowCombinations = calcCombinations(rowHint!!.hintRight,wholeRow.size)
        for(i in wholeRow) {
            if (i.value != 0) { // it can't be me! Cannot discard my own value
                rowCombinations.removeIf { j -> !j.contains(i.value)}
            }
        }
        val colCombinations = calcCombinations(colHint!!.hintDown,wholeCol.size)
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
}