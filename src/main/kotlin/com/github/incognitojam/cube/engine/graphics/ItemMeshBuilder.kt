package com.github.incognitojam.cube.engine.graphics

class ItemMeshBuilder {

    private var vertices = ArrayList<Float>()
    private var textureCoordinates = ArrayList<Float>()
    private var ambientLights = ArrayList<Float>()
    private var indices = ArrayList<Int>()

    private var vertexCount = 0
    private var indexCount = 0
    private var faceCount = 0

//    fun getItemMesh(item: Item, scale: Float): ItemMesh {
//        item.getTextureCoordinates()?.let { faceTextureCoordinates ->
//            val faceVertices =
//            val ambientLight = LIGHT_TOP
//            addFace(faceVertices, faceTextureCoordinates, ambientLight, scale)
//        }
//
//        return build()
//    }

    private fun addFace(faceVertices: FloatArray, faceTextureCoordinates: FloatArray, ambientLight: Float, scale: Float) {
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

    private fun build() = ItemMesh(vertices.toFloatArray(), textureCoordinates.toFloatArray(), ambientLights.toFloatArray(), indices.toIntArray())

    override fun toString() = "ItemMeshBuilder(vertexCount=$vertexCount, indexCount=$indexCount, faceCount=$faceCount)"

    companion object {
        // Indices delta
        val INDICES_DELTA = intArrayOf(
                0, 1, 2,
                2, 3, 0
        )

        // Lighting
        val LIGHT_TOP = 1.0f
    }

}