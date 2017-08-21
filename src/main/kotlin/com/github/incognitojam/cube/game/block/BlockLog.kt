package com.github.incognitojam.cube.game.block

import com.github.incognitojam.cube.game.item.Items
import com.github.incognitojam.cube.game.world.Direction
import com.github.incognitojam.cube.game.world.Direction.*

class BlockLog(id: Byte) : Block(id) {

    override fun getItem() = Items.LOG

    override fun getTextureId(direction: Direction) = when (direction) {
        NORTH, EAST, SOUTH, WEST -> 20
        UP, DOWN -> 21
    }

}