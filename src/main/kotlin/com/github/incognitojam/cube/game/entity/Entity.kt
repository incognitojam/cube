package com.github.incognitojam.cube.game.entity

import com.github.incognitojam.cube.engine.graphics.ShaderProgram
import com.github.incognitojam.cube.engine.maths.*
import com.github.incognitojam.cube.game.collider.Collider
import com.github.incognitojam.cube.game.world.Direction.DOWN
import com.github.incognitojam.cube.game.world.Location
import com.github.incognitojam.cube.game.world.World
import org.joml.*

abstract class Entity(val world: World, val width: Float, val height: Float, private val mass: Float) {

    protected var x = 0f
    protected var y = 0f
    protected var z = 0f

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
    val rotationOffset: Vector3fc = Vector3f(width / 2f, 0f, width / 2f).negate()

    val blockPosition: Vector3ic
        get() = Vector3i(x.floorInt(), y.floorInt(), z.floorInt())
    val location: Location
        get() = Location(world, blockPosition)
    val groundLocation: Location
        get() = location.getAdjacent(DOWN)
    val collider = Collider(width, height)

    val velocity = Vector3f()

    open var dead = false
    var grounded = false

    open fun initialise() {
        collider.initialise()
    }

    open fun update(delta: Float) {
        // Check if a collision would be made if falling to determine if the entity is on the ground
        grounded = collider.doesCollide(this, Vector3f(position).sub(0f, 1 / 16f, 0f), world)

        if (grounded) {
            if (velocity.y < 0f) velocity.y = 0f
            velocity.x *= .9f
            velocity.z *= .9f
        } else {
            velocity.y -= 9.81f * delta
            velocity.mul(.99f)
        }

        // Attempt to move by the current velocity
        val deltaPosition = move(velocity.x * delta, velocity.y * delta, velocity.z * delta)
        if (deltaPosition != null) {
            // If the move was successful notify the entity of the position change
            onPositionChange(deltaPosition.x(), deltaPosition.y(), deltaPosition.z())
        }
    }

    open fun render(shader: ShaderProgram, modelViewMatrix: Matrix4f) = Unit

    open fun delete() = collider.delete()

    private fun move(deltaX: Float, deltaY: Float, deltaZ: Float): Vector3fc? {
        val deltaPosition = Vector3f()

        if (deltaX != 0f) {
            val deltaPos = Vector3f(deltaX, 0f, 0f)
            if (_move(deltaPos)) deltaPosition.add(deltaPos)
        }
        if (deltaY != 0f) {
            val deltaPos = Vector3f(0f, deltaY, 0f)
            if (_move(deltaPos)) deltaPosition.add(deltaPos)
        }
        if (deltaZ != 0f) {
            val deltaPos = Vector3f(0f, 0f, deltaZ)
            if (_move(deltaPos)) deltaPosition.add(deltaPos)
        }

        return deltaPosition
    }

    private fun _move(delta: Vector3fc): Boolean {
        val temporaryDelta = Vector3f(delta)
        var collides = true
        var minimumLength: Boolean
        while (true) {
            minimumLength = temporaryDelta.length() >= (1 / 1024F)
            if (!minimumLength) break
            val newPosition = Vector3f(position).add(temporaryDelta)
            collides = collider.doesCollide(this, newPosition, world)
            if (!collides) break
            temporaryDelta.div(2f)
        }

        // TODO (improvement) Check that movement does not pass through obstructions too

        return if (!collides) {
            x += temporaryDelta.x
            y += temporaryDelta.y
            z += temporaryDelta.z
            true
        } else {
            velocity.sub(delta)
            false
        }
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

    open fun resolveMovement(deltaX: Float, deltaY: Float, deltaZ: Float): Vector3fc {
        var modX = 0f
        var modZ = 0f
        if (deltaX != 0f) {
            modX += (yaw - 90f).toRadians().sin() * -1f * deltaX
            modZ += (yaw - 90f).toRadians().cos() * deltaX
        }
        if (deltaZ != 0f) {
            modX += yaw.toRadians().sin() * -1f * deltaZ
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

    fun setRotation(rotation: Vector2fc) = setRotation(rotation.x(), rotation.y())

    fun addRotation(deltaX: Float, deltaY: Float) = setRotation(yaw + deltaX, pitch + deltaY)

    open fun onRotationChange(deltaYaw: Float, deltaPitch: Float) = Unit

    override fun toString(): String {
        return "Entity(world=$world, width=$width, height=$height, mass=$mass, position=$position, rotation=$rotation, velocity=$velocity)"
    }

}