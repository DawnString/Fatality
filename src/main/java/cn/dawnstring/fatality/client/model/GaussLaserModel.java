package cn.dawnstring.fatality.client.model;

import cn.dawnstring.fatality.entity.projectile.GaussLaserProjectile;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class GaussLaserModel extends HierarchicalModel<GaussLaserProjectile> {
    private final ModelPart root;
    private final ModelPart laser;

    public GaussLaserModel(ModelPart root) {
        this.root = root;
        this.laser = root.getChild("laser");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // 根据gauss_laser.json模型文件创建对应的模型
        // 模型文件显示是一个从[7,3,4]到[10,6,13]的立方体
        PartDefinition laser = partdefinition.addOrReplaceChild("laser",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-1.5F, -1.5F, -4.5F, 3.0F, 3.0F, 9.0F), // 调整为合适的尺寸
                PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(GaussLaserProjectile entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // 激光不需要复杂的动画，可以添加简单的旋转效果
        this.laser.yRot = ageInTicks * 0.2F; // 缓慢旋转
    }
}