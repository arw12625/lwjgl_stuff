#version 330 core

uniform mat4 proj_view_model;

in vec3 vertex_position;
in vec3 vertex_normal;
in vec3 vertex_color;

out vec4 vertex;
out vec3 normal;
out vec3 color;

void main()
{
    vertex = vec4(vertex_position, 1);
    gl_Position = proj_view_model * vertex;
    normal = vertex_normal;
    color = vertex_color;

}