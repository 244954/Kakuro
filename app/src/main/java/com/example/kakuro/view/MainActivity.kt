package com.example.kakuro.view

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.kakuro.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun onclickbuttonstaged(v: View) {
        gotoboard()
    }
    fun gotoboard() {
        val kakuroIntent = Intent(this, KakuroActivity::class.java)
        startActivity(kakuroIntent)
    }
}
