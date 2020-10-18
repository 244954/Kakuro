package com.example.kakuro

import com.example.kakuro.gamelogic.*
import org.junit.Test

import org.junit.Assert.*

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
}
