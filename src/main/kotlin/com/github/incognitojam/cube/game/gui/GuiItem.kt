package com.github.incognitojam.cube.game.gui

import com.github.incognitojam.cube.engine.Window
import com.github.incognitojam.cube.engine.graphics.ShaderProgram
import com.github.incognitojam.cube.engine.graphics.Transformation
import com.github.incognitojam.cube.engine.graphics.mesh.AbstractMesh
import com.github.incognitojam.cube.engine.maths.MathsUtils
import org.joml.*

abstract class GuiItem {

    abstract var width: Int
    abstract var height: Int

    protected var x = 0
    protected var y = 0
    val position: Vector2ic
        get() = Vector2i(x, y)
    val renderPosition: Vector3fc
        get() = Vector3f(x.toFloat(), y.toFloat(), Z_POS)

    var mesh: AbstractMesh? = null

    open fun initialise() = Unit

    open fun update() = Unit

    open fun render(shader: ShaderProgram, projectionMatrix: Matrix4f) {
        mesh?.let { mesh ->
            // Set orthographic and model matrix for this HUD item
            val projectionModelMatrix = Transformation.getOrthographicProjectionModelMatrix(this, projectionMatrix)
            shader.setUniform("projectionModelMatrix", projectionModelMatrix)
            shader.setUniform("colour", Vector4f(1f, 1f, 1f, 1f))
            val hasTexture = if (mesh.hasTexture) 1 else 0
            shader.setUniform("hasTexture", hasTexture)

            // Render the mesh for this HUD item
            mesh.render()
        }
    }

    open fun resize(window: Window) = Unit

    open fun delete() {
        mesh?.delete()
    }

    fun setPosition(x: Int, y: Int) {
        this.x = x
        this.y = y
    }

    fun setPosition(position: Vector2ic) = setPosition(position.x(), position.y())

    override fun toString(): String {
        return "GuiItem(position=${MathsUtils.format(position)}, mesh=$mesh)"
    }

    companion object {
        const val Z_POS: Float = 0.0f
    }

}
