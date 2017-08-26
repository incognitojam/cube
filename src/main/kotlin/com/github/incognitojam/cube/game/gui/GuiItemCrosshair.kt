package com.github.incognitojam.cube.game.gui

import com.github.incognitojam.cube.engine.graphics.TextureMap
import com.github.incognitojam.cube.engine.graphics.mesh.BlockMeshBuilder
import com.github.incognitojam.cube.engine.graphics.mesh.TexturedMesh

class GuiItemCrosshair(private val textureMap: TextureMap) : GuiItem() {

    override var width = 32
    override var height = width

    override fun initialise() {
        val positions = floatArrayOf(
                -0.5f, -0.5f, Z_POS,
                -0.5f, 0.5f, Z_POS,
                0.5f, 0.5f, Z_POS,
                0.5f, -0.5f, Z_POS
        ).map { it * height }.toFloatArray()

        val textureCoordinates = textureMap.getTextureCoordinates(0)

        mesh = TexturedMesh(positions, textureCoordinates, BlockMeshBuilder.INDICES_DELTA, textureMap)
        mesh?.initialise()
    }

}