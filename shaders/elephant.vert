#version 150
in vec3 inPosition; // input from the vertex buffer
in vec2 inTexCoord; // input from the vertex buffer
in vec3 inNormal; // input from the vertex buffer
out vec3 vertColor; // output from this shader to the next pipleline stage
uniform mat4 view;
uniform mat4 projection;
uniform vec3 lightPos;
out vec3 light;
out vec3 viewDirection;
out vec3 normal;

void main() {
    gl_Position = projection * view * vec4(inPosition*0.01, 1.0);
    //vertColor = inNormal * 0.5 + 0.5;
    vertColor = vec3(inTexCoord ,inTexCoord.x);
    normal = inNormal;
    light = lightPos - (view * vec4(inPosition,1.0)).xyz;
    viewDirection = -(view * vec4(inPosition,1.0)).xyz;
 //   outTexCoord =inTexCoord;
    //vertColor = inPosition*0.01;
}