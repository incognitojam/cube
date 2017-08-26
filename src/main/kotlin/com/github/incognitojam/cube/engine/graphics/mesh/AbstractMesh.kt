package com.github.incognitojam.cube.engine.graphics.mesh

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.*
import org.lwjgl.system.MemoryUtil

abstract class AbstractMesh(protected val positions: FloatArray, protected val indices: IntArray) {

    private var vaoId: Int = -1
    private val vboIdList = ArrayList<Int>()
    private var vertexCount: Int = indices.size

    private var initialised = false
    open val hasTexture = false

    fun initialise() {
        createVao()
        createVbos()
        createElementBuffer(indices)
        unbindVao()

        initialised = true
    }

    private fun createVao() {
        vaoId = glGenVertexArrays()
        glBindVertexArray(vaoId)
    }

    open protected fun createVbos() {
        createFloatVbo(0, 3, positions)
    }

    protected fun createFloatVbo(index: Int, size: Int, floats: FloatArray, bufferType: Int = GL_ARRAY_BUFFER) {
        createBuffer(floats, bufferType)
        glVertexAttribPointer(index, size, GL_FLOAT, false, 0, 0)
    }

    private fun createElementBuffer(indices: IntArray) {
        createBuffer(indices, GL_ELEMENT_ARRAY_BUFFER)
    }

    private fun createBuffer(floats: FloatArray, bufferType: Int) {
        val vboId = glGenBuffers()
        vboIdList += vboId
        val floatBuffer = MemoryUtil.memAllocFloat(floats.size)
        floatBuffer.put(floats).flip()
        glBindBuffer(bufferType, vboId)
        glBufferData(bufferType, floatBuffer, GL_STATIC_DRAW)
        MemoryUtil.memFree(floatBuffer)
    }

    private fun createBuffer(indices: IntArray, bufferType: Int) {
        val intBuffer = MemoryUtil.memAllocInt(indices.size)
        intBuffer.put(indices).flip()
        val vboId = glGenBuffers()
        vboIdList += vboId
        glBindBuffer(bufferType, vboId)
        glBufferData(bufferType, intBuffer, GL_STATIC_DRAW)
        MemoryUtil.memFree(intBuffer)
    }

    fun render(mode: Int = GL_TRIANGLES) {
        if (!initialised) error("Mesh was not initialised!")

        bindDependencies()
        bindBuffers()
        glDrawElements(mode, vertexCount, GL_UNSIGNED_INT, 0)
        unbindBuffers()
        unbindDependencies()
    }

    open protected fun bindDependencies() = Unit

    open protected fun unbindDependencies() = Unit

    private fun iterateVbos(callback: (Int, Int) -> Unit) {
        (0 until vboIdList.size).forEach { callback.invoke(it, vboIdList[it]) }
    }

    private fun unbindVao() {
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindVertexArray(0)
    }

    private fun bindBuffers() {
        glBindVertexArray(vaoId)
        iterateVbos { index, _ -> glEnableVertexAttribArray(index) }
    }

    private fun unbindBuffers() {
        iterateVbos { index, _ -> glDisableVertexAttribArray(index) }
        unbindVao()
    }

    fun delete(deleteDependencies: Boolean = false) {
        unbindBuffers()
        deleteBuffers()
        if (deleteDependencies) {
            unbindDependencies()
            deleteDependencies()
        }
    }

    private fun deleteBuffers() {
        unbindBuffers()
        iterateVbos { _, vboId -> glDeleteBuffers(vboId) }
        glDeleteVertexArrays(vaoId)
    }

    open protected fun deleteDependencies() = Unit

}