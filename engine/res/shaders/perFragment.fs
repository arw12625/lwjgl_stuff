#version 330 core
#define MAX_LIGHTS 16

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

vec3 calcLight(vec3 ambient, vec3 diffuse, vec3 specular, vec3 light_dir, vec3 normal);

in vec4 position;
in vec3 normal;
in vec2 tex_coord;

out vec4 fragColor;

uniform sampler2D tex;

void main()
{
    vec3 finalColor;

    vec3 toView = normalize(-position.xyz);

    for(int i = 0; i < num_lights; i++) {
        vec3 lightDirIn = lights[i].dir.xyz;
        vec3 lightDirOut = reflect(lightDirIn, normal);
        
        vec3 amb = lights[i].ambient.xyz;
        
        float diffConst = max(dot(-normal, lightDirIn), 0);
        vec3 diff = lights[i].diffuse.xyz * diffConst;

        vec3 spec = lights[i].specular.xyz * max(dot(lightDirOut, toView), 0);
        
        finalColor += amb + diff + spec;
    }

    fragColor = texture(tex, tex_coord) * vec4(finalColor, 1);

}
