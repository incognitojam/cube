package com.github.incognitojam.cube.game.gui

import com.github.incognitojam.cube.engine.Window
import com.github.incognitojam.cube.engine.graphics.FontTexture
import com.github.incognitojam.cube.engine.graphics.TextureMap
import com.github.incognitojam.cube.game.world.World
import java.awt.Font

class GuiHud(private val world: World): Gui() {

    private lateinit var hudTextureMap: TextureMap

    private lateinit var fontTexture: FontTexture
    private lateinit var statusTextItem: TextItem
    private lateinit var debugTextItem: TextItem
    private lateinit var crosshair: GuiItemCrosshair
    private lateinit var hotbar: GuiItemHotbar

    override fun initialise() {
        hudTextureMap = TextureMap.loadTextureMap("textures/gui/hud.png", 4)

        fontTexture = FontTexture(FONT, CHARSET)
        statusTextItem = TextItem("", fontTexture)
        addGuiItem(statusTextItem)
        debugTextItem = TextItem("", fontTexture)
        addGuiItem(debugTextItem)

        crosshair = GuiItemCrosshair(hudTextureMap)
        addGuiItem(crosshair)

        hotbar = GuiItemHotbar(world.player.inventory, hudTextureMap)
        addGuiItem(hotbar)
    }

    fun setStatusText(statusText: String) {
        statusTextItem.text = statusText
    }

    fun setDebugText(debugText: String) {
        debugTextItem.text = debugText
    }

    override fun resize(window: Window) {
        super.resize(window)

        statusTextItem.setPosition(10, window.height - 50)
        debugTextItem.setPosition(10, 10)

        crosshair.setPosition(window.width / 2, window.height / 2)
        hotbar.setPosition(window.width / 2, window.height - hotbar.height)
    }

    companion object {
        val FONT = Font("Arial", Font.PLAIN, 20)
        val CHARSET = Charsets.ISO_8859_1
    }

}