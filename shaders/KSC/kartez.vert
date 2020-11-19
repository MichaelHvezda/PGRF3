#version 150

in vec2 inPosition; // input from the vertex buffer


uniform mat4 view;
uniform float objCount;
uniform float varObj;
uniform mat4 projection;
out vec2 texCoord;

const float PI = 3.1415;

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
    texCoord = inPosition;
    vec2 position = inPosition * 2 - 1;
    if(objCount==0){
        vec3 position3d = vec3(position.x,position.y,1)*vec3(sin(position.y)+cos(position.x),sin(position.y),cos(position.x));
        position3d = rotateYZ(position3d,-PI/2)/4;
        position3d = position3d + vec3(0,-1,0);
        gl_Position = projection * view * vec4(position3d, 1.0);
    }
    if(objCount==1){
        position = (position +1 )/2;
        vec3 position3d = vec3(pow(position.x,varObj),pow(position.y,varObj),position.x*sin(varObj)+cos(varObj)*position.y)/4;
        //position3d = rotateYZ(position3d,-PI/2);
        position3d = position3d + vec3(-1,-1,-0.5);
        gl_Position = projection * view * vec4(position3d, 1.0);
    }
}