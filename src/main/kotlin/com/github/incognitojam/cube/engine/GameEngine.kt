package com.github.incognitojam.cube.engine

import com.github.incognitojam.cube.engine.time.Timer

class GameEngine(windowTitle: String, width: Int, height: Int, vSync: Boolean, private val gameLogic: IGameLogic): Runnable {

    private val window: Window = Window(windowTitle, width, height, vSync)
    private val gameLoopThread: Thread = Thread(this, "GAME_LOOP_THREAD")
    private val mouseInput = MouseInput()
    var running: Boolean = false

    private var lastSecond: Double = Timer.getTime()
    private var frames: Int = 0
    private var updates: Int = 0

    fun start() {
        running = true
        val osName = System.getProperty("os.name")
        if (osName.contains("Mac")) {
            gameLoopThread.run()
        } else {
            gameLoopThread.start()
        }
    }

    override fun run() {
        try {
            initialise()
            startGameLoop()
        } catch (exception: Exception) {
            exception.printStackTrace()
        } finally {
            delete()
        }
    }

    @Throws(Exception::class)
    private fun initialise() {
        Timer.start("gameLoop")
        window.initialise()
        mouseInput.initialise(window)
        gameLogic.initialise(window)
    }

    private fun startGameLoop() {
        var elapsedTime: Double
        var accumulator = .0
        val interval = 1.0 / TARGET_UPS

        while (running && !window.windowShouldClose()) {
            val currentTime = Timer.getTime()
            val difference = currentTime - lastSecond
            frames++
            if (difference > 1) {
                lastSecond = currentTime
                gameLogic.status(frames, updates)
                frames = 0
                updates = 0
            }

            elapsedTime = Timer.getElapsedTime("gameLoop")
            accumulator += elapsedTime

            input()

            while (accumulator >= interval) {
                update(interval.toFloat())
                updates++
                accumulator -= interval
            }

            render()

            // If vSync has not been enabled, sync manually
            if (!window.vSync) {
                sync()
            }
        }
    }

    private fun delete() = gameLogic.delete()

    private fun sync() {
        val loopSlot = 1f / TARGET_FPS
        val endTime = Timer.getLastLoopTime("gameLoop") + loopSlot
        while (Timer.getTime() < endTime) {
            try {
                Thread.sleep(1)
            } catch (_: InterruptedException) {
            }
        }
    }

    private fun input() {
        mouseInput.input(window)
        gameLogic.input(window, mouseInput)
    }

    private fun update(delta: Float) = gameLogic.update(window, delta, mouseInput)

    private fun render() {
        gameLogic.render(window)
        window.update()
    }

    companion object {
        const val TARGET_FPS = 60.0
        const val TARGET_UPS = 20.0
    }

}