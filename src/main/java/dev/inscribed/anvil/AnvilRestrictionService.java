package dev.inscribed.anvil;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public final class AnvilRestrictionService {
    public static boolean allowed(ItemStack base,ItemStack result) {
        if(result.isEmpty() || !dev.inscribed.config.InscribedConfig.RESTRICT_ANVIL.get())return true;
        return same(base.getOrDefault(DataComponents.ENCHANTMENTS,ItemEnchantments.EMPTY),result.getOrDefault(DataComponents.ENCHANTMENTS,ItemEnchantments.EMPTY))
            && same(base.getOrDefault(DataComponents.STORED_ENCHANTMENTS,ItemEnchantments.EMPTY),result.getOrDefault(DataComponents.STORED_ENCHANTMENTS,ItemEnchantments.EMPTY));
    }
    private static boolean same(ItemEnchantments a,ItemEnchantments b) {
        if(a.size()!=b.size())return false;
        for(var e:a.entrySet())if(b.getLevel(e.getKey())!=e.getIntValue())return false;
        return true;
    }
    private AnvilRestrictionService() {}
}
