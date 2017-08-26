package com.github.incognitojam.cube.game.entity

import com.github.incognitojam.cube.engine.graphics.ShaderProgram
import com.github.incognitojam.cube.engine.graphics.mesh.BlockMesh
import com.github.incognitojam.cube.engine.graphics.mesh.BlockMeshBuilder
import com.github.incognitojam.cube.game.inventory.ItemStack
import com.github.incognitojam.cube.game.item.Items
import com.github.incognitojam.cube.game.world.World
import org.joml.Matrix4f

class EntityItem(world: World) : Entity(world, 0.3f, 0.3f, 1.0f) {

    lateinit var itemStack: ItemStack
    private var blockMesh: BlockMesh? = null
//    private var itemMesh: ItemMesh? = null

    override fun initialise() {
        super.initialise()

        val item = itemStack.item
        val block = item.getBlock()
        if (block != null) {
            val blockMeshBuilder = BlockMeshBuilder()
            blockMeshBuilder.addBlock(block, width, true)
            blockMesh = blockMeshBuilder.build()
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

    companion object {
        const val JUMP_VELOCITY = 5f
    }

}