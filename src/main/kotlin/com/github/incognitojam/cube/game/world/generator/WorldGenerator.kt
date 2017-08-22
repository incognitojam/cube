package com.github.incognitojam.cube.game.world.generator

import com.github.incognitojam.cube.game.block.Blocks
import com.github.incognitojam.cube.game.world.Location
import com.github.incognitojam.cube.game.world.chunk.Chunk
import org.joml.SimplexNoise
import org.joml.Vector3i
import java.util.*

interface WorldGenerator {
    fun getLayerGenerator(chunk: Chunk): LayerGenerator
}

interface LayerGenerator {
    fun getMinimumChunkY(): Int

    fun getMaximumChunkY(): Int

    fun getHeight(): Int = getMaximumChunkY() - getMinimumChunkY() + 1

    fun getChunkGenerator(worldGenerator: WorldGenerator, chunk: Chunk): ChunkGenerator
}

interface ChunkGenerator {
    fun generateChunk(layerGenerator: LayerGenerator, chunk: Chunk)
}

abstract class AbstractWorldGenerator(private val name: String, val seed: Long) : WorldGenerator {
    override abstract fun getLayerGenerator(chunk: Chunk): LayerGenerator

    fun generateChunk(chunk: Chunk) {
        val layerGenerator = getLayerGenerator(chunk)
        val chunkGenerator = layerGenerator.getChunkGenerator(this, chunk)
        chunkGenerator.generateChunk(layerGenerator, chunk)
    }

    override fun toString(): String {
        return "WorldGenerator(name='$name', seed=$seed)"
    }
}

class ComplexGenerator(seed: Long) : AbstractWorldGenerator("ComplexGenerator", seed) {
    override fun getLayerGenerator(chunk: Chunk): LayerGenerator {
        return when {
            chunk.y in 1..2 -> SurfaceLayerGenerator
            chunk.y in -3..0 -> CaveLayerGenerator
            else -> SkyLayerGenerator
        }
    }

    object SurfaceLayerGenerator : LayerGenerator {
        override fun getMinimumChunkY() = 1

        override fun getMaximumChunkY() = 2

        override fun getChunkGenerator(worldGenerator: WorldGenerator, chunk: Chunk): ChunkGenerator {
            return object : ChunkGenerator {
                override fun generateChunk(layerGenerator: LayerGenerator, chunk: Chunk) {
                    val blockArray = Array(Location.CHUNK_SIZE_CUBED) { Blocks.AIR }

                    // TODO (feature) Add trees and other plants

                    for (localX in Location.CHUNK_LENGTH) {
                        for (localZ in Location.CHUNK_LENGTH) {
                            val globalX = chunk.x * Location.CHUNK_SIZE + localX
                            val globalZ = chunk.z * Location.CHUNK_SIZE + localZ

                            val noise = (SimplexNoise.noise(globalX.toFloat() / 64F, globalZ.toFloat() / 64F) + 1) / 2F
                            val height: Int = ((Location.CHUNK_SIZE * layerGenerator.getHeight()) * noise).toInt()

                            for (localY in 0 until Location.CHUNK_SIZE) {
                                val layerY = (chunk.y - layerGenerator.getMinimumChunkY()) * Location.CHUNK_SIZE + localY
                                val blockIndex = Chunk.getIndexForPosition(localX, localY, localZ)

                                val globalY = chunk.y * Location.CHUNK_SIZE + localY
                                val gravelNoise = (SimplexNoise.noise(globalX.toFloat() / 16F, globalY.toFloat() / 16F, globalZ.toFloat() / 16F) + 1) / 2F

                                val waterBiomeNoise = (SimplexNoise.noise(globalX.toFloat() / 256F, globalZ.toFloat() / 256F) + 1) / 2F

                                blockArray[blockIndex] = when {
                                    layerY <= height && gravelNoise >= 0.95 -> Blocks.GRAVEL

                                    layerY == height -> Blocks.GRASS

                                    layerY == height + 1 && Random().nextFloat() <= 0.005f -> Blocks.PUMPKIN

                                    layerY > height - 2 && layerY < height -> Blocks.DIRT

                                    layerY < height -> Blocks.STONE

                                    waterBiomeNoise >= 0.5 && globalY <= 4 -> Blocks.WATER

                                    else -> Blocks.AIR
                                }
                            }
                        }
                    }

                    chunk.generateBlocks { blockArray[it] }
                }
            }
        }
    }

    object CaveLayerGenerator : LayerGenerator {
        override fun getMinimumChunkY() = -3

        override fun getMaximumChunkY() = 0

        override fun getChunkGenerator(worldGenerator: WorldGenerator, chunk: Chunk): ChunkGenerator {
            return object : ChunkGenerator {
                override fun generateChunk(layerGenerator: LayerGenerator, chunk: Chunk) {
                    // TODO (feature) Add ores, gravel.. and caves..
                    chunk.generateBlocks { Blocks.STONE }
                }
            }
        }
    }

    object SkyLayerGenerator : LayerGenerator {
        override fun getMinimumChunkY() = 5

        override fun getMaximumChunkY() = Int.MAX_VALUE

        override fun getChunkGenerator(worldGenerator: WorldGenerator, chunk: Chunk): ChunkGenerator {
            return object : ChunkGenerator {
                override fun generateChunk(layerGenerator: LayerGenerator, chunk: Chunk) {
                    // TODO (feature) Add clouds
                    chunk.generateBlocks { Blocks.AIR }
                }
            }
        }
    }
}

operator fun Vector3i.component1() = x
operator fun Vector3i.component2() = y
operator fun Vector3i.component3() = z