package com.github.incognitojam.cube.game.item

import com.github.incognitojam.cube.game.block.Blocks

class ItemGravel(id: Byte) : Item(id, "gravel") {
    
    override fun getBlock() = Blocks.GRAVEL
    
}