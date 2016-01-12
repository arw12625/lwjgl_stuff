#version 330 core
#define MAX_LIGHTS 16

uniform mat4 proj_view_model;
uniform mat4 view_model;
uniform mat3 normal_mat;

in vec3 vertex_position;
in vec3 vertex_normal;
in vec2 vertex_tex_coord;

out vec4 position;
out vec3 normal;
out vec2 tex_coord;

void main()
{
    position = view_model * vec4(vertex_position,1);
    normal = normalize((normal_mat * vertex_normal));
    tex_coord = vertex_tex_coord;

    gl_Position = proj_view_model * vec4(vertex_position,1);
}


