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
import com.example.kakuro.datahandling.DatabaseHelper
import com.example.kakuro.dialog.VictoryDialog
import com.example.kakuro.gamelogic.KakuroCellBlank
import com.example.kakuro.gamelogic.KakuroCellHint
import com.example.kakuro.gamelogic.KakuroCellValue
import com.example.kakuro.misc.Stopwatch
import java.util.*
import java.util.concurrent.TimeUnit


class KakuroActivity : AppCompatActivity(), KakuroBoardView.OnTouchListener, VictoryDialog.DialogListener {

    private lateinit var viewModel : KakuroViewModel
    private var size = 1
    private var timePassed: Long = 0
    private var chronometer : Chronometer? = null
    //private var timePassed : Long = 0
    //private val stopwatch = Stopwatch()
    private var counter : MenuItem? = null
    private lateinit var database : DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kakuro_board)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        kakuroBoard.registerListener(this) // i implement the interface

        val b = intent.extras
        val boardNumber = b?.getInt("board")
        viewModel = ViewModelProviders.of(this).get(KakuroViewModel::class.java)
        database = DatabaseHelper(this)

        when(boardNumber!!) {
            0 -> {
                val boardValues = getBoardFromDb()
                viewModel.startViewModelFromSavedState(size, boardValues)
            }
            1 -> {
                // generate random
                //boardValues = getBoardFromFile(0)
            }
            2 -> {
                // scan
                //boardValues = getBoardFromFile(0)
            }
            else -> {
                val boardValues = getBoardFromFile(boardNumber)
                viewModel.startViewModel(size, boardValues)
            }
        }

        kakuroBoard.setSize(size) // set size for Kakuro View
        viewModel.kakuroGame.selectedCellLiveData.observe(this, Observer { updateSelectedCellUI(it)  })
        viewModel.kakuroGame.cellsLiveData.observe(this, Observer { updateCells(it) })

        val buttons = listOf(buttonOne, buttonTwo, buttonThree, buttonFour, buttonFive,
            buttonSix, buttonSeven, buttonEight, buttonNine)
        buttons.forEachIndexed { index, button ->
            button.setOnClickListener {
                if (viewModel.kakuroGame.handleInput(index + 1)) {
                    val dialog = VictoryDialog()
                    dialog.putTime(SystemClock.elapsedRealtime() - chronometer!!.base)
                    chronometer?.stop()
                    dialog.show(supportFragmentManager,"example") // dialog
                }
            }
        }

        buttonSolve.setOnClickListener {
            viewModel.kakuroGame.solvePuzzle()
        }
    }

    override fun onDestroy() {
        database.clearData()
        database.insertData1(size, size, SystemClock.elapsedRealtime() - chronometer!!.base)
        insertToDb() // insert to table 2

        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()

        viewModel.kakuroGame.updateTime(SystemClock.elapsedRealtime() - chronometer!!.base)
        //stopwatch.stop()
        chronometer!!.stop()
    }

    override fun onResume() {
        super.onResume()

        /*
        if (stopwatch.wasStopped()) {
            stopwatch.startWithTime(viewModel.kakuroGame.getTime())
        }
        else {
            stopwatch.start()
        }
        */

        chronometer?.base = SystemClock.elapsedRealtime() - viewModel.kakuroGame.getTime()
        chronometer?.start()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu, menu)

        counter = menu?.findItem(R.id.counter)

        /*
        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val time = stopwatch.getElapsedTime()
                val ms = (TimeUnit.MILLISECONDS.toMinutes(time) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time))).toString() +
                        ":"+ (TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time))).toString()
                this@KakuroActivity.runOnUiThread(java.lang.Runnable {
                    counter?.title = ms
                })
            }
        }, 0, 500) // every half a second
        */
        chronometer = counter?.actionView as Chronometer
        chronometer?.base = SystemClock.elapsedRealtime() - timePassed
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

    private fun insertToDb() {
        for (row in 0 until size) {
            for (col in 0 until size) {
                val cell = viewModel.kakuroGame.getCell(row, col)
                when(cell) {
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

    private fun getBoardFromDb() : Array<Array<KakuroCell?>> {
        // Get size and time from db
        val cursor = database.getData1()
        if (cursor.count == 1) {
            cursor.moveToFirst()
            size = cursor.getString(1).toInt()
            timePassed = cursor.getString(3).toLong()
        }
        cursor.close()

        val board: Array<Array<KakuroCell?>> = Array(size) {
            arrayOfNulls<KakuroCell>(size)
        }

        // Get board from db
        val cursor2 = database.getData2()
        if (cursor2.count > 0) {
            while (cursor2.moveToNext()) {
                val row = cursor2.getString(1).toInt()
                val col = cursor2.getString(2).toInt()
                when(cursor2.getString(3).toInt()) {
                    0 -> {
                        // blank
                        board[row][col] = KakuroCellBlank(row, col)
                    }
                    1 -> {
                        // value
                        board[row][col] = KakuroCellValue(row, col, cursor2.getString(4).toInt())
                    }
                    2 -> {
                        // hint
                        board[row][col] = KakuroCellHint(row, col, cursor2.getString(4).toInt(), cursor2.getString(5).toInt())
                    }
                }
            }
        }
        cursor2.close()

        return board
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

    override fun victoryAftermath() { // happens when dialog is closed
        database.clearData()

        finish()
    }
}
