package com.github.incognitojam.cube.game.entity

import com.github.incognitojam.cube.engine.graphics.BlockMesh
import com.github.incognitojam.cube.engine.graphics.BlockMeshBuilder
import com.github.incognitojam.cube.engine.graphics.ShaderProgram
import com.github.incognitojam.cube.game.block.Blocks
import com.github.incognitojam.cube.game.inventory.ItemStack
import com.github.incognitojam.cube.game.item.Items
import com.github.incognitojam.cube.game.world.World
import org.joml.Matrix4f

class EntityItem(world: World, val itemStack: ItemStack) : Entity(world, .3f, .3f, 1f) {

    private lateinit var blockMesh: BlockMesh

    override fun onInitialise() {
        super.onInitialise()
        blockMesh = BlockMeshBuilder().getBlockMesh(itemStack.item.getBlock() ?: Blocks.DIRT, width)
    }

    override fun onUpdate(delta: Float) {
        super.onUpdate(delta)
        addRotation(0f, 1f)

        if (itemStack.quantity == 0 || itemStack.item == Items.AIR) dead = true
    }

    override fun onRender(shader: ShaderProgram, modelViewMatrix: Matrix4f) {
        super.onRender(shader, modelViewMatrix)
        blockMesh.onRender()
    }

    override fun onCleanup() {
        super.onCleanup()
        blockMesh.onCleanup()
    }

}