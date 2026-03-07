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
    // 基础顶点变换
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    
    // 传递变量
    vertexColor = Color;
    texCoord0 = UV0;
    lightmapCoord = UV2;
    
    // 元素长枪特效：脉动缩放和旋转
    float pulse = sin(Time * 0.3) * 0.1 + 0.9;
    float rotation = Time * 0.5;
    
    // 应用脉动效果
    vec3 worldPos = Position * pulse;
    
    // 应用旋转效果
    float cosRot = cos(rotation);
    float sinRot = sin(rotation);
    worldPos.xz = vec2(
        worldPos.x * cosRot - worldPos.z * sinRot,
        worldPos.x * sinRot + worldPos.z * cosRot
    );
    
    // 最终位置计算
    gl_Position = ProjMat * ModelViewMat * vec4(worldPos, 1.0);
    
    // 添加颜色脉动效果
    vertexColor.rgb *= (0.8 + sin(Time * 0.4) * 0.2);
}