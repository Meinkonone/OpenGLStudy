precision mediump float;
varying vec2 vTextureCoord;
uniform float uAlpha;
uniform sampler2D uTextureSampler;

uniform int uKernelSize;
uniform vec2 uKernelOffset[220];
uniform float uKernelValue[220];

void main() {
    vec4 cOut = vec4(0.0, 0.0, 0.0, 0.0);
    vec4 vKernelValue = vec4(1.0, 1.0, 1.0, 1.0);
    int i;
    float temp;
    for (i = 0; i < uKernelSize; i++) {
        vKernelValue = vec4(uKernelValue[i], uKernelValue[i], uKernelValue[i], uKernelValue[i]);
        cOut += vKernelValue * texture2D(uTextureSampler, vTextureCoord + uKernelOffset[i]);
    }
    gl_FragColor = cOut;
    gl_FragColor *= uAlpha;
}