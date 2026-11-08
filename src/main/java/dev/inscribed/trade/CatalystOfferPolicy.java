package dev.inscribed.trade;
import net.minecraft.world.entity.npc.Villager;
/** Runtime ownership only; no custom item components or changes to vanilla offer serialization. */
public interface CatalystOfferPolicy {
    void inscribed$setOwner(Villager owner);
}
