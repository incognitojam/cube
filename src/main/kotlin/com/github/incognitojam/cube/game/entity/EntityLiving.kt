package com.github.incognitojam.cube.game.entity

import com.github.incognitojam.cube.game.world.World

abstract class EntityLiving(world: World, width: Float, height: Float, mass: Float, private var health: Int) :
        Entity(world, width, height, mass) {

    private val living: Boolean
        get() = health > 0

    override var dead: Boolean
        get() = !living
        set(value) {
            if (!value) health = 0
        }

}