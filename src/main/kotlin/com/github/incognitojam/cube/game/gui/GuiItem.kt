package com.github.incognitojam.cube.game.gui

import com.github.incognitojam.cube.engine.Window
import com.github.incognitojam.cube.engine.graphics.Mesh
import com.github.incognitojam.cube.engine.graphics.ShaderProgram
import com.github.incognitojam.cube.engine.graphics.Transformation
import com.github.incognitojam.cube.engine.maths.MathsUtils
import org.joml.*

abstract class GuiItem {

    abstract var width: Float
    abstract var height: Float

    protected var x = 0f
    protected var y = 0f
    val position: Vector2fc
        get() = Vector2f(x, y)
    val renderPosition: Vector3fc
        get() = Vector3f(position.x(), position.y(), Z_POS)

    var mesh: Mesh? = null

    open fun initialise() = Unit

    open fun update() = Unit

    open fun render(shader: ShaderProgram, projectionMatrix: Matrix4f) {
        mesh?.let { mesh ->
            // Set orthographic and model matrix for this HUD item
            val projectionModelMatrix = Transformation.getOrthographicProjectionModelMatrix(this, projectionMatrix)
            shader.setUniform("projectionModelMatrix", projectionModelMatrix)
            shader.setUniform("colour", Vector4f(1f, 1f, 1f, 1f))
            val hasTexture = if (mesh.texture != null) 1 else 0
            shader.setUniform("hasTexture", hasTexture)

            // Render the mesh for this HUD item
            mesh.render()
        }
    }

    open fun resize(window: Window) = Unit

    open fun delete() {
        mesh?.delete()
    }

    fun setPosition(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    fun setPosition(position: Vector2fc) = setPosition(position.x(), position.y())

    override fun toString(): String {
        return "GuiItem(position=${MathsUtils.format(position, 3)}, mesh=$mesh)"
    }

    companion object {
        const val Z_POS: Float = 0.0f
    }

}
