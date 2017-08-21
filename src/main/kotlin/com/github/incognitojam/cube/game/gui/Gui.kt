package com.github.incognitojam.cube.game.gui

import com.github.incognitojam.cube.engine.Window
import com.github.incognitojam.cube.engine.graphics.ShaderProgram
import org.joml.Matrix4f

open class Gui {

    private val guiItems = ArrayList<GuiItem>()

    open fun onInitialise() {

    }

    open fun onUpdate() {
        guiItems.forEach(GuiItem::onUpdate)
    }

    open fun onRender(shader: ShaderProgram, projectionMatrix: Matrix4f) {
        guiItems.forEach {
            shader.setUniform("texture_sampler", 0)
            it.onRender(shader, projectionMatrix)
        }
    }

    open fun onResize(window: Window) {
        guiItems.forEach { it.onResize(window) }
    }

    open fun onCleanup() {
        guiItems.forEach(GuiItem::onCleanup)
    }

    fun addGuiItem(guiItem: GuiItem) {
        guiItems.add(guiItem)
        guiItem.onInitialise()
    }

}