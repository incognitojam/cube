package com.github.incognitojam.cube.engine.graphics

class TextureMap private constructor(val filename: String, private val size: Int, width: Int, height: Int, id: Int) :
        Texture(width, height, id) {

    private val unit = 1F / size

    fun getTextureCoordinates(index: Int) = getTextureCoordinates(index % size, index / size)

    fun getTextureCoordinates(x: Int, y: Int): FloatArray {
        val textureX = x * unit
        val textureY = y * unit

        return floatArrayOf(
                textureX, textureY + unit, // Bottom left
                textureX + unit, textureY + unit, // Bottom right
                textureX + unit, textureY, // Top right
                textureX, textureY // Top left
        )
    }

    override fun toString(): String {
        return "TextureMap(filename='$filename', size=$size, unit=$unit)"
    }

    companion object {
        fun loadTextureMap(filename: String, size: Int): TextureMap {
            val texture = Texture.loadTexture(filename)
            return TextureMap(filename, size, texture.width, texture.height, texture.id)
        }
    }

}