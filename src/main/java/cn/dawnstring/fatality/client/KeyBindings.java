package cn.dawnstring.fatality.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

/**
 * 键盘绑定管理
 */
public class KeyBindings {
    public static final KeyMapping OPEN_ATTRIBUTE_PANEL = new KeyMapping(
            "key.fatality.open_attribute_panel",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            "key.categories.fatality"
    );

    public static final KeyMapping ACTIVATE_MECHANICAL_HEART = new KeyMapping(
            "key.fatality.activate_mechanical_heart",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "key.categories.fatality"
    );

    public static final KeyMapping ACTIVATE_ELEMENTAL_HEART = new KeyMapping(
            "key.fatality.activate_elemental_heart",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            "key.categories.fatality"
    );
}