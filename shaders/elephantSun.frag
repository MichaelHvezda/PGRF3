#version 150
in vec2 texCoord;
out vec4 outColor; // output from the fragment shader
uniform sampler2D texture1;


void main() {
    /*vec4 ambient = vec4(vec3(0.2), 1.0);

    float NdotL = max(0, dot(normalize(normal), normalize(light)));
    vec4 diffuse = vec4(NdotL * vec3(0.7), 1.0);

    // TODO specular

    vec4 finalColor = ambient + diffuse;// + specular;
    vec4 textureColor = texture(texture1, texCoord);

    outColor = finalColor * textureColor;*/
    //outColor = texture(texture1, texCoord);
    outColor = vec4(1,1,1.0,1.0);
    //outColor = texture(texture1, texCoord);
    //    outColor = vec4(1.0, gl_FragCoord.y / 600f, 0.0, 1.0);
}