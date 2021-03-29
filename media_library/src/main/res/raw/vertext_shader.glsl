attribute vec4 vPotion;
attribute vec4 vTexCoordinate;

uniform mat4 textureTransform;
uniform vec2 v_TexCoordinate;

void main() {
    v_TexCoordinate = (textureTransform * vTexCoordinate).xy;
    gl_Position = vPotion;
}