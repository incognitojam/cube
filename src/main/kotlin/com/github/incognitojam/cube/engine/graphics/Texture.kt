package com.github.incognitojam.cube.engine.graphics

import com.github.incognitojam.cube.engine.file.FileUtils
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL30.glGenerateMipmap
import java.io.InputStream

open class Texture private constructor(val width: Int, val height: Int, protected val id: Int) {

    private constructor(parameters: Triple<Int, Int, Int>) : this(parameters.first, parameters.second, parameters.third)

    constructor(filename: String) : this(loadTexture(filename))

    constructor(inputStream: InputStream) : this(loadTexture(inputStream))

    fun bind() {
        glBindTexture(GL_TEXTURE_2D, id)
    }

    fun delete() {
        glDeleteTextures(id)
    }

    override fun toString(): String {
        return "Texture(width=$width, height=$height, id=$id)"
    }

    companion object {
        @Throws(Exception::class)
        private fun loadTexture(inputStream: InputStream): Triple<Int, Int, Int> {
            val (width, height, buffer) = FileUtils.loadImageResource(inputStream)

            // Create a new OpenGL texture
            val textureId = glGenTextures()
            // Bind the texture
            glBindTexture(GL_TEXTURE_2D, textureId)

            // Tell OpenGL how to unpack the RGBA bytes. Each component is 1 byte size
            glPixelStorei(GL_UNPACK_ALIGNMENT, 1)

            // Upload the texture data
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer)

            // Generate Mip Map
            glGenerateMipmap(GL_TEXTURE_2D)

            // Prevent blurriness with textures
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)

            return Triple(width, height, textureId)
        }

        @Throws(Exception::class)
        private fun loadTexture(filename: String): Triple<Int, Int, Int>
            = loadTexture(FileUtils.loadInputStream(filename))
    }

}