package com.example.kakuro

import com.example.kakuro.gamelogic.*
import com.example.kakuro.misc.TestBoards
import com.example.kakuro.misc.WeightedRandomSelection
import org.chocosolver.solver.Model
import org.chocosolver.solver.variables.IntVar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun kakuroCells_areCorrect() {
        val kakuroCell = KakuroCellBlank(3, 4)
        assertEquals(kakuroCell.essential, false)
        assertEquals(kakuroCell.row, 3)
    }
    @Test
    fun kakurotest2() {
        val kakuroCellValue = KakuroCellValue(2, 5)
        val kakuroCellValue2 = KakuroCellValue(2, 6, 3)
        assertEquals(kakuroCellValue.value, 0)
        assertEquals(kakuroCellValue2.value, 3)
        assertEquals(kakuroCellValue2.essential, true)
    }
    @Test
    fun arraytest() {
        val array1 = intArrayOf(1, 2, 3)
        val array2 = intArrayOf(4, 5, 6)
        val myarr = arrayOf(array1, array2)

        assertEquals(myarr[1][0], 4)
        assertEquals(myarr[0][1], 2)
    }
    @Test
    fun kakuroBoardTest() {
        val array1 = arrayOf(1, 2, 0, 1, 0)
        val array2 = arrayOf(6, 2, 1, 0, 1)
        val myarr = arrayOf(array1, array2)

        val board = KakuroBoardModel(3, myarr)
        assertEquals(board.board[0][0]!!.essential, false)
    }
    @Test
    fun abstractTest() {
        val cell : KakuroCell
        cell = KakuroCellHint(1, 1, 2, 2)
        assertEquals(cell.hintRight, 2)
    }

    @Test
    fun castingTest() {
        val cell : KakuroCell
        cell = KakuroCellBlank(2, 2)
        val cell2 = cell as? KakuroCellHint
        cell2?.hintDown = 10
        assertEquals(10, 10)
    }

    @Test
    fun boardTest() {
        val kakuroBoardRaw : Array<Array<Int>> = arrayOf(
            arrayOf(8, 2, 1 ,3, 0),
            arrayOf(24, 4, 2, 1 ,0),
            arrayOf(18, 4, 3, 1, 0),
            arrayOf(9, 2, 4, 1, 0),
            arrayOf(7, 3, 2, 1, 1),
            arrayOf(23, 3, 2, 2, 1),
            arrayOf(23, 3, 1, 3, 1),
            arrayOf(6, 3, 1, 4, 1)
        )

        val kakuroBoard = KakuroBoardModel(5, kakuroBoardRaw)
        val kak = kakuroBoard.board[1][1] as? KakuroCellHint
        assertEquals(kak!!.hintDown, 7)
    }

    @Test
    fun boardTest2() {
        val kakuroBoardRaw : Array<Array<Int>> = arrayOf(
            arrayOf(8, 2, 1 ,3, 0),
            arrayOf(24, 4, 2, 1 ,0),
            arrayOf(18, 4, 3, 1, 0),
            arrayOf(9, 2, 4, 1, 0),
            arrayOf(7, 3, 2, 1, 1),
            arrayOf(23, 3, 2, 2, 1),
            arrayOf(23, 3, 1, 3, 1),
            arrayOf(6, 3, 1, 4, 1)
        )

        val kakuroBoard = KakuroBoardModel(5, kakuroBoardRaw)

        assertEquals(kakuroBoard.getRow(2, 2).size, 4)
        assertEquals(kakuroBoard.getColumn(2, 2).size, 3)
        assertEquals(kakuroBoard.getColumnHint(2, 2)?.hintDown, 23)
        assertEquals(kakuroBoard.getRowHint(2, 2)?.hintRight, 24)
    }

    @Test
    fun mapTest() {
        val arr = arrayListOf<KakuroCellValue>(KakuroCellValue(1, 1, 2), KakuroCellValue(1, 1, 3))
        assertEquals(arr.map { it.value }, arrayListOf(2, 3))
    }

    @Test
    fun testArrays() {
        val arr = Array(3){Array(5){0}}
        arr[2][4] = 2
        assertEquals(arr[2][4], 2)
    }

    @Test
    fun combinationsTest() {
        val solutions = KakuroSolver.calcCombinations(28, 4)
        assertEquals(solutions.size, 2)
    }

    @Test
    fun mergeTest() {
        val sols = KakuroSolver.mergeCombinations(arrayListOf(arrayOf(1, 4), arrayOf(2, 3)), arrayListOf(arrayOf(1, 5), arrayOf(2, 4)))
        assertEquals(sols.size, 3)
    }

    @Test
    fun possibleCombinationsTest() {
        val kakuroBoardRaw : Array<Array<Int>> = arrayOf(
            arrayOf(8, 2, 1 ,3, 0),
            arrayOf(24, 4, 2, 1 ,0),
            arrayOf(18, 4, 3, 1, 0),
            arrayOf(9, 2, 4, 1, 0),
            arrayOf(7, 3, 2, 1, 1),
            arrayOf(23, 3, 2, 2, 1),
            arrayOf(23, 3, 1, 3, 1),
            arrayOf(6, 3, 1, 4, 1)
        )

        val kakuroBoard = KakuroBoardModel(5, kakuroBoardRaw)
        val kakuroSolver = KakuroSolver(kakuroBoard)
        val possibles = kakuroSolver.getPossibleValues(4, 2)

        assertEquals(possibles.size, 2)
    }

    @Test
    fun solvingTest() {
        val kakuroBoardRaw : Array<Array<Int>> = arrayOf(
            arrayOf(8, 2, 1 ,3, 0),
            arrayOf(24, 4, 2, 1 ,0),
            arrayOf(18, 4, 3, 1, 0),
            arrayOf(9, 2, 4, 1, 0),
            arrayOf(7, 3, 2, 1, 1),
            arrayOf(23, 3, 2, 2, 1),
            arrayOf(23, 3, 1, 3, 1),
            arrayOf(6, 3, 1, 4, 1)
        )

        val kakuroBoard = KakuroBoardModel(5, kakuroBoardRaw)
        val kakuroSolver = KakuroSolver(kakuroBoard)
        kakuroSolver.solveTrivial()
        assertEquals((kakuroBoard.board[1][3] as KakuroCellValue).value, 6)
        assertEquals((kakuroBoard.board[1][4] as KakuroCellValue).value, 2)
    }

    @Test
    fun chocoTest() {
        val n = 8
        val model = Model("$n-queens problem")
        val vars = arrayOfNulls<IntVar>(n)
        for (q in 0 until n) {
            vars[q] = model.intVar("Q_$q", 1, n)
        }
        for (i in 0 until n - 1) {
            for (j in i + 1 until n) {
                model.arithm(vars[i], "!=", vars[j]).post()
                model.arithm(vars[i], "!=", vars[j], "-", j - i).post()
                model.arithm(vars[i], "!=", vars[j], "+", j - i).post()
            }
        }
        val solution = model.getSolver().findSolution()
        if (solution != null) {
            println(solution.toString())
        }
        assertEquals(1, 1)
    }

    @Test
    fun allRowsColsTest() {
        val kakuroBoardRaw : Array<Array<Int>> = arrayOf(
            arrayOf(8, 2, 1 ,3, 0),
            arrayOf(24, 4, 2, 1 ,0),
            arrayOf(18, 4, 3, 1, 0),
            arrayOf(9, 2, 4, 1, 0),
            arrayOf(7, 3, 2, 1, 1),
            arrayOf(23, 3, 2, 2, 1),
            arrayOf(23, 3, 1, 3, 1),
            arrayOf(6, 3, 1, 4, 1)
        )

        val kakuroBoard = KakuroBoardModel(5, kakuroBoardRaw)

        println(kakuroBoard.getAllRowsAndCols().toString())

        assertEquals(1, 1)
    }

    @Test
    fun distinctTest() {
        val l = listOf(1, 2 ,3, 3).toMutableList()
        l.distinct()

        assertEquals(l.size, 4)
    }

    @Test
    fun backtrackingTest() {
        // val solver = BacktrackingSolver()
        // val tiles = solver.solve()
        assertEquals(4, 4)
    }

    @Test
    fun backtrackingSolvingTest() {
        val kakuroBoardRaw : Array<Array<Int>> = arrayOf(
            arrayOf(8, 2, 1 ,3, 0),
            arrayOf(24, 4, 2, 1 ,0),
            arrayOf(18, 4, 3, 1, 0),
            arrayOf(9, 2, 4, 1, 0),
            arrayOf(7, 3, 2, 1, 1),
            arrayOf(23, 3, 2, 2, 1),
            arrayOf(23, 3, 1, 3, 1),
            arrayOf(6, 3, 1, 4, 1)
        )

        val kakuroBoard = KakuroBoardModel(5, kakuroBoardRaw)
        val kakuroSolver = BacktrackingSolver(kakuroBoard)
        kakuroSolver.solve()
        assertEquals((kakuroBoard.board[1][3] as KakuroCellValue).value, 6)
        assertEquals((kakuroBoard.board[1][4] as KakuroCellValue).value, 2)
    }

    @Test
    fun weightedSelectionTest() {
        val weightedSelection = WeightedRandomSelection<String>()
        weightedSelection.add(10.0, "less likely").add(70.0, "more likely").add(1.0, "very unlikely")
        for (i in 0..50) {
            println(weightedSelection.next())
        }
    }

    @Test
    fun generatorTest() {
        val boardGenerator = BoardGenerator()
        val board = boardGenerator.generate()
        assertNotEquals(board.size, 0)
    }

    @Test
    fun boardTesterTest() {
        // val string = "5\n8 2 1 3 0\n24 4 2 1 0\n18 4 3 1 0\n9 2 4 1 0\n7 3 2 1 1\n23 3 2 2 1\n23 3 1 3 1\n6 3 1 4 1"
        // val string = "5\n17 3 1 1 0\n11 3 2 1 0\n21 3 3 2 0\n10 3 4 2 0\n7 2 1 1 1\n30 4 1 2 1\n10 4 1 3 1\n12 2 3 4 1"
        // val string = "5\n8 2 1 1 0\n15 3 2 1 0\n11 3 3 2 0\n13 2 4 3 0\n16 2 1 1 1\n7 3 1 2 1\n7 3 2 3 1\n17 2 3 4 1"
        val string = "13\n" +
                "11 2 1 2 0\n" +
                "14 3 1 5 0\n" +
                "8 2 1 9 0\n" +
                "31 6 2 2 0\n" +
                "6 2 2 9 0\n" +
                "15 3 3 2 0\n" +
                "3 2 3 6 0\n" +
                "23 4 3 9 0\n" +
                "14 2 4 1 0\n" +
                "10 2 4 4 0\n" +
                "16 2 4 8 0\n" +
                "10 2 4 11 0\n" +
                "17 3 5 1 0\n" +
                "3 2 5 5 0\n" +
                "19 3 5 8 0\n" +
                "12 3 6 1 0\n" +
                "24 5 6 6 0\n" +
                "26 5 7 3 0\n" +
                "13 3 7 10 0\n" +
                "12 3 8 3 0\n" +
                "8 2 8 7 0\n" +
                "8 3 8 10 0\n" +
                "10 2 9 1 0\n" +
                "10 2 9 4 0\n" +
                "10 2 9 8 0\n" +
                "7 2 9 11 0\n" +
                "14 4 10 1 0\n" +
                "9 2 10 6 0\n" +
                "20 3 10 9 0\n" +
                "9 2 11 3 0\n" +
                "28 6 11 6 0\n" +
                "8 2 12 3 0\n" +
                "12 3 12 6 0\n" +
                "10 2 12 10 0\n" +
                "24 3 4 1 1\n" +
                "16 2 9 1 1\n" +
                "39 6 1 2 1\n" +
                "3 2 9 2 1\n" +
                "6 3 1 3 1\n" +
                "10 4 5 3 1\n" +
                "6 3 10 3 1\n" +
                "24 3 2 4 1\n" +
                "39 6 7 4 1\n" +
                "17 2 1 5 1\n" +
                "3 2 4 5 1\n" +
                "7 3 7 5 1\n" +
                "7 3 1 6 1\n" +
                "6 3 5 6 1\n" +
                "24 3 10 6 1\n" +
                "6 3 1 7 1\n" +
                "24 3 6 7 1\n" +
                "6 3 10 7 1\n" +
                "23 3 4 8 1\n" +
                "4 2 8 8 1\n" +
                "3 2 11 8 1\n" +
                "39 6 1 9 1\n" +
                "24 3 9 9 1\n" +
                "7 3 1 10 1\n" +
                "11 4 5 10 1\n" +
                "23 3 10 10 1\n" +
                "4 2 3 11 1\n" +
                "22 6 7 11 1\n" +
                "16 2 3 12 1\n" +
                "7 3 7 12 1"
        val size = TestBoards.getSize(string)
        val arr = TestBoards.getBoardFromFile(string)
        val board = KakuroBoardModel(size, arr)
        val solver = BacktrackingSolver(board)
        var solutions = 1L
        val solList = mutableListOf<Long>()
        for (i in board.board) {
            for (j in i) {
                if (j is KakuroCellValue) {
                    val pos = solver.getPossibleValues(j.row, j.column).size.toLong()
                    solList.add(pos)
                    solutions *= pos
                }
            }
        }
        solList.sort()
        val numbers =  solList.groupingBy { it }.eachCount()

        solutions *= 1L
    }
}
