package com.example.kakuro.gamelogic

class KakuroSolver(model: KakuroBoardModel) {

    private val size = model.size
    private val board = model.board // shortcuts

    // Pair(number in hint, length of hint) -> possible number in row/column
    private val dict = mapOf<Pair<Int,Int>,Array<Array<Int>>>(
        Pair(3, 2) to arrayOf(arrayOf(1, 2)),
        Pair(4, 2) to arrayOf(arrayOf(1, 3)),
        Pair(5, 2) to arrayOf(arrayOf(1, 4), arrayOf(2, 3))
                /*
        Pair(6, 2) to arrayOf(1, 2, 4, 5),
        Pair(7, 2) to arrayOf(1, 2, 3, 4, 5, 6),
        Pair(8, 2) to arrayOf(1, 2, 3, 5, 6 ,7),
        Pair(9, 2) to arrayOf(1, 2, 3, 4, 5, 6, 7, 8),
        Pair(10, 2) to arrayOf(1, 2, 3, 4, 6, 7, 8, 9),
        Pair(11, 2) to arrayOf(2, 3, 4, 5, 6, 7, 8, 9),
        Pair(12, 2) to arrayOf(3, 4, 5, 7, 8, 9),
        Pair(13, 2) to arrayOf(4, 5, 6, 7, 8, 9),
        Pair(14, 2) to arrayOf(5, 6, 8, 9),
        Pair(15, 2) to arrayOf(6, 7, 8, 9),
        Pair(16, 2) to arrayOf(7, 9),
        Pair(17, 2) to arrayOf(8, 9),

        Pair(6, 3) to arrayOf(1, 2, 3),
        Pair(7, 3) to arrayOf(1, 2, 4),
        Pair(8, 3) to arrayOf(1, 2, 3, 4, 5),
        Pair(9, 3) to arrayOf(1, 2, 3, 4, 5, 6),
        Pair(10, 3) to arrayOf(1, 2, 3, 4, 6, 7),
        Pair(11, 3) to arrayOf(2, 3, 4, 5, 6, 7, 8, 9),
        Pair(12, 3) to arrayOf(3, 4, 5, 7, 8, 9),
        Pair(13, 3) to arrayOf(4, 5, 6, 7, 8, 9),
        Pair(14, 3) to arrayOf(5, 6, 8, 9),
        Pair(15, 3) to arrayOf(6, 7, 8, 9),
        Pair(16, 3) to arrayOf(7, 9),
        Pair(17, 3) to arrayOf(8, 9) */
    )

    fun solveTrivial() {
        for (row in 0 until size) {
            for (col in 0 until size) {
                val cell = board[row][col]
                if (cell is KakuroCellValue) {

                }
            }
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

    }

    fun getPossibleValues(row: Int, col: Int): ArrayList<Int> {
        val possibles = ArrayList<Int>()



        return possibles
    }
}