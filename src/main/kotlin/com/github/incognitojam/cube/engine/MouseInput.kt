package com.github.incognitojam.cube.engine

import org.joml.Vector2d
import org.joml.Vector2f
import org.lwjgl.glfw.GLFW.*

class MouseInput {

    private val previousPos = Vector2d(-1.0, -1.0)
    private val currentPos = Vector2d()
    private val previousScrollPos = Vector2d()
    private val scrollPos = Vector2d()

    val displayVec: Vector2f = Vector2f()

    private var inWindow = false
    var leftButtonPressed = false
        get() = field
        private set(value) {
            field = value
        }
    var rightButtonPressed = false
        get() = field
        private set(value) {
            field = value
        }

    var scrollX = 0.0
    var scrollY = 0.0

    fun initialise(window: Window) {
        glfwSetCursorPosCallback(window.windowHandle) { _, xPos, yPos ->
            currentPos.x = xPos
            currentPos.y = yPos
        }
        glfwSetCursorEnterCallback(window.windowHandle) { _, entered -> inWindow = entered }
        glfwSetMouseButtonCallback(window.windowHandle) { _, button, action, _ ->
            leftButtonPressed = button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS
            rightButtonPressed = button == GLFW_MOUSE_BUTTON_2 && action == GLFW_PRESS
        }
        glfwSetScrollCallback(window.windowHandle) { _, xOffset, yOffset ->
            scrollPos.x += xOffset
            scrollPos.y += yOffset
        }
    }

    fun input(window: Window) {
        displayVec.x = 0f
        displayVec.y = 0f
//        if (previousPos.x > 0 && previousPos.y > 0 && inWindow) {
        if (inWindow) {
            val deltaX = currentPos.x - previousPos.x
            val deltaY = currentPos.y - previousPos.y

            val rotateX = deltaX != 0.0
            val rotateY = deltaY != 0.0
            if (rotateX) {
                displayVec.y = deltaX.toFloat()
            }
            if (rotateY) {
                displayVec.x = deltaY.toFloat()
            }
        }
        previousPos.x = currentPos.x
        previousPos.y = currentPos.y

        scrollX = scrollPos.x - previousScrollPos.x
        scrollY = scrollPos.y - previousScrollPos.y
        previousScrollPos.set(scrollPos)
    }

    override fun toString(): String = "MouseInput(displVec=$displayVec)"

}