package com.github.incognitojam.cube.engine.graphics

class TextureMap(val filename: String, private val size: Int) : Texture(filename) {

    private val unit = 1F / size

    override fun toString(): String {
        return "TextureMap(filename='$filename', size=$size, unit=$unit)"
    }

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

}