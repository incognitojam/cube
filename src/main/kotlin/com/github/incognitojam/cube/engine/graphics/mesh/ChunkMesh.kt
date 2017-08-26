package com.github.incognitojam.cube.engine.graphics.mesh

class ChunkMesh(positions: FloatArray, textureCoordinates: FloatArray, ambientLight: FloatArray, private val vegetationColours: FloatArray,
                 indices: IntArray) : BlockMesh(positions, textureCoordinates, ambientLight, indices) {

    override fun createVbos() {
        super.createVbos()
        createFloatVbo(3, 1, vegetationColours)
    }

}