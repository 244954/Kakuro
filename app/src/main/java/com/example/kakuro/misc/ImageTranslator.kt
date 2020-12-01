package com.example.kakuro.misc

import android.R.attr.radius
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import com.example.kakuro.R
import com.example.kakuro.gamelogic.KakuroCell
import com.example.kakuro.gamelogic.KakuroCellValue
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin


class ImageTranslator {

    var gridSize: Int = 0 // assume grid is square
    var epsilon: Double = 0.0
    var epsilonEdges: Double = 0.0
    var epsilonNear: Double = 0.0
    var epsilonSlope: Double = 0.0

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
        Imgproc.findContours(
            image,
            contours,
            hierarchy,
            Imgproc.RETR_TREE,
            Imgproc.CHAIN_APPROX_SIMPLE
        )

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
        transformMat.put(
            0,
            0,
            leftupX,
            leftupY,
            rightupX,
            rightupY,
            leftdownX,
            leftdownY,
            rightdownX,
            rightdownY
        )
        return transformMat
    }

    private fun transformPerspective(image: Mat, transformMat: Mat) {
        val imageSize = image.size()
        val destMat = Mat(4, 2, CvType.CV_32F)
        destMat.put(
            0,
            0,
            0.0,
            0.0,
            imageSize.width,
            0.0,
            0.0,
            imageSize.height,
            imageSize.width,
            imageSize.height
        )
        val perspectiveMatrix = Imgproc.getPerspectiveTransform(transformMat, destMat)
        Imgproc.warpPerspective(image, image, perspectiveMatrix, imageSize, Imgproc.INTER_CUBIC)
    }

    private fun legalLine(
        lineList: MutableList<Triple<Double, Double, Double>>,
        x0: Double,
        y0: Double,
        a: Double,
        epsilon1: Double
    ): Boolean {
        for (triple in lineList) {
            if (abs(x0 - triple.first) < epsilon1 && abs(y0 - triple.second) < epsilon1) {
                return false
            }
        }
        lineList.add(Triple(x0, y0, a))
        return true
    }

    private fun detectGrid(image: Mat) {
        val edges = Mat()
        val lines = Mat()
        Imgproc.Canny(image, edges, 50.0, 100.0)
        Imgproc.HoughLines(edges, lines, 1.0, Math.PI / 180.0, 250, 0.0, 0.0)
        // filter lines
        val imageSize = image.size()
        epsilon = imageSize.height / 10.0
        epsilonEdges = imageSize.height / 25
        epsilonNear = imageSize.height / 50
        epsilonSlope = 1.0 / 50.0
        val gridLines = mutableListOf<Triple<Double, Double, Double>>()
        // (x0, y0, a)
        if (!lines.empty()) {
            for (x in 0 until lines.rows()) {
                val rho = lines[x, 0][0]
                val theta = lines[x, 0][1]
                val a = cos(theta)
                val b = sin(theta)
                val x0 = a * rho
                val y0 = b * rho
                val pt1 = Point(
                    (x0 + imageSize.width * -b).roundToInt().toDouble(),
                    (y0 + imageSize.height * a).roundToInt().toDouble()
                )
                val pt2 = Point(
                    (x0 - imageSize.width * -b).roundToInt().toDouble(),
                    (y0 - imageSize.height * a).roundToInt().toDouble()
                )
                if (abs(pt1.x - pt2.x) < epsilon || abs(pt1.y - pt2.y) < epsilon) { // ignore diagonal lines
                    if (pt1.x in epsilonEdges..(imageSize.width - epsilonEdges) ||
                        pt2.x in epsilonEdges..(imageSize.width - epsilonEdges) &&
                        pt1.y in epsilonEdges..(imageSize.height - epsilonEdges) ||
                        pt2.y in epsilonEdges..(imageSize.height - epsilonEdges)) { // ignore if too close to the edges
                        legalLine(gridLines, x0, y0, a, epsilonNear) // not too close to other lines
//                        if (legalLine(gridLines, x0, y0, a, epsilonNear)) { // not too close to other lines
//                            Imgproc.line(image, pt1, pt2, Scalar(0.0, 0.0, 255.0), Imgproc.LINE_AA)
//                        }
                    }
                }
            }
            gridSize = gridLines.size / 2 + 1
        }
    }

    private fun splitIntoTiles(image: Mat): Array<Array<Mat?>>? {
        if (gridSize == 0) {
            return null
        }
        else {
            val size = image.size()
            val rectWidth = (size.width / gridSize).toInt()
            val rectHeight = (size.height / gridSize).toInt()

            val grid = Array(gridSize) {
                arrayOfNulls<Mat>(gridSize)
            }
            for (i in 0 until gridSize) {
                for (j in 0 until gridSize) {
                    grid[i][j] = image.submat(
                        i * rectHeight,
                        (i + 1) * rectHeight,
                        j * rectWidth,
                        (j + 1) * rectWidth
                    )
                }
            }
            return grid
        }
    }

    private fun hasADiagonalLine(lines: Mat, imageSize: Size): Boolean {
        if (!lines.empty()) {
            for (x in 0 until lines.rows()) {
                val rho = lines[x, 0][0]
                val theta = lines[x, 0][1]
                val a = cos(theta)
                val b = sin(theta)
                val x0 = a * rho
                val y0 = b * rho
                val pt1 = Point(
                    (x0 + imageSize.width * -b).roundToInt().toDouble(),
                    (y0 + imageSize.height * a).roundToInt().toDouble()
                )
                val pt2 = Point(
                    (x0 - imageSize.width * -b).roundToInt().toDouble(),
                    (y0 - imageSize.height * a).roundToInt().toDouble()
                )
                val slope = if (abs(pt2.x - pt1.x) < 0.01) {  // protect us from division by 0 !
                    0.0
                } else {
                    (pt2.y - pt1.y) / (pt2.x - pt1.x)
                }
                if (abs(slope - 1.0) < 0.01) {  // needs more specific epsilon!
                    return true
                }
            }
        }
        return false
    }

    private fun identifyTiles(grid: Array<Array<Mat?>>): Array<Array<KakuroCell?>> {
        val board = Array(gridSize) {
            arrayOfNulls<KakuroCell>(gridSize)
        }

        var color: Mat
        val gray = Mat()
        val edges = Mat()
        val lines = Mat()
        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        var imageSize: Size

        for (i in 0 until gridSize) {
            for (j in 0 until gridSize) {
                color = grid[i][j]!!
                Imgproc.cvtColor(color, gray, Imgproc.COLOR_BGR2GRAY)
                imageSize = color.size()

                // detect diagonal line

                Imgproc.Canny(color, edges, 50.0, 100.0)
                Imgproc.HoughLines(edges, lines, 1.0, Math.PI / 180.0, 100, 0.0, 0.0)

                if (hasADiagonalLine(lines, imageSize)) {
                    // detect numbers

                    Imgproc.findContours(
                        gray,
                        contours,
                        hierarchy,
                        Imgproc.RETR_TREE,
                        Imgproc.CHAIN_APPROX_SIMPLE
                    )

                    val contoursPoly = arrayOfNulls<MatOfPoint2f>(contours.size)
                    val boundRect: Array<Rect?>? = arrayOfNulls(contours.size)

                    for (k in contours.indices) {
                        contoursPoly[k] = MatOfPoint2f()
                        Imgproc.approxPolyDP(MatOfPoint2f(*contours[k].toArray()), contoursPoly[k], 5.0, true)
                        boundRect!![k] = Imgproc.boundingRect(contoursPoly[k])
                    }

                    for (k in contours.indices) {
                        // evaluate bound rects
                    }
                }
                else {
                    board[i][j] = KakuroCellValue(i, j)
                }
            }
        }

        return board
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
        detectGrid(color)
        val gridTiles = splitIntoTiles(color)!!
        identifyTiles(gridTiles)

        printImage(gridTiles[1][2]!!, activity)
    }
}