package com.github.incognitojam.cube.game.item

import com.github.incognitojam.cube.game.block.Blocks

class ItemGrass(id: Byte) : Item(id, "grass") {
    
    override fun getBlock() = Blocks.GRASS
    
}