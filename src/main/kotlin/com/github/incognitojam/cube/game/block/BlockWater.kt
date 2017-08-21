package com.github.incognitojam.cube.game.block

import com.github.incognitojam.cube.game.item.Items
import com.github.incognitojam.cube.game.world.Direction

class BlockWater(id: Byte) : Block(id, true, false, false) {

    override fun getItem() = Items.WATER

    override fun getTextureId(direction: Direction) = 14

}