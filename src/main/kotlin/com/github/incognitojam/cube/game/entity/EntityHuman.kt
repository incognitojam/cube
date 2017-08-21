package com.github.incognitojam.cube.game.entity

import com.github.incognitojam.cube.game.inventory.Inventory
import com.github.incognitojam.cube.game.world.World

open class EntityHuman(world: World, val name: String) : EntityHungry(world, .9F, 1.9F, 70F, 100, 100) {

    val inventory = Inventory(8, 4, "$name's Inventory")

    override fun toString(): String {
        return "EntityHuman(name='$name',super=${super.toString()})"
    }

}