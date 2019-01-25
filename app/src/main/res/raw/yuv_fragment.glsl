precision mediump float;

uniform sampler2D tex_y;
uniform sampler2D tex_u;
uniform sampler2D tex_v;
varying vec2 textureCoordinate;
void main() {
      highp float y = texture2D(tex_y, textureCoordinate.xy).r;
      highp float u = texture2D(tex_u, textureCoordinate.xy).r - 0.5;
      highp float v = texture2D(tex_v, textureCoordinate.xy).r - 0.5;
      highp float r = y + 1.402 * v;
      highp float g = y - 0.344 * u - 0.714 * v;
      highp float b = y + 1.772 * u;
      gl_FragColor = vec4(1,0,0,1.0);
 }
