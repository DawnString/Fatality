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
    // 高斯激光特效：能量聚焦和电光效果
    float energyFocus = sin(Time * 0.4 + Position.y * 5.0) * 0.2;
    float electricPulse = sin(Time * 0.8) * 0.3 + 0.7;
    
    // 应用能量聚焦效果
    vec3 worldPos = Position;
    worldPos.xz *= (1.0 + energyFocus);
    
    // 应用电光脉动
    worldPos *= electricPulse;
    
    // 基础顶点变换
    gl_Position = ProjMat * ModelViewMat * vec4(worldPos, 1.0);
    
    // 传递变量
    vertexColor = Color;
    texCoord0 = UV0;
    lightmapCoord = UV2;
    
    // 高斯激光颜色特效：电光蓝色
    float colorIntensity = sin(Time * 0.6) * 0.4 + 0.6;
    vertexColor.rgb = vec3(0.2, 0.4, 1.0) * colorIntensity;
    
    // 添加电光闪烁效果
    float flash = sin(Time * 2.0) * 0.5 + 0.5;
    vertexColor.a *= flash;
}