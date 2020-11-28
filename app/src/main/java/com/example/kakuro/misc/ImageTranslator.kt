package com.example.kakuro.misc

import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import com.example.kakuro.R
import com.example.kakuro.view.MainActivity
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs


class ImageTranslator {

    init {
        OpenCVLoader.initDebug()
    }

    fun processImage(activity: AppCompatActivity) {
        // val img = Imgcodecs.imread(this.getDrawable(R.drawable.logo)?.toString())

        val img =
            Utils.loadResource(activity, R.drawable.kakuro_image, Imgcodecs.IMREAD_COLOR)
        // val img = Mat.zeros(100, 400, CvType.CV_8UC3)

        // convert to bitmap:
        val bm = Bitmap.createBitmap(img.cols(), img.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(img, bm)

        // find the imageview and draw it!
        val iv: ImageView = activity.findViewById<View>(R.id.imageView) as ImageView
        iv.setImageBitmap(bm)
    }
}