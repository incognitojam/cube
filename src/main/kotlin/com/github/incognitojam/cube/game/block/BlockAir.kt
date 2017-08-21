package com.github.incognitojam.cube.game.block

import com.github.incognitojam.cube.game.item.Items
import com.github.incognitojam.cube.game.world.Direction

class BlockAir(id: Byte) : Block(id, false, false) {

    override fun getItem() = Items.AIR

    override fun getTextureId(direction: Direction) = 0

}