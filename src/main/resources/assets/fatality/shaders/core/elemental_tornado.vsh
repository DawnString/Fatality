#version 150

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in vec2 UV2;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform float Time;
uniform vec3 EntityPos;

out vec4 vertexColor;
out vec2 texCoord0;
out vec2 lightmapCoord;

void main() {
    // 龙卷风特效：螺旋扭曲和能量脉动
    float spiralEffect = sin(Time * 0.2 + Position.y * 2.0) * 0.3;
    float pulse = sin(Time * 0.15) * 0.2 + 0.8;
    
    // 应用螺旋扭曲效果
    vec3 worldPos = Position;
    worldPos.x += spiralEffect * sin(Position.y * 3.0 + Time * 0.5);
    worldPos.z += spiralEffect * cos(Position.y * 3.0 + Time * 0.5);
    
    // 应用脉动缩放
    worldPos *= pulse;
    
    // 基础顶点变换
    gl_Position = ProjMat * ModelViewMat * vec4(worldPos, 1.0);
    
    // 传递变量
    vertexColor = Color;
    texCoord0 = UV0;
    lightmapCoord = UV2;
    
    // 龙卷风颜色特效：多层颜色混合
    float colorWave = sin(Time * 0.25 + Position.y * 4.0) * 0.3 + 0.7;
    vertexColor.rgb *= vec3(0.1, 0.6, 0.8) * colorWave;
    
    // 添加透明度脉动
    vertexColor.a *= (0.6 + sin(Time * 0.3) * 0.4);
}