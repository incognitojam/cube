package com.github.incognitojam.cube.game.entity

import com.github.incognitojam.cube.engine.graphics.ShaderProgram
import com.github.incognitojam.cube.engine.maths.clone
import com.github.incognitojam.cube.game.collider.Collider
import com.github.incognitojam.cube.game.world.Direction
import com.github.incognitojam.cube.game.world.Direction.*
import com.github.incognitojam.cube.game.world.Location
import com.github.incognitojam.cube.game.world.World
import org.joml.*
import java.util.*

open class EntityLiving(world: World, width: Float, height: Float, mass: Float, var health: Int) : Entity(world, width, height, mass) {

    val living: Boolean
        get() = health > 0

    override var dead: Boolean
        get() = !living
        set(value) {
            if (!value) health = 0
        }

}

open class EntityHungry(world: World, width: Float, height: Float, mass: Float, health: Int, var hunger: Int) : EntityLiving(world, width,
        height, mass, health) {

    val starving: Boolean
        get() = hunger == 0

}

open class Entity(val world: World, val width: Float, val height: Float, val mass: Float) {

    private var x: Float = 0f
    private var y: Float = 0f
    private var z: Float = 0f

    val position: Vector3f
        get() = Vector3f(x, y, z)

    private var yaw: Float = 0f
    private var pitch: Float = 0f

    val rotation: Vector2f
        get() = Vector2f(yaw, pitch)
    val rotationRadians: Vector3f
        get() = Vector3f(Math.toRadians(yaw.toDouble()).toFloat(), Math.toRadians(pitch.toDouble()).toFloat(), 0f)

    val blockPosition: Vector3i
        get() = Vector3i(Math.floor(position.x.toDouble()).toInt(),
                Math.floor(position.y.toDouble()).toInt(),
                Math.floor(position.z.toDouble()).toInt())
    val location: Location
        get() = Location(world, blockPosition)
    val groundLocation: Location
        get() = location.getAdjacent(DOWN)
    val collider = Collider(width, height)


    private val forces = ArrayList<Pair<Vector3f, Int>>()
    val velocity = Vector3f()
    val acceleration: Vector3fc
        get() = Vector3f(resultantForce).div(mass)
    val resultantForce = Vector3f()

    val momentum: Vector3f
            // p = mv
        get() = Vector3f(velocity.x * mass, velocity.y * mass, velocity.z * mass)
    val kineticEnergy: Float
            // Ek = (1/2)mv^2
        get() = .5f * mass * velocity.lengthSquared()

    open var dead = false
    var grounded = false

    open fun onInitialise() {
        collider.onInitialise()
    }

    private fun getRequiredForce(entity: Entity, targetVelocity: Vector3f, delta: Float): Vector3f {
        val dVelocity = targetVelocity.clone().sub(entity.velocity)
        val acceleration = dVelocity.div(delta)
        return acceleration.mul(mass)
    }

    open fun onUpdate(delta: Float) {
        // Check if a collision would be made if falling to determine if the entity is on the ground
        val tempPos = position.clone().apply { y -= 1 / 16f }
        val collidesWorld = collider.doesCollideWorld(tempPos, world)
        if (collidesWorld) {
            val collidingBlockPos = Vector3i(collider.collidingBlockPos)
            val direction = Direction.getDirection(collidingBlockPos.sub(blockPosition))
            val force = when (direction) {
                NORTH -> Vector3f(0f, 0f, -resultantForce.z)
                SOUTH -> Vector3f(0f, 0f, resultantForce.z)
                WEST -> Vector3f(-resultantForce.x, 0f, 0f)
                EAST -> Vector3f(resultantForce.x, 0f, 0f)
                DOWN -> Vector3f(0f, -resultantForce.y, 0f)
                UP -> Vector3f(0f, resultantForce.y, 0f)
            }

            addForce(force)
        }

        grounded = collidesWorld || collider.doesCollideEntity(this, tempPos, world) != null

        // Iterate all forces to calculate acceleration
        resultantForce.zero()
        val newForces = ArrayList<Pair<Vector3f, Int>>()
        forces.forEach { (force, duration) ->
            resultantForce.add(force)
            if (duration > 1) newForces.add(Pair(force, duration - 1))
        }
        forces.clear()
        forces.addAll(newForces)

        resultantForce.y -= World.G * mass

        // Calculate resistive force (friction from ground or air resistance above ground)
        val resistance: Float = if (grounded) {
            val frictionCoefficient = 0.50F
            val normalReaction = Math.max(0f, -resultantForce.y) // -weight, unless not grounded or jumping or something
            normalReaction * frictionCoefficient
        } else {
            val dragCoefficient = .01F
            val area = width * height

            velocity.lengthSquared() * dragCoefficient * area
        }

        // Add resistive forces to axes where there is a magnitude of velocity
        if (velocity.x != 0F) resultantForce.x += (if (velocity.x > 0) -1 else 1) * Math.min(Math.abs(velocity.x) * mass, resistance)
        if (velocity.y != 0F) resultantForce.y += (if (velocity.y > 0) -1 else 1) * Math.min(Math.abs(velocity.y) * mass, resistance)
        if (velocity.z != 0F) resultantForce.z += (if (velocity.z > 0) -1 else 1) * Math.min(Math.abs(velocity.z) * mass, resistance)

        // Apply the current acceleration to velocity
        velocity.add(Vector3f(acceleration).mul(delta))

        // If the velocity is too small set it to zero and return
        if (velocity.lengthSquared() < 1E-4) {
            velocity.zero()
            return
        }

        // If the player is on the ground cancel any negative vertical velocity
        if (grounded && velocity.y < 0f) velocity.y = 0f

        // Attempt to move by the current velocity
        if (move(velocity.x * delta, velocity.y * delta, velocity.z * delta)) {
            // If the move was successful notify the entity of the position change
            onPositionChange()
        }
    }

