package cn.dawnstring.fatality.client;

import cn.dawnstring.fatality.items.accessory.HeartOfTheElements;
import cn.dawnstring.fatality.items.accessory.MechanicalHeart;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "fatality", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class KeyInputHandler {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (KeyBindings.OPEN_ATTRIBUTE_PANEL.consumeClick() && minecraft.screen == null) {
            minecraft.setScreen(new AttributePanelScreen());
        }

        // 检查机械之心激活按键
        if (KeyBindings.ACTIVATE_MECHANICAL_HEART.consumeClick()) {
            handleMechanicalHeartActivation(minecraft.player);
        }

        // 检查元素之心激活按键（使用不同的按键）
        if (KeyBindings.ACTIVATE_ELEMENTAL_HEART.consumeClick()) {
            handleElementalHeartActivation(minecraft.player);
        }
    }

    private static void handleMechanicalHeartActivation(Player player) {
        if (MechanicalHeart.activate(player)) {
            // 激活成功
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§a机械之心已激活！伤害加成50%"), true);
        } else if (MechanicalHeart.hasMechanicalHeartEquipped(player)) {
            if (MechanicalHeart.isActive(player)) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c机械之心已在激活状态"), true);
            } else if (!MechanicalHeart.isFullyCharged(player)) {
                float progress = MechanicalHeart.getChargeProgress(player) * 100;
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§e机械之心充能中: " + String.format("%.0f%%", progress)), true);
            }
        }
    }

    private static void handleElementalHeartActivation(Player player) {
        if (HeartOfTheElements.activate(player)) {
            // 激活成功
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§a元素之心已激活！伤害加成100%"), true);
        } else if (HeartOfTheElements.hasHeartOfTheElementsEquipped(player)) {
            if (HeartOfTheElements.isActive(player)) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c元素之心已在激活状态"), true);
            } else if (!HeartOfTheElements.isFullyCharged(player)) {
                float progress = HeartOfTheElements.getChargeProgress(player) * 100;
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§e元素之心充能中: " + String.format("%.0f%%", progress)), true);
            }
        }
    }
}