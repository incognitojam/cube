package com.github.incognitojam.cube.game.gui

import com.github.incognitojam.cube.engine.Window
import com.github.incognitojam.cube.engine.file.FileUtils
import com.github.incognitojam.cube.engine.graphics.ShaderProgram
import com.github.incognitojam.cube.engine.graphics.Transformation

class GuiRenderer {

    private val guiShader = ShaderProgram()

    @Throws(Exception::class)
    fun initialise() {
        guiShader.initialise()
        guiShader.createVertexShader(FileUtils.loadTextResource("shaders/hud.vertex.glsl"))
        guiShader.createFragmentShader(FileUtils.loadTextResource("shaders/hud.fragment.glsl"))
        guiShader.link()

        // Create uniforms for orthographic-model projection matrix and base colour
        guiShader.createUniform("projectionModelMatrix")
        guiShader.createUniform("colour")
        guiShader.createUniform("hasTexture")
    }

    fun render(window: Window, gui: Gui) {
        guiShader.bind()

        val orthographic = Transformation.getOrthographicProjectionMatrix(0, window.width, window.height, 0)
        gui.render(guiShader, orthographic)

        guiShader.unbind()
    }

    fun delete() {
        guiShader.delete()
    }

}