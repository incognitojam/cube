package com.github.incognitojam.cube.game.world

import com.github.incognitojam.cube.engine.Window
import com.github.incognitojam.cube.engine.file.FileUtils
import com.github.incognitojam.cube.engine.graphics.Camera
import com.github.incognitojam.cube.engine.graphics.ShaderProgram
import com.github.incognitojam.cube.engine.graphics.Transformation
import com.github.incognitojam.cube.engine.graphics.mesh.ColouredMesh
import com.github.incognitojam.cube.game.world.chunk.Chunk
import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.opengl.GL11.*

class WorldRenderer {

    private val blockShader = ShaderProgram()
    private val waterShader = ShaderProgram()
    private val basicShader = ShaderProgram()
    private val entityShader = ShaderProgram()

    private lateinit var errorMesh: ColouredMesh
    private lateinit var targetBlockMesh: ColouredMesh

    var debug = false

    @Throws(Exception::class)
    fun initialise() {
        val positions = floatArrayOf(
                0f, 0f, 1f,
                1f, 0f, 1f,
                1f, 1f, 1f,
                0f, 1f, 1f,
                0f, 0f, 0f,
                1f, 0f, 0f,
                1f, 1f, 0f,
                0f, 1f, 0f
        )

        val errorColours = floatArrayOf(
                1f, .5f, .5f,
                1f, .5f, .5f,
                1f, .5f, .5f,
                1f, .5f, .5f,
                1f, .5f, .5f,
                1f, .5f, .5f,
                1f, .5f, .5f,
                1f, .5f, .5f
        )

        val targetColours = floatArrayOf(
                1f, 1f, 1f,
                1f, 1f, 1f,
                1f, 1f, 1f,
                1f, 1f, 1f,
                1f, 1f, 1f,
                1f, 1f, 1f,
                1f, 1f, 1f,
                1f, 1f, 1f
        )

        val indices = intArrayOf(
                0, 1, 2,
                2, 3, 0,
                1, 5, 6,
                6, 2, 1,
                7, 6, 5,
                5, 4, 7,
                4, 0, 3,
                3, 7, 4,
                4, 5, 1,
                1, 0, 4,
                3, 2, 6,
                6, 7, 3
        )

        errorMesh = ColouredMesh(positions, errorColours, indices)
        errorMesh.initialise()
        targetBlockMesh = ColouredMesh(positions, targetColours, indices)
        targetBlockMesh.initialise()

        blockShader.initialise()
        blockShader.createVertexShader(FileUtils.loadTextResource("shaders/world.vertex.glsl"))
        blockShader.createFragmentShader(FileUtils.loadTextResource("shaders/world.fragment.glsl"))
        blockShader.link()

        blockShader.createUniform("projectionMatrix")
        blockShader.createUniform("modelViewMatrix")
        blockShader.createUniform("textureSampler")


        waterShader.initialise()
        waterShader.createVertexShader(FileUtils.loadTextResource("shaders/water.vertex.glsl"))
        waterShader.createFragmentShader(FileUtils.loadTextResource("shaders/water.fragment.glsl"))
        waterShader.link()

        waterShader.createUniform("projectionMatrix")
        waterShader.createUniform("modelViewMatrix")
        waterShader.createUniform("textureSampler")


        basicShader.initialise()
        basicShader.createVertexShader(FileUtils.loadTextResource("shaders/basic.vertex.glsl"))
        basicShader.createFragmentShader(FileUtils.loadTextResource("shaders/basic.fragment.glsl"))
        basicShader.link()

        basicShader.createUniform("projectionMatrix")
        basicShader.createUniform("modelViewMatrix")


        entityShader.initialise()
        entityShader.createVertexShader(FileUtils.loadTextResource("shaders/entity.vertex.glsl"))
        entityShader.createFragmentShader(FileUtils.loadTextResource("shaders/entity.fragment.glsl"))
        entityShader.link()

        entityShader.createUniform("projectionMatrix")
        entityShader.createUniform("modelViewMatrix")
        entityShader.createUniform("textureSampler")
    }

