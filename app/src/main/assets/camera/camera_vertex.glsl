attribute vec4 vPosition;
attribute vec4 vCoord;
uniform mat4 vMatrix;
uniform mat4 vCoordMatrix;
varying vec2 textureCoord;

void main() {
    gl_Position = vMatrix * vPosition;
    textureCoord = (vCoordMatrix * vCoord).xy;
}