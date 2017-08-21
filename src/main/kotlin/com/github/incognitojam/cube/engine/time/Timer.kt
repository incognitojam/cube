package com.github.incognitojam.cube.engine.time

object Timer {

    class TimerInstance(private val name: String) {
        val elapsedTime: Double
            get() = Timer.getElapsedTime(name)

        override fun toString(): String {
            return String.format("%.3G", elapsedTime)
        }
    }

    private val lastLoopTime = HashMap<String, Double>()

    fun start(name: String): TimerInstance {
        lastLoopTime[name] = getTime()
        return TimerInstance(name)
    }

    fun getElapsedTime(name: String): Double {
        val currentTime = getTime()
        val elapsedTime = currentTime - (lastLoopTime[name] ?: -1.0)
        lastLoopTime[name] = currentTime
        return elapsedTime
    }

    fun getLastLoopTime(name: String): Double {
        return lastLoopTime[name] ?: -1.0
    }

    fun getTime(): Double = System.nanoTime() / 1_000_000_000.0

}