package com.github.incognitojam.cube.game.item

import com.github.incognitojam.cube.game.block.Blocks

class ItemPumpkin(id: Byte) : Item(id, "pumpkin") {
    
    override fun getBlock() = Blocks.PUMPKIN
    
}