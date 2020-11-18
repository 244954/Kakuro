package com.example.kakuro.view

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.kakuro.R
import com.example.kakuro.datahandling.DatabaseHelper
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val database = DatabaseHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun onClickContinue(v: View) {
        val kakuroIntent = Intent(this, KakuroActivity::class.java)
        val b = Bundle()
        b.putInt("board",0) // 0 means load saved
        kakuroIntent.putExtras(b)
        startActivity(kakuroIntent)
    }

    fun onclickbuttonstaged(v: View) {
        goToSelect()
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
