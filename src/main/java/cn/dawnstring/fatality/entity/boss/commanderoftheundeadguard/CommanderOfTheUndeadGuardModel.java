package cn.dawnstring.fatality.entity.boss.commanderoftheundeadguard;

import cn.dawnstring.fatality.client.model.ModModelLayers;
import cn.dawnstring.fatality.entity.boss.commanderoftheundeadguard.CommanderOfTheUndeadGuard;
import cn.dawnstring.fatality.entity.boss.commanderoftheundeadguard.CommanderOfTheUndeadGuardAnimation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;

public class CommanderOfTheUndeadGuardModel<T extends Entity> extends HierarchicalModel<T> {

	public static final ModelLayerLocation LAYER_LOCATION = ModModelLayers.COMMANDER_OF_THE_UNDEAD_GUARD_LAYER;
	private final ModelPart boss;
	private final ModelPart body;
	private final ModelPart up;
	private final ModelPart bone9;
	private final ModelPart arm;
	private final ModelPart l_arm;
	private final ModelPart l_upperarm;
	private final ModelPart l_arm_armor;
	private final ModelPart l_forearm;
	private final ModelPart l_hand;
	private final ModelPart r_arm;
	private final ModelPart r_upperarm;
	private final ModelPart r_arm_armor;
	private final ModelPart r_forearm;
	private final ModelPart r_hand;
	private final ModelPart sword;
	private final ModelPart head;
	private final ModelPart cape;
	private final ModelPart bone8;
	private final ModelPart bone;
	private final ModelPart bone2;
	private final ModelPart bone3;
	private final ModelPart bone4;
	private final ModelPart bone5;
	private final ModelPart bone6;
	private final ModelPart bone7;
	private final ModelPart leg;
	private final ModelPart r_leg;
	private final ModelPart r_thigh;
	private final ModelPart r_knee;
	private final ModelPart r_calf;
	private final ModelPart r_feet;
	private final ModelPart l_leg;
	private final ModelPart l_thigh;
	private final ModelPart l_knee;
	private final ModelPart l_calf;
	private final ModelPart l_feet;

	// 动画相关字段
	private float animationTime;
	private AnimationDefinition currentAnimation;

	// 动画状态
	public final AnimationState walkAnimationState = new AnimationState();
	public final AnimationState rushAnimationState = new AnimationState();
	public final AnimationState attackAnimationState = new AnimationState();
	public final AnimationState jumpAnimationState = new AnimationState();

	@Override
	public ModelPart root() {
		return boss;
	}

