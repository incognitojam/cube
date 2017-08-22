package com.github.incognitojam.cube.game.world.chunk

import com.github.incognitojam.cube.engine.graphics.ChunkMesh
import com.github.incognitojam.cube.engine.graphics.ChunkMeshBuilder
import com.github.incognitojam.cube.engine.graphics.WaterMesh
import com.github.incognitojam.cube.engine.graphics.WaterMeshBuilder
import com.github.incognitojam.cube.game.block.Block
import com.github.incognitojam.cube.game.block.Blocks
import com.github.incognitojam.cube.game.world.Direction
import com.github.incognitojam.cube.game.world.Location
import com.github.incognitojam.cube.game.world.World
import org.joml.Vector3f
import org.joml.Vector3i

class Chunk(val world: World, val chunkPosition: Vector3i) {

    val x: Int
        get() = chunkPosition.x
    val y: Int
        get() = chunkPosition.y
    val z: Int
        get() = chunkPosition.z

    val worldPos = Vector3f(chunkPosition).mul(Location.CHUNK_SIZE.toFloat())

    private var empty = false
    private val blocks = Array(Location.CHUNK_SIZE_CUBED) { 0.toByte() }

    private var blockMeshDirty = true
    private var waterMeshDirty = true
    var blockMesh: ChunkMesh? = null
    var waterMesh: WaterMesh? = null

    fun initialise() {
        world.generator.generateChunk(this)
    }

    fun update(delta: Float) {
    }

    fun delete() {
        blockMesh?.delete()
        waterMesh?.delete()
    }

    fun generateBlocks(generator: (Int) -> Block) {
        empty = true
        for (index in Location.CHUNK_VOLUME) {
            val block = generator.invoke(index)
            if (empty && block.visible) empty = false
            blocks[index] = block.id
        }

        if (!empty) {
            blockMeshDirty = true
            waterMeshDirty = true
        }
    }

    fun buildMesh() {
        if (empty) {
            blockMesh?.delete()
            blockMesh = null
            waterMesh?.delete()
            waterMesh = null

            blockMeshDirty = false
            waterMeshDirty = false
            return
        }

        if (!blockMeshDirty && !waterMeshDirty) return

        val blockMeshBuilder = ChunkMeshBuilder()
        val waterMeshBuilder = WaterMeshBuilder()

        for (blockIndex in 0 until Location.CHUNK_SIZE_CUBED) {
            val blockId = getBlockId(blockIndex)!!
            val block = Blocks.getBlockById(blockId)
            val localPosition = getPositionForIndex(blockIndex)
            val localX = localPosition.x
            val localY = localPosition.y
            val localZ = localPosition.z
            val location = Location(this, localPosition)

            if (block != null && block.visible) {
                if (block == Blocks.WATER) {
                    val top = location.getAdjacent(Direction.UP).block
                    if (top != Blocks.WATER && (top == null || !top.opaque)) {
                        val textureCoordinates = block.getTextureCoordinates(Direction.UP)
                        waterMeshBuilder.addFace(localX, localY, localZ, ChunkMeshBuilder.WATER_TOP_VERTICES, textureCoordinates, ChunkMeshBuilder.LIGHT_TOP)
                    }
                } else {
                    var vegetationValue = -999F

                    val left = location.getAdjacent(Direction.LEFT).block?.opaque ?: false
                    val right = location.getAdjacent(Direction.RIGHT).block?.opaque ?: false
                    val bottom = location.getAdjacent(Direction.DOWN).block?.opaque ?: false
                    val top = location.getAdjacent(Direction.UP).block?.opaque ?: false
                    val back = location.getAdjacent(Direction.BACK).block?.opaque ?: false
                    val front = location.getAdjacent(Direction.FRONT).block?.opaque ?: false

                    if (!left) {
                        val textureCoordinates = block.getTextureCoordinates(Direction.LEFT)
                        blockMeshBuilder.addFace(localX, localY, localZ, ChunkMeshBuilder.LEFT_VERTICES, textureCoordinates, ChunkMeshBuilder.LIGHT_X, vegetationValue)
                    }

                    if (!right) {
                        val textureCoordinates = block.getTextureCoordinates(Direction.RIGHT)
                        blockMeshBuilder.addFace(localX, localY, localZ, ChunkMeshBuilder.RIGHT_VERTICES, textureCoordinates, ChunkMeshBuilder.LIGHT_X, vegetationValue)
                    }

                    if (!bottom) {
                        val textureCoordinates = block.getTextureCoordinates(Direction.DOWN)
                        blockMeshBuilder.addFace(localX, localY, localZ, ChunkMeshBuilder.BOTTOM_VERTICES, textureCoordinates, ChunkMeshBuilder.LIGHT_BOTTOM, vegetationValue)
                    }

                    if (!back) {
                        val textureCoordinates = block.getTextureCoordinates(Direction.BACK)
                        blockMeshBuilder.addFace(localX, localY, localZ, ChunkMeshBuilder.BACK_VERTICES, textureCoordinates, ChunkMeshBuilder.LIGHT_Z, vegetationValue)
                    }

                    if (!front) {
                        val textureCoordinates = block.getTextureCoordinates(Direction.FRONT)
                        blockMeshBuilder.addFace(localX, localY, localZ, ChunkMeshBuilder.FRONT_VERTICES, textureCoordinates, ChunkMeshBuilder.LIGHT_Z, vegetationValue)
                    }

                    if (!top) {
                        if (block == Blocks.GRASS) vegetationValue = 1.0f

                        val textureCoordinates = block.getTextureCoordinates(Direction.UP)
                        blockMeshBuilder.addFace(localX, localY, localZ, ChunkMeshBuilder.TOP_VERTICES, textureCoordinates, ChunkMeshBuilder.LIGHT_TOP, vegetationValue)
                    }
                }
            }
        }
        blockMesh = blockMeshBuilder.build(Blocks.getTextureMap())
        waterMesh = waterMeshBuilder.build(Blocks.getTextureMap())

        blockMeshDirty = false
        waterMeshDirty = false
    }

