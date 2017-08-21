package com.github.incognitojam.cube.engine.file

import de.matthiasmann.twl.utils.PNGDecoder
import de.matthiasmann.twl.utils.PNGDecoder.Format
import java.io.InputStream
import java.nio.ByteBuffer

object FileUtils {

    @Throws(Exception::class)
    fun loadInputStream(filename: String): InputStream
        = this::class.java.classLoader.getResourceAsStream(filename)

    @Throws(Exception::class)
    fun loadTextResource(filename: String): String {
        return this::class.java.classLoader.getResource(filename).readText(Charsets.UTF_8)
    }

    @Throws(Exception::class)
    fun loadImageResource(inputStream: InputStream): Triple<Int, Int, ByteBuffer> {
        val decoder = PNGDecoder(inputStream)
        val buffer = ByteBuffer.allocateDirect(4 * decoder.width * decoder.height)
        decoder.decode(buffer, decoder.width * 4, Format.RGBA)
        buffer.flip()

        return Triple(decoder.width, decoder.height, buffer)
    }

}