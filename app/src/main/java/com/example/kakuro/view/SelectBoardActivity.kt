package com.example.kakuro.view

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.kakuro.R

class SelectBoardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_board)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
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
        val kakuroIntent = Intent(this, KakuroActivity::class.java)
        val b = Bundle()
        b.putInt("board",selectedBoard)
        kakuroIntent.putExtras(b)
        startActivity(kakuroIntent)
    }
}
