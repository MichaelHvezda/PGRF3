#version 150
in vec3 vertColor; // input from the previous pipeline stage
out vec4 outColor; // output from the fragment shader
in vec2 texCoord;
in vec3 light;
in vec3 viewDirection;
in vec3 normal;
uniform sampler2D texture1;
void main() {
    vec4 ambient = vec4(vec3(0.2), 1.0);

    float NdotL = max(0, dot(normalize(normal), normalize(light)));
    vec4 diffuse = vec4(NdotL * vec3(0.7), 1.0);

    // TODO specular
    vec3 mirrLight = light - 2*normal*(light*normal);
    float cosB = max(0, dot(normalize(viewDirection), normalize(mirrLight)));
    float cosBPow = pow(cosB,0.9f);
    vec4 specular = vec4(cosB*vec3(0.9), 1.0);

    vec4 finalColor = ambient + diffuse + specular;
    vec4 textureColor = vec4(0.8,0.8,0.8, 1.0);
    outColor = finalColor * textureColor;
    //outColor = vec4(0.8,0.8,0.8, 1.0);
    //outColor = vec4(1.0);
}