package cn.dawnstring.fatality.mixins;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.BossEvent;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EndDragonFight.class)
public class EndDragonFightMixin {

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerBossEvent;<init>(Lnet/minecraft/network/chat/Component;Lnet/minecraft/world/BossEvent$BossBarColor;Lnet/minecraft/world/BossEvent$BossBarOverlay;)V"))
    private ServerBossEvent onCreateBossEvent(Component name, BossEvent.BossBarColor color, BossEvent.BossBarOverlay overlay) {
        ServerBossEvent bossEvent = new ServerBossEvent(name, color, overlay);
        bossEvent.setCreateWorldFog(false);
        bossEvent.setDarkenScreen(false);
        return bossEvent;
    }
}
