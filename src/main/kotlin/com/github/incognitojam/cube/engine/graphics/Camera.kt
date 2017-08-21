package com.github.incognitojam.cube.engine.graphics

import com.github.incognitojam.cube.engine.maths.MathsUtils
import com.github.incognitojam.cube.engine.maths.clone
import com.github.incognitojam.cube.game.block.Block
import com.github.incognitojam.cube.game.entity.Entity
import com.github.incognitojam.cube.game.world.Direction
import com.github.incognitojam.cube.game.world.Location
import com.github.incognitojam.cube.game.world.World
import org.joml.*

class Camera {

    private val viewMatrix = Matrix4f()
    private val tmp = Vector3f()

    private var x: Float = 0f
    private var y: Float = 0f
    private var z: Float = 0f

    val position: Vector3f
        get() = Vector3f(x, y, z)

    private var pitch: Float = 0f
    private var yaw: Float = 0f

    val rotation: Vector2f
        get() = Vector2f(pitch, yaw)

    val rotationRadians: Vector3f
        get() = Vector3f(Math.toRadians(yaw.toDouble()).toFloat(), Math.toRadians(pitch.toDouble()).toFloat(), 0f)

    val forwardRay: Vector3f
        get() = viewMatrix.positiveZ(tmp).negate()

    fun getViewMatrix(): Matrix4f {
        return Transformation.getViewMatrix(this, viewMatrix)
    }

    fun setPosition(position: Vector3f) {
        this.x = position.x
        this.y = position.y
        this.z = position.z
    }

    fun setPosition(x: Float, y: Float, z: Float) {
        this.x = x
        this.y = y
        this.z = z
    }

    fun setRotation(rotation: Vector2f) {
        this.yaw = rotation.x
        this.pitch = rotation.y
    }

    fun setRotation(yaw: Float, pitch: Float) {
        this.yaw = yaw
        this.pitch = pitch
    }

    fun castRayForBlock(radius: Float, world: World, callback: (Block) -> Boolean): Pair<Location, Direction>? {
        val result = raycastBlock(radius) { blockPos ->
            Location(world, blockPos).block?.let { callback.invoke(it) } ?: false
        } ?: return null
        return Pair(Location(world, result.first), result.second.getOpposite())
    }

    inline fun <reified T : Entity> castRayForEntity(radius: Float, world: World, callback: (T) -> Boolean): T? {
        val rayIncrement = 1 / 16f
        val ray = Vector3f(forwardRay).normalize(rayIncrement)
        val fractionalRay = Vector3f(ray).apply {
            x = 1f / x
            y = 1f / y
            z = 1f / z
        }

        val origin = Vector3f(position)

        val nearbyEntities = world.getEntitiesInRadius(origin, radius).filter { it is T }
        for (entity in nearbyEntities.sortedBy { it.position.distanceSquared(origin) }) {
            val collider = entity.collider

            val test1 = (collider.minPoint.x - origin.x) * fractionalRay.x
            val test2 = (collider.maxPoint.x - origin.x) * fractionalRay.x
            val test3 = (collider.minPoint.y - origin.y) * fractionalRay.y
            val test4 = (collider.maxPoint.y - origin.y) * fractionalRay.y
            val test5 = (collider.minPoint.z - origin.z) * fractionalRay.z
            val test6 = (collider.maxPoint.z - origin.z) * fractionalRay.z

            val testMin = Math.max(Math.max(Math.min(test1, test2), Math.min(test3, test4)), Math.min(test5, test6))
            val testMax = Math.min(Math.min(Math.max(test1, test2), Math.max(test3, test4)), Math.max(test5, test6))

            if (testMax >= 0 && testMin <= testMax && callback.invoke(entity as T)) return entity
        }

        return null
    }

    private fun raycastBlock(radius: Float, callback: (Vector3i) -> Boolean): Pair<Vector3i, Direction>? {
        var distance = 0f
        val rayIncrement = 1 / 1024f
        val ray = Vector3f(forwardRay).normalize(rayIncrement)
        var direction = Direction.UP

        val origin = Vector3f(position)
        val tempPos = origin.clone()
        val blockPos = MathsUtils.floatVectorToIntVector(origin)

        while (true) {
            if (callback.invoke(blockPos)) return Pair(blockPos, direction)
            if (distance >= radius) return null

            val newBlockPos = MathsUtils.floatVectorToIntVector(tempPos)
            while (blockPos == newBlockPos) {
                tempPos.add(ray)
                newBlockPos.set(MathsUtils.floatVectorToIntVector(tempPos))
                distance += rayIncrement
            }
            val difference = newBlockPos.clone().sub(blockPos)
            direction = Direction.getDirection(difference)
            blockPos.set(newBlockPos)
        }
    }

    override fun toString(): String {
        return "Camera(position=$position, rotation=$rotation)"
    }

}