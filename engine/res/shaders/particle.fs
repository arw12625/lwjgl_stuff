#version 330 core

in vec4 color;
in vec2 dist;

out vec4 fragColor;

void main()
{
    float d = dot(dist, dist);
    if(d > 1)
        discard;
    fragColor.rgb = color.rgb;

    fragColor.a = color.a * clamp(4 - 4 * d, 0, 1);
}