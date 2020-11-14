package com.example.kakuro.view

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.example.kakuro.R
import com.example.kakuro.gamelogic.KakuroCell
import com.example.kakuro.viewmodel.KakuroViewModel
import kotlinx.android.synthetic.main.activity_kakuro_board.*
import android.os.CountDownTimer
import android.os.SystemClock
import android.view.View
import android.widget.Chronometer
import java.util.concurrent.TimeUnit


class KakuroActivity : AppCompatActivity(), KakuroBoardView.OnTouchListener {

    private lateinit var viewModel : KakuroViewModel
    private var size = 1
    private val oldTimer: Long = 3600000
    private var timer: Long = oldTimer // need to be changed, it can't really be a countdown
    private var timePassed: Long = 0
    private var chronometer : Chronometer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kakuro_board)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        kakuroBoard.registerListener(this) // i implement the interface

        val b = intent.extras
        val boardNumber = b?.getInt("board")
        val boardValues = getBoardFromFile(boardNumber!!)

        kakuroBoard.setSize(size) // set size for Kakuro View

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

        buttonSolve.setOnClickListener {
            viewModel.kakuroGame.solvePuzzle()
        }
    }

    override fun onPause() {
        super.onPause()

        val base = TimeUnit.MILLISECONDS.toSeconds(chronometer!!.base)
        viewModel.kakuroGame.updateTime(SystemClock.elapsedRealtime())
        chronometer!!.stop()
    }

    override fun onResume() {
        super.onResume()

        //val passedTime = TimeUnit.MILLISECONDS.toSeconds(viewModel.kakuroGame.getTime())
        //timer = oldTimer - viewModel.kakuroGame.getTime()
        //onCreateOptionsMenu(null) // restart timer

        val base = TimeUnit.MILLISECONDS.toSeconds(viewModel.kakuroGame.getTime())
        chronometer?.base = chronometer!!.base + SystemClock.elapsedRealtime() - viewModel.kakuroGame.getTime()
        chronometer?.start()
    }

    fun createCounter() {

        /*
        object : CountDownTimer(oldTimer, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                timePassed = timer - millisUntilFinished
                var minutes = (TimeUnit.MILLISECONDS.toMinutes(timePassed) -TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timePassed))).toString()
                if (minutes.length == 1) {
                    minutes = "0$minutes"
                }
                var seconds = (TimeUnit.MILLISECONDS.toSeconds(timePassed) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timePassed))).toString()
                if (seconds.length == 1) {
                    seconds = "0$seconds"
                }
                val hms = "$minutes:$seconds"
                //val hms = (TimeUnit.MILLISECONDS.toMinutes(timePassed) -TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timePassed))).toString() + ":" + (TimeUnit.MILLISECONDS.toSeconds(timePassed) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timePassed))).toString()
                counter?.title = hms

                if (kakuroBoard.isShown) { // don't update when user is not here
                    viewModel.kakuroGame.updateTime(timePassed)
                }
            }

            override fun onFinish() {
                counter?.title = "##:##"
            }
        }.start()
        */
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu, menu)

        val counter = menu?.findItem(R.id.counter)
        chronometer = counter?.actionView as Chronometer
        chronometer?.base = SystemClock.elapsedRealtime()
        chronometer?.start()

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        if (id==android.R.id.home) {
            finish()
        }

        return super.onOptionsItemSelected(item)
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
