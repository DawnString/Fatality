package cn.dawnstring.fatality.mixins;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import cn.dawnstring.fatality.inventory.AccessoryInventory;
import cn.dawnstring.fatality.system.ManaSystem;

@Mixin(Player.class)
public class PlayerMixin {

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void saveAccessoryData(CompoundTag tag, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        AccessoryInventory inventory = AccessoryInventory.get(player);
        tag.put("FatalityAccessory", inventory.serializeNBT());

        // 保存魔法值数据
        CompoundTag manaData = new CompoundTag();
        manaData.putFloat("currentMana", ManaSystem.getCurrentMana(player));
        tag.put("FatalityMana", manaData);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void loadAccessoryData(CompoundTag tag, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        AccessoryInventory inventory = AccessoryInventory.get(player);
        if (tag.contains("FatalityAccessory")) {
            inventory.deserializeNBT(tag.getCompound("FatalityAccessory"));
            inventory.updatePlayerAttributes();
        }

        // 加载魔法值数据
        if (tag.contains("FatalityMana")) {
            CompoundTag manaData = tag.getCompound("FatalityMana");
            float savedMana = manaData.getFloat("currentMana");
            ManaSystem.setCurrentMana(player, savedMana);
        }
    }
}