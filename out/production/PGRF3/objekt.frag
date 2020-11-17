#version 150

out vec4 outColor; // output from the fragment shader
uniform sampler2D texture1;

in vec2 texCoord;
in vec3 light;
in vec3 viewDirection;
in vec3 normal;
in vec3 pixPos;
in vec3 camera;

void main() {
    vec4 ambient = vec4(vec3(0.9), 1.0);

    float NdotL = max(0, dot(normalize(normal), normalize(light)));
    vec4 diffuse = vec4(NdotL * vec3(0.9), 1.0);

    // TODO specular

    //vec3 mirrLight = light - 2.0*normal*dot(normal,light);
    //float cosB = max(0, dot(normalize(viewDirection), normalize(mirrLight)));
    //float cosBPow = pow(cosB,24);
    //vec4 specular = vec4(cosB*vec3(0.9,0,0), 1.0);
    vec3 mirrLight = reflect(normalize(-light),normalize(normal));
    float cosB = max(0, dot(normalize(camera), normalize(mirrLight)));
    float cosBPow = pow(cosB,32);
    vec4 specular = vec4(cosBPow*vec3(1,0,0), 1.0);

   //vec3 norm = normalize(normal);
   //vec3 viewDir = normalize(viewDirection - pixPos.xyz);
   //vec3 reflects = reflect(-light,norm);
   //float spec = pow(max(dot(viewDirection, mirrLight), 0.0), 32);
   //vec4 specular = vec4(spec*vec3(0.9,0,0),1.0 );


    vec4 finalColor =  specular;
    //vec4 finalColor = ambient + diffuse + specular;
    vec4 textureColor = vec4(0.1,0.1,0.1,1.0);

    outColor = finalColor * textureColor;
    //outColor = vec4(cameraDirection,1);
}