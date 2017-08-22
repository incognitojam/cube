package com.github.incognitojam.cube.game.block

import com.github.incognitojam.cube.engine.graphics.TextureMap

object Blocks {

    private val registry = HashMap<Byte, Block>()
    private lateinit var textureMap: TextureMap

    lateinit var AIR: Block
    lateinit var GRASS: Block
    lateinit var DIRT: Block
    lateinit var STONE: Block
    lateinit var GRAVEL: Block
    lateinit var LOG: Block
    lateinit var WATER: Block
    lateinit var PUMPKIN: Block

    fun initialise() {
        AIR = registerBlock(BlockAir(0))
        GRASS = registerBlock(BlockGrass(1))
        DIRT = registerBlock(BlockDirt(2))
        STONE = registerBlock(BlockStone(3))
        GRAVEL = registerBlock(BlockGravel(4))
        LOG = registerBlock(BlockLog(5))
        WATER = registerBlock(BlockWater(6))
        PUMPKIN = registerBlock(BlockPumpkin(7))

        textureMap = TextureMap("textures/blocks/blocks.png", 16)
    }

    fun delete() = textureMap.delete()

    fun getBlockById(id: Byte): Block? {
        return registry[id]
    }

    fun getTextureMap(): TextureMap {
        return textureMap
    }

    private fun registerBlock(block: Block): Block {
        registry[block.id] = block
        return block
    }

}