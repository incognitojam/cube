package com.github.incognitojam.cube.game.block

import com.github.incognitojam.cube.game.item.Items
import com.github.incognitojam.cube.game.world.Direction
import com.github.incognitojam.cube.game.world.Direction.NORTH
import com.github.incognitojam.cube.game.world.Direction.UP

class BlockPumpkin(id: Byte) : Block(id) {

    override fun getItem() = Items.PUMPKIN

    override fun getTextureId(direction: Direction) = when(direction) {
        NORTH -> 119
        UP -> 102
        else -> 118
    }

}