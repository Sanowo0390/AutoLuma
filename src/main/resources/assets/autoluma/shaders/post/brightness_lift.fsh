#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D InSampler;

layout(location = 0) in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform LiftConfig {
    float Strength;
};

layout(location = 0) out vec4 fragColor;

void main() {
    vec4 source = texture(InSampler, texCoord);
    float amount = clamp(Strength, 0.0, 1.0);
    vec3 color = clamp(source.rgb, vec3(0.0), vec3(1.0));

    // Unlike a pure gamma lightmap, this lifts even black pixels.
    // Preserve texture contrast while preventing white clipping.
    vec3 corrected = pow(color, vec3(1.0 - 0.65 * amount));
    corrected += (1.0 - corrected) * (0.10 * amount);
    fragColor = vec4(clamp(corrected, 0.0, 1.0), source.a);
}
