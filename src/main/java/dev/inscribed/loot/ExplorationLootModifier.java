package dev.inscribed.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.LootModifier;

/** Additive public NeoForge GLM. Never replaces existing structure loot. */
public final class ExplorationLootModifier extends LootModifier {
    public static final MapCodec<ExplorationLootModifier> CODEC = RecordCodecBuilder.mapCodec(i -> codecStart(i).apply(i, ExplorationLootModifier::new));
    public ExplorationLootModifier(LootItemCondition[] conditions) { super(conditions); }
    @Override protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        LootInjection.inject(loot, context);
        return loot;
    }
    @Override public MapCodec<ExplorationLootModifier> codec() { return CODEC; }
}
