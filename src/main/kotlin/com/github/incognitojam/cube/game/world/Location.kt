package com.github.incognitojam.cube.game.world

import com.github.incognitojam.cube.engine.maths.MathsUtils
import com.github.incognitojam.cube.game.block.Block
import com.github.incognitojam.cube.game.block.Blocks
import com.github.incognitojam.cube.game.world.chunk.Chunk
import org.joml.Vector3fc
import org.joml.Vector3i
import org.joml.Vector3ic

class Location(val world: World, val globalX: Int, val globalY: Int, val globalZ: Int) {

    val globalPosition: Vector3i
        get() = Vector3i(globalX, globalY, globalZ)

    val chunkX: Int = globalToChunk(globalX)

    val chunkY: Int = globalToChunk(globalY)

    val chunkZ: Int = globalToChunk(globalZ)

    val chunkPosition: Vector3i
        get() = Vector3i(chunkX, chunkY, chunkZ)

    val chunk: Chunk?
        get() = world.getChunk(chunkPosition)

    val localX: Int = globalToLocal(globalX)

    val localY: Int = globalToLocal(globalY)

    val localZ: Int = globalToLocal(globalZ)

    val localPosition: Vector3i
        get() = Vector3i(localX, localY, localZ)

    var blockData: Byte?
        get() = chunk?.getBlockId(localPosition)
        set(value) {
            value?.let { chunk?.setBlock(localPosition, it) }
        }

    var block: Block?
        get() = blockData?.let { Blocks.getBlockById(it) }
        set(value) {
            value?.let { chunk?.setBlock(localPosition, it.id) }
        }

    constructor(world: World, globalPosition: Vector3ic) : this(world, globalPosition.x(), globalPosition.y(), globalPosition.z())

    constructor(world: World, globalPosition: Vector3fc) : this(world, MathsUtils.floatVectorToIntVector(globalPosition))

    constructor(chunk: Chunk, localPosition: Vector3ic) : this(chunk.world, localToGlobal(chunk, localPosition))

    constructor(chunk: Chunk, localX: Int, localY: Int, localZ: Int) : this(chunk.world, localToGlobal(chunk, localX, localY, localZ))

    fun getAdjacent(direction: Direction): Location {
        return getRelative(direction.x, direction.y, direction.z)
    }

    fun getRelative(deltaX: Int, deltaY: Int, deltaZ: Int): Location {
        return Location(world, globalX + deltaX, globalY + deltaY, globalZ + deltaZ)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Location

        if (world != other.world) return false
        if (globalX != other.globalX) return false
        if (globalY != other.globalY) return false
        if (globalZ != other.globalZ) return false

        return true
    }

    override fun hashCode(): Int {
        var result = world.hashCode()
        result = 31 * result + globalX
        result = 31 * result + globalY
        result = 31 * result + globalZ
        return result
    }

    override fun toString(): String {
        return "Location(world=$world, globalX=$globalX, globalY=$globalY, globalZ=$globalZ, block=$block)"
    }

    companion object {
        const val CHUNK_SIZE: Int = 16
        const val CHUNK_SIZE_SQUARED: Int = CHUNK_SIZE * CHUNK_SIZE
        const val CHUNK_SIZE_CUBED: Int = CHUNK_SIZE_SQUARED * CHUNK_SIZE

        val CHUNK_LENGTH: IntRange = 0 until CHUNK_SIZE
        val CHUNK_AREA: IntRange = 0 until CHUNK_SIZE_SQUARED
        val CHUNK_VOLUME: IntRange = 0 until CHUNK_SIZE_CUBED

        fun globalToLocal(global: Int): Int {
            return Math.floorMod(global, CHUNK_SIZE)
        }

        fun globalToLocal(globalX: Int, globalY: Int, globalZ: Int): Vector3ic {
            return Vector3i(globalToLocal(globalX), globalToLocal(globalY), globalToLocal(globalZ))
        }

        fun globalToLocal(globalPosition: Vector3ic): Vector3ic {
            return globalToLocal(globalPosition.x(), globalPosition.y(), globalPosition.z())
        }

        fun globalToChunk(global: Int): Int {
            return Math.floorDiv(global, CHUNK_SIZE)
        }

        fun globalToChunk(globalX: Int, globalY: Int, globalZ: Int): Vector3ic {
            return Vector3i(globalToChunk(globalX), globalToChunk(globalY), globalToChunk(globalZ))
        }

        fun globalToChunk(globalPosition: Vector3ic): Vector3ic {
            return globalToChunk(globalPosition.x(), globalPosition.y(), globalPosition.z())
        }

        fun localToGlobal(chunk: Int, local: Int): Int {
            return (chunk * CHUNK_SIZE) + local
        }

        fun localToGlobal(chunk: Chunk, localX: Int, localY: Int, localZ: Int): Vector3ic {
            return Vector3i(localToGlobal(chunk.x, localX), localToGlobal(chunk.y, localY), localToGlobal(chunk.z, localZ)).toImmutable()
        }

        fun localToGlobal(chunk: Chunk, local: Vector3ic): Vector3ic {
            return localToGlobal(chunk, local.x(), local.y(), local.z())
        }

        fun chunkToGlobal(chunk: Int): Int {
            return chunk * CHUNK_SIZE
        }

        fun chunkToGlobal(chunkX: Int, chunkY: Int, chunkZ: Int): Vector3ic {
            return Vector3i(chunkToGlobal(chunkX), chunkToGlobal(chunkY), chunkToGlobal(chunkZ))
        }

        fun chunkToGlobal(chunkPosition: Vector3ic): Vector3ic {
            return chunkToGlobal(chunkPosition.x(), chunkPosition.y(), chunkPosition.z())
        }

        fun chunkToGlobal(chunk: Chunk): Vector3ic {
            return chunkToGlobal(chunk.x, chunk.y, chunk.z)
        }
    }

}