    fun render(window: Window, camera: Camera, world: World) {
        blockShader.bind()

        val projectionMatrix = Transformation.getPerspectiveProjectionMatrix(FOV, window.aspectRatio, Z_NEAR, Z_FAR)
        blockShader.setUniform("projectionMatrix", projectionMatrix)
        blockShader.setUniform("textureSampler", 0)

        val viewMatrix = camera.getViewMatrix()
        val chunks = ArrayList<Chunk>()
        val playerChunk = world.player.location.chunkPosition
        world.forChunksInRadius(playerChunk, 4) { _, chunk ->
            if (chunk != null && !chunk.empty) chunks.add(chunk)
        }

        for (chunk in chunks) {
            val blockMesh = chunk.blockMesh ?: continue
            val modelMatrix = Transformation.buildChunkModelMatrix(chunk)
            val modelViewMatrix = Transformation.buildModelViewMatrix(modelMatrix, viewMatrix)
            blockShader.setUniform("modelViewMatrix", modelViewMatrix)
            blockMesh.render()
        }

        blockShader.unbind()

        waterShader.bind()
        waterShader.setUniform("projectionMatrix", projectionMatrix)
        waterShader.setUniform("textureSampler", 0)
        for (chunk in chunks) {
            val waterMesh = chunk.waterMesh ?: continue
            val modelMatrix = Transformation.buildChunkModelMatrix(chunk)
            val modelViewMatrix = Transformation.buildModelViewMatrix(modelMatrix, viewMatrix)
            waterShader.setUniform("modelViewMatrix", modelViewMatrix)
            waterMesh.render()
        }
        waterShader.unbind()


        entityShader.bind()

        entityShader.setUniform("projectionMatrix", projectionMatrix)

        var modelMatrix: Matrix4f
        var modelViewMatrix: Matrix4f
        val entities = world.getEntities()
        for (entity in entities) {
            modelMatrix = Transformation.buildEntityModelMatrix(entity)
            modelViewMatrix = Transformation.buildModelViewMatrix(modelMatrix, viewMatrix)
            entityShader.setUniform("modelViewMatrix", modelViewMatrix)
            entity.render(entityShader, modelViewMatrix)
        }

        entityShader.unbind()


        basicShader.bind()

        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
        basicShader.setUniform("projectionMatrix", projectionMatrix)

        val targetPos = world.player.targetBlock

        modelMatrix = Transformation.buildModelMatrix(Vector3f(targetPos), Vector3f(), Vector3f())
        modelViewMatrix = Transformation.buildModelViewMatrix(modelMatrix, viewMatrix)
        basicShader.setUniform("modelViewMatrix", modelViewMatrix)
        targetBlockMesh.render(GL_LINE_LOOP)

        if (debug) {
            for (entity in entities) {
                val collider = entity.collider
                val entityColliderMesh = collider.mesh

                modelMatrix = Transformation.buildModelMatrix(Vector3f(collider.collidingBlockPos), Vector3f(), Vector3f())
                modelViewMatrix = Transformation.buildModelViewMatrix(modelMatrix, viewMatrix)
                basicShader.setUniform("modelViewMatrix", modelViewMatrix)
                errorMesh.render(GL_LINE_LOOP)

                modelMatrix = Transformation.buildModelMatrix(entity.position, Vector3f(), Vector3f())
                modelViewMatrix = Transformation.buildModelViewMatrix(modelMatrix, viewMatrix)
                basicShader.setUniform("modelViewMatrix", modelViewMatrix)
                entityColliderMesh.render(GL_LINE_LOOP)
            }
        }

        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL)

        basicShader.unbind()
    }

    fun delete() = blockShader.delete()

    companion object {
        val FOV = 100.0f
        val Z_NEAR = 0.1f
        val Z_FAR = 1000f
    }

}