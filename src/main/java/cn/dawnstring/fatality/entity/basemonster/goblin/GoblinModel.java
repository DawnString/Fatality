package cn.dawnstring.fatality.entity.basemonster.goblin;

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

public class GoblinModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = ModModelLayers.GOBLIN_LAYER;
	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart arm;
	private final ModelPart left;
	private final ModelPart right;
	private final ModelPart leg;
	private final ModelPart left2;
	private final ModelPart right2;

	public GoblinModel(ModelPart root) {
		this.body = root.getChild("body");
		this.head = this.body.getChild("head");
		this.arm = this.body.getChild("arm");
		this.left = this.arm.getChild("left");
		this.right = this.arm.getChild("right");
		this.leg = this.body.getChild("leg");
		this.left2 = this.leg.getChild("left2");
		this.right2 = this.leg.getChild("right2");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 15).addBox(-2.0F, 2.0F, -9.0F, 3.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 4.0F, 6.0F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 29).addBox(-1.0F, -4.0F, 5.0F, 1.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-3.0F, -5.0F, -5.0F, 5.0F, 5.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(8, 29).addBox(-1.0F, -4.0F, -8.0F, 1.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.0F, -6.0F));

		PartDefinition arm = body.addOrReplaceChild("arm", CubeListBuilder.create(), PartPose.offset(-1.0F, 4.0F, -2.0F));

		PartDefinition left = arm.addOrReplaceChild("left", CubeListBuilder.create().texOffs(18, 15).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, -1.0F));

		PartDefinition right = arm.addOrReplaceChild("right", CubeListBuilder.create().texOffs(18, 25).addBox(-1.0F, 0.0F, -2.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, -7.0F));

		PartDefinition leg = body.addOrReplaceChild("leg", CubeListBuilder.create(), PartPose.offset(-1.0F, 4.0F, -2.0F));

		PartDefinition left2 = leg.addOrReplaceChild("left2", CubeListBuilder.create().texOffs(26, 24).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, -2.0F));

		PartDefinition right2 = leg.addOrReplaceChild("right2", CubeListBuilder.create().texOffs(26, 15).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, -6.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		// 应用行走动画 - 手臂和腿部摆动
		float walkSpeed = 0.6662f;
		float walkDegree = 0.5f;
		
		// 手臂动画
		this.left.xRot = (float) Math.sin(limbSwing * walkSpeed) * walkDegree * limbSwingAmount;
		this.right.xRot = (float) Math.sin(limbSwing * walkSpeed + (float)Math.PI) * walkDegree * limbSwingAmount;
		
		// 腿部动画
		this.left2.xRot = (float) Math.sin(limbSwing * walkSpeed + (float)Math.PI) * walkDegree * limbSwingAmount;
		this.right2.xRot = (float) Math.sin(limbSwing * walkSpeed) * walkDegree * limbSwingAmount;
		
		// 头部跟随玩家
		this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);
		this.head.xRot = headPitch * ((float)Math.PI / 180F);
		
		// 身体轻微摆动
		float bodySwing = (float) Math.sin(limbSwing * 0.6662F) * 0.1F * limbSwingAmount;
		this.body.yRot = bodySwing;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}