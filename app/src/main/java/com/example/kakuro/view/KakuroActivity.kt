package com.example.kakuro.view

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.SystemClock
import android.view.Menu
import android.view.MenuItem
import android.widget.Chronometer
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.kakuro.R
import com.example.kakuro.datahandling.DatabaseHelper
import com.example.kakuro.dialog.VictoryDialog
import com.example.kakuro.gamelogic.KakuroCell
import com.example.kakuro.gamelogic.KakuroCellBlank
import com.example.kakuro.gamelogic.KakuroCellHint
import com.example.kakuro.gamelogic.KakuroCellValue
import com.example.kakuro.viewmodel.KakuroViewModel
import kotlinx.android.synthetic.main.activity_kakuro_board.*


class KakuroActivity : AppCompatActivity(), KakuroBoardView.OnTouchListener, VictoryDialog.DialogListener {

    private lateinit var viewModel : KakuroViewModel
    private var size = 1
    private var timePassed: Long = 0
    private var chronometer : Chronometer? = null
    private var gameIsFinished = false
    private var counter : MenuItem? = null
    private lateinit var database : DatabaseHelper
    private var hintsLeft = 3

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
            0 -> { // saved
                val boardValues = getBoardFromDb()
                viewModel.startViewModelFromSavedState(size, boardValues)
            }
            1 -> { // generated
                val boardValues = getBoardFromDb()
                viewModel.startViewModelFromSavedState(size, boardValues)
            }
            2 -> { // scanned
                val boardValues = getBoardFromDb()
                viewModel.startViewModelFromSavedState(size, boardValues)
            }
            else -> {
                val boardValues = getBoardFromDb()
                viewModel.startViewModelFromSavedState(size, boardValues)
            }
        }

        kakuroBoard.setSize(size) // set size for Kakuro View
        viewModel.kakuroGame.selectedCellLiveData.observe(
            this,
            Observer { updateSelectedCellUI(it) })
        viewModel.kakuroGame.cellsLiveData.observe(this, Observer { updateCells(it) })

        val buttons = listOf(
            buttonOne, buttonTwo, buttonThree, buttonFour, buttonFive,
            buttonSix, buttonSeven, buttonEight, buttonNine
        )
        buttons.forEachIndexed { index, button ->
            button.setOnClickListener {
                if (viewModel.kakuroGame.isGameFinishedAndHandleInput(index + 1)) { // if game is won
                    val dialog = VictoryDialog()
                    dialog.putTime(SystemClock.elapsedRealtime() - chronometer!!.base)
                    chronometer?.stop()
                    dialog.show(supportFragmentManager, "example") // dialog
                }
            }
        }

        clearButton.setOnClickListener {
            val dialogClickListener =
                DialogInterface.OnClickListener { dialog, which ->
                    when (which) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            viewModel.kakuroGame.clearBoard()
                            hintsLeft = viewModel.kakuroGame.board.recommendedHintsAmount()
                            if (viewModel.kakuroGame.isBoardCorrect) {
                                hintButton.text =
                                    resources.getString(R.string.hintButton, hintsLeft)
                                hintButton.isEnabled = true
                            }
                            dialog.dismiss()
                        }
                        DialogInterface.BUTTON_NEGATIVE -> {
                            dialog.dismiss()
                        }
                    }
                }

            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setMessage(resources.getString(R.string.clearBoardDialogText))
                .setPositiveButton(resources.getString(R.string.yes), dialogClickListener)
                .setNegativeButton(resources.getString(R.string.no), dialogClickListener).show()
        }

        if (viewModel.kakuroGame.isBoardCorrect) { // there is solution
            solveButton.setOnClickListener {
                viewModel.kakuroGame.solvePuzzle()
            }

            if (hintsLeft > 0) {
                hintButton.setOnClickListener {
                    if (hintsLeft > 0) {
                        hintsLeft -= 1
                        viewModel.kakuroGame.giveHint()
                        hintButton.text = resources.getString(R.string.hintButton, hintsLeft)

                        val toast = Toast.makeText(
                            this,
                            resources.getString(R.string.hintsLeft, hintsLeft),
                            Toast.LENGTH_SHORT
                        )
                        toast.show()
                        if (hintsLeft == 0) {
                            hintButton.text = resources.getString(R.string.hintButton, hintsLeft)
                            hintButton.isEnabled = false
                        }
                    } else {
                        hintButton.isEnabled = false
                    }
                }
                hintButton.text = resources.getString(R.string.hintButton, hintsLeft)
                hintButton.isEnabled = true
            } else {
                hintButton.text = resources.getString(R.string.hintButton, hintsLeft)
                hintButton.isEnabled = false
            }
        }
        else { // no solution found
            solveButton.isEnabled = false
            hintButton.isEnabled = false
            val toast = Toast.makeText(
                this,
                resources.getString(R.string.noSolution),
                Toast.LENGTH_SHORT
            )
            toast.show()
        }
    }

    override fun onDestroy() {
        if (!gameIsFinished) { // inelegant but whatever
            database.clearData()
            database.insertDataGeneral(
                size,
                size,
                SystemClock.elapsedRealtime() - chronometer!!.base,
                hintsLeft // to be updated
            )
            viewModel.kakuroGame.board.insertToDb(database)
        }

        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()

        viewModel.kakuroGame.updateTime(SystemClock.elapsedRealtime() - chronometer!!.base)
        chronometer!!.stop()
    }

    override fun onResume() {
        super.onResume()

        chronometer?.base = SystemClock.elapsedRealtime() - viewModel.kakuroGame.getTime()
        chronometer?.start()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu, menu)

        counter = menu?.findItem(R.id.counter)

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

    private fun getBoardFromDb() : Array<Array<KakuroCell?>> {
        // Get size and time from db
        val cursor = database.getDataGeneral()
        if (cursor.count == 1) {
            cursor.moveToFirst()
            size = cursor.getString(1).toInt()
            timePassed = cursor.getString(3).toLong()
            hintsLeft = cursor.getString(4).toInt()
        }
        cursor.close()

        val board: Array<Array<KakuroCell?>> = Array(size) {
            arrayOfNulls<KakuroCell>(size)
        }

        // Get board from db
        val cursor2 = database.getDataBoard()
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
                        board[row][col] = KakuroCellHint(
                            row, col, cursor2.getString(4).toInt(), cursor2.getString(
                                5
                            ).toInt()
                        )
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
        gameIsFinished = true

        finish()
    }
}
