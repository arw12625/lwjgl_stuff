#version 330 core

uniform mat4 view_model;
uniform mat3 normal_mat;

struct DirLight {
    vec3 dir;
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
};
uniform DirLight dir_light;

vec3 calcLight(vec3 ambient, vec3 diffuse, vec3 specular, vec3 light_dir, vec3 normal, vec3 view_dir);

in vec4 vertex;
in vec3 normal;
in vec3 color;

out vec3 fragColor;

void main()
{
    vec3 normal_view = normalize((normal_mat * normal).xyz);
    vec3 view_dir = normalize((-view_model * vertex).xyz);

    vec3 result = calcLight(dir_light.ambient, dir_light.diffuse, dir_light.specular, normalize(dir_light.dir), normal_view, view_dir);

    result*=color;
    fragColor = result;
}


vec3 calcLight(vec3 ambient, vec3 diffuse, vec3 specular, vec3 light_dir, vec3 normalf, vec3 view_dir) {
    
    float diff = max(dot(normalf, -light_dir), 0.0);
    float spec = max(dot(view_dir, reflect(light_dir, normalf)), 0.0);

    return (ambient + diff*diffuse + spec * specular);

}
