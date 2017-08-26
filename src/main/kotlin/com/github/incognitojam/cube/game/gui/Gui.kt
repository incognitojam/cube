package com.github.incognitojam.cube.game.gui

import com.github.incognitojam.cube.engine.Window
import com.github.incognitojam.cube.engine.graphics.ShaderProgram
import org.joml.Matrix4f

open class Gui {

    private val guiItems = ArrayList<GuiItem>()

    open fun initialise() = Unit

    open fun update() {
        guiItems.forEach(GuiItem::update)
    }

    open fun render(shader: ShaderProgram, projectionMatrix: Matrix4f) {
        guiItems.forEach {
            shader.setUniform("textureSampler", 0)
            it.render(shader, projectionMatrix)
        }
    }

    open fun resize(window: Window) {
        guiItems.forEach { it.resize(window) }
    }

    open fun delete() {
        guiItems.forEach(GuiItem::delete)
    }

    fun addGuiItem(guiItem: GuiItem) {
        guiItems.add(guiItem)
        guiItem.initialise()
    }

}