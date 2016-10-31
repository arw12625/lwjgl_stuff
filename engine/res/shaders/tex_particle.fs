#version 330 core

in vec2 tex_coord;

out vec4 fragColor;

uniform sampler2D tex;

void main()
{
    fragColor = texture(tex, tex_coord);
}