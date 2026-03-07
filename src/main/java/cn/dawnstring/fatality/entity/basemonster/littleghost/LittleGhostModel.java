package cn.dawnstring.fatality.entity.basemonster.littleghost;

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

public class LittleGhostModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = ModModelLayers.LITTLE_GHOST_LAYER;
	private final ModelPart body;
	private final ModelPart leg1;
	private final ModelPart leg2;
	private final ModelPart leg3;
	private final ModelPart leg4;
	private final ModelPart leg5;
	private final ModelPart leg6;
	private final ModelPart leg7;
	private final ModelPart leg8;

	public LittleGhostModel(ModelPart root) {
		this.body = root.getChild("body");
		this.leg1 = this.body.getChild("leg1");
		this.leg2 = this.body.getChild("leg2");
		this.leg3 = this.body.getChild("leg3");
		this.leg4 = this.body.getChild("leg4");
		this.leg5 = this.body.getChild("leg5");
		this.leg6 = this.body.getChild("leg6");
		this.leg7 = this.body.getChild("leg7");
		this.leg8 = this.body.getChild("leg8");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition leg1 = body.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, 0.0F, -3.0F));

		PartDefinition leg2 = body.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(8, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, 0.0F, 0.0F));

		PartDefinition leg3 = body.addOrReplaceChild("leg3", CubeListBuilder.create().texOffs(16, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, 0.0F, 3.0F));

		PartDefinition leg4 = body.addOrReplaceChild("leg4", CubeListBuilder.create().texOffs(24, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -3.0F));

		PartDefinition leg5 = body.addOrReplaceChild("leg5", CubeListBuilder.create().texOffs(32, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 3.0F));

		PartDefinition leg6 = body.addOrReplaceChild("leg6", CubeListBuilder.create().texOffs(40, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, 0.0F, -3.0F));

		PartDefinition leg7 = body.addOrReplaceChild("leg7", CubeListBuilder.create().texOffs(48, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, 0.0F, 0.0F));

		PartDefinition leg8 = body.addOrReplaceChild("leg8", CubeListBuilder.create().texOffs(56, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, 0.0F, 3.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		// 应用行走动画 - 所有腿部交替摆动
		float walkSpeed = 0.6662f;
		float walkDegree = 0.5f;
		
		// 腿部动画 - 交替摆动
		this.leg1.xRot = (float) Math.sin(limbSwing * walkSpeed) * walkDegree * limbSwingAmount;
		this.leg2.xRot = (float) Math.sin(limbSwing * walkSpeed + (float)Math.PI * 0.25f) * walkDegree * limbSwingAmount;
		this.leg3.xRot = (float) Math.sin(limbSwing * walkSpeed + (float)Math.PI * 0.5f) * walkDegree * limbSwingAmount;
		this.leg4.xRot = (float) Math.sin(limbSwing * walkSpeed + (float)Math.PI * 0.75f) * walkDegree * limbSwingAmount;
		this.leg5.xRot = (float) Math.sin(limbSwing * walkSpeed + (float)Math.PI) * walkDegree * limbSwingAmount;
		this.leg6.xRot = (float) Math.sin(limbSwing * walkSpeed + (float)Math.PI * 1.25f) * walkDegree * limbSwingAmount;
		this.leg7.xRot = (float) Math.sin(limbSwing * walkSpeed + (float)Math.PI * 1.5f) * walkDegree * limbSwingAmount;
		this.leg8.xRot = (float) Math.sin(limbSwing * walkSpeed + (float)Math.PI * 1.75f) * walkDegree * limbSwingAmount;
		
		// 身体轻微浮动效果
		float floatEffect = (float) Math.sin(ageInTicks * 0.1f) * 0.1f;
		this.body.y = 24.0f + floatEffect;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}