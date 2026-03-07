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
    // 高能元素球特效：能量波动和爆炸效果
    float energyWave = sin(Time * 0.35 + length(Position) * 4.0) * 0.25;
    float explosionPulse = sin(Time * 0.2) * 0.15 + 0.85;
    
    // 应用能量波动效果
    vec3 worldPos = Position * (1.0 + energyWave);
    
    // 应用爆炸脉动
    worldPos *= explosionPulse;
    
    // 基础顶点变换
    gl_Position = ProjMat * ModelViewMat * vec4(worldPos, 1.0);
    
    // 传递变量
    vertexColor = Color;
    texCoord0 = UV0;
    lightmapCoord = UV2;
    
    // 高能元素球颜色特效：炽热橙色
    float heatIntensity = sin(Time * 0.4) * 0.5 + 0.5;
    vertexColor.rgb = vec3(1.0, 0.6, 0.1) * heatIntensity;
    
    // 添加能量核心效果
    float coreEffect = sin(Time * 0.8) * 0.3 + 0.7;
    vertexColor.a *= coreEffect;
}