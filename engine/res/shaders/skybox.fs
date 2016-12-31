#version 330 core

in vec3 vertex;

out vec3 fragColor;

const vec3 dark = vec3(.2,.2,.5);
const vec3 light = vec3(.5,.5,.9);

void main()
{
    vec3 normVec = normalize(vertex);
    float a = (normVec.y + 1) / 2;
    fragColor = mix(dark, light,a) ;
}