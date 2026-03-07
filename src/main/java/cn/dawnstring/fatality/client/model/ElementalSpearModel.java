package cn.dawnstring.fatality.client.model;

import cn.dawnstring.fatality.entity.projectile.ElementalSpearProjectile;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class ElementalSpearModel extends HierarchicalModel<ElementalSpearProjectile> {
    private final ModelPart root;
    private final ModelPart spear;

    public ElementalSpearModel(ModelPart root) {
        this.root = root;
        this.spear = root.getChild("spear");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // 根据elemental_spear.json模型文件创建对应的模型
        PartDefinition spear = partdefinition.addOrReplaceChild("spear",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-0.5F, -0.5F, -6.0F, 1.0F, 1.0F, 12.0F), // 调整为长条形状
                PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(ElementalSpearProjectile entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // 矛可以添加旋转效果
        this.spear.yRot = ageInTicks * 0.4F;
    }
}