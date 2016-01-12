#version 330 core

in vec3 vertex_position;
const vec3 vertex_color = vec3(1);

out vec3 color;

void main()
{
    color = vec3(1,1,1);
    gl_Position = vec4(vertex_position, 1);
}