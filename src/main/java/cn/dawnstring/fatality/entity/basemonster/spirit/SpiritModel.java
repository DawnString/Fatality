package cn.dawnstring.fatality.entity.basemonster.spirit;

import cn.dawnstring.fatality.client.model.ModModelLayers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

// Made with Blockbench 5.0.7
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports

public class SpiritModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = ModModelLayers.SPIRIT_LAYER;
	private final ModelPart body;
	private final ModelPart wing;
	private final ModelPart left;
	private final ModelPart right;

	public SpiritModel(ModelPart root) {
		this.body = root.getChild("body");
		this.wing = this.body.getChild("wing");
		this.left = this.wing.getChild("left");
		this.right = this.wing.getChild("right");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -5.0F, -3.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition wing = body.addOrReplaceChild("wing", CubeListBuilder.create(), PartPose.offset(1.0F, -2.0F, -4.0F));

		PartDefinition left = wing.addOrReplaceChild("left", CubeListBuilder.create().texOffs(8, 13).addBox(0.0F, -1.0F, -2.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(16, 7).addBox(0.0F, -3.0F, -3.0F, 0.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 13).addBox(0.0F, -4.0F, -5.0F, 0.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(6, 8).addBox(0.0F, -4.0F, -8.0F, 0.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(16, 0).addBox(0.0F, -5.0F, -7.0F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(12, 16).addBox(0.0F, -3.0F, -2.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, -1.0F, 1.0F));

		PartDefinition right = wing.addOrReplaceChild("right", CubeListBuilder.create().texOffs(4, 13).addBox(0.0F, -1.0F, -2.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(16, 3).addBox(0.0F, -3.0F, -3.0F, 0.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(12, 8).addBox(0.0F, -4.0F, -5.0F, 0.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 8).addBox(0.0F, -4.0F, -8.0F, 0.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(12, 13).addBox(0.0F, -5.0F, -7.0F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(16, 11).addBox(0.0F, -3.0F, -2.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, -1.0F, 1.0F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		// 应用飞行动画
		this.left.xRot = (float) Math.sin(ageInTicks * 0.2f) * 0.5f;
		this.right.xRot = (float) Math.sin(ageInTicks * 0.2f + (float)Math.PI) * 0.5f;
		
		// 翅膀摆动动画
		float wingSwing = (float) Math.sin(ageInTicks * 0.3f) * 0.3f;
		this.left.yRot = wingSwing;
		this.right.yRot = -wingSwing;
		
		// 头部跟随玩家
		this.body.yRot = netHeadYaw * ((float)Math.PI / 180F);
		this.body.xRot = headPitch * ((float)Math.PI / 180F);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}