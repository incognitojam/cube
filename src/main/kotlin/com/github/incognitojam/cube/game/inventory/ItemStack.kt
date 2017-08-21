package com.github.incognitojam.cube.game.inventory

import com.github.incognitojam.cube.game.block.Block
import com.github.incognitojam.cube.game.item.Item
import com.github.incognitojam.cube.game.item.Items

class ItemStack(var item: Item, quantity: Int) {

    var quantity: Int = quantity
        set(value) {
            if (value == 0) item = Items.AIR
            field = value
        }

    val maxStackSize: Int
        get() = item.getMaxStackSize()

    constructor(item: Item) : this(item, 1)

    constructor(block: Block, quantity: Int) : this(block.getItem(), quantity)

    constructor(block: Block) : this(block.getItem(), 1)

    constructor(itemStack: ItemStack) : this(itemStack.item, itemStack.quantity)

    constructor() : this(Items.AIR, 0)

    fun set(itemStack: ItemStack) {
        item = itemStack.item
        quantity = itemStack.quantity
    }

    override fun toString(): String {
        return "ItemStack(item='${item.name}', quantity=$quantity)"
    }

}