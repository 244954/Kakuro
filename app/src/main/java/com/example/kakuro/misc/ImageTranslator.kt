package com.example.kakuro.misc

import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import com.example.kakuro.R
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc


class ImageTranslator {

    init {
        OpenCVLoader.initDebug()
    }

    private fun getImage(activity: AppCompatActivity): Mat {
        return Utils.loadResource(activity, R.drawable.kakuro_image, Imgcodecs.IMREAD_COLOR)
    }

    private fun getGrayImageOfOriginal(original: Mat, dest: Mat): Mat {
        Imgproc.cvtColor(original, dest, Imgproc.COLOR_BGR2GRAY)
        return dest
    }

    private fun getColorImageOfOriginal(original: Mat, dest: Mat): Mat {
        Imgproc.cvtColor(original, dest, Imgproc.COLOR_GRAY2BGR)
        return dest
    }

    private fun initialGaussBlur(image: Mat) {
        Imgproc.GaussianBlur(image, image, Size(3.0, 3.0), 0.0, 0.0, Core.BORDER_DEFAULT)
    }

    private fun gaussianBlurAfterTransform(image: Mat) {
        Imgproc.GaussianBlur(image, image, Size(7.0, 7.0), 0.0, 0.0, Core.BORDER_DEFAULT)
    }

    private fun binarizeImage(image: Mat) {
        Imgproc.threshold(image, image, 90.0, 255.0, Imgproc.THRESH_BINARY_INV)
    }

    private fun detectCorners(image: Mat): Mat {
        val contours: MutableList<MatOfPoint> = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(image, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE)

        // left-down, X minimized, Y maximized
        var leftdownX = 1000000.0
        var leftdownY = 0.0
        // left-up, X minimized,, Y minimized,
        var leftupX = 1000000.0
        var leftupY = 1000000.0
        // right-up, X maximized, Y minimized
        var rightupX = 0.0
        var rightupY = 1000000.0
        // right-down, X maximized, Y maximized
        var rightdownX = 0.0
        var rightdownY = 0.0

        var localsum: Double
        var localdiff: Double
        for (i in contours) {
            val points = i.toList()
            for (j in points) {
                localsum = j.x + j.y  // x + y
                localdiff = j.x - j.y // x -y

                if (localsum < leftupX + leftupY) {
                    leftupX = j.x
                    leftupY = j.y
                }
                if (localsum > rightdownX + rightdownY) {
                    rightdownX = j.x
                    rightdownY = j.y
                }
                if (localdiff > rightupX - rightupY) {
                    rightupX = j.x
                    rightupY = j.y
                }
                if (localdiff < leftdownX - leftdownY) {
                    leftdownX = j.x
                    leftdownY = j.y
                }
            }
        }

        val transformMat = Mat(4, 2, CvType.CV_32F)
        transformMat.put(0, 0, leftupX, leftupY, rightupX, rightupY, leftdownX, leftdownY, rightdownX, rightdownY)
        return transformMat
    }

    private fun transformPerspective(image: Mat, transformMat: Mat) {
        val imageSize = image.size()
        val destMat = Mat(4, 2, CvType.CV_32F)
        destMat.put(0, 0, 0.0, 0.0, imageSize.width, 0.0, 0.0, imageSize.height, imageSize.width, imageSize.height)
        val perspectiveMatrix = Imgproc.getPerspectiveTransform(transformMat, destMat)
        Imgproc.warpPerspective(image, image, perspectiveMatrix, imageSize, Imgproc.INTER_CUBIC)
    }

    private fun printImage(image: Mat, activity: AppCompatActivity) {
        // convert to bitmap:
        val bm = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(image, bm)
        // find the imageview and draw it:
        val iv: ImageView = activity.findViewById<View>(R.id.imageView) as ImageView
        iv.setImageBitmap(bm)
    }

    fun processImage(activity: AppCompatActivity) {
        val color = getImage(activity)
        val gray = Mat()
        getGrayImageOfOriginal(color, gray)
        initialGaussBlur(gray)
        binarizeImage(gray)
        getColorImageOfOriginal(gray, color)
        val transformMat = detectCorners(gray)
        transformPerspective(color, transformMat)
        gaussianBlurAfterTransform(color)
        getGrayImageOfOriginal(color, gray)

        printImage(color, activity)
    }
}