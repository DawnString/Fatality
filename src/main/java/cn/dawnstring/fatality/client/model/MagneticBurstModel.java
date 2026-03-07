package cn.dawnstring.fatality.client.model;

import cn.dawnstring.fatality.entity.projectile.MagneticBurstProjectile;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class MagneticBurstModel extends HierarchicalModel<MagneticBurstProjectile> {
    private final ModelPart root;
    private final ModelPart burst;

    public MagneticBurstModel(ModelPart root) {
        this.root = root;
        this.burst = root.getChild("burst");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // 根据magnetic_burst.json模型文件创建对应的模型
        PartDefinition burst = partdefinition.addOrReplaceChild("burst",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-2.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F), // 调整为爆发形状
                PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(MagneticBurstProjectile entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // 磁暴可以添加旋转和缩放效果
        this.burst.yRot = ageInTicks * 0.5F;
        this.burst.xRot = ageInTicks * 0.3F;
    }
}