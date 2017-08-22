package com.github.incognitojam.cube.engine

interface IGameLogic {

    @Throws(Exception::class)
    fun initialise(window: Window)

    fun status(frames: Int, updates: Int)

    fun input(window: Window, mouseInput: MouseInput)

    fun update(window: Window, delta: Float, mouseInput: MouseInput)

    fun render(window: Window)

    fun delete()

}
