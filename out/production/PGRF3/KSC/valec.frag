#version 150
in vec2 texCoord;
out vec4 outColor; // output from the fragment shader
uniform sampler2D texture1;



void main() {
        outColor = vec4(1,0,0.0,1.0);
}