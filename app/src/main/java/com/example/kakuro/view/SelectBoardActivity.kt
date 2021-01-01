package com.example.kakuro.view

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.kakuro.R
import com.example.kakuro.datahandling.DatabaseHelper
import com.example.kakuro.gamelogic.*

class SelectBoardActivity : AppCompatActivity() {

    private lateinit var database: DatabaseHelper
    private var size: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_board)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        database = DatabaseHelper(this)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        if (id==android.R.id.home) {
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

    fun onClickSelectBoard(v: View) {
        val selectedBoard = v.tag.toString().toInt()
        val simplifiedBoard = getBoardFromFile(selectedBoard)
        val boardModel = KakuroBoardModel(size, simplifiedBoard)
        database.clearData()
        database.insertDataGeneral(size, size, 0, boardModel.recommendedHintsAmount())
        boardModel.insertToDb(database)
        val kakuroIntent = Intent(this, KakuroActivity::class.java)
        val b = Bundle()
        b.putInt("board",selectedBoard)
        kakuroIntent.putExtras(b)
        startActivity(kakuroIntent)
    }

    private fun getBoardFromFile(nr: Int) : Array<Array<Int>> {
        val text = application.assets.open("board$nr").bufferedReader().use {
            it.readText()
        }

        var lines = text.lines()
        size = lines[0].toInt()
        lines = lines.drop(1)

        val arr = Array(lines.size) { Array(5) { 0 }}

        for (linenr in 0 until lines.size) {
            val line = lines[linenr].split(" ")
            for (number in 0 until line.size) {
                arr[linenr][number] = line[number].toInt()
            }
        }

        return arr
    }
}
