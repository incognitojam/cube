package com.github.incognitojam.cube.game.gui

import com.github.incognitojam.cube.engine.Window
import com.github.incognitojam.cube.engine.graphics.Mesh
import com.github.incognitojam.cube.engine.graphics.ShaderProgram
import com.github.incognitojam.cube.engine.graphics.Transformation
import com.github.incognitojam.cube.engine.maths.MathsUtils
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f

abstract class GuiItem {

    abstract var width: Float
    abstract var height: Float

    val position = Vector2f()
    val renderPosition: Vector3f
        get() = Vector3f(position.x, position.y, Z_POS)

    var mesh: Mesh? = null

    open fun onInitialise() {

    }

    open fun onUpdate() {

    }

    open fun onRender(shader: ShaderProgram, projectionMatrix: Matrix4f) {
        mesh?.let { mesh ->
            // Set orthographic and model matrix for this HUD item
            val projectionModelMatrix = Transformation.getOrthographicProjectionModelMatrix(this, projectionMatrix)
            shader.setUniform("projectionModelMatrix", projectionModelMatrix)
            shader.setUniform("colour", Vector4f(1f, 1f, 1f, 1f))
            val hasTexture = if (mesh.texture != null) 1 else 0
            shader.setUniform("hasTexture", hasTexture)

            // Render the mesh for this HUD item
            mesh.onRender()
        }
    }

    open fun onResize(window: Window) {

    }

    open fun onCleanup() {
        mesh?.onCleanup()
    }

    fun setPosition(x: Float, y: Float) {
        position.set(x, y)
    }

    fun setPosition(position: Vector2f) {
        this.position.set(position)
    }

    override fun toString(): String {
        return "GuiItem(position=${MathsUtils.format(position, 3)}, mesh=$mesh)"
    }

    companion object {
        const val Z_POS: Float = 0.0f
    }

}
