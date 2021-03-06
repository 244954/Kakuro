package com.example.kakuro.misc

import android.content.ContentResolver
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.kakuro.R
import com.example.kakuro.gamelogic.KakuroCell
import com.example.kakuro.gamelogic.KakuroCellBlank
import com.example.kakuro.gamelogic.KakuroCellHint
import com.example.kakuro.gamelogic.KakuroCellValue
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import kotlin.math.*


data class MutableTriple(var first: Rect, var second: Boolean, var third: Int)


class ImageTranslator(private val context: AppCompatActivity) {

    var gridSize: Int = 0 // assume grid is square
    var epsilon: Double = 0.0
    var epsilonEdges: Double = 0.0
    var epsilonNear: Double = 0.0
    var epsilonSlope: Double = 0.0

    private var interpreter: Interpreter? = null
    var isInitialized = false
        private set

    private var inputImageWidth: Int = 0 // will be inferred from TF Lite model
    private var inputImageHeight: Int = 0 // will be inferred from TF Lite model
    private var modelInputSize: Int = 0 // will be inferred from TF Lite model

    private var imageToDraw: Mat? = null


    init {
        OpenCVLoader.initDebug()
        initializeInterpreter()
    }

    private fun initializeInterpreter() {
        // Load the TF Lite model created earlier
        val assetManager = context.assets
        val model = loadModelFile(assetManager)

        // Initialize TF Lite Interpreter with NNAPI enabled
        val options = Interpreter.Options()
        options.setUseNNAPI(true)
        val interpreter = Interpreter(model, options)

        // Read input shape from model file
        val inputShape = interpreter.getInputTensor(0).shape()
        inputImageWidth = inputShape[1]
        inputImageHeight = inputShape[2]
        modelInputSize = FLOAT_TYPE_SIZE * inputImageWidth * inputImageHeight * PIXEL_SIZE

        // Finish interpreter initialization
        this.interpreter = interpreter
        isInitialized = true
    }

    private fun loadModelFile(assetManager: AssetManager): ByteBuffer {
        val fileDescriptor = assetManager.openFd(MODEL_FILE)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(modelInputSize)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(inputImageWidth * inputImageHeight)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        for (pixelValue in pixels) {
            val alpha = (pixelValue shr 24 and 0xFF).toFloat()
            val r = (pixelValue shr 16 and 0xFF).toFloat()
            val g = (pixelValue shr 8 and 0xFF).toFloat()
            val b = (pixelValue and 0xFF).toFloat()

            // Convert RGB to grayscale and normalize pixel value to [0..1]
            val normalizedPixelValue = (r * 0.2989f + g * 0.587f + b * 0.114f) / 255.0f
            byteBuffer.putFloat(normalizedPixelValue)
        }

        return byteBuffer
    }

    private fun digitRecognition(mat: Mat): Int {
        // val imageTest = Utils.loadResource(context, R.drawable.imagetest, Imgcodecs.IMREAD_GRAYSCALE)
        val bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, bitmap)

        // Preprocessing: resize the input
        val resizedImage = Bitmap.createScaledBitmap(
            bitmap,
            inputImageWidth,
            inputImageHeight,
            true
        )
        val byteBuffer = convertBitmapToByteBuffer(resizedImage)

        val result = Array(1) { FloatArray(OUTPUT_CLASSES_COUNT) }
        interpreter?.run(byteBuffer, result)

