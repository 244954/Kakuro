package com.example.kakuro

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
        val array1 = arrayOf(1, 2, 3)
        val array2 = arrayOf(4, 5, 6)
        val myarr = arrayOf(array1, array2)

        val board = KakuroBoardModel(3, myarr)
        assertEquals(board.board[0][0], null)
    }
}
