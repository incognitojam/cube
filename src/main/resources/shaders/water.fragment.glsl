#version 330

out vec4 outColour;
in vec2  passTextureCoord;
in float passAmbientLight;

uniform sampler2D textureSampler;

void main()
{
    outColour = texture(textureSampler, passTextureCoord) * passAmbientLight;
    outColour.a = 0.3F;
}