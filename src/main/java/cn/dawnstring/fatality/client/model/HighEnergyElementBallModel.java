package cn.dawnstring.fatality.client.model;

import cn.dawnstring.fatality.entity.projectile.HighEnergyElementBallProjectile;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class HighEnergyElementBallModel extends HierarchicalModel<HighEnergyElementBallProjectile> {
    private final ModelPart root;
    private final ModelPart ball;

    public HighEnergyElementBallModel(ModelPart root) {
        this.root = root;
        this.ball = root.getChild("ball");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // 根据high_energy_element_ball.json模型文件创建对应的模型
        PartDefinition ball = partdefinition.addOrReplaceChild("ball",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F), // 调整为合适的尺寸
                PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(HighEnergyElementBallProjectile entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // 能量球可以添加旋转和缩放效果
        this.ball.yRot = ageInTicks * 0.3F;
        this.ball.xRot = ageInTicks * 0.2F;
    }
}