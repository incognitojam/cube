#version 330

in vec2  passTextureCoord;
in float passAmbientLight;
in float passVegetationValue;

out vec4 outColour;

uniform sampler2D textureSampler;

void main()
{
    outColour = texture(textureSampler, passTextureCoord) * passAmbientLight;
    outColour.a = 1;

    if (passVegetationValue >= 0) {
        vec4 grassBright = vec4(116.0 / 255.0, 177.0 / 255.0, 052.0 / 255.0, 255.0 / 255.0);
        vec4 grassDead   = vec4(145.0 / 255.0, 103.0 / 255.0, 025.0 / 255.0, 255.0 / 255.0);

        vec4 vegetationColour = (grassBright * passVegetationValue) + (grassDead * (1 - passVegetationValue));
        outColour *= vegetationColour;
    }
}