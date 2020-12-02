package com.example.kakuro.view

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import com.example.kakuro.R
import com.example.kakuro.datahandling.DatabaseHelper
import com.example.kakuro.misc.ImageTranslator
import kotlinx.android.synthetic.main.activity_main.*
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs


class MainActivity : AppCompatActivity() {

    private lateinit var database: DatabaseHelper
    private lateinit var openCVConverter: ImageTranslator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        database = DatabaseHelper(this)
        openCVConverter = ImageTranslator(this)
    }

    fun onClickContinue(v: View) {
        val kakuroIntent = Intent(this, KakuroActivity::class.java)
        val b = Bundle()
        b.putInt("board", 0) // 0 means load saved
        kakuroIntent.putExtras(b)
        startActivity(kakuroIntent)
    }

    fun onclickbuttonstaged(v: View) {
        goToSelect()
    }

    fun onClickGenerate(v: View) {
        openCVConverter.processImage()
//        OpenCVLoader.initDebug()
//        // val img = Imgcodecs.imread(this.getDrawable(R.drawable.logo)?.toString())
//
//        val img = Mat.zeros(100, 400, CvType.CV_8UC3)
//
//        // convert to bitmap:
//        val bm = Bitmap.createBitmap(img.cols(), img.rows(), Bitmap.Config.ARGB_8888)
//        Utils.matToBitmap(img, bm)
//
//        // find the imageview and draw it!
//        val iv: ImageView = findViewById<View>(R.id.imageView) as ImageView
//        iv.setImageBitmap(bm)
    }

    fun goToSelect() {
        val kakuroIntent = Intent(this, SelectBoardActivity::class.java)
        startActivity(kakuroIntent)
    }

    override fun onResume() {
        super.onResume()
        getData1() // check whether it is possible to continue every time we enter this screen
    }

    private fun getData1() {
        val cursor = database.getData1()
        button_continue.isEnabled = ( cursor.count == 1 )
        cursor.close()
    }
}
