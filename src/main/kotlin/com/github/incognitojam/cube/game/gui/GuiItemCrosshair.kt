package com.github.incognitojam.cube.game.gui

import com.github.incognitojam.cube.engine.graphics.ChunkMeshBuilder
import com.github.incognitojam.cube.engine.graphics.Mesh
import com.github.incognitojam.cube.engine.graphics.TextureMap

class GuiItemCrosshair(private val textureMap: TextureMap) : GuiItem() {

    override var width = 32F
    override var height = width

    override fun onInitialise() {
        val positions = floatArrayOf(
                -0.5f, -0.5f, Z_POS,
                -0.5f, 0.5f, Z_POS,
                0.5f, 0.5f, Z_POS,
                0.5f, -0.5f, Z_POS
        ).map { it * height }.toFloatArray()

        val textureCoordinates = textureMap.getTextureCoordinates(0)

        mesh = Mesh(positions, textureCoordinates, ChunkMeshBuilder.INDICES_DELTA, textureMap)
    }

}