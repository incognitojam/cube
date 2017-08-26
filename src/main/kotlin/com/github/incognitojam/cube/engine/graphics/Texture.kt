package com.github.incognitojam.cube.engine.graphics

import com.github.incognitojam.cube.engine.file.FileUtils
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL30.glGenerateMipmap
import java.io.InputStream
import java.nio.ByteBuffer

open class Texture(var width: Int, var height: Int, val id: Int) {

    fun bind() {
        glBindTexture(GL_TEXTURE_2D, id)
    }

    fun unbind() {
        glBindTexture(GL_TEXTURE_2D, 0)
    }

    fun delete() {
        glDeleteTextures(id)
    }

    fun update(width: Int, height: Int, buffer: ByteBuffer) {
        uploadTexture(width, height, buffer)
        setupTexture()

        this.width = width
        this.height = height
    }

    override fun toString(): String {
        return "Texture(width=$width, height=$height, id=$id)"
    }

    companion object {
        @Throws(Exception::class)
        fun loadTexture(inputStream: InputStream): Texture {
            // Load the texture and metadata from the stream
            val (width, height, buffer) = FileUtils.loadImageResource(inputStream)

            // Create a new OpenGL texture
            val textureId = glGenTextures()

            // Bind the texture
            glBindTexture(GL_TEXTURE_2D, textureId)

            // Tell OpenGL how to unpack the RGBA bytes. Each component is 1 byte size
            glPixelStorei(GL_UNPACK_ALIGNMENT, 1)

            // Upload the texture data
            uploadTexture(width, height, buffer)

            // Set texture parameters
            setupTexture()

            return Texture(width, height, textureId)
        }

        @Throws(Exception::class)
        fun loadTexture(filename: String): Texture = loadTexture(FileUtils.loadInputStream(filename))

        private fun uploadTexture(width: Int, height: Int, buffer: ByteBuffer) {
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer)
        }

        private fun setupTexture() {
            glGenerateMipmap(GL_TEXTURE_2D)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
        }
    }

}