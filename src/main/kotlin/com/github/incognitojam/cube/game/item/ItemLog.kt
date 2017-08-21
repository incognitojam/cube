package com.github.incognitojam.cube.game.item

import com.github.incognitojam.cube.game.block.Blocks

class ItemLog(id: Byte) : Item(id, "log") {
    
    override fun getBlock() = Blocks.LOG
    
}