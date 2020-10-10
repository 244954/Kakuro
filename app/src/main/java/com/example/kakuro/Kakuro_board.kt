package com.example.kakuro

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class Kakuro_board : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kakuro_board)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}
