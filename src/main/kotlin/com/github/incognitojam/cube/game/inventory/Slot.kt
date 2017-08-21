package com.github.incognitojam.cube.game.inventory

import com.github.incognitojam.cube.game.item.Items

class Slot(val index: Int, val itemStack: ItemStack) {

    constructor(index: Int) : this(index, ItemStack())

    fun isEmpty() = itemStack.item == Items.AIR || itemStack.quantity == 0

    fun isNotEmpty() = !isEmpty()

    fun isFull() = itemStack.quantity == itemStack.maxStackSize

    fun isNotFull() = !isFull()

    fun hasRoom(other: ItemStack) = itemStack.item == other.item && itemStack.maxStackSize - itemStack.quantity >= other.quantity

    override fun toString(): String {
        return "Slot(index=$index, itemStack=$itemStack)"
    }

}