	public CommanderOfTheUndeadGuardModel(ModelPart root) {
		this.boss = root.getChild("boss");
		this.body = this.boss.getChild("body");
		this.up = this.body.getChild("up");
		this.bone9 = this.up.getChild("bone9");
		this.arm = this.up.getChild("arm");
		this.l_arm = this.arm.getChild("l_arm");
		this.l_upperarm = this.l_arm.getChild("l_upperarm");
		this.l_arm_armor = this.l_upperarm.getChild("l_arm_armor");
		this.l_forearm = this.l_upperarm.getChild("l_forearm");
		this.l_hand = this.l_forearm.getChild("l_hand");
		this.r_arm = this.arm.getChild("r_arm");
		this.r_upperarm = this.r_arm.getChild("r_upperarm");
		this.r_arm_armor = this.r_upperarm.getChild("r_arm_armor");
		this.r_forearm = this.r_upperarm.getChild("r_forearm");
		this.r_hand = this.r_forearm.getChild("r_hand");
		this.sword = this.r_hand.getChild("sword");
		this.head = this.up.getChild("head");
		this.cape = this.up.getChild("cape");
		this.bone8 = this.cape.getChild("bone8");
		this.bone = this.cape.getChild("bone");
		this.bone2 = this.cape.getChild("bone2");
		this.bone3 = this.cape.getChild("bone3");
		this.bone4 = this.cape.getChild("bone4");
		this.bone5 = this.cape.getChild("bone5");
		this.bone6 = this.cape.getChild("bone6");
		this.bone7 = this.cape.getChild("bone7");
		this.leg = this.body.getChild("leg");
		this.r_leg = this.leg.getChild("r_leg");
		this.r_thigh = this.r_leg.getChild("r_thigh");
		this.r_knee = this.r_thigh.getChild("r_knee");
		this.r_calf = this.r_thigh.getChild("r_calf");
		this.r_feet = this.r_calf.getChild("r_feet");
		this.l_leg = this.leg.getChild("l_leg");
		this.l_thigh = this.l_leg.getChild("l_thigh");
		this.l_knee = this.l_thigh.getChild("l_knee");
		this.l_calf = this.l_thigh.getChild("l_calf");
		this.l_feet = this.l_calf.getChild("l_feet");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition boss = partdefinition.addOrReplaceChild("boss", CubeListBuilder.create(), PartPose.offset(-3.0F, 6.0F, -1.0F));

		PartDefinition body = boss.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, -10.0F, 1.0F));

		PartDefinition up = body.addOrReplaceChild("up", CubeListBuilder.create(), PartPose.offset(0.0F, 10.0F, -1.0F));

		PartDefinition bone9 = up.addOrReplaceChild("bone9", CubeListBuilder.create().texOffs(142, 20).addBox(-1.0F, 7.0F, -2.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(70, 117).addBox(-3.0F, 5.0F, -3.0F, 5.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(96, 19).addBox(-3.0F, 3.0F, -5.0F, 5.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(96, 8).addBox(-3.0F, 2.0F, -5.0F, 6.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(46, 121).addBox(-4.0F, -1.0F, -4.0F, 1.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(30, 100).addBox(-4.0F, -4.0F, -6.0F, 1.0F, 3.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(92, 85).addBox(-4.0F, -10.0F, -7.0F, 1.0F, 6.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(56, 100).addBox(3.0F, -4.0F, -6.0F, 1.0F, 3.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(92, 121).addBox(3.0F, -1.0F, -4.0F, 1.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(0, 95).addBox(3.0F, -10.0F, -7.0F, 1.0F, 6.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(148, 151).addBox(2.0F, -11.0F, -6.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(122, 95).addBox(-4.0F, -11.0F, -4.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(128, 8).addBox(1.0F, -11.0F, -4.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(0, 152).addBox(2.0F, -11.0F, 4.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(142, 26).addBox(-3.0F, -12.0F, 4.0F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(40, 142).addBox(-3.0F, -12.0F, -6.0F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(92, 70).addBox(-3.0F, -1.0F, -6.0F, 6.0F, 3.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(0, 78).addBox(-3.0F, -4.0F, -7.0F, 6.0F, 3.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(82, 105).addBox(2.0F, 3.0F, -5.0F, 1.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(48, 76).addBox(-3.0F, -10.0F, -8.0F, 6.0F, 6.0F, 16.0F, new CubeDeformation(0.0F))
		.texOffs(110, 121).addBox(3.0F, 3.0F, -4.0F, 1.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(122, 85).addBox(-4.0F, 3.0F, -4.0F, 1.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(92, 117).addBox(-2.0F, 5.0F, -5.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(64, 121).addBox(2.0F, 5.0F, -4.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(10, 137).addBox(2.0F, 5.0F, 2.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(128, 143).addBox(-2.0F, 5.0F, 3.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -9.0F, 1.0F));

		PartDefinition arm = up.addOrReplaceChild("arm", CubeListBuilder.create(), PartPose.offset(3.0F, -18.0F, 10.0F));

		PartDefinition l_arm = arm.addOrReplaceChild("l_arm", CubeListBuilder.create(), PartPose.offset(-4.0F, 0.0F, -1.0F));

		PartDefinition l_upperarm = l_arm.addOrReplaceChild("l_upperarm", CubeListBuilder.create().texOffs(90, 142).addBox(-2.0F, 7.0F, 2.0F, 4.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(0, 125).addBox(-2.0F, -1.0F, 1.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(40, 146).addBox(-2.0F, 0.0F, 0.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition l_arm_armor = l_upperarm.addOrReplaceChild("l_arm_armor", CubeListBuilder.create().texOffs(144, 68).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(104, 105).addBox(-7.0F, -5.0F, -1.0F, 6.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(80, 125).addBox(-1.0F, -4.0F, 1.0F, 2.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(10, 141).addBox(-7.0F, -4.0F, 5.0F, 6.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(30, 95).addBox(-7.0F, 0.0F, 5.0F, 6.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(144, 74).addBox(-1.0F, -6.0F, -3.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(124, 31).addBox(-9.0F, -4.0F, -1.0F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(40, 78).addBox(-9.0F, -6.0F, -3.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, 0.0F, 1.0F));

		PartDefinition l_forearm = l_upperarm.addOrReplaceChild("l_forearm", CubeListBuilder.create().texOffs(128, 61).addBox(-2.0F, 0.0F, -3.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(30, 137).addBox(-2.0F, -3.0F, 1.0F, 4.0F, 9.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(122, 132).addBox(2.0F, -1.0F, -2.0F, 1.0F, 7.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(132, 132).addBox(-3.0F, -1.0F, -2.0F, 1.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 8.0F, 4.0F));

		PartDefinition l_hand = l_forearm.addOrReplaceChild("l_hand", CubeListBuilder.create().texOffs(144, 102).addBox(-1.0F, 0.0F, -3.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(80, 146).addBox(-2.0F, 0.0F, 0.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(70, 98).addBox(-1.0F, 1.0F, -3.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(144, 59).addBox(-2.0F, 3.0F, -2.0F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(144, 62).addBox(-2.0F, 2.0F, -1.0F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));

		PartDefinition r_arm = arm.addOrReplaceChild("r_arm", CubeListBuilder.create(), PartPose.offset(-4.0F, 0.0F, -17.0F));

		PartDefinition r_upperarm = r_arm.addOrReplaceChild("r_upperarm", CubeListBuilder.create().texOffs(142, 132).addBox(-2.0F, 7.0F, -5.0F, 4.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(126, 19).addBox(-2.0F, -1.0F, -5.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(146, 80).addBox(-2.0F, 0.0F, -1.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition r_arm_armor = r_upperarm.addOrReplaceChild("r_arm_armor", CubeListBuilder.create().texOffs(144, 106).addBox(-1.0F, -4.0F, 3.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 115).addBox(-7.0F, -5.0F, -1.0F, 6.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(110, 131).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(104, 141).addBox(-7.0F, -4.0F, -2.0F, 6.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(142, 17).addBox(-7.0F, 0.0F, -3.0F, 6.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(144, 112).addBox(-1.0F, -6.0F, 5.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(64, 125).addBox(-9.0F, -4.0F, -1.0F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(40, 86).addBox(-9.0F, -6.0F, 5.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, 0.0F, -5.0F));

		PartDefinition r_forearm = r_upperarm.addOrReplaceChild("r_forearm", CubeListBuilder.create().texOffs(128, 71).addBox(-1.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(58, 137).addBox(-1.0F, -3.0F, -3.0F, 4.0F, 9.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(80, 135).addBox(3.0F, -1.0F, -2.0F, 1.0F, 7.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 137).addBox(-2.0F, -1.0F, -2.0F, 1.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 8.0F, -3.0F));

		PartDefinition r_hand = r_forearm.addOrReplaceChild("r_hand", CubeListBuilder.create().texOffs(144, 118).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(90, 146).addBox(-2.0F, 0.0F, -1.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(76, 98).addBox(-1.0F, 1.0F, 2.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(144, 65).addBox(-2.0F, 3.0F, 0.0F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(68, 144).addBox(-2.0F, 2.0F, -1.0F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 6.0F, -1.0F));

		PartDefinition sword = r_hand.addOrReplaceChild("sword", CubeListBuilder.create().texOffs(128, 81).addBox(-5.0F, -2.0F, -1.0F, 7.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(144, 51).addBox(2.0F, -4.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(22, 149).addBox(1.0F, -2.0F, 1.0F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(40, 149).addBox(1.0F, -2.0F, -2.0F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(152, 68).addBox(-6.0F, -2.0F, 1.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(152, 71).addBox(-6.0F, -2.0F, -2.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(88, 149).addBox(-6.0F, -3.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(96, 149).addBox(0.0F, -3.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(152, 74).addBox(1.0F, -5.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(152, 77).addBox(1.0F, 2.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(80, 149).addBox(-6.0F, 0.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(118, 149).addBox(0.0F, 0.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(138, 0).addBox(2.0F, -6.0F, -2.0F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(140, 31).addBox(2.0F, 2.0F, -2.0F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(96, 0).addBox(4.0F, -4.0F, -1.0F, 19.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(96, 45).addBox(4.0F, -3.0F, -2.0F, 19.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(96, 47).addBox(4.0F, -3.0F, 1.0F, 19.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(30, 98).addBox(4.0F, 0.0F, -2.0F, 19.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(96, 49).addBox(4.0F, 0.0F, 1.0F, 19.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(100, 146).addBox(23.0F, -3.0F, -1.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(30, 147).addBox(23.0F, 0.0F, -1.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(140, 37).addBox(23.0F, -2.0F, -2.0F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(104, 149).addBox(26.0F, -2.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 3.0F, 1.0F));

		PartDefinition head = up.addOrReplaceChild("head", CubeListBuilder.create().texOffs(136, 43).addBox(3.0F, -4.0F, -2.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(152, 0).addBox(4.0F, -5.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(36, 150).addBox(3.0F, -4.0F, 2.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(46, 132).addBox(-2.0F, -9.0F, 3.0F, 5.0F, 9.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(16, 127).addBox(-3.0F, -8.0F, -3.0F, 1.0F, 8.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(92, 132).addBox(-2.0F, -9.0F, -4.0F, 5.0F, 9.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(132, 151).addBox(3.0F, -4.0F, -3.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(128, 104).addBox(3.0F, -9.0F, -3.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(24, 115).addBox(-2.0F, -9.0F, -3.0F, 5.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, -20.0F, 1.0F));

		PartDefinition cape = up.addOrReplaceChild("cape", CubeListBuilder.create(), PartPose.offset(-7.0F, -20.0F, 1.0F));

		PartDefinition bone8 = cape.addOrReplaceChild("bone8", CubeListBuilder.create().texOffs(96, 31).addBox(-1.0F, -2.0F, -11.0F, 2.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(92, 52).addBox(-2.0F, 0.0F, -13.0F, 2.0F, 2.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, -1.0F, 5.0F));

		PartDefinition bone = cape.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(48, 52).addBox(-1.0F, -2.0F, -15.0F, 2.0F, 4.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 5.0F));

		PartDefinition bone2 = cape.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -2.0F, -16.0F, 2.0F, 4.0F, 22.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 7.0F, 5.0F));

		PartDefinition bone3 = cape.addOrReplaceChild("bone3", CubeListBuilder.create().texOffs(0, 26).addBox(-1.0F, -2.0F, -16.0F, 2.0F, 4.0F, 22.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 11.0F, 5.0F));

		PartDefinition bone4 = cape.addOrReplaceChild("bone4", CubeListBuilder.create().texOffs(48, 0).addBox(-1.0F, 2.0F, -16.0F, 2.0F, 4.0F, 22.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 11.0F, 5.0F));

		PartDefinition bone5 = cape.addOrReplaceChild("bone5", CubeListBuilder.create().texOffs(48, 26).addBox(-1.0F, 2.0F, -16.0F, 2.0F, 4.0F, 22.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 15.0F, 5.0F));

		PartDefinition bone6 = cape.addOrReplaceChild("bone6", CubeListBuilder.create().texOffs(0, 52).addBox(-1.0F, -2.0F, -16.0F, 2.0F, 4.0F, 22.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 23.0F, 5.0F));

		PartDefinition bone7 = cape.addOrReplaceChild("bone7", CubeListBuilder.create().texOffs(144, 122).addBox(-1.0F, -2.0F, 4.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(146, 6).addBox(-1.0F, -2.0F, -14.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(152, 3).addBox(-1.0F, -2.0F, 3.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(104, 132).addBox(-1.0F, -2.0F, 2.0F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(70, 115).addBox(-1.0F, -2.0F, 1.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(6, 152).addBox(-1.0F, -2.0F, 0.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(58, 132).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(74, 147).addBox(-1.0F, -2.0F, -2.0F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(40, 137).addBox(-1.0F, -2.0F, -3.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(22, 152).addBox(-1.0F, -2.0F, -4.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(76, 115).addBox(-1.0F, -2.0F, -5.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(40, 152).addBox(-1.0F, -2.0F, -6.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(152, 49).addBox(-1.0F, -2.0F, -7.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(136, 147).addBox(-1.0F, -2.0F, -8.0F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(24, 141).addBox(-1.0F, -2.0F, -9.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(16, 149).addBox(-1.0F, -2.0F, -11.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(152, 52).addBox(-1.0F, -2.0F, -10.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(148, 43).addBox(-1.0F, -2.0F, -12.0F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(142, 148).addBox(-1.0F, -2.0F, -16.0F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(152, 55).addBox(-1.0F, -2.0F, -15.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 27.0F, 5.0F));

		PartDefinition leg = body.addOrReplaceChild("leg", CubeListBuilder.create(), PartPose.offset(0.0F, 8.0F, -4.0F));

		PartDefinition r_leg = leg.addOrReplaceChild("r_leg", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition r_thigh = r_leg.addOrReplaceChild("r_thigh", CubeListBuilder.create().texOffs(148, 148).addBox(0.0F, 6.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(30, 127).addBox(-1.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 0.0F, 0.0F));

		PartDefinition r_knee = r_thigh.addOrReplaceChild("r_knee", CubeListBuilder.create().texOffs(50, 146).addBox(-1.0F, -2.0F, 0.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(48, 150).addBox(-1.0F, -2.0F, 2.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(110, 150).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(58, 147).addBox(-1.0F, -3.0F, -2.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(110, 146).addBox(1.0F, -2.0F, 0.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(30, 150).addBox(1.0F, -3.0F, 0.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 9.0F, -1.0F));

		PartDefinition r_calf = r_thigh.addOrReplaceChild("r_calf", CubeListBuilder.create().texOffs(128, 114).addBox(-2.0F, 1.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(140, 85).addBox(-2.0F, 0.0F, -2.0F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(124, 43).addBox(1.0F, 0.0F, -2.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(16, 125).addBox(1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 9.0F, 0.0F));

		PartDefinition r_feet = r_calf.addOrReplaceChild("r_feet", CubeListBuilder.create().texOffs(46, 115).addBox(-1.0F, 2.0F, -2.0F, 8.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(104, 138).addBox(2.0F, 1.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(144, 128).addBox(-1.0F, 0.0F, 2.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(10, 146).addBox(-1.0F, 0.0F, -3.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(82, 98).addBox(-2.0F, 0.0F, -2.0F, 1.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(142, 136).addBox(1.0F, 0.0F, -2.0F, 1.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(68, 137).addBox(-1.0F, -1.0F, -2.0F, 2.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 7.0F, 0.0F));

		PartDefinition l_leg = leg.addOrReplaceChild("l_leg", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 8.0F));

		PartDefinition l_thigh = l_leg.addOrReplaceChild("l_thigh", CubeListBuilder.create().texOffs(128, 51).addBox(-1.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(8, 149).addBox(0.0F, 6.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 0.0F, 0.0F));

		PartDefinition l_knee = l_thigh.addOrReplaceChild("l_knee", CubeListBuilder.create().texOffs(66, 147).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(54, 151).addBox(-1.0F, -2.0F, 1.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(60, 151).addBox(-1.0F, -2.0F, -2.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(128, 147).addBox(-1.0F, -3.0F, 2.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 148).addBox(1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(66, 151).addBox(1.0F, -3.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 9.0F, 0.0F));

		PartDefinition l_calf = l_thigh.addOrReplaceChild("l_calf", CubeListBuilder.create().texOffs(128, 123).addBox(-2.0F, 1.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(140, 90).addBox(-2.0F, 0.0F, -2.0F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(20, 125).addBox(1.0F, 0.0F, -2.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(128, 17).addBox(1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 9.0F, 0.0F));

		PartDefinition l_feet = l_calf.addOrReplaceChild("l_feet", CubeListBuilder.create().texOffs(104, 115).addBox(-1.0F, 2.0F, -2.0F, 8.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(126, 151).addBox(2.0F, 1.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(146, 12).addBox(-1.0F, 0.0F, 2.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(20, 146).addBox(-1.0F, 0.0F, -3.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(142, 142).addBox(-2.0F, 0.0F, -2.0F, 1.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(118, 143).addBox(1.0F, 0.0F, -2.0F, 1.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(140, 95).addBox(-1.0F, -1.0F, -2.0F, 2.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 7.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 256, 256);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		// 重置所有骨骼的旋转，避免累积旋转错误
		resetAllBones();
		
		// 处理头部旋转（在动画系统之上叠加）
		this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);
		this.head.xRot = headPitch * ((float)Math.PI / 180F);
	}
	
	/**
	 * 重置所有骨骼的旋转和位置，使用STAND动画的默认角度作为初始状态
	 */
	private void resetAllBones() {
		// 重置头部（STAND动画：0.0, 0.0, -7.5）
		this.head.xRot = 0.0F;
		this.head.yRot = 0.0F;
		this.head.zRot = -7.5F * ((float)Math.PI / 180F);
		this.head.x = 0.0F;
		this.head.y = 0.0F;
		this.head.z = 0.0F;
		
		// 重置身体（STAND动画：0.0, 0.0, 17.5）
		this.body.xRot = 0.0F;
		this.body.yRot = 0.0F;
		this.body.zRot = 17.5F * ((float)Math.PI / 180F);
		this.body.x = 0.0F;
		this.body.y = 0.0F;
		this.body.z = 0.0F;
		
		// 重置上身
		this.up.xRot = 0.0F;
		this.up.yRot = 0.0F;
		this.up.zRot = 0.0F;
		this.up.x = 0.0F;
		this.up.y = 0.0F;
		this.up.z = 0.0F;
		
		// 重置左臂（STAND动画：0.0, 0.0, -12.5）
		this.l_arm.xRot = 0.0F;
		this.l_arm.yRot = 0.0F;
		this.l_arm.zRot = -12.5F * ((float)Math.PI / 180F);
		this.l_arm.x = 0.0F;
		this.l_arm.y = 0.0F;
		this.l_arm.z = 0.0F;
		
		this.l_upperarm.xRot = 0.0F;
		this.l_upperarm.yRot = 0.0F;
		this.l_upperarm.zRot = 0.0F;
		
		this.l_forearm.xRot = 0.0F;
		this.l_forearm.yRot = 0.0F;
		this.l_forearm.zRot = 0.0F;
		
		this.l_hand.xRot = 0.0F;
		this.l_hand.yRot = 0.0F;
		this.l_hand.zRot = 0.0F;
		
		// 重置右臂（STAND动画：-81.4083, -21.6524, -66.7876）
		this.r_arm.xRot = -81.4083F * ((float)Math.PI / 180F);
		this.r_arm.yRot = -21.6524F * ((float)Math.PI / 180F);
		this.r_arm.zRot = -66.7876F * ((float)Math.PI / 180F);
		this.r_arm.x = 0.0F;
		this.r_arm.y = 0.0F;
		this.r_arm.z = 0.0F;
		
		this.r_upperarm.xRot = 0.0F;
		this.r_upperarm.yRot = 0.0F;
		this.r_upperarm.zRot = 0.0F;
		
		// 重置右前臂（STAND动画：-60.0065, -6.5025, -93.7604）
		this.r_forearm.xRot = -60.0065F * ((float)Math.PI / 180F);
		this.r_forearm.yRot = -6.5025F * ((float)Math.PI / 180F);
		this.r_forearm.zRot = -93.7604F * ((float)Math.PI / 180F);
		
		this.r_hand.xRot = 0.0F;
		this.r_hand.yRot = 0.0F;
		this.r_hand.zRot = 0.0F;
		
		// 重置左腿（STAND动画：12.3159, 2.1539, -27.2676）
		this.l_leg.xRot = 12.3159F * ((float)Math.PI / 180F);
		this.l_leg.yRot = 2.1539F * ((float)Math.PI / 180F);
		this.l_leg.zRot = -27.2676F * ((float)Math.PI / 180F);
		this.l_leg.x = 0.0F;
		this.l_leg.y = 0.0F;
		this.l_leg.z = 0.0F;
		
		this.l_thigh.xRot = 0.0F;
		this.l_thigh.yRot = 0.0F;
		this.l_thigh.zRot = 0.0F;
		
		// 重置左小腿（STAND动画：-2.5, 0.0, 17.5）
		this.l_calf.xRot = -2.5F * ((float)Math.PI / 180F);
		this.l_calf.yRot = 0.0F;
		this.l_calf.zRot = 17.5F * ((float)Math.PI / 180F);
		
		// 重置左脚（STAND动画：-7.3242, -1.6189, -4.8964）
		this.l_feet.xRot = -7.3242F * ((float)Math.PI / 180F);
		this.l_feet.yRot = -1.6189F * ((float)Math.PI / 180F);
		this.l_feet.zRot = -4.8964F * ((float)Math.PI / 180F);
		
		// 重置右腿（STAND动画：-5.0, 0.0, -32.5）
		this.r_leg.xRot = -5.0F * ((float)Math.PI / 180F);
		this.r_leg.yRot = 0.0F;
		this.r_leg.zRot = -32.5F * ((float)Math.PI / 180F);
		this.r_leg.x = 0.0F;
		this.r_leg.y = 0.0F;
		this.r_leg.z = 0.0F;
		
		this.r_thigh.xRot = 0.0F;
		this.r_thigh.yRot = 0.0F;
		this.r_thigh.zRot = 0.0F;
		
		// 重置右小腿（STAND动画：0.0, 0.0, 17.5）
		this.r_calf.xRot = 0.0F;
		this.r_calf.yRot = 0.0F;
		this.r_calf.zRot = 17.5F * ((float)Math.PI / 180F);
		
		// 重置右脚（STAND动画：0.0, 0.0, -2.5）
		this.r_feet.xRot = 0.0F;
		this.r_feet.yRot = 0.0F;
		this.r_feet.zRot = -2.5F * ((float)Math.PI / 180F);
		
		// 重置披风
		this.cape.xRot = 0.0F;
		this.cape.yRot = 0.0F;
		this.cape.zRot = 0.0F;
		
		// 重置主骨骼
		this.boss.xRot = 0.0F;
		this.boss.yRot = 0.0F;
		this.boss.zRot = 0.0F;
		this.boss.x = 0.0F;
		this.boss.y = 0.0F;
		this.boss.z = 0.0F;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		boss.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}