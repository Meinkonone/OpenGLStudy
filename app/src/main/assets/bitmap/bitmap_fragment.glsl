precision mediump float;
uniform sampler2D vTexture;
varying vec2 textureCoord;
uniform bool isVertical;

void main() {
    vec2 tex_offset =vec2(1.0/300.0, 1.0/300.0);
    vec4 orColor=texture2D(vTexture, textureCoord);
    float orAlpha=orColor.a;
    float weight[5];
    weight[0] = 0.227027;
    weight[1] = 0.1945946;
    weight[2] = 0.1216216;
    weight[3] = 0.054054;
    weight[4] = 0.016216;
    vec3 color=orColor.rgb*weight[0];
    int i;
    if (!isVertical){
        for (i=1;i<5;i++){
            color+=texture2D(vTexture, textureCoord+vec2(tex_offset.x * float(i), 0.0)).rgb*weight[i];
            color+=texture2D(vTexture, textureCoord-vec2(tex_offset.x * float(i), 0.0)).rgb*weight[i];
        }
    } else {
        for (i=1;i<5;i++){
            color+=texture2D(vTexture, textureCoord+vec2(0.0, tex_offset.y * float(i))).rgb*weight[i];
            color+=texture2D(vTexture, textureCoord-vec2(0.0, tex_offset.y * float(i))).rgb*weight[i];
        }
    }
    gl_FragColor=vec4(color, orAlpha);
}