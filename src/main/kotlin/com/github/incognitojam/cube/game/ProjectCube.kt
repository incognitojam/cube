package com.github.incognitojam.cube.game

import com.github.incognitojam.cube.engine.IGameLogic
import com.github.incognitojam.cube.engine.MouseInput
import com.github.incognitojam.cube.engine.Window
import com.github.incognitojam.cube.engine.maths.MathsUtils
import com.github.incognitojam.cube.game.block.Blocks
import com.github.incognitojam.cube.game.entity.EntityPlayer
import com.github.incognitojam.cube.game.gui.GuiHud
import com.github.incognitojam.cube.game.gui.GuiRenderer
import com.github.incognitojam.cube.game.item.Items
import com.github.incognitojam.cube.game.world.World
import org.lwjgl.glfw.GLFW.GLFW_KEY_C
import org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER
import org.lwjgl.opengl.GL11.*

class ProjectCube : IGameLogic {

    private lateinit var world: World
    private lateinit var hud: GuiHud
    private lateinit var guiRenderer: GuiRenderer
    private lateinit var player: EntityPlayer

    override fun initialise(window: Window) {
        Blocks.initialise()
        Items.initialise()

        world = World("World", 4)
        world.initialise()
        player = world.player
        player.name = "Player"

        guiRenderer = GuiRenderer()
        guiRenderer.initialise()
        hud = GuiHud(world)
        hud.initialise()

        player.setPositionWithoutColliding(8.5f, 512f, 8.5f)
        player.setRotation(0f, 0f)
    }

    override fun status(frames: Int, updates: Int) {
        hud.setStatusText("FPS: $frames, UPS: $updates")
    }

    override fun input(window: Window, mouseInput: MouseInput) {
        player.input(window, mouseInput)

        if (window.isKeyPressed(GLFW_KEY_C)) {
            world.worldRenderer.debug = !world.worldRenderer.debug
        }

        if (window.isKeyPressed(GLFW_KEY_ENTER)) {
            window.captureMouse = !window.captureMouse
        }
    }

    override fun update(window: Window, delta: Float, mouseInput: MouseInput) {
        // Update camera based on mouse
        if (window.captureMouse) {
            val rotVec = mouseInput.displayVec
            player.addRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY)
        }

        world.update(delta)

        val debugText = """
            |Position: ${MathsUtils.format(player.position)}
            |Rotation: ${MathsUtils.format(player.rotation)}
            |Camera Rotation: ${MathsUtils.format(player.camera.rotation)}
            |Velocity: ${MathsUtils.format(player.velocity)}
            |Grounded: ${player.grounded}, Jumping: ${player.jumping}, Jump Cooldown: ${player.jumpCooldown} ticks
            |Stood above ${player.groundLocation.block} at ${MathsUtils.format(player.groundLocation.globalPosition)}
            """.trimMargin("|")

        hud.setDebugText(debugText)
        hud.update()
    }

    private fun clear() {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        glClearColor(.15f, .35f, .65f, 1f)
    }

    override fun render(window: Window) {
        clear()

        if (window.resized) {
            glViewport(0, 0, window.width, window.height)
            window.resized = false

            hud.resize(window)
        }

        glEnable(GL_CULL_FACE)
        glCullFace(GL_BACK)
        world.render(window)
        glDisable(GL_CULL_FACE)
        guiRenderer.render(window, hud)
    }

    override fun delete() {
        world.delete()
        hud.delete()
        guiRenderer.delete()

        Blocks.delete()
        Items.delete()
    }

    companion object {
        const val MOUSE_SENSITIVITY = 0.2f

        const val PLAYER_CROUCH_SPEED = 0.5f
        const val PLAYER_WALK_SPEED = 1.0f
        const val PLAYER_RUN_SPEED = 4.0f
        const val PLAYER_JUMP_SPEED = 5.0f
    }

}
