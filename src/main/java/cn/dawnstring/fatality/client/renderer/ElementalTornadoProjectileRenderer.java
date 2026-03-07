package cn.dawnstring.fatality.client.renderer;

import cn.dawnstring.fatality.client.shader.ProjectileShaderManager;
import cn.dawnstring.fatality.entity.projectile.ElementalTornadoProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class ElementalTornadoProjectileRenderer extends EntityRenderer<ElementalTornadoProjectile> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("fatality", "textures/empty.png");
    private static final Vector3f TORNADO_COLOR = new Vector3f(0.1f, 0.6f, 0.8f); // 蓝色龙卷风颜色

    public ElementalTornadoProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(ElementalTornadoProjectile entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        // 元素龙卷风投射物主要依赖粒子效果，但添加OpenGL特效增强视觉效果
        poseStack.pushPose();
        
        // 使用着色器渲染龙卷风能量光环
        renderTornadoHaloWithShader(poseStack, bufferSource, packedLight, entity.tickCount, entity.position());
        
        // 使用着色器渲染龙卷风核心能量
        renderTornadoCoreWithShader(poseStack, bufferSource, packedLight, entity.tickCount, entity.position());
        
        poseStack.popPose();
        
        // 粒子效果已经在ElementalTornadoProjectile的tick方法中生成
    }
    
    private void renderTornadoHalo(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int tickCount) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        
        // 龙卷风能量光环效果 - 多层光环
        float haloRadius1 = 2.0f + Mth.sin(tickCount * 0.1f) * 0.3f;
        float haloRadius2 = 1.5f + Mth.sin(tickCount * 0.12f) * 0.25f;
        float haloRadius3 = 1.0f + Mth.sin(tickCount * 0.14f) * 0.2f;
        
        int haloSegments = 48;
        float alpha1 = 0.3f + Mth.sin(tickCount * 0.15f) * 0.2f;
        float alpha2 = 0.4f + Mth.sin(tickCount * 0.18f) * 0.25f;
        float alpha3 = 0.5f + Mth.sin(tickCount * 0.21f) * 0.3f;
        
        // 外层光环
        renderHaloRing(matrix, vertexConsumer, packedLight, pose, haloRadius1, haloSegments, alpha1);
        
        // 中层光环
        renderHaloRing(matrix, vertexConsumer, packedLight, pose, haloRadius2, haloSegments, alpha2);
        
        // 内层光环
        renderHaloRing(matrix, vertexConsumer, packedLight, pose, haloRadius3, haloSegments, alpha3);
    }
    
    private void renderHaloRing(Matrix4f matrix, VertexConsumer vertexConsumer, int packedLight, PoseStack.Pose pose, float radius, int segments, float alpha) {
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (2 * Math.PI * i / segments);
            float angle2 = (float) (2 * Math.PI * (i + 1) / segments);
            
            float x1 = radius * Mth.cos(angle1);
            float z1 = radius * Mth.sin(angle1);
            float x2 = radius * Mth.cos(angle2);
            float z2 = radius * Mth.sin(angle2);
            
            vertexConsumer.vertex(matrix, x1, 0, z1)
                .color(TORNADO_COLOR.x(), TORNADO_COLOR.y(), TORNADO_COLOR.z(), alpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, x2, 0, z2)
                .color(TORNADO_COLOR.x(), TORNADO_COLOR.y(), TORNADO_COLOR.z(), alpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, 0, 0, 0)
                .color(TORNADO_COLOR.x(), TORNADO_COLOR.y(), TORNADO_COLOR.z(), alpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
        }
    }
    
    private void renderTornadoCore(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int tickCount) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        
        // 龙卷风核心能量效果 - 旋转的能量核心
        float coreRadius = 0.4f + Mth.sin(tickCount * 0.3f) * 0.1f;
        int coreSegments = 20;
        float coreAlpha = 0.7f + Mth.sin(tickCount * 0.4f) * 0.2f;
        
        for (int i = 0; i < coreSegments; i++) {
            float angle1 = (float) (2 * Math.PI * i / coreSegments);
            float angle2 = (float) (2 * Math.PI * (i + 1) / coreSegments);
            
            float x1 = coreRadius * Mth.cos(angle1);
            float z1 = coreRadius * Mth.sin(angle1);
            float x2 = coreRadius * Mth.cos(angle2);
            float z2 = coreRadius * Mth.sin(angle2);
            
            vertexConsumer.vertex(matrix, x1, 0, z1)
                .color(0.2f, 0.8f, 1.0f, coreAlpha) // 亮蓝色核心
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, x2, 0, z2)
                .color(0.2f, 0.8f, 1.0f, coreAlpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            vertexConsumer.vertex(matrix, 0, 0, 0)
                .color(0.2f, 0.8f, 1.0f, coreAlpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
        }
    }

    @Override
    public ResourceLocation getTextureLocation(ElementalTornadoProjectile entity) {
        // 返回一个空的纹理位置，因为我们使用粒子效果渲染
        return ResourceLocation.fromNamespaceAndPath("fatality", "textures/empty.png");
    }
    
    // ========== 着色器渲染方法 ==========
    
    /**
     * 使用着色器渲染龙卷风能量光环
     */
    private void renderTornadoHaloWithShader(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int tickCount, net.minecraft.world.phys.Vec3 position) {
        // 使用着色器渲染外层龙卷风能量光环
        VertexConsumer haloConsumer = bufferSource.getBuffer(ProjectileShaderManager.createElementalTornadoRenderType());
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        
        // 龙卷风能量光环效果 - 多层光环
        float haloRadius1 = 2.0f + Mth.sin(tickCount * 0.1f) * 0.3f;
        float haloRadius2 = 1.5f + Mth.sin(tickCount * 0.12f) * 0.25f;
        float haloRadius3 = 1.0f + Mth.sin(tickCount * 0.14f) * 0.2f;
        
        int haloSegments = 48;
        float alpha1 = 0.3f + Mth.sin(tickCount * 0.15f) * 0.2f;
        float alpha2 = 0.4f + Mth.sin(tickCount * 0.18f) * 0.25f;
        float alpha3 = 0.5f + Mth.sin(tickCount * 0.21f) * 0.3f;
        
        // 外层光环
        renderHaloRing(matrix, haloConsumer, packedLight, pose, haloRadius1, haloSegments, alpha1);
        
        // 中层光环
        renderHaloRing(matrix, haloConsumer, packedLight, pose, haloRadius2, haloSegments, alpha2);
        
        // 内层光环
        renderHaloRing(matrix, haloConsumer, packedLight, pose, haloRadius3, haloSegments, alpha3);
    }
    
    /**
     * 使用着色器渲染龙卷风核心能量
     */
    private void renderTornadoCoreWithShader(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int tickCount, net.minecraft.world.phys.Vec3 position) {
        // 使用着色器渲染龙卷风核心能量
        VertexConsumer coreConsumer = bufferSource.getBuffer(ProjectileShaderManager.createElementalTornadoRenderType());
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        
        // 龙卷风核心能量效果 - 旋转的能量核心
        float coreRadius = 0.4f + Mth.sin(tickCount * 0.3f) * 0.1f;
        int coreSegments = 20;
        float coreAlpha = 0.7f + Mth.sin(tickCount * 0.4f) * 0.2f;
        
        for (int i = 0; i < coreSegments; i++) {
            float angle1 = (float) (2 * Math.PI * i / coreSegments);
            float angle2 = (float) (2 * Math.PI * (i + 1) / coreSegments);
            
            float x1 = coreRadius * Mth.cos(angle1);
            float z1 = coreRadius * Mth.sin(angle1);
            float x2 = coreRadius * Mth.cos(angle2);
            float z2 = coreRadius * Mth.sin(angle2);
            
            coreConsumer.vertex(matrix, x1, 0, z1)
                .color(0.2f, 0.8f, 1.0f, coreAlpha) // 亮蓝色核心
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            coreConsumer.vertex(matrix, x2, 0, z2)
                .color(0.2f, 0.8f, 1.0f, coreAlpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
                
            coreConsumer.vertex(matrix, 0, 0, 0)
                .color(0.2f, 0.8f, 1.0f, coreAlpha)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0, 1, 0)
                .endVertex();
        }
    }
}