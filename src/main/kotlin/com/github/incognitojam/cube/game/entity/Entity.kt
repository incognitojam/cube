package com.github.incognitojam.cube.game.entity

import com.github.incognitojam.cube.engine.graphics.ShaderProgram
import com.github.incognitojam.cube.engine.maths.*
import com.github.incognitojam.cube.game.collider.Collider
import com.github.incognitojam.cube.game.world.Direction.DOWN
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

    private var x = 0f
    private var y = 0f
    private var z = 0f

    val position: Vector3fc
        get() = Vector3f(x, y, z)

    private var yaw = 0f
        set(value) {
            field = MathsUtils.positiveModulo(value, 360f)
        }
    private var pitch = 0f
        set(value) {
            field = MathsUtils.positiveModulo(value, 360f)
        }

    val rotation: Vector2fc
        get() = Vector2f(yaw, pitch).toImmutable()
    val rotationRadians: Vector3fc
        get() = Vector3f(yaw.toRadians(), pitch.toRadians(), 0f)

    val blockPosition: Vector3ic
        get() = Vector3i(x.floorInt(), y.floorInt(), z.floorInt())
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
        get() = velocity.clone().mul(mass)
    val kineticEnergy: Float
        get() = .5f * mass * velocity.lengthSquared()

    open var dead = false
    var grounded = false

    open fun initialise() {
        collider.initialise()
    }

    private fun getRequiredForce(entity: Entity, targetVelocity: Vector3f, delta: Float): Vector3f {
        val dVelocity = targetVelocity.clone().sub(entity.velocity)
        val acceleration = dVelocity.div(delta)
        return acceleration.mul(mass)
    }

    open fun update(delta: Float) {
        // Check if a collision would be made if falling to determine if the entity is on the ground
        grounded = collider.doesCollide(this, Vector3f(position).sub(0f, 1 / 16f, 0f), world)

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
            val frictionCoefficient = 5F
            val normalReaction = Math.max(0f, -resultantForce.y) // -weight, unless not grounded or jumping or something
            normalReaction * frictionCoefficient
        } else {
            val dragCoefficient = .01F
            val area = width * height

            velocity.lengthSquared() * dragCoefficient * area
        }

        // Add resistive forces to axes where there is a magnitude of velocity
        if (velocity.x != 0F) resultantForce.x += Math.min(velocity.x.abs() * mass, resistance).copySign(-velocity.x)
        if (velocity.y != 0F) resultantForce.y += Math.min(velocity.y.abs() * mass, resistance).copySign(-velocity.y)
        if (velocity.z != 0F) resultantForce.z += Math.min(velocity.z.abs() * mass, resistance).copySign(-velocity.z)

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
        val oldX = x
        val oldY = y
        val oldZ = z
        if (move(velocity.x * delta, velocity.y * delta, velocity.z * delta)) {
            // If the move was successful notify the entity of the position change
            onPositionChange(x - oldX, y - oldY, z - oldZ)
        }
    }

    open fun render(shader: ShaderProgram, modelViewMatrix: Matrix4f) = Unit

    open fun delete() = collider.delete()

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
        var temporaryDelta = delta.clone()
        var collides = true
        var minimumLength: Boolean
        while (true) {
            minimumLength = temporaryDelta.length() >= (1 / 1024F)
            if (!minimumLength) break
            val newPosition = Vector3f(position).add(temporaryDelta)
            collides = collider.doesCollide(this, newPosition, world)
            if (!collides) break
            temporaryDelta /= 2F
        }

        // TODO (improvement) Check that movement does not pass through obstructions too

        if (!collides) {
            x += temporaryDelta.x
            y += temporaryDelta.y
            z += temporaryDelta.z
        } else {
            velocity.sub(delta)
        }
        return !collides
    }

    fun setPositionWithoutColliding(x: Float, y: Float, z: Float) {
        val oldX = this.x
        val oldY = this.y
        val oldZ = this.z
        this.x = x
        this.y = y
        this.z = z

        onPositionChange(x - oldX, y - oldY, z - oldZ)
    }

    open fun resolveMovement(deltaX: Float, deltaY: Float, deltaZ: Float): Vector3f {
        var modX = 0F
        var modZ = 0F
        if (deltaX != 0F) {
            modX += (yaw - 90f).toRadians().sin() * -1.0F * deltaX
            modZ += (yaw - 90f).toRadians().cos() * deltaX
        }
        if (deltaZ != 0F) {
            modX += yaw.toRadians().sin() * -1.0F * deltaZ
            modZ += yaw.toRadians().cos() * deltaZ
        }

        return Vector3f(modX, deltaY, modZ)
    }

    open fun onPositionChange(deltaX: Float, deltaY: Float, deltaZ: Float) = Unit

    fun setRotation(yaw: Float, pitch: Float) {
        val oldYaw = this.yaw
        val oldPitch = this.pitch
        this.yaw = yaw
        this.pitch = pitch

        onRotationChange(yaw - oldYaw, pitch - oldPitch)
    }

    fun setRotation(rotation: Vector2f) = setRotation(rotation.x, rotation.y)

    fun addRotation(deltaX: Float, deltaY: Float) = setRotation(yaw + deltaX, pitch + deltaY)

    open fun onRotationChange(deltaYaw: Float, deltaPitch: Float) = Unit

    fun addForce(force: Vector3f, duration: Int = 1) {
        forces.add(Pair(force, duration))
    }

    fun addForce(forceX: Float, forceY: Float, forceZ: Float, duration: Int = 1) = addForce(Vector3f(forceX, forceY, forceZ), duration)

    override fun toString(): String {
        return "Entity(world=$world, width=$width, height=$height, mass=$mass, position=$position, rotation=$rotation, velocity=$velocity, acceleration=$acceleration)"
    }

}