package com.github.incognitojam.cube.game.world.generator

import com.github.incognitojam.cube.game.block.Block
import com.github.incognitojam.cube.game.block.Blocks
import com.github.incognitojam.cube.game.world.Location
import com.github.incognitojam.cube.game.world.chunk.Chunk

class ClassicGenerator(worldHeight: Int) : AbstractWorldGenerator(worldHeight) {

    init {
        addLevel(0 until worldHeight, SurfaceLevel(this))
//        addLevel(0 until 1, CaveLevel(this))
    }

}

class SurfaceLevel(worldGenerator: AbstractWorldGenerator) : AbstractLevel(worldGenerator, "Surface") {
    override fun addBiomes() {
        addBiome(FlatBiome(this), 2.0)
//        addBiome(HillBiome(this), 1.0)
    }

    abstract class BasicBiome(name: String, private val scale: Int, private val maxHeight: Int, private val level: AbstractLevel) :
            AbstractBiome(name) {

        private val layers = ArrayList<Pair<Block, IntRange>>()

        protected fun addLayer(block: Block, depth: Int) {
            val sumOfHeights = layers.sumBy { it.second.count() }
            val range = sumOfHeights until sumOfHeights + depth
            layers.add(Pair(block, range))
        }

        protected fun addLayer(block: Block) = addLayer(block, 1)

        protected fun addContinuousLayer(block: Block) = addLayer(block, Location.CHUNK_SIZE)

        override fun getChunkBlocks(chunk: Chunk): Array<Block> {
            val chunkWorldPos = chunk.worldPos

            var x = 0
            var z = 0
            val heightMap = Array(Location.CHUNK_SIZE_SQUARED) {
                val globalX = chunkWorldPos.x() + x
                val globalZ = chunkWorldPos.z() + z
                val height = level.getSurfaceHeight(globalX, globalZ)

                x++
                if (x > 15) {
                    x = 0
                    z++
                }

                if (height > 0) height else 0
            }

            var heightMapIndex = 0
            val blocks = Array(Location.CHUNK_SIZE_CUBED) { Blocks.AIR }
            val levelChunkYOffset = level.getChunkRelativeMinimumY(chunk.y)
            for (localX in Location.CHUNK_LENGTH) {
                for (localZ in Location.CHUNK_LENGTH) {
                    val levelSurfaceY = heightMap[heightMapIndex]
                    val localSurfaceY = levelSurfaceY - levelChunkYOffset

                    if (localSurfaceY < 0) continue

                    for ((block, range) in layers) {
                        for (layerDepth in range) {
                            val localY = localSurfaceY - layerDepth
                            if (localY > 15) continue
                            if (localY < 0) break
                            val index = Chunk.getIndexForPosition(localX, localY, localZ)
                            blocks[index] = block
                        }
                    }

                    heightMapIndex++
                }
            }

            return blocks
        }

        override fun getSurfaceHeight(globalX: Int, globalZ: Int): Int {
            return NoiseGenerator.getHeight(globalX, globalZ, scale, maxHeight)
        }

    }

    class FlatBiome(level: AbstractLevel) : BasicBiome("Flat", 256, level.heightBlocks / 2, level) {
        init {
            addLayer(Blocks.GRASS)
            addLayer(Blocks.DIRT, 2)
            addContinuousLayer(Blocks.STONE)
        }
    }

    class HillBiome(level: AbstractLevel) : BasicBiome("Hill", 64, level.heightBlocks, level) {
        init {
            addLayer(Blocks.GRASS)
            addLayer(Blocks.DIRT, 1)
            addContinuousLayer(Blocks.STONE)
        }
    }

}

class CaveLevel(worldGenerator: AbstractWorldGenerator) : AbstractLevel(worldGenerator, "Cave") {

    override fun addBiomes() {
        addBiome(CaveBiome(this), 1.0)
    }

    class CaveBiome(private val level: AbstractLevel) : AbstractBiome("Cave") {
        override fun getChunkBlocks(chunk: Chunk): Array<Block> {
            return Array(Location.CHUNK_SIZE_CUBED) { Blocks.STONE }
        }

        override fun getSurfaceHeight(globalX: Int, globalZ: Int) = level.heightBlocks
    }

}
