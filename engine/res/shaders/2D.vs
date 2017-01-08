#version 330 core

in vec2 vertex_position;

uniform mat4 view_model;

void main() {
    gl_Position = view_model * vec4(vertex_position, 0,1);
}