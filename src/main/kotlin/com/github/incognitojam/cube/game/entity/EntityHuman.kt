package com.github.incognitojam.cube.game.entity

import com.github.incognitojam.cube.game.inventory.Inventory
import com.github.incognitojam.cube.game.world.World

open class EntityHuman(world: World) : EntityHungry(world, 0.9f, 1.9f, 70f, 100, 100) {

    var name = "Human"
    val inventory = Inventory(9, 4, "$name's Inventory")

    override fun toString(): String {
        return "EntityHuman(name='$name',super=${super.toString()})"
    }

}