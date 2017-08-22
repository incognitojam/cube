package com.github.incognitojam.cube.engine.graphics

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.*
import org.lwjgl.system.MemoryUtil

open class AbstractMesh(protected val positions: FloatArray, protected val indices: IntArray) {

    private var vaoId: Int = -1
    private val vboIdList = ArrayList<Int>()
    private var vertexCount: Int = -1

    fun initialise() {
        createVao()
        createVbos()
        createElementBuffer(indices)
        unbindVao()
    }

    protected fun createVao() {
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

    protected fun createElementBuffer(ints: IntArray) {
        createBuffer(ints, GL_ELEMENT_ARRAY_BUFFER)
    }

    protected fun createBuffer(floats: FloatArray, bufferType: Int) {
        val floatBuffer = MemoryUtil.memAllocFloat(floats.size)
        floatBuffer.put(floats).flip()
        val vboId = glGenBuffers()
        vboIdList += vboId
        glBindBuffer(bufferType, vboId)
        glBufferData(bufferType, floatBuffer, GL_STATIC_DRAW)
        MemoryUtil.memFree(floatBuffer)
    }

    protected fun createBuffer(ints: IntArray, bufferType: Int) {
        val intBuffer = MemoryUtil.memAllocInt(ints.size)
        intBuffer.put(ints).flip()
        val vboId = glGenBuffers()
        vboIdList += vboId
        glBindBuffer(bufferType, vboId)
        glBufferData(bufferType, intBuffer, GL_STATIC_DRAW)
        MemoryUtil.memFree(intBuffer)
    }

    fun render(mode: Int) {
        bindDependencies()
        bindBuffers()
        glDrawElements(mode, vertexCount, GL_UNSIGNED_INT, 0)
        unbindBuffers()
    }

    open protected fun bindDependencies() = Unit

    protected fun iterateVbos(callback: (Int, Int) -> Unit) {
        (0 until vboIdList.size).forEach { callback.invoke(it, vboIdList[it]) }
    }

    protected fun unbindVao() {
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindVertexArray(0)
    }

    protected fun bindBuffers() {
        glBindVertexArray(vaoId)
        iterateVbos { index, _ -> glEnableVertexAttribArray(index) }
    }

    protected fun unbindBuffers() {
        iterateVbos { index, _ -> glDisableVertexAttribArray(index) }
        unbindVao()
    }

    fun delete(deleteDependencies: Boolean) {
        unbindBuffers()
        deleteBuffers()
        if (deleteDependencies) {
            deleteDependencies()
        }
    }

    protected fun deleteBuffers() {
        unbindBuffers()
        iterateVbos { _, vboId -> glDeleteBuffers(vboId) }
        glDeleteVertexArrays(vaoId)
    }

    open protected fun deleteDependencies() = Unit

}