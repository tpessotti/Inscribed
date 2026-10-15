package dev.inscribed.registry;

import dev.inscribed.Inscribed;
import dev.inscribed.altar.AltarState;
import dev.inscribed.pedestal.InfusionPedestalBlock;
import dev.inscribed.pedestal.PedestalBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.*;

public final class InscribedRegistries {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Inscribed.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Inscribed.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Inscribed.MOD_ID);
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Inscribed.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Inscribed.MOD_ID);
    public static final DeferredRegister<com.mojang.serialization.MapCodec<? extends net.neoforged.neoforge.common.loot.IGlobalLootModifier>> LOOT_MODIFIERS = DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Inscribed.MOD_ID);
    public static final DeferredHolder<com.mojang.serialization.MapCodec<? extends net.neoforged.neoforge.common.loot.IGlobalLootModifier>, com.mojang.serialization.MapCodec<dev.inscribed.loot.ExplorationLootModifier>> EXPLORATION_LOOT = LOOT_MODIFIERS.register("exploration", () -> dev.inscribed.loot.ExplorationLootModifier.CODEC);
    public static final DeferredBlock<InfusionPedestalBlock> PEDESTAL = BLOCKS.register("infusion_pedestal",
            () -> new InfusionPedestalBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS).noOcclusion().pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK)));
    public static final DeferredItem<dev.inscribed.knowledge.GuideBookItem> GUIDE_BOOK = ITEMS.register("guide_book", () -> new dev.inscribed.knowledge.GuideBookItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<dev.inscribed.knowledge.KnowledgeData>> KNOWLEDGE = ATTACHMENTS.register("knowledge",
            () -> AttachmentType.builder(dev.inscribed.knowledge.KnowledgeData::empty).serialize(dev.inscribed.knowledge.KnowledgeData.CODEC).copyOnDeath().build());
    public static final DeferredItem<BlockItem> PEDESTAL_ITEM = ITEMS.registerSimpleBlockItem(PEDESTAL);
    public static final DeferredItem<Item> ENCHANTMENT_CATALYST = ITEMS.registerSimpleItem("enchantment_catalyst", new Item.Properties());
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PedestalBlockEntity>> PEDESTAL_ENTITY = BLOCK_ENTITIES.register("infusion_pedestal",
            () -> BlockEntityType.Builder.of(PedestalBlockEntity::new, PEDESTAL.get()).build(null));
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<AltarState>> ALTAR = ATTACHMENTS.register("altar_target",
            () -> AttachmentType.builder(AltarState::empty).serialize(AltarState.CODEC).sync(AltarState.STREAM_CODEC).build());
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("inscribed",
            () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.inscribed"))
                    .icon(() -> new ItemStack(PEDESTAL_ITEM.get())).displayItems((p, out) -> {out.accept(PEDESTAL_ITEM.get());out.accept(GUIDE_BOOK.get());out.accept(ENCHANTMENT_CATALYST.get());}).build());
    public static void register(IEventBus bus) {
        BLOCKS.register(bus); ITEMS.register(bus); BLOCK_ENTITIES.register(bus); ATTACHMENTS.register(bus); TABS.register(bus);
        LOOT_MODIFIERS.register(bus);
    }
    private InscribedRegistries() {}
}
