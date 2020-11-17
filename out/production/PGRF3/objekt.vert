#version 150

in vec2 inPosition; // input from the vertex buffer

uniform mat4 view;
uniform mat4 projection;
uniform vec3 lightPos;
uniform vec3 cameraPos;

out vec2 texCoord;
out vec3 light;
out vec3 viewDirection;
out vec3 normal;
out vec3 pixPos;
out vec3 camera;
const float PI = 3.1415;

vec3 getSphere(vec2 pos) {
    float az = pos.x * PI;// souřadnice z gridu je v <-1;1> a chceme v rozsahu <-PI;PI>
    float ze = pos.y * PI / 2;// souřadnice z gridu je v <-1;1> a chceme v rozsahu <-PI/2;PI/2>
    float r = 1;

    float x = r * cos(az) * cos(ze);
    float y = r * sin(az) * cos(ze);
    float z = r * sin(ze);

    return vec3(x, y, z);
}
vec3 getGrid(vec2 pos) {


    return vec3(pos.x, pos.y, 0);
}
//vec3 getNormalSphere(vec2 pos) {
//    float az = pos.x * PI;
//    float ze = pos.y * PI / 2;
//
//    vec3 u = vec3(-PI * sin(az) * cos(ze), PI * cos(az) * cos(ze), 0);
//    vec3 v = vec3(-PI/2 * cos(az) * sin(ze), -PI/2 * sin(az) * sin(ze), PI/2 * cos(ze));
//
//    return cross(u, v);
//}

vec3 getNormalSphere2(vec2 pos) {
    vec3 u = getSphere(pos + vec2(0.001, 0)) - getSphere(pos - vec2(0.001, 0));
    vec3 v = getSphere(pos + vec2(0, 0.001)) - getSphere(pos - vec2(0, 0.001));
    return cross(u, v);
}
vec3 getNormalGrid2(vec2 pos) {
    vec3 u = getGrid(pos + vec2(0.001, 0)) - getGrid(pos - vec2(0.001, 0));
    vec3 v = getGrid(pos + vec2(0, 0.001)) - getGrid(pos - vec2(0, 0.001));
    return cross(u, v);
}

//float getZ(vec2 pos) {
//    return sin(pos.x * 10);
//}

void main() {
    texCoord = inPosition;
    //    vec2 position = vec2(inPosition.x * 2 - 1, inPosition.y * 2 - 1);
    vec2 position = inPosition * 2 - 1;
    vec3 pos3;
    //    vec3 normal;

    pos3 = getSphere(position);
    normal = getNormalSphere2(position);

    gl_Position = projection * view * vec4(pos3, 1.0);

    vec4 pos4 = vec4(pos3, 1.0);
    pixPos = pos3;
    light = lightPos - pos3;
    //    light = lightPos - (mat3(view) * pos3);
    camera = cameraPos - pos3;

    viewDirection = -(view * pos4).xyz;
}