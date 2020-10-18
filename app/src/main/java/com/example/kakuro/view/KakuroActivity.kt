package com.example.kakuro.view

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.kakuro.R
import com.example.kakuro.gamelogic.KakuroCell
import com.example.kakuro.viewmodel.KakuroViewModel
import kotlinx.android.synthetic.main.activity_kakuro_board.*

class KakuroActivity : AppCompatActivity(), KakuroBoardView.OnTouchListener {

    private lateinit var viewModel : KakuroViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kakuro_board)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        kakuroBoard.registerListener(this) // i implement the interface

        viewModel = ViewModelProviders.of(this).get(KakuroViewModel::class.java)
        viewModel.kakuroGame.selectedCellLiveData.observe(this, Observer { updateSelectedCellUI(it)  })
        viewModel.kakuroGame.cellsLiveData.observe(this, Observer { updateCells(it) })
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
