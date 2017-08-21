package com.github.incognitojam.cube.game.block

import com.github.incognitojam.cube.game.item.Items
import com.github.incognitojam.cube.game.world.Direction

class BlockDirt(id: Byte) : Block(id) {

    override fun getItem() = Items.DIRT

    override fun getTextureId(direction: Direction) = 2

}