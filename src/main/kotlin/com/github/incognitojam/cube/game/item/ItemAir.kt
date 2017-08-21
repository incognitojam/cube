package com.github.incognitojam.cube.game.item

import com.github.incognitojam.cube.game.block.Blocks

class ItemAir(id: Byte) : Item(id, "air") {

    override fun getBlock() = Blocks.AIR

}