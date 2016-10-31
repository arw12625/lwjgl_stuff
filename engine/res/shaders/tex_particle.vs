#version 330 core

uniform mat4 proj;
uniform mat4 view_model;

in vec3 base;
in vec2 vertex_tex_coord;

in float isize;
in vec3 center;
in vec2 tex_offset;

out vec2 tex_coord;

void main() {
    gl_Position = proj * (vec4(isize * base, 0) + view_model * vec4(center, 1));
    tex_coord = vertex_tex_coord + tex_offset;
}