package com.github.incognitojam.cube.engine.graphics.mesh

import com.github.incognitojam.cube.game.block.Block
import com.github.incognitojam.cube.game.world.Direction
import com.github.incognitojam.cube.game.world.Direction.*

class BlockMeshBuilder {

    private var vertices = ArrayList<Float>()
    private var textureCoordinates = ArrayList<Float>()
    private var ambientLights = ArrayList<Float>()
    private var indices = ArrayList<Int>()

    private var vertexCount = 0
    private var indexCount = 0
    private var faceCount = 0

    fun addBlock(block: Block, scale: Float, itemDrop: Boolean) {
        for (direction in Direction.values()) addFace(block, direction, scale, itemDrop)
    }

    fun addFace(block: Block, direction: Direction, scale: Float = 1f, itemDrop: Boolean = false) {
        val faceVertices: FloatArray
        val ambientLight: Float

        when (direction) {
            NORTH -> {
                faceVertices = BACK_VERTICES
                ambientLight = LIGHT_Z
            }
            EAST -> {
                faceVertices = RIGHT_VERTICES
                ambientLight = LIGHT_X
            }
            SOUTH -> {
                faceVertices = FRONT_VERTICES
                ambientLight = LIGHT_Z
            }
            WEST -> {
                faceVertices = LEFT_VERTICES
                ambientLight = LIGHT_X
            }
            UP -> {
                faceVertices = TOP_VERTICES
                ambientLight = LIGHT_TOP
            }
            else -> {
                faceVertices = BOTTOM_VERTICES
                ambientLight = LIGHT_BOTTOM
            }
        }
        addFace(faceVertices, block.getTextureCoordinates(direction, itemDrop), ambientLight, scale)
    }

    fun addFace(faceVertices: FloatArray, faceTextureCoordinates: FloatArray, ambientLight: Float, scale: Float) {
        val newVertices = faceVertices.map { it * scale }.toTypedArray()
        vertices.addAll(newVertices)

        // Add texture coordinates
        val newTextureCoordinates = faceTextureCoordinates.toTypedArray()
        textureCoordinates.addAll(newTextureCoordinates)

        // Add ambient light value
        ambientLights.add(ambientLight)
        ambientLights.add(ambientLight)
        ambientLights.add(ambientLight)
        ambientLights.add(ambientLight)

        // Add indices for triangle coordinates (offset by vertexCount so far)
        val newIndices = INDICES_DELTA.map { it + vertexCount }.toTypedArray()
        indices.addAll(newIndices)

        // Update counts
        vertexCount += 4
        indexCount += 6
        faceCount++
    }

    fun build() = BlockMesh(vertices.toFloatArray(), textureCoordinates.toFloatArray(), ambientLights.toFloatArray(), indices.toIntArray())
            .apply { initialise() }

    override fun toString() = "BlockMeshBuilder(vertexCount=$vertexCount, indexCount=$indexCount, faceCount=$faceCount)"

    companion object {
        // Z+ is towards the user and Y+ is towards the top of the world.
        // X- is left and X+ is right.
        // See https://en.wikibooks.org/wiki/OpenGL_Programming/Modern_OpenGL_Tutorial_05

        // Z-
        val BACK_VERTICES = floatArrayOf(
                1f, 0f, 0f,
                0f, 0f, 0f,
                0f, 1f, 0f,
                1f, 1f, 0f
        )

        // Z+
        val FRONT_VERTICES = floatArrayOf(
                0f, 0f, 1f,
                1f, 0f, 1f,
                1f, 1f, 1f,
                0f, 1f, 1f
        )

        // Y-
        val BOTTOM_VERTICES = floatArrayOf(
                0f, 0f, 0f,
                1f, 0f, 0f,
                1f, 0f, 1f,
                0f, 0f, 1f
        )

        // Y+
        val TOP_VERTICES = floatArrayOf(
                0f, 1f, 1f,
                1f, 1f, 1f,
                1f, 1f, 0f,
                0f, 1f, 0f
        )

        // Y+
        val WATER_TOP_VERTICES = floatArrayOf(
                0f, 15f / 16f, 1f,
                1f, 15f / 16f, 1f,
                1f, 15f / 16f, 0f,
                0f, 15f / 16f, 0f
        )

        // X-
        val LEFT_VERTICES = floatArrayOf(
                0f, 0f, 0f,
                0f, 0f, 1f,
                0f, 1f, 1f,
                0f, 1f, 0f
        )

        // X+
        val RIGHT_VERTICES = floatArrayOf(
                1f, 0f, 1f,
                1f, 0f, 0f,
                1f, 1f, 0f,
                1f, 1f, 1f
        )

        fun getVertices(direction: Direction): FloatArray {
            return when(direction) {
                NORTH -> BACK_VERTICES
                EAST -> RIGHT_VERTICES
                SOUTH -> FRONT_VERTICES
                WEST -> LEFT_VERTICES
                UP -> TOP_VERTICES
                DOWN -> BOTTOM_VERTICES
            }
        }


        // Indices delta
        val INDICES_DELTA = intArrayOf(
                0, 1, 2,
                2, 3, 0
        )


        // Lighting
        val LIGHT_TOP = 1.0f
        val LIGHT_X = 0.8f
        val LIGHT_Z = 0.6f
        val LIGHT_BOTTOM = 0.4f

        fun getAmbientLight(direction: Direction): Float {
            return when(direction) {
                NORTH, SOUTH -> LIGHT_Z
                EAST, WEST -> LIGHT_X
                UP -> LIGHT_TOP
                DOWN -> LIGHT_BOTTOM
            }
        }
    }

}