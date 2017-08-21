package com.github.incognitojam.cube.engine.graphics

import com.github.incognitojam.cube.engine.maths.clone
import com.github.incognitojam.cube.game.entity.Entity
import com.github.incognitojam.cube.game.gui.GuiItem
import com.github.incognitojam.cube.game.world.chunk.Chunk
import org.joml.Matrix4f
import org.joml.Vector3f

object Transformation {

    private val perspectiveMatrix = Matrix4f()
    private val orthographicMatrix = Matrix4f()
    private val modelMatrix = Matrix4f()
    private val modelViewMatrix = Matrix4f()

    fun getPerspectiveProjectionMatrix(fov: Float, aspectRatio: Float, zNear: Float, zFar: Float): Matrix4f {
        perspectiveMatrix.identity()

        val yScale = aspectRatio / Math.tan(Math.toRadians(fov / 2.0)).toFloat()
        val xScale = yScale / aspectRatio
        val frustumLength = zFar - zNear

        return perspectiveMatrix.m00(xScale)
                .m11(yScale)
                .m22(-(zFar + zNear) / frustumLength)
                .m23(-1.0F)
                .m32(-(2 * zNear * zFar) / frustumLength)
                .m33(0F)
    }

    fun getOrthographicProjectionMatrix(left: Int, right: Int, bottom: Int, top: Int): Matrix4f {
        orthographicMatrix.identity()
        orthographicMatrix.setOrtho2D(left.toFloat(), right.toFloat(), bottom.toFloat(), top.toFloat())
        return orthographicMatrix
    }

    fun getOrthographicProjectionModelMatrix(position: Vector3f, orthographicMatrix: Matrix4f): Matrix4f {
        return orthographicMatrix.clone().mul(Matrix4f().translation(position))
    }

    fun getOrthographicProjectionModelMatrix(guiItem: GuiItem, orthographicMatrix: Matrix4f): Matrix4f {
        return getOrthographicProjectionModelMatrix(guiItem.renderPosition, orthographicMatrix)
    }

    fun getViewMatrix(camera: Camera, viewMatrix: Matrix4f): Matrix4f {
        viewMatrix.identity()
                .rotate(camera.rotationRadians.x, Vector3f(1f, 0f, 0f))
                .rotate(camera.rotationRadians.y, Vector3f(0f, 1f, 0f))
                .rotate(camera.rotationRadians.z, Vector3f(0f, 0f, 1f))
                .translate(-camera.position.x, -camera.position.y, -camera.position.z)
        return viewMatrix
    }

    fun buildModelMatrix(position: Vector3f, rotation: Vector3f, rotationOffset: Vector3f): Matrix4f {
        return modelMatrix.translation(position)
                .rotate(rotation.x, Vector3f(1f, 0f, 0f))
                .rotate(rotation.y, Vector3f(0f, 1f, 0f))
                .rotate(rotation.z, Vector3f(0f, 0f, 1f))
                .translate(rotationOffset.negate())
    }

    fun buildChunkModelMatrix(chunk: Chunk): Matrix4f {
        return buildModelMatrix(chunk.worldPos, Vector3f(), Vector3f())
    }

    fun buildEntityModelMatrix(entity: Entity): Matrix4f {
        return buildModelMatrix(entity.position, entity.rotationRadians, Vector3f(entity.width / 2f, 0f, entity.width / 2f))
    }

    fun buildModelViewMatrix(modelMatrix: Matrix4f, viewMatrix: Matrix4f): Matrix4f {
        return viewMatrix.mulAffine(modelMatrix, modelViewMatrix)
    }

}