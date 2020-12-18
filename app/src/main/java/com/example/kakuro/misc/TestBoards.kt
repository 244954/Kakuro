package com.example.kakuro.misc

class TestBoards {
    companion object{
        fun getSize(text: String): Int {
            val lines = text.lines()
            return lines[0].toInt()
        }
        fun getBoardFromFile(text: String) : Array<Array<Int>> {

            var lines = text.lines()
            lines = lines.drop(1)

            val arr = Array(lines.size) { Array(5) { 0 }}

            for (linenr in lines.indices) {
                val line = lines[linenr].split(" ")
                for (number in line.indices) {
                    arr[linenr][number] = line[number].toInt()
                }
            }

            return arr
        }
    }
}