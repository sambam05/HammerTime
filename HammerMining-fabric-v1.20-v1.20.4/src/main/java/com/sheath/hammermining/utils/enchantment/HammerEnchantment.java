package com.sheath.hammermining.utils.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;

/**
 * Simple, single-level hammer enchantment for 1.20.x runtime registration.
 */
public class HammerEnchantment extends Enchantment {
    public HammerEnchantment() {
        super(Rarity.UNCOMMON, EnchantmentTarget.DIGGER, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMinPower(int level) {
        return 15 + (level - 1) * 5;
    }

    @Override
    public int getMaxPower(int level) {
        return 30 + (level - 1) * 5;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public boolean isTreasure() {
        return false;
    }
}
