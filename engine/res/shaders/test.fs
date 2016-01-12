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

in vec3 vertex;
in vec3 normal;
in vec3 color;

out vec4 fragColor;

void main()
{
    vec3 normal_view = normal_mat * normal;
    vec3 view_dir = normalize(-view_model * vertex);

    vec3 result = calcLight(dir_light.ambient, dir_light.diffuse, dir_light.specular, dir_light.dir, normal_view, view_dir);

    result*=color;
    fragColor = vec4(result, 1);
}


vec3 calcLight(vec3 ambient, vec3 diffuse, vec3 specular, vec3 light_dir, vec3 normal, vec3 view_dir) {
    
    float diff = max(dot(normal, -light_dir), 0.0);
    float spec = max(dot(view_dir, reflect(light_dir, normal)), 0.0);

    return (ambient + diffuse * diff + specular * spec);

}
