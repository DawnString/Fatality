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
    // 起源激光特效：神圣光芒和追踪效果
    float divineLight = sin(Time * 0.25 + Position.y * 6.0) * 0.2;
    float trackingPulse = sin(Time * 0.15) * 0.1 + 0.9;
    
    // 应用神圣光芒效果
    vec3 worldPos = Position;
    worldPos.xz *= (1.0 + divineLight);
    
    // 应用追踪脉动
    worldPos *= trackingPulse;
    
    // 基础顶点变换
    gl_Position = ProjMat * ModelViewMat * vec4(worldPos, 1.0);
    
    // 传递变量
    vertexColor = Color;
    texCoord0 = UV0;
    lightmapCoord = UV2;
    
    // 起源激光颜色特效：神圣金色
    float divineIntensity = sin(Time * 0.3) * 0.4 + 0.6;
    vertexColor.rgb = vec3(1.0, 0.9, 0.3) * divineIntensity;
    
    // 添加神圣光环效果
    float haloEffect = sin(Time * 0.5) * 0.2 + 0.8;
    vertexColor.a *= haloEffect;
}