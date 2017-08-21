package com.github.incognitojam.cube.game.inventory

import com.github.incognitojam.cube.game.container.Container

class Inventory(width: Int, height: Int, name: String) : Container(width, height, name) {

    val hotbarSize = width

    var selectedIndex = 0
        set(value) {
            field = Math.floorMod(value, hotbarSize)
        }

    val itemInHand: ItemStack
        get() = getSlot(selectedIndex)!!.itemStack

    override fun toString(): String {
        return "Inventory(hotbarSize=$hotbarSize, selectedIndex=$selectedIndex, super=${super.toString()})"
    }

}