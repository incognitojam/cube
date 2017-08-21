package com.github.incognitojam.cube.game.world

import org.joml.Vector3f
import org.joml.Vector3i

enum class Direction(val x: Int, val y: Int, val z: Int) {
    NORTH(0, 0, -1),
    EAST(1, 0, 0),
    SOUTH(0, 0, 1),
    WEST(-1, 0, 0),
    UP(0, 1, 0),
    DOWN(0, -1, 0);

    fun getOpposite() = getDirection(-x, -y, -z)

    fun toVector() = Vector3i(x, y, z)

    companion object {
        val FRONT = SOUTH
        val BACK = NORTH
        val LEFT = WEST
        val RIGHT = EAST

        fun getDirection(x: Int, y: Int, z: Int): Direction {
            return when {
                z == -1 -> NORTH
                x == 1 -> EAST
                z == 1 -> SOUTH
                x == -1 -> WEST
                y == 1 -> UP
                else -> DOWN
            }
        }

        fun getDirection(x: Float, y: Float, z: Float): Direction {
            val x1 = Math.abs(x) - Math.abs(y) - Math.abs(z)
            val y1 = Math.abs(y) - Math.abs(x) - Math.abs(z)
            val z1 = Math.abs(z) - Math.abs(y) - Math.abs(x)
            return if (x1 > y1 && x1 > z1) {
                getDirection(_direction(x), 0, 0)
            } else if (y1 > x1 && y1 > z1) {
                getDirection(0, _direction(y), 0)
            } else {
                getDirection(0, 0, _direction(z))
            }
        }

        fun getDirection(direction: Vector3f): Direction {
            return getDirection(direction.x, direction.y, direction.z)
        }

        private fun _direction(value: Float): Int {
            return if (value > 0) 1 else if (value < 0) -1 else 0
        }

        fun getDirection(direction: Vector3i): Direction {
            return getDirection(Vector3f(direction))
        }
    }
}