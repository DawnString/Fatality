package cn.dawnstring.fatality.entity.basemonster.desertbeetle;

import cn.dawnstring.fatality.Fatality;
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

public class DesertBeetleModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = ModModelLayers.DESERT_BEETLE_LAYER;
	private final ModelPart body;
	private final ModelPart mouth;
	private final ModelPart left3;
	private final ModelPart right3;
	private final ModelPart back;
	private final ModelPart right;
	private final ModelPart left;
	private final ModelPart leg;
	private final ModelPart leftf;
	private final ModelPart rightf;
	private final ModelPart leftb;
	private final ModelPart rightb;
	private final ModelPart bone;

	public DesertBeetleModel(ModelPart root) {
		this.body = root.getChild("body");
		this.mouth = this.body.getChild("mouth");
		this.left3 = this.mouth.getChild("left3");
		this.right3 = this.mouth.getChild("right3");
		this.back = this.body.getChild("back");
		this.right = this.back.getChild("right");
		this.left = this.back.getChild("left");
		this.leg = this.body.getChild("leg");
		this.leftf = this.leg.getChild("leftf");
		this.rightf = this.leg.getChild("rightf");
		this.leftb = this.leg.getChild("leftb");
		this.rightb = this.leg.getChild("rightb");
		this.bone = this.body.getChild("bone");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(-3.0F, 23.0F, -6.0F));

		PartDefinition mouth = body.addOrReplaceChild("mouth", CubeListBuilder.create(), PartPose.offset(4.0F, -2.0F, -8.0F));

		PartDefinition left3 = mouth.addOrReplaceChild("left3", CubeListBuilder.create(), PartPose.offset(-1.0F, -2.0F, 1.0F));

		PartDefinition cube_r1 = left3.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(44, 0).addBox(0.0F, -2.0F, -8.0F, 2.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.1309F, 0.0F));

		PartDefinition right3 = mouth.addOrReplaceChild("right3", CubeListBuilder.create(), PartPose.offset(-1.0F, -2.0F, 1.0F));

		PartDefinition cube_r2 = right3.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(22, 47).addBox(-2.0F, -2.0F, -8.0F, 2.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.1309F, 0.0F));

		PartDefinition back = body.addOrReplaceChild("back", CubeListBuilder.create(), PartPose.offset(3.0F, 1.0F, 6.0F));

		PartDefinition right = back.addOrReplaceChild("right", CubeListBuilder.create().texOffs(32, 31).addBox(-4.0F, 0.0F, 0.0F, 0.0F, 5.0F, 11.0F, new CubeDeformation(0.0F))
		.texOffs(54, 31).addBox(-4.0F, 0.0F, 11.0F, 0.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(42, 56).addBox(-4.0F, 0.0F, 13.0F, 0.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(54, 43).addBox(-4.0F, 0.0F, 15.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(32, 19).addBox(-4.0F, 0.0F, 0.0F, 4.0F, 0.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(52, 47).addBox(-4.0F, 0.0F, 12.0F, 3.0F, 0.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(54, 17).addBox(-4.0F, 0.0F, 14.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(42, 53).addBox(-4.0F, 0.0F, 15.0F, 1.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.0F, -7.0F, 0.0F, -0.0873F, 0.0F));

		PartDefinition left = back.addOrReplaceChild("left", CubeListBuilder.create().texOffs(0, 32).addBox(0.0F, 0.0F, 0.0F, 4.0F, 0.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(50, 53).addBox(3.0F, 0.0F, 15.0F, 1.0F, 0.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(54, 16).addBox(2.0F, 0.0F, 14.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(52, 49).addBox(1.0F, 0.0F, 12.0F, 3.0F, 0.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 44).addBox(4.0F, 0.0F, 0.0F, 0.0F, 5.0F, 11.0F, new CubeDeformation(0.0F))
		.texOffs(54, 37).addBox(4.0F, 0.0F, 11.0F, 0.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(46, 56).addBox(4.0F, 0.0F, 13.0F, 0.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(50, 56).addBox(4.0F, 0.0F, 15.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.0F, -7.0F, 0.0F, 0.0873F, 0.0F));

		PartDefinition leg = body.addOrReplaceChild("leg", CubeListBuilder.create(), PartPose.offset(7.0F, 0.0F, 0.0F));

		PartDefinition leftf = leg.addOrReplaceChild("leftf", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r3 = leftf.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(44, 16).addBox(0.0F, 0.0F, -1.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition rightf = leg.addOrReplaceChild("rightf", CubeListBuilder.create(), PartPose.offset(-8.0F, 0.0F, 0.0F));

		PartDefinition cube_r4 = rightf.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(22, 44).addBox(-3.0F, 0.0F, -1.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7854F));

		PartDefinition leftb = leg.addOrReplaceChild("leftb", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 12.0F));

		PartDefinition cube_r5 = leftb.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(42, 47).addBox(0.0F, 0.0F, -1.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition rightb = leg.addOrReplaceChild("rightb", CubeListBuilder.create(), PartPose.offset(-8.0F, 0.0F, 12.0F));

		PartDefinition cube_r6 = rightb.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(42, 50).addBox(-3.0F, 0.0F, -1.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7854F));

		PartDefinition bone = body.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(44, 12).addBox(-3.0F, -1.0F, -1.0F, 6.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(52, 51).addBox(-2.0F, 0.0F, 0.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 19).addBox(-5.0F, -5.0F, -21.0F, 10.0F, 7.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-4.0F, -2.0F, -15.0F, 8.0F, 5.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, -3.0F, 14.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		// 应用行走动画 - 腿部摆动
		float walkSpeed = 2.0f;
		float walkDegree = 0.6f;
		
		// 前腿动画
		this.leftf.xRot = (float) Math.sin(limbSwing * walkSpeed) * walkDegree * limbSwingAmount;
		this.rightf.xRot = (float) Math.sin(limbSwing * walkSpeed + (float)Math.PI) * walkDegree * limbSwingAmount;
		
		// 后腿动画（与前腿相位相反）
		this.leftb.xRot = (float) Math.sin(limbSwing * walkSpeed + (float)Math.PI) * walkDegree * limbSwingAmount;
		this.rightb.xRot = (float) Math.sin(limbSwing * walkSpeed) * walkDegree * limbSwingAmount;
		
		// 头部跟随玩家
		this.body.yRot = netHeadYaw * ((float)Math.PI / 180F);
		this.body.xRot = headPitch * ((float)Math.PI / 180F);
		
		// 身体轻微上下浮动
		float bodyBob = (float) Math.sin(limbSwing * 0.6662F) * 0.1F * limbSwingAmount;
		this.body.y += bodyBob;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}