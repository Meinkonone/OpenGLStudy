uniform mat4 uMatrix;
uniform mat4 uTextureMatrix;
attribute vec4 aPosition;
attribute vec4 aTexturePosition;
varying vec2 vTextureCoord;
void main() {
    gl_Position = uMatrix * aPosition;
    vTextureCoord = (uTextureMatrix * aTexturePosition).xy;
}