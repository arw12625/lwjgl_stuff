#version 330 core

in vec2 vertex_position;
in vec2 vertex_tex_coord;

out vec2 tex_coord;
out vec4 color;

void main() {
    gl_Position = vec4(vertex_position, 0,1);
    tex_coord = vertex_tex_coord;
    color = vec4(1,1,1,1);
}