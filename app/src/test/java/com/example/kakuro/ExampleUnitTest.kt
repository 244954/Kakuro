package com.example.kakuro

import com.example.kakuro.gamelogic.*
import org.chocosolver.solver.Model
import org.junit.Test

import org.junit.Assert.*
import org.chocosolver.solver.Solution
import org.chocosolver.solver.constraints.extension.TuplesFactory.arithm
import org.chocosolver.solver.variables.IntVar
import org.opencv.android.OpenCVLoader


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
}
