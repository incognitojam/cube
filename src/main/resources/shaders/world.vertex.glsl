#version 330

layout(location=0) in vec3  inVertexPosition;
layout(location=1) in vec2  inTextureCoord;
layout(location=2) in float inAmbientLight;
layout(location=3) in float inVegetationValue;

out vec2  passTextureCoord;
out float passAmbientLight;
out float passVegetationValue;

uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;

void main()
{
    gl_Position = projectionMatrix * modelViewMatrix * vec4(inVertexPosition, 1.0);
    passTextureCoord = inTextureCoord;
    passAmbientLight = inAmbientLight;
    passVegetationValue = inVegetationValue;
}