package com.github.incognitojam.cube.engine.graphics

class BasicMesh(positions: FloatArray, private val colours: FloatArray, indices: IntArray) : AbstractMesh(positions, indices) {

    override fun createVbos() {
        createFloatVbo(1, 3, colours)
    }

}