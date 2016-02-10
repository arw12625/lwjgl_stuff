#version 330 core

uniform mat4 proj;
uniform mat4 view;

in vec3 base;
in float isize;
in vec3 center;
in vec4 icolor;

out vec4 color;

void main() {
    gl_Position = proj * (vec4(isize * base, 0) + view * vec4(center, 1));
    color = icolor;
}