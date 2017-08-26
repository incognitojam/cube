package com.github.incognitojam.cube.engine.graphics.mesh

import com.github.incognitojam.cube.game.block.Blocks

open class BlockMesh(positions: FloatArray, textureCoordinates: FloatArray, protected val ambientLight: FloatArray, indices: IntArray) :
        TexturedMesh(positions, textureCoordinates, indices, Blocks.getTextureMap()) {

    override fun createVbos() {
        super.createVbos()
        createFloatVbo(2, 1, ambientLight)
    }

}