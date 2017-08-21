package com.github.incognitojam.cube

import com.github.incognitojam.cube.engine.GameEngine
import com.github.incognitojam.cube.game.GamCraft

fun main(args: Array<String>) {

    // required for macOS
    System.setProperty("java.awt.headless", "true")

    val vSync = true
    val gameLogic = GamCraft()
    val gameEngine = GameEngine("Project Cube", 1600, 900, vSync, gameLogic)

    try {
        gameEngine.start()
    } catch (exception: Exception) {
        exception.printStackTrace()
        System.exit(-1)
    }

}