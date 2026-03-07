package cn.dawnstring.fatality.client.model;

import cn.dawnstring.fatality.entity.boss.endofnightmare.EndOfNightmare;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class EndOfNightmareModel extends HierarchicalModel<EndOfNightmare> {
	private final ModelPart root;
	private final ModelPart waist;
	private final ModelPart head;
	private final ModelPart body;
	private final ModelPart right_arm;
	private final ModelPart left_arm;
	private final ModelPart right_leg;
	private final ModelPart left_leg;
	private final ModelPart hem;
	private final ModelPart bone;
	private final ModelPart bone2;
	private final ModelPart bone3;
	private final ModelPart bone4;
	private final ModelPart cape;

	public EndOfNightmareModel(ModelPart root) {
		this.root = root;
		this.waist = root.getChild("waist");
		this.head = this.waist.getChild("head");
		this.body = this.waist.getChild("body");
		this.right_arm = root.getChild("right_arm");
		this.left_arm = root.getChild("left_arm");
		this.right_leg = root.getChild("right_leg");
		this.left_leg = root.getChild("left_leg");
		this.hem = root.getChild("hem");
		this.bone = this.hem.getChild("bone");
		this.bone2 = this.hem.getChild("bone2");
		this.bone3 = this.hem.getChild("bone3");
		this.bone4 = this.hem.getChild("bone4");
		this.cape = root.getChild("cape");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition waist = partdefinition.addOrReplaceChild("waist", CubeListBuilder.create(), PartPose.offset(3.0F, 21.0F, -3.0F));

		PartDefinition head = waist.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 18).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 9.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, -21.0F, 3.0F));

		PartDefinition body = waist.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 18).addBox(-4.0F, -13.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, -8.0F, 3.0F));

		PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 34).addBox(1.0F, -1.0F, -3.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, 1.0F, 1.0F));

		PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(16, 34).addBox(-4.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, 0.0F, 0.0F));

		PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(32, 34).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 12.0F, 0.0F));

		PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(36, 0).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 13.0F, 0.0F));

		PartDefinition hem = partdefinition.addOrReplaceChild("hem", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition bone = hem.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(40, 50).addBox(-1.0F, 5.0F, 0.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(52, 8).addBox(0.0F, 7.0F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(52, 12).addBox(1.0F, 8.0F, 0.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(24, 50).addBox(-2.0F, 0.0F, 0.0F, 4.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -12.0F, -2.0F, -0.3886F, -0.0376F, -0.215F));

		PartDefinition bone2 = hem.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(52, 0).addBox(-2.0F, 5.0F, 0.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(52, 9).addBox(-2.0F, 7.0F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(52, 13).addBox(-2.0F, 8.0F, 0.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(32, 50).addBox(-2.0F, 0.0F, 0.0F, 4.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -12.0F, -2.0F, -0.3442F, 0.0594F, 0.1642F));

		PartDefinition bone3 = hem.addOrReplaceChild("bone3", CubeListBuilder.create().texOffs(48, 48).addBox(0.0F, 0.0F, -2.0F, 0.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(8, 50).addBox(0.0F, 4.0F, -2.0F, 0.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(52, 2).addBox(0.0F, 6.0F, -2.0F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(46, 50).addBox(0.0F, 7.0F, -2.0F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, -12.0F, 0.0F, 0.0F, 0.0F, -0.3054F));

		PartDefinition bone4 = hem.addOrReplaceChild("bone4", CubeListBuilder.create().texOffs(0, 50).addBox(0.0F, 0.0F, -2.0F, 0.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(16, 50).addBox(0.0F, 4.0F, -2.0F, 0.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(52, 5).addBox(0.0F, 6.0F, -2.0F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(52, 10).addBox(0.0F, 7.0F, -2.0F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -12.0F, 0.0F, 0.0F, 0.0F, 0.3054F));

		PartDefinition cape = partdefinition.addOrReplaceChild("cape", CubeListBuilder.create().texOffs(48, 34).addBox(-5.0F, 0.0F, 0.0F, 10.0F, 6.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(48, 40).addBox(-5.0F, 6.0F, 0.0F, 10.0F, 6.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(36, 16).addBox(-5.0F, 12.0F, 0.0F, 10.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(48, 46).addBox(-5.0F, 14.0F, 0.0F, 10.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -2.0F, -0.3927F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}

	@Override
	public void setupAnim(EndOfNightmare entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		// 基础动画设置
		this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);
		this.head.xRot = headPitch * ((float)Math.PI / 180F);

		// 根据Boss状态播放不同动画
		handleBossAnimations(entity, ageInTicks);
	}

	private void handleBossAnimations(EndOfNightmare boss, float ageInTicks) {
		// 根据Boss的AI状态播放相应动画
		if (boss.isCharging()) {
			// 播放冲刺动画
			playRushAnimation(ageInTicks);
		} else if (boss.isCasting()) {
			// 播放施法动画
			playCastAnimation(ageInTicks);
		} else if (boss.isLaserCharging()) {
			// 播放激光蓄力动画
			playLaserAnimation(ageInTicks);
		} else {
			// 播放站立动画
			playIdleAnimation(ageInTicks);
		}
	}

	private void playIdleAnimation(float ageInTicks) {
		// 简单的呼吸动画
		float breathing = (float) Math.sin(ageInTicks * 0.1F) * 0.02F;
		this.body.yRot = breathing;
		this.cape.xRot = -0.3927F + breathing * 0.1F;
	}

	private void playRushAnimation(float ageInTicks) {
		// 冲刺动画
		this.body.xRot = (float) Math.sin(ageInTicks * 0.5F) * 0.3F;
		this.right_arm.xRot = -1.0F;
		this.left_arm.xRot = -0.5F;
	}

	private void playCastAnimation(float ageInTicks) {
		// 施法动画
		float castWave = (float) Math.sin(ageInTicks * 0.8F) * 0.2F;
		this.right_arm.xRot = -1.5F + castWave;
		this.left_arm.xRot = -1.0F + castWave;
	}

	private void playLaserAnimation(float ageInTicks) {
		// 激光蓄力动画
		float chargePulse = (float) Math.sin(ageInTicks * 1.5F) * 0.1F;
		this.right_arm.xRot = -2.0F + chargePulse;
		this.head.xRot = -0.5F;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		waist.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		right_arm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		left_arm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		right_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		left_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		hem.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		cape.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}