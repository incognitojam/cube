package com.github.incognitojam.cube.game.item

import com.github.incognitojam.cube.game.block.Blocks

class ItemDirt(id: Byte) : Item(id, "dirt") {
    
    override fun getBlock() = Blocks.DIRT
    
}