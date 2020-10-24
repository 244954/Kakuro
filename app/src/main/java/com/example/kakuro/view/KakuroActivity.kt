package com.example.kakuro.view

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.kakuro.R
import com.example.kakuro.gamelogic.KakuroCell
import com.example.kakuro.viewmodel.KakuroViewModel
import kotlinx.android.synthetic.main.activity_kakuro_board.*



class KakuroActivity : AppCompatActivity(), KakuroBoardView.OnTouchListener {

    private lateinit var viewModel : KakuroViewModel
    private var size = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kakuro_board)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        kakuroBoard.registerListener(this) // i implement the interface

        val boardValues = getBoardFromFile(3) // changes here are imminent
        // take a parameter from intent creation

        viewModel = ViewModelProviders.of(this).get(KakuroViewModel::class.java)
        viewModel.startViewModel(size, boardValues)
        viewModel.kakuroGame.selectedCellLiveData.observe(this, Observer { updateSelectedCellUI(it)  })
        viewModel.kakuroGame.cellsLiveData.observe(this, Observer { updateCells(it) })

        val buttons = listOf(buttonOne, buttonTwo, buttonThree, buttonFour, buttonFive,
            buttonSix, buttonSeven, buttonEight, buttonNine)
        buttons.forEachIndexed { index, button ->
            button.setOnClickListener {
                viewModel.kakuroGame.handleInput(index + 1) // index starts from 0
            }
        }
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

    private fun updateCells(cells: Array<Array<KakuroCell?>>?) = cells?.let {
        kakuroBoard.updateCells(cells)
    }

    private fun updateSelectedCellUI(cell: Pair<Int, Int>?) = cell?.let {
        kakuroBoard.updateSelectedCellUI(cell.first, cell.second)
    }

    override fun onCellTouched(row: Int, col: Int) {
        viewModel.kakuroGame.updateteSelectedCell(row, col)
    }
}
