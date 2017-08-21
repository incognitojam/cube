package com.github.incognitojam.cube.game.item

import com.github.incognitojam.cube.game.block.Blocks

class ItemWater(id: Byte) : Item(id, "water") {
    
    override fun getBlock() = Blocks.WATER
    
}