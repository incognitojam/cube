package com.github.incognitojam.cube.game.container

import com.github.incognitojam.cube.game.inventory.ItemStack
import com.github.incognitojam.cube.game.inventory.Slot
import java.util.*

open class Container(val width: Int, val height: Int, val name: String) {

    private val size = width * height
    private val slots = Array(size) { Slot(it) }

    fun getSlot(index: Int) = slots.getOrNull(index)

    fun setSlot(index: Int, itemStack: ItemStack) {
        slots.getOrNull(index)?.itemStack?.set(itemStack)
    }

    fun addItem(itemStack: ItemStack): ItemStack {
        var index = 0
        while (index in 0 until size && itemStack.quantity > 0) {
            val slot = slots[index]

            if (slot.isEmpty()) {
                setSlot(index, itemStack)
                itemStack.quantity = 0
            } else if (slot.hasRoom(itemStack)) {
                setSlot(index, ItemStack(itemStack).apply { quantity += slot.itemStack.quantity })
                itemStack.quantity = 0
            } else if (slot.isNotFull() && slot.itemStack.item == itemStack.item) {
                val availableSpace = slot.itemStack.maxStackSize - slot.itemStack.quantity
                setSlot(index, ItemStack(slot.itemStack).apply { quantity = itemStack.maxStackSize })
                itemStack.quantity -= availableSpace
            }

            index++
        }

        return itemStack
    }

    fun isEmpty() = slots.all(Slot::isEmpty)

    fun isNotEmpty() = slots.any(Slot::isNotEmpty)

    fun isFull() = slots.all(Slot::isFull)

    fun isNotFull() = slots.any(Slot::isNotFull)

    override fun toString(): String {
        return "Container(width=$width, height=$height, name='$name', size=$size, slots=${Arrays.toString(slots)})"
    }

}