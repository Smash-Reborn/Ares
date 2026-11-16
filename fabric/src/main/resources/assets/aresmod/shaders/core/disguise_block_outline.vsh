#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:dynamictransforms.glsl>
#include <minecraft:projection.glsl>

layout(location = 0) in vec3 Position;
layout(location = 1) in vec4 Color;
layout(location = 2) in vec2 UV0;

layout(location = 0) out vec4 vertexColor;
layout(location = 1) out vec2 texCoord0;

void main() {
    vec4 view = ModelViewMat * vec4(Position, 1.0);
    // Cross planes on a mooshroom stick past the cow. Slide the glow away from
    // the camera along this view ray so a wall that covers the body covers the
    // glow too. The projected pixel stays put; only the depth changes.
    float distance = length(view.xyz);
    if (distance > 0.001) {
        view.xyz += view.xyz * (DISGUISE_BLOCK_PUSH / distance);
    }
    gl_Position = ProjMat * view;

    vertexColor = Color;
    texCoord0 = UV0;
}
