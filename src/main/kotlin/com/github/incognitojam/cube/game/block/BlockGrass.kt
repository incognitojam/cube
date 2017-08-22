package com.github.incognitojam.cube.game.block

import com.github.incognitojam.cube.game.item.Items
import com.github.incognitojam.cube.game.world.Direction
import com.github.incognitojam.cube.game.world.Direction.*
import org.joml.Vector3f

class BlockGrass(id: Byte) : Block(id) {

    override fun getItem() = Items.GRASS

    override fun getTextureId(direction: Direction, itemDrop: Boolean) = when (direction) {
        UP -> if (itemDrop) 40 else 184
        DOWN -> 2
        NORTH -> 3
        EAST -> 3
        SOUTH -> 3
        WEST -> 3
    }

    companion object {
        val GRASS_BRIGHT = Vector3f(13 / 255f, 178 / 255f, 10 / 255f)
        val GRASS_DEAD = Vector3f(140 / 255f, 82 / 255f, 51 / 255f)

        fun getVegetationColour(percentage: Float): Vector3f {
            return Vector3f(GRASS_BRIGHT).mul(percentage).add(Vector3f(GRASS_DEAD).mul(1f - percentage))
        }
    }

}