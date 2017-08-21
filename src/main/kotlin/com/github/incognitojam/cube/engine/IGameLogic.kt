package com.github.incognitojam.cube.engine

interface IGameLogic {

    @Throws(Exception::class)
    fun onInitialise(window: Window)

    fun onStatus(frames: Int, updates: Int)

    fun onInput(window: Window, mouseInput: MouseInput)

    fun onUpdate(window: Window, delta: Float, mouseInput: MouseInput)

    fun onRender(window: Window)

    fun onCleanup()

}
