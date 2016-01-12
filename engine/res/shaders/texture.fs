#version 330 core

in vec2 tex_coord;
in vec4 color;

out vec4 fragColor;

uniform sampler2D tex;

void main()
{   
    fragColor = texture(tex, tex_coord) * color;

}