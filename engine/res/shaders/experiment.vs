#version 330 core

uniform mat4 proj_view_model;

struct Light {
	vec3 position;
	vec3 color;
	float ambient;
	float diffuse;
};

uniform Light light;
in vec3 vertex_position;
in vec3 vertex_normal;

out vec3 color;

void main()
{
    gl_Position = proj_view_model * vec4(vertex_position, 1);
	
	vec3 dif = normalize(light.position-vertex_position);
	float n = dot(vertex_normal, dif);
	color = (light.ambient + light.diffuse * n) * light.color;

}