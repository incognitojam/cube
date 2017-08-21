package com.github.incognitojam.cube.engine.graphics

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.*
import org.lwjgl.system.MemoryUtil
import java.nio.FloatBuffer
import java.nio.IntBuffer

class BasicMesh(positions: FloatArray, colours: FloatArray, indices: IntArray) {

    private val vaoId: Int
    private val vboIdList = ArrayList<Int>()

    private val vertexCount: Int

    init {
        var positionsBuffer: FloatBuffer? = null
        var coloursBuffer: FloatBuffer? = null
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

            // Positions VBO
            vboId = glGenBuffers()
            vboIdList += vboId
            coloursBuffer = MemoryUtil.memAllocFloat(colours.size)
            coloursBuffer.put(colours).flip()
            glBindBuffer(GL_ARRAY_BUFFER, vboId)
            glBufferData(GL_ARRAY_BUFFER, coloursBuffer, GL_STATIC_DRAW)
            glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0)

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
            coloursBuffer?.let { MemoryUtil.memFree(it) }
            indicesBuffer?.let { MemoryUtil.memFree(it) }
        }
    }

    fun onRender(drawMode: Int = GL_TRIANGLES) {
        // Bind all vertex arrays
        glBindVertexArray(vaoId)
        glEnableVertexAttribArray(0)
        glEnableVertexAttribArray(1)

        // Draw the mesh
        glDrawElements(drawMode, vertexCount, GL_UNSIGNED_INT, 0)

        // Restore state
        glDisableVertexAttribArray(0)
        glDisableVertexAttribArray(1)
        glBindVertexArray(0)
    }

    fun onCleanup() {
        glDisableVertexAttribArray(0)

        // Delete the VBOs
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        for (vboId in vboIdList) {
            glDeleteBuffers(vboId)
        }

        // Delete the VAO
        glBindVertexArray(0)
        glDeleteVertexArrays(vaoId)
    }

}