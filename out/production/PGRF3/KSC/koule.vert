#version 150

in vec2 inPosition; // input from the vertex buffer


uniform mat4 view;
uniform float objCount;
uniform mat4 projection;
out vec2 texCoord;

const float PI = 3.1415;

vec3 getKoule(vec3 pos) {
    float az = pos.x +1;// souřadnice z gridu je v <-1;1> a chceme v rozsahu <-PI;PI>
    float ze = pos.y * PI+ PI;// souřadnice z gridu je v <-1;1> a chceme v rozsahu <-PI/2;PI/2>
    float r = pos.z;

    float x = r * cos(az) * cos(ze);
    float y = r * sin(az) * cos(ze);
    float z = r * sin(ze);

    return vec3(x, y, z);
}
vec3 getKruh(vec3 pos) {
    //float az = pos.x +1;// souřadnice z gridu je v <-1;1> a chceme v rozsahu <-PI;PI>
    float ze = pos.y * PI+ PI;// souřadnice z gridu je v <-1;1> a chceme v rozsahu <-PI/2;PI/2>
    float r = pos.x;

    float x = r * cos(ze);
    float y = r * sin(ze);
    float z = pos.z ;

    return vec3(x, y, z);
}

vec3 getValec(vec3 pos) {
    float az = pos.x +1;// souřadnice z gridu je v <-1;1> a chceme v rozsahu <-PI;PI>
    float ze = pos.y * PI+ PI;// souřadnice z gridu je v <-1;1> a chceme v rozsahu <-PI/2;PI/2>
    float r = pos.z;

    float x = r * cos(ze);
    float y = r * sin(ze);
    float z = az ;

    return vec3(x, y, z);
}

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
        position3d = getKruh(position3d);
        position3d = rotateYZ(position3d,-PI/2)/4;
        position3d = position3d + vec3(0,1,0);
        gl_Position = projection * view * vec4(position3d, 1.0);
    }
    if(objCount==1){
        vec3 position3d = vec3(position.x,position.y,1);
        position3d = getKruh(position3d);
        position3d = getKoule(position3d);
        position3d = rotateYZ(position3d,-PI/2)/4;
        position3d = position3d + vec3(-1,0,0);
        gl_Position = projection * view * vec4(position3d, 1.0);
    }

}