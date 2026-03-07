package cn.dawnstring.fatality.client.model;

import cn.dawnstring.fatality.entity.projectile.OriginLaserProjectile;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class OriginLaserModel extends HierarchicalModel<OriginLaserProjectile> {
    private final ModelPart root;
    private final ModelPart laser;

    public OriginLaserModel(ModelPart root) {
        this.root = root;
        this.laser = root.getChild("laser");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // 根据origin_laser.json模型文件创建对应的模型
        PartDefinition laser = partdefinition.addOrReplaceChild("laser",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-1.0F, -1.0F, -8.0F, 2.0F, 2.0F, 16.0F), // 调整为长激光形状
                PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(OriginLaserProjectile entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // 激光可以添加旋转效果
        this.laser.yRot = ageInTicks * 0.2F;
    }
}