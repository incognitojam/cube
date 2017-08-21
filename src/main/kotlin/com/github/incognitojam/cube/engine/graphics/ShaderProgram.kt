package com.github.incognitojam.cube.engine.graphics

import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL20.*
import org.lwjgl.system.MemoryStack



class ShaderProgram @Throws(Exception::class) constructor() {

    private var programId: Int = 0
    private var vertexShaderId: Int = 0
    private var fragmentShaderId: Int = 0
    private val uniforms = HashMap<String, Int>()

    private var bound = false

    fun onInitialise() {
        programId = GL20.glCreateProgram()
        if (programId == 0) {
            throw Exception("Could not create Shader")
        }
    }

    @Throws(Exception::class)
    fun createUniform(uniformName: String) {
        val uniformLocation = GL20.glGetUniformLocation(programId, uniformName)
        if (uniformLocation < 0) {
            throw Exception("Could not find uniform: $uniformName")
        }
        uniforms[uniformName] = uniformLocation
    }

    fun setUniform(uniformName: String, value: Matrix4f) {
        val uniformLocation = uniforms[uniformName] ?: return
        val stack = MemoryStack.stackPush()

        // Dump the matrix into a float buffer
        val floatBuffer = stack.mallocFloat(16)
        value.get(floatBuffer)
        GL20.glUniformMatrix4fv(uniformLocation, false, floatBuffer)

        stack.close()
    }

    fun setUniform(uniformName: String, value: Int) {
        uniforms[uniformName]?.let { glUniform1i(it, value) }
    }

    fun setUniform(uniformName: String, value: Float) {
        uniforms[uniformName]?.let { glUniform1f(it, value) }
    }

    fun setUniform(uniformName: String, value: Vector3f) {
        uniforms[uniformName]?.let { glUniform3f(it, value.x, value.y, value.z) }
    }

    fun setUniform(uniformName: String, value: Vector4f) {
        uniforms[uniformName]?.let { glUniform4f(it, value.x, value.y, value.z, value.w) }
    }

    fun createVertexShader(shaderCode: String) {
        vertexShaderId = createShader(shaderCode, GL20.GL_VERTEX_SHADER)
    }

    fun createFragmentShader(shaderCode: String) {
        fragmentShaderId = createShader(shaderCode, GL20.GL_FRAGMENT_SHADER)
    }

    @Throws(Exception::class)
    private fun createShader(shaderCode: String, shaderType: Int): Int {
        val shaderId = GL20.glCreateShader(shaderType)
        if (shaderId == 0) {
            throw Exception("Error creating shader. Type: $shaderType")
        }

        GL20.glShaderSource(shaderId, shaderCode)
        GL20.glCompileShader(shaderId)

        if (GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS) == 0) {
            throw Exception("Error compiling Shader code: ${GL20.glGetShaderInfoLog(shaderId, 1024)}")
        }

        GL20.glAttachShader(programId, shaderId)
        return shaderId
    }

    @Throws(Exception::class)
    fun link() {
        GL20.glLinkProgram(programId)
        if (GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS) == 0) {
            throw Exception("Error linking Shader: ${GL20.glGetProgramInfoLog(programId, 1024)}")
        }

        if (vertexShaderId != 0) {
            GL20.glDetachShader(programId, vertexShaderId)
        }

        if (fragmentShaderId != 0) {
            GL20.glDetachShader(programId, fragmentShaderId)
        }

        GL20.glValidateProgram(programId)
        if (GL20.glGetProgrami(programId, GL20.GL_VALIDATE_STATUS) == 0) {
            println("Warning validating Shader code: ${GL20.glGetProgramInfoLog(programId, 1024)}")
        }
    }

    fun bind() {
        bound = true
        GL20.glUseProgram(programId)
    }

    fun unbind() {
        GL20.glUseProgram(0)
        bound = false
    }

    fun onCleanup() {
        unbind()
        if (programId != 0) {
            GL20.glDeleteProgram(programId)
        }
    }

}