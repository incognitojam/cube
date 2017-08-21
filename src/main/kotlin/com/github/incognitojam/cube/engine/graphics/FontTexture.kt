package com.github.incognitojam.cube.engine.graphics

import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.charset.Charset
import javax.imageio.ImageIO

class FontTexture(private val font: Font, private val charset: Charset) {

    private val charMap = HashMap<Char, CharInfo>()
    lateinit var texture: Texture
    var width: Int = 0
    var height: Int = 0

    init {
        buildTexture()
    }

    fun getCharInfo(c: Char) = charMap[c]

    private fun buildTexture() {
        // Get the font metrics for each character for the selected font by using image
        var image = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
        var graphics2D = image.createGraphics()
        graphics2D.font = font
        var fontMetrics = graphics2D.fontMetrics

        val allChars = getAllAvailableChars(charset)
        for (char in allChars.toCharArray()) {
            // Get the size for each character and update global image size
            val charInfo = CharInfo(width, fontMetrics.charWidth(char))
            charMap.put(char, charInfo)
            width += charInfo.width
            height = Math.max(height, fontMetrics.height)
        }
        graphics2D.dispose()

        // Create the image associated to the charset
        image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        graphics2D = image.createGraphics()
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics2D.font = font
        fontMetrics = graphics2D.fontMetrics
        graphics2D.color = Color.WHITE
        graphics2D.drawString(allChars, 0, fontMetrics.ascent)
        graphics2D.dispose()

        // Dump image to a byte buffer
        var inputStream: InputStream? = null
        ByteArrayOutputStream().use { out ->
            ImageIO.write(image, IMAGE_FORMAT, out)
            out.flush()
            inputStream = ByteArrayInputStream(out.toByteArray())
        }

        inputStream?.let { texture = Texture(it) }
    }

    companion object {
        const val IMAGE_FORMAT: String = "png"

        private fun getAllAvailableChars(charset: Charset): String {
            val charsetEncoder = charset.newEncoder()
            val result = StringBuilder()
            (0..Character.MAX_VALUE.toInt())
                    .map { it.toChar() }
                    .filter { charsetEncoder.canEncode(it) }
                    .forEach { result.append(it) }
            println("Found ${result.length} characters for charset ${charset.displayName()}")
            return result.toString()
        }
    }

    class CharInfo(val startX: Int, val width: Int)

}