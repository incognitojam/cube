package com.github.incognitojam.cube.game.world

import com.github.incognitojam.cube.engine.Window
import com.github.incognitojam.cube.engine.time.Timer
import com.github.incognitojam.cube.game.entity.Entity
import com.github.incognitojam.cube.game.entity.EntityItem
import com.github.incognitojam.cube.game.entity.EntityPlayer
import com.github.incognitojam.cube.game.inventory.ItemStack
import com.github.incognitojam.cube.game.tileentity.TileEntity
import com.github.incognitojam.cube.game.world.chunk.Chunk
import com.github.incognitojam.cube.game.world.generator.ClassicGenerator
import org.joml.Vector3f
import org.joml.Vector3fc
import org.joml.Vector3i
import org.joml.Vector3ic
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class World(val name: String, private val worldHeight: Int) {

    private val chunkMap = ConcurrentHashMap<Vector3ic, Chunk>()
    private val generator = ClassicGenerator(worldHeight)
    private val chunkQueue = ArrayDeque<Vector3ic>()
    private val meshQueue = ArrayDeque<Vector3ic>()
    private lateinit var chunkGenerationThread: Thread
    private var running = true

    val worldRenderer = WorldRenderer()

    lateinit var player: EntityPlayer
    private val entities = ArrayList<Entity>()
    private val tileEntities = ArrayList<TileEntity>()

    fun initialise() {
        this.player = spawnEntity<EntityPlayer>().apply { initialise() }
        worldRenderer.initialise()

        CLOSEST_CHUNKS.forEach { chunkQueue.add(it) }

        chunkGenerationThread = Thread {
            while (running) {
                if (chunkQueue.size == 0) continue
                val pair = chunkQueue
                        .sortedBy { it.distance(player.location.chunkPosition) }
                        .take(5)
                        .map { Pair(it, Vector3f(it).mul(Location.CHUNK_SIZEF).add(Location.CHUNK_HALFF, Location.CHUNK_HALFF, Location.CHUNK_HALFF)) }
                        .sortedBy { it.second.distance(player.position) }
                        .first()
                val chunkPos = pair.first
                chunkQueue.remove(chunkPos)

                if (!chunkMap.containsKey(chunkPos)) {
                    val generateChunk = Timer.start("generate_chunk")

                    val chunk = Chunk(this, chunkPos)
                    generator.generateChunk(chunk)

                    chunkMap[chunkPos] = chunk

                    NEIGHBOURING_CHUNKS
                            .mapNotNull { getChunk(chunk.x + it.x(), chunk.y + it.y(), chunk.z + it.z()) }
                            .forEach { it.setDirty() }

                    println("Generated chunk $chunkPos in $generateChunk seconds")
                }
            }

            println("Stopping chunk generation thread..")
        }.apply { start() }

        while (getChunk(0, 0, 0) !is Chunk || getChunk(0, 1, 0) !is Chunk) Thread.sleep(25)
    }

    fun update(delta: Float) {
        val playerChunk = player.location.chunkPosition

//        CLOSEST_CHUNKS.map { playerChunk.clone().add(it) }.forEach { chunkPos ->
//            if (chunkPos.y < worldHeight || getChunk(chunkPos) is Chunk || chunkQueue.contains(chunkPos)) return@forEach
//            chunkQueue.addLast(chunkPos)
//        }

        forChunksInRadius(playerChunk, 3) { chunkPos, chunk ->
            if (chunk == null) {
//                if (!CLOSEST_CHUNKS.contains(chunkPos) && chunkPos.y < worldHeight && !chunkQueue.contains(chunkPos)) {
//                    chunkQueue.addLast(chunkPos)
//                }
                return@forChunksInRadius
            }

            chunk.update(delta)
            if (chunk.isDirty() && !meshQueue.contains(chunkPos)) {
                meshQueue.addLast(chunkPos)
            }
        }

        val iterator = entities.iterator()
        while (iterator.hasNext()) {
            val entity = iterator.next()
            entity.update(delta)
            if (entity.dead) {
                iterator.remove()
                entity.delete()
            }
        }

        var meshCount = 10
        while (meshCount > 0 && meshQueue.isNotEmpty()) {
            val chunk = getChunk(meshQueue.pop())
            if (chunk != null && chunk.isDirty()) chunk.buildMesh()
            meshCount--
        }
    }

    fun render(window: Window) {
        worldRenderer.render(window, player.camera, this)
    }

    fun delete() {
        running = false
        chunkGenerationThread.join(15000)
        chunkMap.values.forEach { it.delete() }
    }

    fun forChunksInRadius(origin: Vector3i, radius: Int, callback: (Vector3i, Chunk?) -> Unit) {
        for (dx in -radius..radius) for (dy in -radius..radius) for (dz in -radius..radius) {
            val chunkPos = Vector3i(origin).add(dx, dy, dz)
            callback.invoke(chunkPos, getChunk(chunkPos))
        }
    }

    fun getChunk(chunkX: Int, chunkY: Int, chunkZ: Int) = chunkMap[Vector3i(chunkX, chunkY, chunkZ)]

    fun getChunk(chunkPosition: Vector3ic) = getChunk(chunkPosition.x(), chunkPosition.y(), chunkPosition.z())

    fun getEntities(): List<Entity> {
        return entities
    }

    fun getEntitiesInRadius(position: Vector3fc, radius: Float): List<Entity> {
        val radiusSquared = radius * radius
        return entities.filter { it.position.distanceSquared(position) <= radiusSquared }
    }

    private inline fun <reified T : Entity> spawnEntity(): T {
        val clazz = T::class.java
        val constructor = clazz.getConstructor(World::class.java)
        val entity = constructor.newInstance(this)
        entities.add(entity)
        return entity
    }

    fun dropItem(itemStack: ItemStack, position: Vector3fc): EntityItem {
        val entityItem = spawnEntity<EntityItem>()
        entityItem.itemStack = itemStack
        entityItem.initialise()

        entityItem.setPositionWithoutColliding(position.x(), position.y(), position.z())
        entityItem.velocity.set(0f, EntityItem.JUMP_VELOCITY, 0f)
        return entityItem
    }

    override fun toString(): String {
        return "World(name='$name', generator=$generator)"
    }

    companion object {
        val CLOSEST_CHUNKS: Array<Vector3ic> = arrayOf(// 15 of them
                Vector3i(0, 0, 0),
                Vector3i(0, 1, 0),
                Vector3i(0, -1, 0),
                Vector3i(-1, 0, 0),
                Vector3i(1, 0, 0),
                Vector3i(0, 0, -1),
                Vector3i(0, 0, 1),
                Vector3i(-1, 1, 0),
                Vector3i(-1, -1, 0),
                Vector3i(1, 1, 0),
                Vector3i(1, -1, 0),
                Vector3i(0, 1, -1),
                Vector3i(0, -1, -1),
                Vector3i(0, 1, 1),
                Vector3i(0, -1, 1)
        )

        val NEIGHBOURING_CHUNKS: Array<Vector3ic> = arrayOf(
                Vector3i(-1, 0, 0),
                Vector3i(1, 0, 0),
                Vector3i(0, 0, -1),
                Vector3i(0, 0, 1),
                Vector3i(0, -1, 0),
                Vector3i(0, 1, 0)
        )
    }

}