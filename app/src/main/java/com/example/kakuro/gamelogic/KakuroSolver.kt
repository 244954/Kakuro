package com.example.kakuro.gamelogic

import org.chocosolver.solver.Model

class KakuroSolver(private val model: KakuroBoardModel) {

    private val size = model.size
    private val board = model.board // shortcuts
    private val posBoard: Array<Array<ArrayList<Int>>> = Array(size) {
        Array(size) {
            ArrayList<Int>()
        }
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
        val model = Model("Kakuro Constraint Problem")

        var newSolution = -1
        while (newSolution != 0) {
            newSolution = 0
            for (row in 0 until size) {
                for (col in 0 until size) {
                    val cell = board[row][col]
                    if (cell is KakuroCellValue && cell.value == 0) {
                        val pos = getPossibleValues(row, col)
                        posBoard[row][col] = pos
                        if (pos.size == 1) {
                            cell.value = pos[0]
                            newSolution ++
                        }
                    }
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
            if (i.value != 0) {
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