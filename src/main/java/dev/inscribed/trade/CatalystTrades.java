package dev.inscribed.trade;

import dev.inscribed.config.InscribedConfig;
import dev.inscribed.registry.InscribedRegistries;
import net.minecraft.world.entity.npc.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.trading.*;

/** One additive, persistent librarian offer. Keep saved offer order and stock intact when unavailable. */
public final class CatalystTrades {
    public static boolean available(Villager villager) {
        return InscribedConfig.CATALYST_TRADE.get()
                && villager.getVillagerData().getLevel() >= InscribedConfig.CATALYST_TRADE_LEVEL.get();
    }
    public static boolean librarian(Merchant merchant) {
        return merchant instanceof Villager v && v.getVillagerData().getProfession() == VillagerProfession.LIBRARIAN;
    }
    public static MerchantOffers offers(Merchant merchant, MerchantOffers saved) {
        if (!librarian(merchant)) return saved;
        var villager = (Villager) merchant;
        if (available(villager)) {
            if (saved.stream().noneMatch(o -> catalyst(o.getResult()))) saved.add(new MerchantOffer(
                    new ItemCost(Items.EMERALD, InscribedConfig.CATALYST_EMERALD_COST.get()),
                    new ItemStack(InscribedRegistries.ENCHANTMENT_CATALYST.get()),
                    InscribedConfig.CATALYST_TRADE_MAX_USES.get(), 15, .05F));
        }
        saved.stream().filter(o -> catalyst(o.getResult())).forEach(o -> ((CatalystOfferPolicy)o).inscribed$setOwner(villager));
        return saved;
    }
    public static boolean catalyst(ItemStack stack) { return stack.is(InscribedRegistries.ENCHANTMENT_CATALYST.get()); }
    public static boolean canTake(Merchant merchant, ItemStack result) {
        return !librarian(merchant) || !catalyst(result) || available((Villager)merchant);
    }
    private CatalystTrades() {}
}
