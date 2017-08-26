package com.github.incognitojam.cube.engine.graphics.mesh

class ChunkMeshBuilder {

    private var vertices = ArrayList<Float>()
    private var textureCoordinates = ArrayList<Float>()
    private var ambientLights = ArrayList<Float>()
    private var vegetationColours = ArrayList<Float>()
    private var indices = ArrayList<Int>()

    private var vertexCount = 0
    private var indexCount = 0
    private var faceCount = 0

    fun addFace(x: Int, y: Int, z: Int, faceVertices: FloatArray, faceTextureCoordinates: FloatArray, ambientLight: Float, vegetationValue: Float) {
        // Add vertex coordinates (offset by position vector)
        val newVertices = faceVertices.mapIndexed { index, value ->
            when (index.rem(3)) {
                0 -> {
                    // x coordinate
                    value + x
                }
                1 -> {
                    // y coordinate
                    value + y
                }
                2 -> {
                    // z coordinate
                    value + z
                }

                else -> error("index.rem(3) !in 0..2")
            }
        }.toTypedArray()
        vertices.addAll(newVertices)

        // Add texture coordinates
        val newTextureCoordinates = faceTextureCoordinates.toTypedArray()
        textureCoordinates.addAll(newTextureCoordinates)

        // Add ambient light value
        ambientLights.add(ambientLight)
        ambientLights.add(ambientLight)
        ambientLights.add(ambientLight)
        ambientLights.add(ambientLight)

        // Add vegetation colour modifier
        vegetationColours.add(vegetationValue)
        vegetationColours.add(vegetationValue)
        vegetationColours.add(vegetationValue)
        vegetationColours.add(vegetationValue)

        // Add indices for triangle coordinates (offset by vertexCount so far)
        val newIndices = BlockMeshBuilder.INDICES_DELTA.map { it + vertexCount }.toTypedArray()
        indices.addAll(newIndices)

        // Update counts
        vertexCount += 4
        indexCount += 6
        faceCount++
    }

    fun build() = ChunkMesh(vertices.toFloatArray(), textureCoordinates.toFloatArray(), ambientLights.toFloatArray(),
            vegetationColours.toFloatArray(), indices.toIntArray()).apply { initialise() }

    override fun toString() = "ChunkMeshBuilder(vertexCount=$vertexCount, indexCount=$indexCount, faceCount=$faceCount)"

}