package com.github.incognitojam.cube.game.entity

import com.github.incognitojam.cube.engine.MouseInput
import com.github.incognitojam.cube.engine.Window
import com.github.incognitojam.cube.engine.graphics.Camera
import com.github.incognitojam.cube.engine.maths.cos
import com.github.incognitojam.cube.engine.maths.sin
import com.github.incognitojam.cube.engine.maths.toRadians
import com.github.incognitojam.cube.game.ProjectCube
import com.github.incognitojam.cube.game.block.Blocks
import com.github.incognitojam.cube.game.inventory.ItemStack
import com.github.incognitojam.cube.game.item.Items
import com.github.incognitojam.cube.game.world.Direction
import com.github.incognitojam.cube.game.world.World
import org.joml.Vector3f
import org.joml.Vector3fc
import org.joml.Vector3i
import org.lwjgl.glfw.GLFW.*
import java.util.*

class EntityPlayer(world: World) : EntityHuman(world) {

    var jumpCooldown = 0
    var jumping: Boolean
        get() = jumpCooldown > 0
        set(value) {
            jumpCooldown = if (value) 6 else 0
        }
    private var sprinting = false
    private var crouching = false

    val camera = Camera(height * 0.75f)
    val targetBlock = Vector3i()
    private var targetFace = Direction.UP
    private var interactCooldown = 0
    private var interacting
        get() = interactCooldown > 0
        set(value) {
            interactCooldown = if (value) 5 else 0
        }

    fun input(window: Window, mouseInput: MouseInput) {
        val forward = window.isKeyPressed(GLFW_KEY_W)
        val left = window.isKeyPressed(GLFW_KEY_A)
        val backward = window.isKeyPressed(GLFW_KEY_S)
        val right = window.isKeyPressed(GLFW_KEY_D)
        val crouch = window.isKeyPressed(GLFW_KEY_LEFT_SHIFT)
        val sprint = window.isKeyPressed(GLFW_KEY_LEFT_CONTROL)
        val jump = window.isKeyPressed(GLFW_KEY_SPACE)
        val use = window.isKeyPressed(GLFW_KEY_E)

        sprinting = sprint
        crouching = crouch

        val bothAxes = (forward || backward) && (left || right)
        val speedModifier = when {
            crouch -> ProjectCube.PLAYER_CROUCH_SPEED
            sprint -> ProjectCube.PLAYER_RUN_SPEED
            else -> ProjectCube.PLAYER_WALK_SPEED
        } * (if (bothAxes) 0.707f else 1f)

        val forceMod = if (grounded) 1.0f else 0.05f

        if (forward) velocity.add(resolveMovement(0f, 0f, -speedModifier * forceMod))
        if (backward) velocity.add(resolveMovement(0f, 0f, speedModifier * forceMod))

        if (left) velocity.add(resolveMovement(-speedModifier * forceMod, 0f, 0f))
        if (right) velocity.add(resolveMovement(speedModifier * forceMod, 0f, 0f))

        if (grounded && jump && !jumping) {
            jumping = true
            velocity.add(0f, ProjectCube.PLAYER_JUMP_SPEED, 0f)
        }

        if (mouseInput.scrollY < 0) {
            inventory.selectedIndex++
        }

        if (mouseInput.scrollY > 0) {
            inventory.selectedIndex--
        }

        val result = camera.castRayForBlock(16f, world) { it.visible }
        if (result != null) {
            val (location, face) = result
            targetBlock.set(location.globalPosition)
            targetFace = face

            if (mouseInput.leftButtonPressed && !interacting) {
                val drop = location.block?.getItemDrop(Random()) ?: ItemStack()
                location.block = Blocks.AIR
                interacting = true

                world.dropItem(drop, Vector3f(location.globalPosition).add(0.5f, 0.25f, 0.5f))
            }

            if (mouseInput.rightButtonPressed && !interacting) {
                val slot = inventory.getSlot(inventory.selectedIndex)!!
                val itemStack = slot.itemStack
                val block = itemStack.item.getBlock()
                if (block != null && itemStack.quantity > 0 && itemStack.item != Items.AIR) {
                    val targetLocation = location.getAdjacent(targetFace)
                    if (targetLocation.block == Blocks.AIR) {
                        targetLocation.block = block
                        interacting = true

                        inventory.setSlot(inventory.selectedIndex, inventory.getSlot(inventory.selectedIndex)!!.itemStack.apply { quantity-- })
                    }
                }
            }
        } else {
            targetBlock.set(0, 0, 0)
        }

        if (use && !interacting) {
            val entityItem: EntityItem? = camera.castRayForEntity(5f, world) { true }
            if (entityItem != null) {
                interacting = true
                val remainder = inventory.addItem(entityItem.itemStack)
                entityItem.itemStack.set(remainder)
            }
        }
    }

    override fun update(delta: Float) {
        super.update(delta)

        if (grounded && jumpCooldown > 0) jumpCooldown--
        if (interacting) interactCooldown--

        val maxSpeed = when {
            sprinting -> ProjectCube.PLAYER_RUN_SPEED
            crouching -> ProjectCube.PLAYER_CROUCH_SPEED
            else -> ProjectCube.PLAYER_WALK_SPEED
        }

        val velocityHorizontal = Vector3f(velocity.x, 0f, velocity.z)
        if (velocityHorizontal.lengthSquared() > maxSpeed * maxSpeed) {
            velocityHorizontal.normalize(maxSpeed)
            velocity.x = velocityHorizontal.x
            velocity.z = velocityHorizontal.z
        }
    }

    override fun resolveMovement(deltaX: Float, deltaY: Float, deltaZ: Float): Vector3fc {
        var modX = 0f
        var modZ = 0f
        if (deltaX != 0f) {
            modX += (camera.pitch - 90f).toRadians().sin() * -1f * deltaX
            modZ += (camera.pitch - 90f).toRadians().cos() * deltaX
        }
        if (deltaZ != 0f) {
            modX += camera.pitch.toRadians().sin() * -1f * deltaZ
            modZ += camera.pitch.toRadians().cos() * deltaZ
        }

        return Vector3f(modX, deltaY, modZ)
    }

    override fun onPositionChange(deltaX: Float, deltaY: Float, deltaZ: Float) = camera.setPosition(x, y + camera.eyeHeight, z)

    override fun onRotationChange(deltaYaw: Float, deltaPitch: Float) = camera.addRotation(deltaYaw, deltaPitch)

}