package com.github.incognitojam.cube.engine.graphics.mesh

import com.github.incognitojam.cube.engine.graphics.Texture

open class ModelMesh(positions: FloatArray, textureCoordinates: FloatArray, protected val normals: FloatArray, indices: IntArray,
                     texture: Texture) : TexturedMesh(positions, textureCoordinates, indices, texture) {

    override fun createVbos() {
        super.createVbos()
        createFloatVbo(2, 3, normals)
    }

}