    fun setDirty() {
        blockMeshDirty = true
        waterMeshDirty = true
    }

    fun isDirty(): Boolean {
        return blockMeshDirty || waterMeshDirty
    }

    private fun getBlockId(index: Int): Byte? {
        return blocks.getOrNull(index)
    }

    fun getBlockId(localX: Int, localY: Int, localZ: Int): Byte? {
        return getBlockId(getIndexForPosition(localX, localY, localZ))
    }

    fun getBlockId(localPosition: Vector3i): Byte? {
        return getBlockId(localPosition.x, localPosition.y, localPosition.z)
    }

    fun getMaximumHeight(localX: Int, localZ: Int): Int {
        if (empty) return -1

        for (localY in (0 until Location.CHUNK_SIZE).sortedDescending()) {
            val blockData = getBlockId(localX, localY, localZ) ?: continue
            val block = Blocks.getBlockById(blockData)
            if (block?.visible == true) return localY
        }
        return -1
    }

    private fun setBlock(index: Int, blockId: Byte) {
        val previousBlockId = blocks[index]
        blocks[index] = blockId
        if (previousBlockId == Blocks.WATER.id || blockId == Blocks.WATER.id) {
            waterMeshDirty = true
        }
        if (previousBlockId != Blocks.WATER.id || blockId != Blocks.WATER.id) {
            blockMeshDirty = true
        }

        val localPosition = getPositionForIndex(index)
        val location = Location(this, localPosition)
        if (localPosition.x == 0) {
            location.getAdjacent(Direction.WEST).chunk?.setDirty()
        } else if (localPosition.x == 15) {
            location.getAdjacent(Direction.EAST).chunk?.setDirty()
        }

        if (localPosition.y == 0) {
            location.getAdjacent(Direction.DOWN).chunk?.setDirty()
        } else if (localPosition.y == 15) {
            location.getAdjacent(Direction.UP).chunk?.setDirty()
        }

        if (localPosition.z == 0) {
            location.getAdjacent(Direction.NORTH).chunk?.setDirty()
        } else if (localPosition.z == 15) {
            location.getAdjacent(Direction.SOUTH).chunk?.setDirty()
        }

        empty = !blocks.any { Blocks.getBlockById(it)?.visible == true }
    }

    fun setBlock(localX: Int, localY: Int, localZ: Int, blockId: Byte) {
        setBlock(getIndexForPosition(localX, localY, localZ), blockId)
    }

    fun setBlock(localPosition: Vector3i, blockId: Byte) {
        setBlock(localPosition.x, localPosition.y, localPosition.z, blockId)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Chunk

        if (world != other.world) return false
        if (chunkPosition != other.chunkPosition) return false

        return true
    }

    override fun hashCode(): Int {
        var result = world.hashCode()
        result = 31 * result + chunkPosition.hashCode()
        return result
    }

    override fun toString(): String {
        return "Chunk(world=$world, chunkPosition=$chunkPosition, blockMeshDirty=$blockMeshDirty, blockMesh=$blockMesh)"
    }

    companion object {
        fun getIndexForPosition(localX: Int, localY: Int, localZ: Int) =
                (localX * Location.CHUNK_SIZE_SQUARED) + (localY * Location.CHUNK_SIZE) + localZ

        fun getPositionForIndex(index: Int): Vector3i {
            val x = index / Location.CHUNK_SIZE_SQUARED
            val y = (index / Location.CHUNK_SIZE).rem(Location.CHUNK_SIZE)
            val z = index.rem(Location.CHUNK_SIZE)

            return Vector3i(x, y, z)
        }
    }

}