#version 330 core
#define MAX_LIGHTS 16

in vec3 position;

uniform mat4 proj_view_model;
void main()
{
    vec4 transformed_vert = proj_view_model * vec4(position,1);
    gl_Position = transformed_vert;
}
