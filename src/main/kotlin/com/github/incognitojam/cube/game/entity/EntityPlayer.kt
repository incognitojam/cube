package com.github.incognitojam.cube.game.entity

import com.github.incognitojam.cube.engine.MouseInput
import com.github.incognitojam.cube.engine.Window
import com.github.incognitojam.cube.engine.graphics.Camera
import com.github.incognitojam.cube.game.GamCraft
import com.github.incognitojam.cube.game.block.Blocks
import com.github.incognitojam.cube.game.inventory.ItemStack
import com.github.incognitojam.cube.game.item.Items
import com.github.incognitojam.cube.game.world.Direction
import com.github.incognitojam.cube.game.world.World
import org.joml.Vector3f
import org.joml.Vector3i
import org.lwjgl.glfw.GLFW.*
import java.util.*

class EntityPlayer(world: World, name: String) : EntityHuman(world, name) {

    var jumpCooldown = 0
    var jumping: Boolean
        get() = jumpCooldown > 0
        set(value) {
            jumpCooldown = if (value) 6 else 0
        }

    val camera = Camera()
    val targetBlock = Vector3i()
    var targetFace = Direction.UP
    var interactCooldown = 0
    var interacting
        get() = interactCooldown > 0
        set(value) {
            interactCooldown = if (value) 5 else 0
        }

    override fun onInitialise() {
        super.onInitialise()

        inventory.addItem(ItemStack(Items.GRASS, 1))
        inventory.addItem(ItemStack(Items.DIRT, 1))
        inventory.addItem(ItemStack(Items.STONE, 1))
        inventory.addItem(ItemStack(Items.GRAVEL, 1))
        inventory.addItem(ItemStack(Items.LOG, 1))
        inventory.addItem(ItemStack(Items.WATER, 1))
    }

    fun onInput(window: Window, mouseInput: MouseInput) {
        val forward = window.isKeyPressed(GLFW_KEY_W)
        val left = window.isKeyPressed(GLFW_KEY_A)
        val backward = window.isKeyPressed(GLFW_KEY_S)
        val right = window.isKeyPressed(GLFW_KEY_D)
        val crouch = window.isKeyPressed(GLFW_KEY_LEFT_SHIFT)
        val sprint = window.isKeyPressed(GLFW_KEY_LEFT_CONTROL)
        val jump = window.isKeyPressed(GLFW_KEY_SPACE)
        val use = window.isKeyPressed(GLFW_KEY_E)

        val bothAxes = (forward || backward) && (left || right)
        val speedModifier = when {
            crouch -> GamCraft.PLAYER_CROUCH_FORCE
            sprint -> GamCraft.PLAYER_RUN_FORCE
            else -> GamCraft.PLAYER_WALK_FORCE
        } * (if (bothAxes) 0.707F else 1F)

        val forceMod = if (grounded) 1f else 0.05f

        if (forward) addForce(resolveMovement(0f, 0f, -speedModifier * forceMod))
        if (backward) addForce(resolveMovement(0f, 0f, speedModifier * forceMod))

        if (left) addForce(resolveMovement(-speedModifier * forceMod, 0f, 0f))
        if (right) addForce(resolveMovement(speedModifier * forceMod, 0f, 0f))

        if (grounded && jump && !jumping) {
            jumping = true
            addForce(0f, GamCraft.PLAYER_JUMP_FORCE, 0f, 2)
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

                world.dropItem(drop, Vector3f(location.globalPosition).add(.5f, .25f, .5f))
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

    override fun onUpdate(delta: Float) {
        super.onUpdate(delta)

        if (grounded && jumpCooldown > 0) jumpCooldown--
        if (interacting) interactCooldown--
    }

    override fun onPositionChange() {
        camera.setPosition(position.x, position.y + 1.5f, position.z)
    }

    override fun onRotationChange() {
        camera.setRotation(rotation.x, rotation.y)
    }

}