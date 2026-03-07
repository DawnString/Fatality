package cn.dawnstring.fatality.client.model;

import cn.dawnstring.fatality.entity.projectile.BulletProjectile;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class BulletProjectileModel extends HierarchicalModel<BulletProjectile> {
    private final ModelPart root;
    private final ModelPart bullet;

    public BulletProjectileModel(ModelPart root) {
        this.root = root;
        this.bullet = root.getChild("bullet");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // 根据你的bullet_projectile.json模型文件创建对应的模型
        // 模型文件显示是一个从[8,6,8]到[9,7,9]的立方体
        PartDefinition bullet = partdefinition.addOrReplaceChild("bullet",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F), // 调整为合适的尺寸
                PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(BulletProjectile entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // 子弹不需要复杂的动画，可以添加简单的旋转效果
        this.bullet.yRot = ageInTicks * 0.1F; // 缓慢旋转
    }
}