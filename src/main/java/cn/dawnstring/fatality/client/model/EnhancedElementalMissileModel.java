package cn.dawnstring.fatality.client.model;

import cn.dawnstring.fatality.entity.projectile.EnhancedElementalMissileProjectile;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class EnhancedElementalMissileModel extends HierarchicalModel<EnhancedElementalMissileProjectile> {
    private final ModelPart root;
    private final ModelPart missile;
    private final ModelPart tip;

    public EnhancedElementalMissileModel(ModelPart root) {
        this.root = root;
        this.missile = root.getChild("missile");
        this.tip = root.getChild("tip");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // 主导弹体 - 比普通导弹稍大
        PartDefinition missile = partdefinition.addOrReplaceChild("missile",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-2.0F, -2.0F, -4.0F, 4.0F, 4.0F, 8.0F), // 增强版导弹更大
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // 导弹尖端 - 增强版特有的尖端
        PartDefinition tip = partdefinition.addOrReplaceChild("tip",
                CubeListBuilder.create().texOffs(8, 0)
                        .addBox(-1.0F, -1.0F, 4.0F, 2.0F, 2.0F, 2.0F), // 尖端部分
                PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(EnhancedElementalMissileProjectile entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // 增强版导弹有更复杂的旋转效果
        float rotationSpeed = 0.4F; // 比普通导弹更快
        float wobbleAmount = 0.1F; // 轻微摆动效果

        // 主导弹体旋转
        this.missile.yRot = ageInTicks * rotationSpeed;
        this.missile.xRot = (float) Math.sin(ageInTicks * 0.2F) * wobbleAmount;

        // 尖端有更快的旋转和摆动
        this.tip.yRot = ageInTicks * rotationSpeed * 1.5F;
        this.tip.xRot = (float) Math.sin(ageInTicks * 0.3F) * wobbleAmount * 1.2F;
    }
}