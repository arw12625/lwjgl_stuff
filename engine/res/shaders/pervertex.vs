#version 330 core
#define MAX_LIGHTS 16

uniform mat4 proj_view_model;
uniform mat4 view_model;
uniform mat3 normal_mat;

struct DirLight {
    vec4 dir;
    vec4 ambient;
    vec4 diffuse;
    vec4 specular;
};

layout (std140) uniform lightBlock
{ 
  int num_lights; int pad1;int pad2;int pad3;
  DirLight[MAX_LIGHTS] lights;
};

vec3 calcLight(vec3 ambient, vec3 diffuse, vec3 specular, vec3 light_dir, vec3 normal, vec3 view_dir);

in vec3 vertex_position;
in vec3 vertex_normal;
const vec3 vertex_color = vec3(0.64, .231, .57);

out vec3 color;

void main()
{
    vec4 transformed_vert = proj_view_model * vec4(vertex_position,1);
    vec3 normal_view = normalize((normal_mat * vertex_normal).xyz);
    vec3 view_dir = normalize((-view_model * vec4(vertex_position,1))).xyz;

    vec3 result = vec3(0);
    for(int i = 0; i < num_lights; i++) {
        result += calcLight(vec3(lights[i].ambient), vec3(lights[i].diffuse), vec3(lights[i].specular), normalize(vec3(lights[i].dir)), normal_view, view_dir);
    }
    result*=vertex_color;
    color = result;
    gl_Position = transformed_vert;
}


vec3 calcLight(vec3 ambient, vec3 diffuse, vec3 specular, vec3 light_dir, vec3 normalf, vec3 view_dir) {
    
    float diff = max(dot(normalf, -light_dir), 0.0);
    float spec = max(dot(view_dir, reflect(light_dir, normalf)), 0.0);

    return (ambient + diff*diffuse + spec * specular);

}
