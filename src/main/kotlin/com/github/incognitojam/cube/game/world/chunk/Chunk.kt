package com.github.incognitojam.cube.game.world.chunk

import com.github.incognitojam.cube.engine.graphics.mesh.BlockMesh
import com.github.incognitojam.cube.engine.graphics.mesh.BlockMeshBuilder
import com.github.incognitojam.cube.engine.graphics.mesh.ChunkMesh
import com.github.incognitojam.cube.engine.graphics.mesh.ChunkMeshBuilder
import com.github.incognitojam.cube.game.block.Block
import com.github.incognitojam.cube.game.block.Blocks
import com.github.incognitojam.cube.game.world.Direction
import com.github.incognitojam.cube.game.world.Location
import com.github.incognitojam.cube.game.world.World
import org.joml.Vector3f
import org.joml.Vector3fc
import org.joml.Vector3i
import org.joml.Vector3ic

class Chunk(val world: World, val chunkPosition: Vector3ic) {

    val x: Int
        get() = chunkPosition.x()
    val y: Int
        get() = chunkPosition.y()
    val z: Int
        get() = chunkPosition.z()

    val worldPos: Vector3ic = Vector3i(chunkPosition).mul(Location.CHUNK_SIZE)
    val renderPos: Vector3fc = Vector3f(worldPos)

    private val blocks = Array(Location.CHUNK_SIZE_CUBED) { 0.toByte() }
    var empty = false

    private var blockMeshDirty = true
    private var waterMeshDirty = true
    var blockMesh: ChunkMesh? = null
    var waterMesh: BlockMesh? = null

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
            if (block.visible) empty = false
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
        val waterMeshBuilder = BlockMeshBuilder()

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
                        waterMeshBuilder.addFace(block, Direction.UP)
                    }
                } else {
                    Direction.values()
                            .filterNot { location.getAdjacent(it).block?.opaque == true }
                            .forEach {
                                val textureCoordinates = block.getTextureCoordinates(it)
                                val vertices = BlockMeshBuilder.getVertices(it)
                                val ambientLight = BlockMeshBuilder.getAmbientLight(it)

                                val vegetationValue = if (it == Direction.UP && block == Blocks.GRASS) {
//                                    world.getTileEntity(Location.localToGlobal(this, localX, localY, localZ)) as TileEntityGrass
                                    1f
                                } else {
                                    -1f
                                }

                                blockMeshBuilder.addFace(localX, localY, localZ, vertices, textureCoordinates,
                                        ambientLight, vegetationValue)
                            }
                }
            }
        }
        blockMesh = blockMeshBuilder.build()
        waterMesh = waterMeshBuilder.build()

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