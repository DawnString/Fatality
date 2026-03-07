package cn.dawnstring.fatality.bosslist;

import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class BossListKeyBinding {
    public static final KeyMapping OPEN_BOSS_LIST = new KeyMapping(
        "key.fatality.open_boss_list",
        GLFW.GLFW_KEY_B,
        "key.categories.fatality"
    );
}