        val resultCorrected = result[0]
        resultCorrected[1] = resultCorrected[7].also { resultCorrected[7] = resultCorrected[1] } // swap
        return getOutputString(resultCorrected)
    }

    private fun getOutputString(output: FloatArray): Int {
        return output.indices.maxBy { output[it] } ?: -1
    }

    private fun getImage(activity: AppCompatActivity): Mat {
        return Utils.loadResource(activity, R.drawable.kakuro_image, Imgcodecs.IMREAD_COLOR)
    }

    private fun getImageFromBitmap(bitmap: Bitmap): Mat {
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)
        return mat
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
                if (abs(slope - 1.0) < 0.1) {  // needs more specific epsilon!
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
                contours.clear() // don't forget clearing arrays

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
                    val boundRectWithDigits: MutableList<MutableTriple> = arrayListOf()

                    // detect all bounding rectangles

                    for (k in contours.indices) {
                        contoursPoly[k] = MatOfPoint2f()
                        Imgproc.approxPolyDP(
                            MatOfPoint2f(*contours[k].toArray()),
                            contoursPoly[k],
                            5.0,
                            true
                        )
                        boundRect!![k] = Imgproc.boundingRect(contoursPoly[k])
                    }

                    // filter detected rectangles with digits on them

                    // for tests
                    // val bitmap = Bitmap.createBitmap(color.cols(), color.rows(), Bitmap.Config.ARGB_8888)
                    // Utils.matToBitmap(color, bitmap)
                    for (k in contours.indices) {
                        if (boundRect!![k]!!.width in (imageSize.width / 70).toInt()..(imageSize.width / 2).toInt()
                            && boundRect[k]!!.height in (imageSize.height / 70).toInt()..(imageSize.height / 2).toInt()) { // delete too small and too big
                            addBoundRect(boundRectWithDigits, boundRect[k]!!)
                        }
                        // Imgproc.rectangle(color, boundRect[k], Scalar(0.0, 120.0, 120.0), 2)
                    }
                    // Utils.matToBitmap(color, bitmap)

                    if (boundRectWithDigits.isEmpty()) { // it has no hints, its blank tile
                        board[i][j] = KakuroCellBlank(i, j)
                    }
                    else {
                        // it's a hint tile
                        // now crop detected digits and pass them to NN for recognition
                        val upNumbers: MutableList<MutableTriple> = arrayListOf()
                        val downNumbers: MutableList<MutableTriple> = arrayListOf()

                        for (k in boundRectWithDigits) {
                            val currRect = k.first
                            val widthDiff = (currRect.height - currRect.width) / 2

                            val digit = color.submat(currRect)
                            val newDigit = Mat(digit.size(), digit.type()) // it's really retarded that i have to do that
                            digit.copyTo(newDigit)
                            if (widthDiff > 0) {
                                Core.copyMakeBorder(
                                    newDigit, newDigit, 0, 0, widthDiff, widthDiff,
                                    Core.BORDER_CONSTANT, Scalar(0.0, 0.0, 0.0)
                                )
                            }
                            Imgproc.resize(
                                newDigit, newDigit, Size(
                                    inputImageWidth.toDouble(),
                                    inputImageHeight.toDouble()
                                ), 0.0, 0.0, Imgproc.INTER_AREA
                            )
                            imageToDraw = newDigit
                            if (isInitialized) {
                                k.third = digitRecognition(newDigit)
                                if (k.second == UP) {
                                    upNumbers.add(k)
                                }
                                else {
                                    downNumbers.add(k)
                                }
                            }
                        }
                        // insert found digits
                        upNumbers.sortByDescending { it.first.x }
                        downNumbers.sortByDescending { it.first.x }
                        var rightHint = 0
                        var downHint = 0
                        for (l in 0 until upNumbers.size) {
                            rightHint += upNumbers[l].third * 10.0.pow(l.toDouble()).toInt()
                        }
                        for (l in 0 until downNumbers.size) {
                            downHint += downNumbers[l].third * 10.0.pow(l.toDouble()).toInt()
                        }
                        board[i][j] = KakuroCellHint(i, j, rightHint, downHint)
                    }
                }
                else {
                    board[i][j] = KakuroCellValue(i, j)
                }
            }
        }

        return board
    }

    private fun addBoundRect(arr: MutableList<MutableTriple>, rect: Rect) {
        for (i in arr) {
            if (rect1InsideRect2(rect, i.first)) {
                return // don't insert
            }
            if (rect1InsideRect2(i.first, rect)) {
                if (rect.x > rect.y) {
                    arr.add(MutableTriple(rect, UP, 0))
                }
                else {
                    arr.add(MutableTriple(rect, DOWN, 0))
                }
                arr.remove(i)
                return
            }
        }
        if (rect.x > rect.y) {
            arr.add(MutableTriple(rect, UP, 0))
        }
        else {
            arr.add(MutableTriple(rect, DOWN, 0))
        }
        return
    }

    private fun rect1InsideRect2(rect1: Rect, rect2: Rect): Boolean {
        if (rect1.x > rect2.x && rect1.x + rect1.width < rect2.x + rect2.width &&
            rect1.y > rect2.y && rect1.y + rect1.height < rect2.y + rect2.height) {
            return true
        }
        return false
    }

    private fun printImage(image: Mat, activity: AppCompatActivity) {
        // convert to bitmap:
        val bm = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(image, bm)
        // find the imageview and draw it:
        val iv: ImageView = activity.findViewById<View>(R.id.imageView) as ImageView
        iv.setImageBitmap(bm)
    }

    fun processImage(bitmap: Bitmap?): Array<Array<KakuroCell?>> {
        val color: Mat = if (bitmap == null) {
            getImage(context)
        } else {
            getImageFromBitmap(bitmap)
        }
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
        return identifyTiles(gridTiles)
    }

    companion object {
        private const val TAG = "DigitClassifier"

        private const val MODEL_FILE = "mnist2.tflite"

        private const val FLOAT_TYPE_SIZE = 4
        private const val PIXEL_SIZE = 1

        private const val OUTPUT_CLASSES_COUNT = 10

        private const val UP = true
        private const val DOWN = false
    }
}