package com.example.kakuro

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun onclickbuttonstaged(v: View) {
        gotoboard()
    }
    fun gotoboard() {
        val kakuroIntent = Intent(this, Kakuro_board::class.java)
        startActivity(kakuroIntent)
    }
}
