#version 450
in vec3 vertColor; // input from the previous pipeline stage
out vec4 outColor; // output from the fragment shader
in vec2 texCoord;
in vec3 light;
in vec3 viewDirection;
in vec3 normal;
in vec3 fragPosition;
in vec3 camera;
uniform sampler2D texture1;
void main() {

    vec3 normal = cross(dFdxFine(fragPosition),dFdyFine( fragPosition));

    vec4 ambient = vec4(vec3(0.2), 1.0);

    float NdotL = max(0, dot(normalize(normal), normalize(light)));
    vec4 diffuse = vec4(NdotL * vec3(0.7), 1.0);

    vec3 mirrLight = reflect(normalize(-light),normalize(normal));
    float cosB = max(0, dot(normalize(camera), normalize(mirrLight)));
    float cosBPow = pow(cosB,32);
    vec4 specular = vec4(cosBPow*vec3(1), 1.0);

    vec4 finalColor = ambient + diffuse + specular;
    vec4 textureColor = vec4(0.1,0.1,0.1, 1.0);
    outColor = finalColor * textureColor;
    //outColor = vec4(0.8,0.8,0.8, 1.0);
    //outColor = vec4(1.0);
}