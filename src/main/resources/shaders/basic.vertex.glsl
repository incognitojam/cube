#version 330

layout(location=0) in vec3 inVertexPosition;
layout(location=1) in vec3 inColour;

uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;

out vec3 passColour;

void main() {
    gl_Position = projectionMatrix * modelViewMatrix * vec4(inVertexPosition, 1.0);
    passColour = inColour;
}