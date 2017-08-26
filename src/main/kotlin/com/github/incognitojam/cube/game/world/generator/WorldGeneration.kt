package com.github.incognitojam.cube.game.world.generator

import com.github.incognitojam.cube.engine.maths.component1
import com.github.incognitojam.cube.engine.maths.component2
import com.github.incognitojam.cube.engine.maths.floorInt
import com.github.incognitojam.cube.game.block.Block
import com.github.incognitojam.cube.game.block.Blocks
import com.github.incognitojam.cube.game.world.Location
import com.github.incognitojam.cube.game.world.chunk.Chunk
import org.joml.SimplexNoise
import org.joml.Vector2i
import org.joml.Vector2ic
import kotlin.collections.set

object NoiseGenerator {

    private fun getNoise(x: Int, z: Int, scale: Int): Float {
        val scaledX = x.toFloat() / scale
        val scaledZ = z.toFloat() / scale
        return (1f + SimplexNoise.noise(scaledX, scaledZ)) / 2f
    }

    fun getHeight(globalX: Int, globalZ: Int, scale: Int, maxHeight: Int): Int {
        return (getNoise(globalX, globalZ, scale) * maxHeight).floorInt()
    }

    fun getBiomeValue(chunkX: Int, chunkZ: Int): Float {
        return getNoise(chunkX, chunkZ, 4)
    }

}

abstract class AbstractWorldGenerator(val worldHeight: Int) {

    private val levels = HashMap<IntRange, AbstractLevel>()

    fun generateChunk(chunk: Chunk) {
        val biome = getLayer(chunk.y)?.getBiome(chunk) ?: EmptyBiome()
        biome.generateChunk(chunk)
    }

    protected fun addLevel(levelRange: IntRange, level: AbstractLevel) {
        if (levelRange.endInclusive >= worldHeight) error("Breached world height!")
        if (levelRange.count() == 0) error("Level must be at least one chunk tall!")

        levels[levelRange] = level
        level.heightChunks = levelRange.count()
        level.addBiomes()
        level.updateBiomeWeightsSum()
    }

    fun getLayerRange(level: AbstractLevel): IntRange {
        return levels.entries.first { it.value == level }.key
    }

    private fun getLayer(chunkY: Int): AbstractLevel? {
        return levels.entries.firstOrNull { chunkY in it.key }?.value
    }

}

abstract class AbstractLevel(private val worldGenerator: AbstractWorldGenerator, val name: String) {

    private val biomes = ArrayList<Pair<AbstractBiome, Double>>()
    private val biomeMap = HashMap<Vector2ic, Int>()
    private var biomeWeightsSum = 0.0

    var heightChunks: Int = 0
    val heightBlocks: Int
        get() = Location.CHUNK_SIZE * heightChunks

    open fun getBiome(chunk: Chunk): AbstractBiome {
        return getBiome(chunk.x, chunk.z)
    }

    private fun getBiome(chunkX: Int, chunkZ: Int): AbstractBiome {
        return getBiome(Vector2i(chunkX, chunkZ))
    }

    private fun getBiome(chunkHorizontal: Vector2ic): AbstractBiome {
        val biomeIndex = getBiomeIndex(chunkHorizontal)
        return biomes[biomeIndex].first
    }

    private fun getBiomeIndex(chunkHorizontal: Vector2ic): Int {
        return biomeMap[chunkHorizontal] ?: run {
            calculateBiomeIndex(chunkHorizontal).apply {
                biomeMap[chunkHorizontal] = this
            }
        }
    }

    fun getLevelMinimumY(): Int {
        return Location.CHUNK_SIZE * worldGenerator.getLayerRange(this).start
    }

    fun getChunkRelativeMinimumY(chunkY: Int): Int {
        return Location.CHUNK_SIZE * chunkY - getLevelMinimumY()
    }

//    fun getLevelYOffset(chunkY: Int): Int {
//        return Location.CHUNK_SIZE * (chunkY - worldGenerator.getLayerRange(this).start)
//    }

    fun getSurfaceHeight(globalX: Int, globalZ: Int, radius: Int = 2): Int {
        val (localX, localZ) = Location.globalToLocal(globalX, globalZ)

        val max = Location.CHUNK_SIZE - radius
        val left = localX < radius
        val right = !left && localX > max
        val forward = localZ < radius
        val backward = !forward && localZ > max
        if (left || right || forward || backward) {
            val chunkHorizontal = Location.globalToChunk(globalX, globalZ)
            var chunkX = chunkHorizontal.x()
            var chunkZ = chunkHorizontal.y()
            if (left) chunkX -= 1 else if (right) chunkX += 1
            if (forward) chunkZ -= 1 else if (backward) chunkZ += 1

            val biomeIndex = getBiomeIndex(chunkHorizontal)
            val newBiomeIndex = getBiomeIndex(Vector2i(chunkX, chunkZ))

            if (biomeIndex != newBiomeIndex) {
                return getAverageHeight(globalX, globalZ, radius)
            }
        }

        return getHeight(globalX, globalZ)
    }

    private fun getAverageHeight(globalX: Int, globalZ: Int, radius: Int): Int {
        val diameter = (2 * radius) + 1
        var totalHeight = 0
        for (modX in -radius..radius) {
            for (modZ in -radius..radius) {
                totalHeight += getHeight(globalX + modX, globalZ + modZ)
            }
        }
        return totalHeight / (diameter * diameter)
    }

    private fun getHeight(globalX: Int, globalZ: Int): Int {
        val chunkX = Location.globalToChunk(globalX)
        val chunkZ = Location.globalToChunk(globalZ)
        return getBiome(chunkX, chunkZ).getSurfaceHeight(globalX, globalZ)
    }

    open fun addBiomes() {
    }

    fun updateBiomeWeightsSum() {
        biomeWeightsSum = biomes.sumByDouble { it.second }
    }

    protected fun addBiome(biome: AbstractBiome, weight: Double) {
        biomes.add(Pair(biome, weight))
    }

    private fun calculateBiomeIndex(chunkHorizontal: Vector2ic): Int {
        if (biomes.size == 1) return 0

        val (chunkX, chunkZ) = chunkHorizontal
        val biomesSortedByWeight = biomes.mapIndexed { index, value -> Pair(index, value.second) }.sortedBy { it.second }
        var biomeValue = NoiseGenerator.getBiomeValue(chunkX, chunkZ) * biomeWeightsSum

        for ((index, weight) in biomesSortedByWeight) {
            biomeValue -= weight
            if (biomeValue <= 0) return index
        }

        error("Failed to find a biome! this=$this")
    }

}

abstract class AbstractBiome(val name: String) {
    fun generateChunk(chunk: Chunk) {
        chunk.generateBlocks { getChunkBlocks(chunk)[it] }
    }

    abstract fun getSurfaceHeight(globalX: Int, globalZ: Int): Int

    protected abstract fun getChunkBlocks(chunk: Chunk): Array<Block>

    override fun toString(): String {
        return "AbstractBiome(name='$name')"
    }
}

class EmptyBiome: AbstractBiome("Empty") {
    override fun getChunkBlocks(chunk: Chunk): Array<Block> {
        return Array(Location.CHUNK_SIZE_CUBED) { Blocks.AIR }
    }

    override fun getSurfaceHeight(globalX: Int, globalZ: Int) = 0
}
