#version 330

in vec2  passTextureCoord;
in float passAmbientLight;

out vec4 outColour;

uniform sampler2D textureSampler;

void main()
{
    outColour = texture(textureSampler, passTextureCoord) * passAmbientLight;
    outColour.a = 1;
}