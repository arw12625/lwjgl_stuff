#version 330 core

uniform mat4 proj_view_model;
in vec3 vertex_position;

void main()
{
    gl_Position = proj_view_model * vec4(vertex_position, 1);

}