#version 330 core

in vec2 tex_coord;
in vec4 color;
flat in int use_tex;

out vec4 fragColor;

uniform sampler2D tex;

void main()
{   
    fragColor = color;
    if(bool(use_tex)) {
        fragColor *= texture(tex, tex_coord);
    } 

}