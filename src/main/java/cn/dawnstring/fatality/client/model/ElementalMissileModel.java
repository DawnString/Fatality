package cn.dawnstring.fatality.client.model;

import cn.dawnstring.fatality.entity.projectile.ElementalMissileProjectile;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class ElementalMissileModel extends HierarchicalModel<ElementalMissileProjectile> {
    private final ModelPart root;
    private final ModelPart missile;

    public ElementalMissileModel(ModelPart root) {
        this.root = root;
        this.missile = root.getChild("missile");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // 根据elemental_missile.json模型文件创建对应的模型
        PartDefinition missile = partdefinition.addOrReplaceChild("missile",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-1.0F, -1.0F, -3.0F, 2.0F, 2.0F, 6.0F), // 调整为导弹形状
                PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(ElementalMissileProjectile entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // 导弹可以添加旋转效果
        this.missile.yRot = ageInTicks * 0.3F;
    }
}