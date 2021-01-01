package com.example.kakuro.view

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
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
        // display dialog
        val alert: AlertDialog.Builder = AlertDialog.Builder(
            this
        )
        val inflater = layoutInflater
        val view: View =
            inflater.inflate(R.layout.dialog_board_size, null)

        alert.setView(view)
        val dialog = alert.create()

        val button1: Button =
            view.findViewById<View>(R.id.button3x3) as Button
        val button2: Button =
            view.findViewById<View>(R.id.button5x5) as Button
        val button3: Button =
            view.findViewById<View>(R.id.button8x8) as Button
        button1.setOnClickListener { chosenSize(dialog, 3) }
        button2.setOnClickListener { chosenSize(dialog, 5) }
        button3.setOnClickListener { chosenSize(dialog, 9) }

        dialog.show()
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
            val boardModel = KakuroBoardModel(board.size, board)
            database.clearData()
            database.insertDataGeneral(board.size, board.size, 0, boardModel.recommendedHintsAmount())
            boardModel.insertToDb(database)

            val kakuroIntent = Intent(this, KakuroActivity::class.java)
            val b = Bundle()
            b.putInt("board", 2) // 2 means scanned
            kakuroIntent.putExtras(b)
            startActivity(kakuroIntent)
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

    private fun chosenSize(dialog: AlertDialog, size: Int) {
        val board = boardGenerator.generate(size)
        val boardModel = KakuroBoardModel(board.size, board)
        database.clearData()
        database.insertDataGeneral(board.size, board.size, 0, boardModel.recommendedHintsAmount())
        boardModel.insertToDb(database)

        val kakuroIntent = Intent(this, KakuroActivity::class.java)
        val b = Bundle()
        b.putInt("board", 1) // 1 means generated
        kakuroIntent.putExtras(b)
        startActivity(kakuroIntent)

        dialog.dismiss()
    }

    companion object {
        private const val pickImage = 100
    }
}
