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

    fun onInitialise(player: EntityPlayer) {
        this.player = player

        spawnEntity(player)
        worldRenderer.onInitialise()

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
                    chunk.onInitialise()

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
        chunkMap.values.flatMap { it.values }.forEach { it.generateMesh() }
        println("\tFinished generating meshes in $meshTimer seconds.")

        println("Finished generating world in $worldTimer seconds.")
    }

    fun onUpdate(delta: Float) {
        chunkMap.values.flatMap { it.values }.forEach { it.onUpdate(delta) }

        val iterator = entities.iterator()
        while (iterator.hasNext()) {
            val entity = iterator.next()
            entity.onUpdate(delta)
            if (entity.dead) {
                iterator.remove()
                entity.onCleanup()
            }
        }
    }

    fun onRender(window: Window) {
        worldRenderer.onRender(window, player.camera, this)
    }

    fun onCleanup() {
        chunkMap.values.flatMap { it.values }.forEach { it.onCleanup() }
    }

    fun getChunks(): List<Chunk> {
        return chunkMap.values.flatMap { it.values }
    }

    fun getChunk(chunkPosition: Vector3i): Chunk? {
        return chunkMap[Vector2i(chunkPosition.x, chunkPosition.z)]?.get(chunkPosition.y)
    }

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
        entity.onInitialise()
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