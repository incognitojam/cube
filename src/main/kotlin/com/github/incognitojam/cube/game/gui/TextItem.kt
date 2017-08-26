package com.github.incognitojam.cube.game.gui

import com.github.incognitojam.cube.engine.graphics.FontTexture
import com.github.incognitojam.cube.engine.graphics.mesh.TexturedMesh

class TextItem(_text: String, private val fontTexture: FontTexture): GuiItem() {

    override var width = 0
    override var height = 0

    var text: String = _text
        set(value) {
            field = value
            mesh?.delete()
            mesh = buildMesh()
            mesh?.initialise()
        }

    private fun buildMesh(): TexturedMesh {
        val positions = ArrayList<Float>()
        val textureCoordinates = ArrayList<Float>()
        val indices = ArrayList<Int>()

        val characters = text.toCharArray()

        val widths = ArrayList<Int>()
        var tempWidth = 0

        var startX = 0f
        var startY = 0f
        var index = 0
        for (char in characters) {
            if (char == '\n') {
                startY += fontTexture.height
                startX = 0f
                widths += tempWidth
                tempWidth = 0
                continue
            }
            val charInfo = fontTexture.getCharInfo(char) ?: continue

            // Build a character tile composed by two triangles

            // Left Top vertex
            positions.add(startX) // x
            positions.add(startY + 0.0f) // y
            positions.add(Z_POS) // z
            textureCoordinates.add(charInfo.startX.toFloat() / fontTexture.width.toFloat())
            textureCoordinates.add(0.0f)
            indices.add(index * VERTICES_PER_QUAD)

            // Left Bottom vertex
            positions.add(startX) // x
            positions.add(startY + fontTexture.height) //y
            positions.add(Z_POS) // z
            textureCoordinates.add(charInfo.startX.toFloat() / fontTexture.width.toFloat())
            textureCoordinates.add(1.0f)
            indices.add(index * VERTICES_PER_QUAD + 1)

            // Right Bottom vertex
            positions.add(startX + charInfo.width) // x
            positions.add(startY + fontTexture.height) // y
            positions.add(Z_POS) // z
            textureCoordinates.add((charInfo.startX + charInfo.width).toFloat() / fontTexture.width.toFloat())
            textureCoordinates.add(1.0f)
            indices.add(index * VERTICES_PER_QUAD + 2)

            // Right Top vertex
            positions.add(startX + charInfo.width) // x
            positions.add(startY + 0.0f) // y
            positions.add(Z_POS) // z
            textureCoordinates.add((charInfo.startX + charInfo.width).toFloat() / fontTexture.width.toFloat())
            textureCoordinates.add(0.0f)
            indices.add(index * VERTICES_PER_QUAD + 3)

            // Add indices for left top and bottom right vertices
            indices.add(index * VERTICES_PER_QUAD)
            indices.add(index * VERTICES_PER_QUAD + 2)

            startX += charInfo.width.toFloat()
            tempWidth += charInfo.width
            index++
        }

        widths += tempWidth
        width = widths.max() ?: 0
        height = widths.size * fontTexture.height

        val positionsArray = positions.toFloatArray()
        val textureCoordinatesArray = textureCoordinates.toFloatArray()
        val indicesArray = indices.toIntArray()

        return TexturedMesh(positionsArray, textureCoordinatesArray, indicesArray, fontTexture.texture)
    }

    companion object {
        const val VERTICES_PER_QUAD: Int = 4
    }

}