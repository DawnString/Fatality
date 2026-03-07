package cn.dawnstring.fatality.items.weapon.melee;

import cn.dawnstring.fatality.items.BaseWeapon;
import cn.dawnstring.fatality.items.WeaponEnum;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

import static cn.dawnstring.fatality.Fatality.DOT_ITEM_DES;

public class DragonfangDagger extends BaseWeapon
{
    public DragonfangDagger() {
        super(new Tier() {
            @Override
            public int getUses() {
                return 0;
            }

            @Override
            public float getSpeed() {
                return 0;
            }

            @Override
            public float getAttackDamageBonus() {
                return 0;
            }

            @Override
            public int getLevel() {
                return 0;
            }

            @Override
            public int getEnchantmentValue() {
                return 0;
            }

            @Override
            public Ingredient getRepairIngredient() {
                return null;
            }
        }, new Properties(), 2000,1f, 1, 0.2f, 1.5f, 0.3f, WeaponEnum.MELEE);

        setStory("由龙牙制作的匕首\n" +
                DOT_ITEM_DES);
    }
}
