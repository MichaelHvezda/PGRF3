#version 150

in vec2 inPosition; // input from the vertex buffer


uniform mat4 view;
uniform mat4 projection;
out vec2 texCoord;

void main() {
    /*texCoord = inPosition;
    vec2 position = inPosition * 2 - 1;
    vec3 pos3;
        pos3 = vec3(position,0);
        normal = vec3(position,0);

    gl_Position = projection * view * vec4(pos3, 1.0);

    vec4 pos4 = vec4(pos3, 1.0);
    light = lightPos - (view * pos4).xyz;
    //    light = lightPos - (mat3(view) * pos3);

    viewDirection = -(view * pos4).xyz;
*/
    //gl_Position = vec4(inPosition, 0.0, 1.0);
    //texCoord = inPosition;
    //texCoord = inPosition;
    //vec2 position = inPosition * 2 - 1;
    //gl_Position =  vec4(inPosition, 0.0, 1.0);
    texCoord = inPosition;
    vec2 position = inPosition * 2 - 1;
    gl_Position = projection * view * vec4(position, 0.0, 1.0);
}