package com.example.kakuro.view

import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.kakuro.R
import com.example.kakuro.datahandling.DatabaseHelper
import com.example.kakuro.gamelogic.*
import com.example.kakuro.misc.ImageTranslator
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var database: DatabaseHelper
    private lateinit var openCVConverter: ImageTranslator
    private lateinit var boardGenerator: BoardGenerator

    private var image: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        database = DatabaseHelper(this)
        openCVConverter = ImageTranslator(this)
        boardGenerator = BoardGenerator()
    }

    fun onClickContinue(v: View) {
        val kakuroIntent = Intent(this, KakuroActivity::class.java)
        val b = Bundle()
        b.putInt("board", 0) // 0 means load saved
        kakuroIntent.putExtras(b)
        startActivity(kakuroIntent)
    }

    fun onClickButtonStaged(v: View) {
        goToSelect()
    }

    fun onClickGenerate(v: View) {
        val board = boardGenerator.generate()
        database.clearData()
        database.insertDataGeneral(board.size, board.size, 0)
        insertToDb(board)

        val kakuroIntent = Intent(this, KakuroActivity::class.java)
        val b = Bundle()
        b.putInt("board", 1) // 1 means generated
        kakuroIntent.putExtras(b)
        startActivity(kakuroIntent)
    }

    fun onClickFromPhoto(v: View) {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(gallery, pickImage)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == pickImage) {

            val imageUri = data?.data
            image = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)

            val board = openCVConverter.processImage(image)
            database.clearData()
            database.insertDataGeneral(board.size, board.size, 0)
            insertToDb(board)

            val kakuroIntent = Intent(this, KakuroActivity::class.java)
            val b = Bundle()
            b.putInt("board", 2) // 2 means scanned
            kakuroIntent.putExtras(b)
            startActivity(kakuroIntent)
        }
    }

    private fun insertToDb(board: Array<Array<KakuroCell?>>) {
        for (row in board.indices) {
            for (col in board.indices) {
                when(val cell = board[row][col]) {
                    is KakuroCellBlank -> {
                        database.insertDataBoard(row, col, 0, 0, 0) // 0 - blank
                    }
                    is KakuroCellValue -> {
                        database.insertDataBoard(row, col, 1, cell.value, 0) // 1 - value
                    }
                    is KakuroCellHint -> {
                        database.insertDataBoard(row, col, 2, cell.hintRight, cell.hintDown) // 2 - hint
                    }
                }
            }
        }
    }

    private fun goToSelect() {
        val kakuroIntent = Intent(this, SelectBoardActivity::class.java)
        startActivity(kakuroIntent)
    }

    override fun onResume() {
        super.onResume()
        getData1() // check whether it is possible to continue every time we enter this screen
    }

    private fun getData1() {
        val cursor = database.getDataGeneral()
        button_continue.isEnabled = ( cursor.count == 1 )
        cursor.close()
    }

    companion object {
        private val pickImage = 100
    }
}
