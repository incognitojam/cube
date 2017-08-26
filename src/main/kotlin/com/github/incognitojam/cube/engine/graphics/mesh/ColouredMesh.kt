package com.github.incognitojam.cube.engine.graphics.mesh

class ColouredMesh(positions: FloatArray, private val colours: FloatArray, indices: IntArray) : AbstractMesh(positions, indices) {

    override fun createVbos() {
        super.createVbos()
        createFloatVbo(1, 3, colours)
    }

}