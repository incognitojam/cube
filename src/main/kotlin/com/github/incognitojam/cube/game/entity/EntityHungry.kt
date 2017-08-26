package com.github.incognitojam.cube.game.entity

import com.github.incognitojam.cube.game.world.World

abstract class EntityHungry(world: World, width: Float, height: Float, mass: Float, health: Int, private var hunger: Int) :
        EntityLiving(world, width, height, mass, health) {

    val starving: Boolean
        get() = hunger == 0

}