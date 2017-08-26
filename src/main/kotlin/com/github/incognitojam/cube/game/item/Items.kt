package com.github.incognitojam.cube.game.item

import com.github.incognitojam.cube.engine.graphics.TextureMap

object Items {

    private val registry = HashMap<Byte, Item>()
    private lateinit var textureMap: TextureMap

    lateinit var AIR: Item
    lateinit var GRASS: Item
    lateinit var DIRT: Item
    lateinit var STONE: Item
    lateinit var GRAVEL: Item
    lateinit var LOG: Item
    lateinit var WATER: Item
    lateinit var PUMPKIN: Item

    fun initialise() {
        AIR = registerItem(ItemAir(0))
        GRASS = registerItem(ItemGrass(1))
        DIRT = registerItem(ItemDirt(2))
        STONE = registerItem(ItemStone(3))
        GRAVEL = registerItem(ItemGravel(4))
        LOG = registerItem(ItemLog(5))
        WATER = registerItem(ItemWater(6))
        PUMPKIN = registerItem(ItemPumpkin(7))

        textureMap = TextureMap.loadTextureMap("textures/items/items.png", 16)
    }

    fun delete() {
        textureMap.delete()
    }

    fun getItemById(id: Byte): Item? {
        return registry[id]
    }

    fun getTextureMap(): TextureMap {
        return textureMap
    }

    private fun registerItem(item: Item): Item {
        registry[item.id] = item
        return item
    }

}