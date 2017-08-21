package com.github.incognitojam.cube.engine.graphics

import org.lwjgl.opengl.*
import org.lwjgl.system.MemoryUtil
import java.nio.FloatBuffer
import java.nio.IntBuffer

class BlockMesh(positions: FloatArray, textureCoordinates: FloatArray, ambientLight: FloatArray, indices: IntArray, private val texture: Texture) {

    private val vaoId: Int
    private val vboIdList = ArrayList<Int>()

    private val vertexCount: Int

    init {
        var positionsBuffer: FloatBuffer? = null
        var textureCoordinatesBuffer: FloatBuffer? = null
        var ambientLightBuffer: FloatBuffer? = null
        var indicesBuffer: IntBuffer? = null

        vertexCount = indices.size

        try {
            // Create VAO
            vaoId = GL30.glGenVertexArrays()
            GL30.glBindVertexArray(vaoId)

            // Positions VBO
            var vboId = GL15.glGenBuffers()
            vboIdList += vboId
            positionsBuffer = MemoryUtil.memAllocFloat(positions.size)
            positionsBuffer.put(positions).flip()
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId)
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, positionsBuffer, GL15.GL_STATIC_DRAW)
            GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0)

            // Texture coordinates VBO
            vboId = GL15.glGenBuffers()
            vboIdList += vboId
            textureCoordinatesBuffer = MemoryUtil.memAllocFloat(textureCoordinates.size)
            textureCoordinatesBuffer.put(textureCoordinates).flip()
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId)
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, textureCoordinatesBuffer, GL15.GL_STATIC_DRAW)
            GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 0, 0)

            // Ambient light VBO
            vboId = GL15.glGenBuffers()
            vboIdList += vboId
            ambientLightBuffer = MemoryUtil.memAllocFloat(ambientLight.size)
            ambientLightBuffer.put(ambientLight).flip()
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId)
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, ambientLightBuffer, GL15.GL_STATIC_DRAW)
            GL20.glVertexAttribPointer(2, 1, GL11.GL_FLOAT, false, 0, 0)

            // Index VBO
            vboId = GL15.glGenBuffers()
            vboIdList += vboId
            indicesBuffer = MemoryUtil.memAllocInt(indices.size)
            indicesBuffer.put(indices).flip()
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboId)
            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL15.GL_STATIC_DRAW)

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)
            GL30.glBindVertexArray(0)
        } finally {
            positionsBuffer?.let { MemoryUtil.memFree(it) }
            textureCoordinatesBuffer?.let { MemoryUtil.memFree(it) }
            ambientLightBuffer?.let { MemoryUtil.memFree(it) }
            indicesBuffer?.let { MemoryUtil.memFree(it) }
        }
    }

    fun onRender() {
        // Activate first texture bank
        GL13.glActiveTexture(GL13.GL_TEXTURE0)

        // Bind the texture
        texture.bind()

        // Bind all vertex arrays
        GL30.glBindVertexArray(vaoId)
        for (vboIndex in 0 until vboIdList.size) {
            GL20.glEnableVertexAttribArray(vboIndex)
        }

        // Draw the mesh
        GL11.glDrawElements(GL11.GL_TRIANGLES, vertexCount, GL11.GL_UNSIGNED_INT, 0)

        // Restore state
        for (vboIndex in 0 until vboIdList.size) {
            GL20.glDisableVertexAttribArray(vboIndex)
        }
        GL30.glBindVertexArray(0)
    }

    fun onCleanup() {
        for (vboIndex in 0 until vboIdList.size) {
            GL20.glDisableVertexAttribArray(vboIndex)
        }

        // Delete the VBOs
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)
        for (vboId in vboIdList) {
            GL15.glDeleteBuffers(vboId)
        }

        // Delete the VAO
        GL30.glBindVertexArray(0)
        GL30.glDeleteVertexArrays(vaoId)
    }

}