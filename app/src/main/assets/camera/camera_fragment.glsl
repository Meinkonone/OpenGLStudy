precision mediump float;
uniform sampler2D vTexture;
varying vec2 textureCoord;

void main() {
    gl_FragColor=texture2D(vTexture, textureCoord);

    /*vec2 uv = textureCoord.xy;

    float fStep = 0.0015;

    //分别对应四个像素点
    vec4 sample0 = texture2D(vTexture, vec2(uv.x - fStep, uv.y - fStep));
    vec4 sample1 = texture2D(vTexture, vec2(uv.x + fStep, uv.y + fStep));
    vec4 sample2 = texture2D(vTexture, vec2(uv.x + fStep, uv.y - fStep));
    vec4 sample3 = texture2D(vTexture, vec2(uv.x - fStep, uv.y + fStep));

    //求平均值
    gl_FragColor = (sample0 + sample1 + sample2 + sample3) / 4.0;*/

    /*int i;
    vec4 sum = vec4(0.0);
    vec2 uv = textureCoord.xy;

    //用来存储3x3的卷积矩阵
    float Kernel[9];
    Kernel[6] = 1.0;
    Kernel[7] = 2.0;
    Kernel[8] = 1.0;
    Kernel[3] = 2.0;
    Kernel[4] = 4.0;
    Kernel[5] = 2.0;
    Kernel[0] = 1.0;
    Kernel[1] = 2.0;
    Kernel[2] = 1.0;

    float fStep = 0.005;
    //像素点偏移位置
    vec2 Offset[9];
    Offset[0] = vec2(-fStep,-fStep);
    Offset[1] = vec2(0.0,-fStep);
    Offset[2] = vec2(fStep,-fStep);
    Offset[3] = vec2(-fStep,0.0);
    Offset[4] = vec2(0.0,0.0);
    Offset[5] = vec2(fStep,0.0);
    Offset[6] = vec2(-fStep, fStep);
    Offset[7] = vec2(0.0, fStep);
    Offset[8] = vec2(fStep, fStep);

    for (i = 0; i < 9; i++){
        vec4 tmp = texture2D(vTexture, uv + Offset[i]);
        sum += tmp * Kernel[i];
    }
    gl_FragColor = sum / 16.0;*/
}