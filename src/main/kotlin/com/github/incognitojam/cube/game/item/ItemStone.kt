package com.github.incognitojam.cube.game.item

import com.github.incognitojam.cube.game.block.Blocks

class ItemStone(id: Byte) : Item(id, "stone") {
    
    override fun getBlock() = Blocks.STONE
    
}