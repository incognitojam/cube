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
            onInit()
            startGameLoop()
        } catch (exception: Exception) {
            exception.printStackTrace()
        } finally {
            onCleanup()
        }
    }

    @Throws(Exception::class)
    private fun onInit() {
        Timer.start("gameLoop")
        window.onInitialise()
        mouseInput.onInitialise(window)
        gameLogic.onInitialise(window)
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
                gameLogic.onStatus(frames, updates)
                frames = 0
                updates = 0
            }

            elapsedTime = Timer.getElapsedTime("gameLoop")
            accumulator += elapsedTime

            onInput()

            while (accumulator >= interval) {
                onUpdate(interval.toFloat())
                updates++
                accumulator -= interval
            }

            onRender()

            // If vSync has not been enabled, sync manually
            if (!window.vSync) {
                sync()
            }
        }
    }

    private fun onCleanup() = gameLogic.onCleanup()

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

    private fun onInput() {
        mouseInput.onInput(window)
        gameLogic.onInput(window, mouseInput)
    }

    private fun onUpdate(delta: Float) = gameLogic.onUpdate(window, delta, mouseInput)

    private fun onRender() {
        gameLogic.onRender(window)
        window.update()
    }

    companion object {
        const val TARGET_FPS = 60.0
        const val TARGET_UPS = 20.0
    }

}