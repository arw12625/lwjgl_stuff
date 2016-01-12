#version 330 core

in vec2 tex_coord;

out vec4 fragColor;

uniform vec4 color;
uniform sampler2D tex;

void main()
{   
    fragColor = vec4(color.rgb, color.a * texture(tex, tex_coord).a);

}