    open fun onRender(shader: ShaderProgram, modelViewMatrix: Matrix4f) {

    }

    open fun onCleanup() {
        collider.onCleanup()
    }

    private fun move(deltaX: Float, deltaY: Float, deltaZ: Float): Boolean {
        var positionChanged = false

        if (deltaX != 0F) {
            positionChanged = _move(Vector3f(deltaX, 0f, 0f))
        }

        if (deltaY != 0F) {
            positionChanged = _move(Vector3f(0f, deltaY, 0f)) || positionChanged
        }

        if (deltaZ != 0F) {
            positionChanged = _move(Vector3f(0f, 0f, deltaZ)) || positionChanged
        }

        return positionChanged
    }

    private fun _move(delta: Vector3f): Boolean {
        var temporaryDelta = delta
        var collides = true
        var minimumLength: Boolean
        while (true) {
            minimumLength = temporaryDelta.length() >= (1 / 1024F)
            if (!minimumLength) break
            val newPosition = position.clone().add(temporaryDelta)
            collides = collider.doesCollideWorld(newPosition, world) || collider.doesCollideEntity(this, newPosition, world) != null
            if (!collides) break
            temporaryDelta /= 2F
        }

        // TODO (improvement) Check that movement does not pass through obstructions too

        if (!collides) {
            x += temporaryDelta.x
            y += temporaryDelta.y
            z += temporaryDelta.z
        }
        return !collides
    }

    fun setPositionWithoutColliding(x: Float, y: Float, z: Float) {
        this.x = x
        this.y = y
        this.z = z
        onPositionChange()
    }

    fun resolveMovement(deltaX: Float, deltaY: Float, deltaZ: Float): Vector3f {
        if (deltaX == 0F && deltaY == 0F && deltaZ == 0F) return Vector3f()

        var modX = 0F
        var modZ = 0F
        if (deltaX != 0F) {
            modX += Math.sin(Math.toRadians(rotation.y.toDouble() - 90.0)).toFloat() * -1.0F * deltaX
            modZ += Math.cos(Math.toRadians(rotation.y.toDouble() - 90.0)).toFloat() * deltaX
        }
        if (deltaZ != 0F) {
            modX += Math.sin(Math.toRadians(rotation.y.toDouble())).toFloat() * -1.0F * deltaZ
            modZ += Math.cos(Math.toRadians(rotation.y.toDouble())).toFloat() * deltaZ
        }

        return Vector3f(modX, deltaY, modZ)
    }

    open fun onPositionChange() = Unit

    fun setRotation(yaw: Float, pitch: Float) {
        this.yaw = yaw
        this.pitch = pitch

        while (this.yaw >= 360F) this.yaw -= 360F
        while (this.pitch >= 360F) this.pitch -= 360F
        while (this.pitch < 0F) this.pitch += 360F
        while (this.pitch < 0F) this.pitch += 360F

        onRotationChange()
    }

    fun setRotation(rotation: Vector2f) = setRotation(rotation.x, rotation.y)

    fun addRotation(deltaX: Float, deltaY: Float) = setRotation(rotation.x + deltaX, rotation.y + deltaY)

    open fun onRotationChange() = Unit

    fun addForce(force: Vector3f, duration: Int = 1) {
        forces.add(Pair(force, duration))
    }

    fun addForce(forceX: Float, forceY: Float, forceZ: Float, duration: Int = 1) {
        addForce(Vector3f(forceX, forceY, forceZ), duration)
    }

    override fun toString(): String {
        return "Entity(world=$world, width=$width, height=$height, mass=$mass, position=$position, rotation=$rotation, velocity=$velocity, acceleration=$acceleration)"
    }

}