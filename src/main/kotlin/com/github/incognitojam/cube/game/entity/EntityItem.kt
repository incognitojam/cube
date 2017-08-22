package com.github.incognitojam.cube.game.entity

import com.github.incognitojam.cube.engine.graphics.BlockMesh
import com.github.incognitojam.cube.engine.graphics.BlockMeshBuilder
import com.github.incognitojam.cube.engine.graphics.ShaderProgram
import com.github.incognitojam.cube.game.inventory.ItemStack
import com.github.incognitojam.cube.game.item.Items
import com.github.incognitojam.cube.game.world.World
import org.joml.Matrix4f

class EntityItem(world: World, val itemStack: ItemStack) : Entity(world, .3f, .3f, 1f) {

    private var blockMesh: BlockMesh? = null
//    private var itemMesh: ItemMesh? = null

    override fun initialise() {
        super.initialise()
        val item = itemStack.item
        val block = item.getBlock()
        if (block != null) {
            blockMesh = BlockMeshBuilder().getBlockMesh(block, width)
        } else {
//            itemMesh = ItemMeshBuilder().getItemMesh(item, width)
        }
    }

    override fun update(delta: Float) {
        super.update(delta)
        addRotation(0f, 1f)

        if (itemStack.quantity == 0 || itemStack.item == Items.AIR) dead = true
    }

    override fun render(shader: ShaderProgram, modelViewMatrix: Matrix4f) {
        super.render(shader, modelViewMatrix)
        blockMesh?.render()
//        itemMesh?.render()
    }

    override fun delete() {
        super.delete()
        blockMesh?.delete()
//        itemMesh?.delete()
    }

}