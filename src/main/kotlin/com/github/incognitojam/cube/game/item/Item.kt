package com.github.incognitojam.cube.game.item

import com.github.incognitojam.cube.game.block.Block

open class Item(val id: Byte, val name: String) {

    open fun getBlock(): Block? = null

    open fun getTextureId(): Int? = null

    fun getTextureCoordinates() = getTextureId()?.let { Items.getTextureMap().getTextureCoordinates(it) }

    fun getMaxStackSize(): Int = 100

    override fun equals(other: Any?) = this === other || (other is Item && id == other.id)

    override fun hashCode() = id.toInt()

    override fun toString() = "Item(id=$id, name='$name')"

}

open class ItemFood(id: Byte, name: String, val hunger: Float) : Item(id, name) {

    override fun toString(): String {
        return "ItemFood(id=$id, name='$name', hunger=$hunger)"
    }

}