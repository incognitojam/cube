package com.github.incognitojam.cube.engine.graphics.mesh

import com.github.incognitojam.cube.engine.graphics.Texture

open class TexturedMesh(positions: FloatArray, protected val textureCoordinates: FloatArray, indices: IntArray,
                        protected val texture: Texture) : AbstractMesh(positions, indices) {

    override val hasTexture = true

    override fun createVbos() {
        super.createVbos()
        createFloatVbo(1, 2, textureCoordinates)
    }

    override fun bindDependencies() = texture.bind()

    override fun deleteDependencies() = texture.delete()

}