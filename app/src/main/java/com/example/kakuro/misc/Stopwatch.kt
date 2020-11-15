package com.example.kakuro.misc

class Stopwatch () {

    private var startTime : Long = 0
    private var stopTime : Long = 0
    private var timePassed : Long = 0
    private var running : Boolean = false

    fun startWithTime(time: Long) {
        this.startTime = System.currentTimeMillis() - time
        this.running = true
    }

    fun start() {
        this.startTime = System.currentTimeMillis()
        this.running = true
    }

    fun stop() {
        this.stopTime = System.currentTimeMillis()
        this.running = false
        this.timePassed += this.stopTime - this.startTime
    }

    fun getElapsedTime() : Long {
        if (running) {
            return timePassed + (System.currentTimeMillis() - this.startTime)
        }
        return timePassed
    }

    fun wasStopped() : Boolean {
        return !running && startTime != 0L
    }
}