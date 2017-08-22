package com.github.incognitojam.cube.game.block

import com.github.incognitojam.cube.game.inventory.ItemStack
import com.github.incognitojam.cube.game.item.Item
import com.github.incognitojam.cube.game.world.Direction
import java.util.*

abstract class Block(val id: Byte, val visible: Boolean = true, val opaque: Boolean = visible, val solid: Boolean = visible) {

    abstract fun getItem(): Item

    open fun getItemDrop(random: Random): ItemStack {
        return ItemStack(getItem(), 1)
    }

    abstract fun getTextureId(direction: Direction, itemDrop: Boolean): Int

    fun getTextureCoordinates(direction: Direction, itemDrop: Boolean = false)
            = Blocks.getTextureMap().getTextureCoordinates(getTextureId(direction, itemDrop))

    override fun equals(other: Any?): Boolean {
        return other is Block && other.id == id
    }

    override fun hashCode(): Int {
        return id.toInt()
    }

    override fun toString(): String {
        return "Block(id=$id, item=${getItem()})"
    }

}