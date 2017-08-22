package com.github.incognitojam.cube.game.world

import com.github.incognitojam.cube.engine.Window
import com.github.incognitojam.cube.engine.time.Timer
import com.github.incognitojam.cube.game.GamCraft
import com.github.incognitojam.cube.game.entity.Entity
import com.github.incognitojam.cube.game.entity.EntityItem
import com.github.incognitojam.cube.game.entity.EntityPlayer
import com.github.incognitojam.cube.game.inventory.ItemStack
import com.github.incognitojam.cube.game.world.chunk.Chunk
import com.github.incognitojam.cube.game.world.generator.ComplexGenerator
import org.joml.Vector2i
import org.joml.Vector3f
import org.joml.Vector3i
import java.util.*

class World(val name: String, val seed: Long) {

    private val chunkMap = HashMap<Vector2i, HashMap<Int, Chunk>>()
    private val chunkGenerationQueue = ArrayDeque<Vector3i>()
    private val meshGenerationQueue = ArrayDeque<Vector3i>()

    val worldRenderer = WorldRenderer()
    val generator = ComplexGenerator(seed)

    lateinit var player: EntityPlayer
    private val entities = ArrayList<Entity>()

    fun initialise(player: EntityPlayer) {
        this.player = player

        spawnEntity(player)
        worldRenderer.initialise()

        // Generate the world
        println("Generating world..")
        val worldTimer = Timer.start("generateWorld")

        println("\tInitializing chunks..")
        val chunkTimer = Timer.start("generateWorld:initChunks")
        for (chunkX in -5..5) {
            for (chunkY in -3..5) {
                for (chunkZ in -5..5) {
                    val chunkPosition = Vector3i(chunkX, chunkY, chunkZ)
                    val chunk = Chunk(this, chunkPosition)
                    chunk.initialise()

                    val chunkHorizontal = Vector2i(chunkX, chunkZ)
                    val chunksVertical = chunkMap[chunkHorizontal] ?: HashMap()
                    chunksVertical[chunkY] = chunk
                    chunkMap[chunkHorizontal] = chunksVertical
                }
            }
        }
        println("\tFinished initializing in $chunkTimer seconds.")

        println("\tGenerating meshes..")
        val meshTimer = Timer.start("generateWorld:genMesh")
        chunkMap.values.flatMap { it.values }.forEach { it.buildMesh() }
        println("\tFinished generating meshes in $meshTimer seconds.")

        println("Finished generating world in $worldTimer seconds.")
    }

    fun update(delta: Float) {
        val playerChunk = player.location.chunk!!.chunkPosition
        forChunksInRadius(playerChunk, 5) { chunkPos, chunk ->
            if (chunk == null) {
                if (!chunkGenerationQueue.contains(chunkPos)) chunkGenerationQueue.addLast(chunkPos)
                return@forChunksInRadius
            }

            chunk.update(delta)
            if (chunk.isDirty() && !meshGenerationQueue.contains(chunkPos)) {
                meshGenerationQueue.addLast(chunkPos)
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

        var chunkCount = 5
        while (chunkCount > 0 && chunkGenerationQueue.isNotEmpty()) {
            val chunkPos = chunkGenerationQueue.pop()
            if (chunkMap[Vector2i(chunkPos.x, chunkPos.z)]?.containsKey(chunkPos.y) != true) {
                val chunk = Chunk(this, chunkPos)
                chunk.initialise()

                val chunkHorizontal = Vector2i(chunk.x, chunk.z)
                val chunksVertical = chunkMap[chunkHorizontal] ?: HashMap()
                chunksVertical[chunk.y] = chunk
                chunkMap[chunkHorizontal] = chunksVertical
            }
            chunkCount--
        }

        var meshCount = 10
        while (meshCount > 0 && meshGenerationQueue.isNotEmpty()) {
            val chunk = getChunk(meshGenerationQueue.pop())
            if (chunk != null && chunk.isDirty()) chunk.buildMesh()
            meshCount--
        }
    }

    fun render(window: Window) {
        worldRenderer.render(window, player.camera, this)
    }

    fun delete() {
        chunkMap.values.flatMap { it.values }.forEach { it.delete() }
    }

    fun getChunks(): List<Chunk> {
        return chunkMap.values.flatMap { it.values }
    }

    fun forChunksInRadius(origin: Vector3i, radius: Int, callback: (Vector3i, Chunk?) -> Unit) {
        for (dx in -radius..radius) for (dy in -radius..radius) for (dz in -radius..radius) {
            val chunkPos = Vector3i(origin).add(dx, dy, dz)
            callback.invoke(chunkPos, getChunk(chunkPos))
        }
    }

    fun getChunk(chunkX: Int, chunkY: Int, chunkZ: Int) = chunkMap[Vector2i(chunkX, chunkZ)]?.get(chunkY)

    fun getChunk(chunkPosition: Vector3i) = getChunk(chunkPosition.x, chunkPosition.y, chunkPosition.z)

    fun getMaximumBlock(globalX: Int, globalZ: Int): Location? {
        val chunkPos = Vector2i(Location.globalToChunk(globalX), Location.globalToChunk(globalZ))
        val chunks = chunkMap[chunkPos]?.values ?: return null
        val localPos = Vector2i(Location.globalToLocal(globalX), Location.globalToLocal(globalZ))

        var highestChunk: Chunk? = null
        var localY = -1
        for (chunk in chunks.sortedByDescending { it.y }) {
            highestChunk = chunk
            localY = chunk.getMaximumHeight(localPos.x, localPos.y)
            if (localY != -1) break
        }
        if (localY == -1 || highestChunk == null) return null
        val globalY = Location.localToGlobal(highestChunk.y, localY)
        return Location(this, globalX, globalY, globalZ)
    }

    fun getEntities(): List<Entity> {
        return entities
    }

    fun getEntitiesInRadius(position: Vector3f, radius: Float): List<Entity> {
        val radiusSquared = radius * radius
        return entities.filter { it.position.distanceSquared(position) <= radiusSquared }
    }

    fun <T : Entity> spawnEntity(entity: T): T {
        entities.add(entity)
        entity.initialise()
        return entity
    }

    fun dropItem(itemStack: ItemStack, position: Vector3f): EntityItem {
        val entityItem = EntityItem(this, itemStack)
        entityItem.setPositionWithoutColliding(position.x, position.y, position.z)
        entityItem.addForce(0f, GamCraft.ENTITY_ITEM_JUMP_FORCE, 0f, 2)
        return spawnEntity(entityItem)
    }

    override fun toString(): String {
        return "World(name='$name', seed=$seed, generator=$generator)"
    }
    companion object {
        const val G = 9.81F
    }

}