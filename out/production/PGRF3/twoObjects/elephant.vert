#version 450
in vec3 inPosition; // input from the vertex buffer
in vec2 inTexCoord; // input from the vertex buffer
in vec3 inNormal; // input from the vertex buffer
out vec3 vertColor; // output from this shader to the next pipleline stage
uniform mat4 view;
uniform mat4 projection;
uniform vec3 lightPos;
uniform vec3 cameraPos;
out vec3 light;
out vec3 viewDirection;
out vec3 fragPosition;
out vec3 camera;

const float otoceni = 3.1415;

vec3 rotateXY(vec3 pos,float oto){
    return vec3(
    pos.x*cos(oto)-pos.y*sin(oto),
    pos.x*sin(oto)+pos.y*cos(oto),
    pos.z);
}

vec3 rotateYZ(vec3 pos,float oto){
    return vec3(
    pos.x,
    pos.y*cos(oto)-pos.z*sin(oto),
    pos.y*sin(oto)+pos.z*cos(oto));
}

vec3 rotateXZ(vec3 pos,float oto){
    return vec3(
    pos.x*cos(oto)-pos.z*sin(oto),
    pos.y,
    pos.x*sin(oto)+pos.z*cos(oto));
}

void main() {

    //vertColor = inNormal * 0.5 + 0.5;
    //vertColor = vec3(inTexCoord ,inTexCoord.x);
    vec3 position = inPosition*0.025;
    position = rotateYZ(position,otoceni);
    position = rotateXY(position,otoceni);

    //position = vec3(position.x + 5,position.y,position.z);

    fragPosition =position;

    light = lightPos - position;
    camera = cameraPos - position;
    viewDirection = -( vec4(position,1.0)).xyz;
    gl_Position = projection * view * vec4(position, 1.0);
 //   outTexCoord =inTexCoord;
    //vertColor = inPosition*0.01;
}