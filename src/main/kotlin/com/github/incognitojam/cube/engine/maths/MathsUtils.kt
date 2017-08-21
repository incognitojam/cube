package com.github.incognitojam.cube.engine.maths

import org.joml.*

object MathsUtils {

    fun format(vector: Vector2f, digits: Int)
            = "x=" + format(vector.x, digits) + ", y=" + format(vector.y, digits)

    fun format(vector: Vector3f, digits: Int)
            = "x=" + format(vector.x, digits) + ", y=" + format(vector.y, digits) + ", z=" + format(vector.z, digits)

    fun format(vector: Vector3i)
            = "x=" + vector.x + ", y=" + vector.y + ", z=" + vector.z

    fun format(float: Float, digits: Int): String = java.lang.String.format("%.${digits}f", float)

    fun format(double: Double, digits: Int): String = java.lang.String.format("%.${digits}f", double)

    fun floatVectorToIntVector(floatPos: Vector3f): Vector3i {
        val x = Math.floor(floatPos.x.toDouble()).toInt()
        val y = Math.floor(floatPos.y.toDouble()).toInt()
        val z = Math.floor(floatPos.z.toDouble()).toInt()

        return Vector3i(x, y, z)
    }

}

fun Vector3f.clone(): Vector3f = Vector3f(this)

fun Vector3i.clone(): Vector3i = Vector3i(this)

fun Matrix4f.clone(): Matrix4f = Matrix4f(this)