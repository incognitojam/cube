package com.github.incognitojam.cube.engine

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryUtil.NULL

class Window(val title: String, var width: Int, var height: Int, val vSync: Boolean) {

    val aspectRatio: Float
        get() = width.toFloat() / height.toFloat()

    var windowHandle: Long = -1L
        get() = field
        private set(value) {
            field = value
        }
    var resized = true
    var captureMouse = true
    private var capturedMouse = false

    fun onInitialise() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set()

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit()) {
            throw IllegalStateException("Unable to initialize GLFW")
        }

        glfwDefaultWindowHints() // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE) // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE) // the window will be resizable
        glfwWindowHint(GLFW_FOCUSED, GLFW_TRUE)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1)
//        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
//        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE)

        // Create the window
        windowHandle = glfwCreateWindow(width, height, title, NULL, NULL)
        if (windowHandle == NULL) {
            throw RuntimeException("Failed to create the GLFW window")
        }

        // Setup resize callback
        glfwSetFramebufferSizeCallback(windowHandle) { _, width, height ->
            this.width = width
            this.height = height
            this.resized = true
        }

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(windowHandle) { window, key, _, action, _ ->
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true) // We will detect this in the rendering loop
            }
        }

        // Get the resolution of the primary monitor
        val videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor())
        // Center our window
        glfwSetWindowPos(
                windowHandle,
                (videoMode.width() - width) / 2,
                (videoMode.height() - height) / 2
        )

        // Make the OpenGL context current
        glfwMakeContextCurrent(windowHandle)

        // Enable v-sync
        glfwSwapInterval(if (vSync) 1 else 0)

        // Make the window visible
        glfwShowWindow(windowHandle)

        GL.createCapabilities()

        // Set the clear colour
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        glEnable(GL_DEPTH_TEST)
        glEnable(GL_CULL_FACE)
        glCullFace(GL_BACK)

        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
    }

    fun update() {
        glfwSwapBuffers(windowHandle)
        glfwPollEvents()

        if (captureMouse != capturedMouse) {
            if (captureMouse) {
                glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
            } else {
                glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_NORMAL)
            }
            capturedMouse = captureMouse
        }
    }

    fun setClearColor(r: Float, g: Float, b: Float, a: Float) = glClearColor(r, g, b, a)

    fun isKeyPressed(keyCode: Int) = glfwGetKey(windowHandle, keyCode) == GLFW_PRESS

    fun isMousePressed(keyCode: Int) = glfwGetMouseButton(windowHandle, keyCode) == GLFW_PRESS

    fun windowShouldClose() = glfwWindowShouldClose(windowHandle)

}