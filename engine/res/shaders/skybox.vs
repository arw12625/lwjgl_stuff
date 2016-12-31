#version 330 core

in vec3 position;

uniform mat4 view_model;
uniform mat4 proj;

out vec3 vertex;

void main()
{
    vertex = position;
    
    gl_Position =  proj * vec4(mat3(view_model) * vertex, 1);

}