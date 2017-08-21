package com.github.incognitojam.cube.engine.graphics

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import org.lwjgl.opengl.GL13.glActiveTexture
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.*
import org.lwjgl.system.MemoryUtil
import java.nio.FloatBuffer
import java.nio.IntBuffer

class Mesh(positions: FloatArray, textureCoordinates: FloatArray?, indices: IntArray, val texture: Texture?) {

    private val vaoId: Int
    private val vboIdList = ArrayList<Int>()

    private val vertexCount: Int

    init {
        var positionsBuffer: FloatBuffer? = null
        var textureCoordinatesBuffer: FloatBuffer? = null
        var indicesBuffer: IntBuffer? = null

        vertexCount = indices.size

        try {
            // Create VAO
            vaoId = glGenVertexArrays()
            glBindVertexArray(vaoId)

            // Positions VBO
            var vboId = glGenBuffers()
            vboIdList += vboId
            positionsBuffer = MemoryUtil.memAllocFloat(positions.size)
            positionsBuffer.put(positions).flip()
            glBindBuffer(GL_ARRAY_BUFFER, vboId)
            glBufferData(GL_ARRAY_BUFFER, positionsBuffer, GL_STATIC_DRAW)
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0)

            if (textureCoordinates != null) {
                // Texture coordinates VBO
                vboId = glGenBuffers()
                vboIdList += vboId
                textureCoordinatesBuffer = MemoryUtil.memAllocFloat(textureCoordinates.size)
                textureCoordinatesBuffer.put(textureCoordinates).flip()
                glBindBuffer(GL_ARRAY_BUFFER, vboId)
                glBufferData(GL_ARRAY_BUFFER, textureCoordinatesBuffer, GL_STATIC_DRAW)
                glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0)
            }

            // Index VBO
            vboId = glGenBuffers()
            vboIdList += vboId
            indicesBuffer = MemoryUtil.memAllocInt(indices.size)
            indicesBuffer.put(indices).flip()
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboId)
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW)

            glBindBuffer(GL_ARRAY_BUFFER, 0)
            glBindVertexArray(0)
        } finally {
            positionsBuffer?.let { MemoryUtil.memFree(it) }
            textureCoordinatesBuffer?.let { MemoryUtil.memFree(it) }
            indicesBuffer?.let { MemoryUtil.memFree(it) }
        }
    }

    fun onRender() {
        // Activate first texture bank
        glActiveTexture(GL_TEXTURE0)

        // Bind the texture
        texture?.bind()

        // Bind all vertex arrays
        glBindVertexArray(vaoId)
        for (vboIndex in 0 until vboIdList.size) {
            glEnableVertexAttribArray(vboIndex)
        }

        // Draw the mesh
        glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0)

        // Restore state
        for (vboIndex in 0 until vboIdList.size) {
            glDisableVertexAttribArray(vboIndex)
        }
        glBindVertexArray(0)
    }

    fun deleteBuffers() {
        for (vboIndex in 0 until vboIdList.size) {
            glDisableVertexAttribArray(vboIndex)
        }

        // Delete the VBOs
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        for (vboId in vboIdList) {
            glDeleteBuffers(vboId)
        }

        // Delete the VAO
        glBindVertexArray(0)
        glDeleteVertexArrays(vaoId)
    }

    fun onCleanup() {
        deleteBuffers()

        // Delete the texture
        texture?.onCleanup()
    }

    override fun toString(): String {
        return "Mesh(texture=$texture, vaoId=$vaoId, vboIdList=$vboIdList, vertexCount=$vertexCount)"
    }

}