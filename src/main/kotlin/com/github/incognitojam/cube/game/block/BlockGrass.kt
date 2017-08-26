package com.github.incognitojam.cube.game.block

import com.github.incognitojam.cube.game.item.Items
import com.github.incognitojam.cube.game.world.Direction
import com.github.incognitojam.cube.game.world.Direction.*

class BlockGrass(id: Byte) : Block(id) {

    override fun getItem() = Items.GRASS

    override fun getTextureId(direction: Direction, itemDrop: Boolean) = when (direction) {
        UP -> if (itemDrop) 184 else 40
        DOWN -> 2
        NORTH -> 3
        EAST -> 3
        SOUTH -> 3
        WEST -> 3
    }

}