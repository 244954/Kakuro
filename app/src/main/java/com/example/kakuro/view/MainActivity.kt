package com.example.kakuro.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.kakuro.R
import com.example.kakuro.datahandling.DatabaseHelper
import com.example.kakuro.gamelogic.KakuroCell
import com.example.kakuro.gamelogic.KakuroCellBlank
import com.example.kakuro.gamelogic.KakuroCellHint
import com.example.kakuro.gamelogic.KakuroCellValue
import com.example.kakuro.misc.ImageTranslator
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var database: DatabaseHelper
    private lateinit var openCVConverter: ImageTranslator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        database = DatabaseHelper(this)
        openCVConverter = ImageTranslator(this)
    }

    fun onClickContinue(v: View) {
        val kakuroIntent = Intent(this, KakuroActivity::class.java)
        val b = Bundle()
        b.putInt("board", 0) // 0 means load saved
        kakuroIntent.putExtras(b)
        startActivity(kakuroIntent)
    }

    fun onclickbuttonstaged(v: View) {
        goToSelect()
    }

    fun onClickGenerate(v: View) {
        val board = openCVConverter.processImage()
        database.clearData()
        database.insertData1(board.size, board.size, 0)
        insertToDb(board)

        val kakuroIntent = Intent(this, KakuroActivity::class.java)
        val b = Bundle()
        b.putInt("board", 2) // 0 means load saved
        kakuroIntent.putExtras(b)
        startActivity(kakuroIntent)
    }

    private fun insertToDb(board: Array<Array<KakuroCell?>>) {
        for (row in board.indices) {
            for (col in board.indices) {
                when(val cell = board[row][col]) {
                    is KakuroCellBlank -> {
                        database.insertData2(row, col, 0, 0, 0) // 0 - blank
                    }
                    is KakuroCellValue -> {
                        database.insertData2(row, col, 1, cell.value, 0) // 1 - value
                    }
                    is KakuroCellHint -> {
                        database.insertData2(row, col, 2, cell.hintRight, cell.hintDown) // 2 - hint
                    }
                }
            }
        }
    }

    fun goToSelect() {
        val kakuroIntent = Intent(this, SelectBoardActivity::class.java)
        startActivity(kakuroIntent)
    }

    override fun onResume() {
        super.onResume()
        getData1() // check whether it is possible to continue every time we enter this screen
    }

    private fun getData1() {
        val cursor = database.getData1()
        button_continue.isEnabled = ( cursor.count == 1 )
        cursor.close()
    }
}
