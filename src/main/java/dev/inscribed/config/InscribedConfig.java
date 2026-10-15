package dev.inscribed.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class InscribedConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue KNOWLEDGE_PICKUP, KNOWLEDGE_TRADE, KNOWLEDGE_INVENTORY, KNOWLEDGE_EXTRACTION, LSBC_KNOWLEDGE, JEI_RECIPES;
    public static final ModConfigSpec.IntValue KNOWLEDGE_INTERVAL;
    public static final ModConfigSpec.BooleanValue LOOT_INJECTION, CATALYST_LOOT, BOOK_LOOT, PHASE_VISUALS;
    public static final ModConfigSpec.DoubleValue LOOT_MULTIPLIER, TREASURE_LOOT_MULTIPLIER;
    public static final ModConfigSpec.IntValue BOOKSHELF_RADIUS, CHISELED_RADIUS, PEDESTAL_RADIUS, MAX_PEDESTALS, VERTICAL_RADIUS, INSPECTION_TICKS, BOOK_COOLDOWN, SELECTION_TIMEOUT, SOUND_INTERVAL, PARTICLE_INTERVAL;
    public static final ModConfigSpec.BooleanValue REQUIRE_SHIFT, RENDER_ITEMS, PARTICLES, DYNAMIC_LIGHT;
    public static final ModConfigSpec.BooleanValue ENABLE_INFUSION, ENABLE_EXTRACTION, RESTRICT_ANVIL, ENABLE_LENS, ENABLE_BOOK, SOUNDS;
    public static final ModConfigSpec.DoubleValue TARGET_SCALE, PEDESTAL_ITEM_SCALE, ITEM_SCALE, TARGET_HEIGHT, PEDESTAL_HEIGHT, IDLE_ROTATION, RUNNING_ROTATION, IDLE_BOB, RUNNING_BOB, BOB_SPEED, RENDER_DISTANCE, INSPECTION_REACH, SELECTION_REACH, SOUND_VOLUME;
    public static final ModConfigSpec.ConfigValue<String> INFUSION_START_SOUND, EXTRACTION_START_SOUND, RUNNING_SOUND, INFUSION_COMPLETE_SOUND, EXTRACTION_COMPLETE_SOUND, EXTRACTION_FAILURE_SOUND;
    public static final ModConfigSpec.ConfigValue<String> VALIDATION_SOUND, ERROR_SOUND, EXTRACTION_TRANSFER_SOUND, INFUSION_AMBIENT_SOUND;
    static {
        var b = new ModConfigSpec.Builder();
        b.push("altar");
        BOOKSHELF_RADIUS = b.comment("Horizontal Euclidean radius; vertical range is controlled by vertical_radius.")
                .defineInRange("bookshelf_radius", 5, 1, 16);
        CHISELED_RADIUS = b.defineInRange("chiseled_bookshelf_radius", 5, 1, 16);
        PEDESTAL_RADIUS = b.defineInRange("pedestal_radius", 4, 1, 16);
        MAX_PEDESTALS = b.comment("Exceeding this count rejects the altar rather than picking a subset.")
                .defineInRange("max_pedestals", 8, 1, 8);
        VERTICAL_RADIUS=b.comment("Vertical scan distance above/below the table.").defineInRange("vertical_radius",2,0,16);
        b.pop().push("ritual");
        REQUIRE_SHIFT = b.define("require_shift_right_click", true);
        ENABLE_INFUSION=b.define("enable_infusion",true);
        ENABLE_EXTRACTION=b.define("enable_extraction",true);
        SELECTION_TIMEOUT=b.comment("Infusion selection expiry in server ticks.").defineInRange("selection_timeout_ticks",1200,20,72000);
        SELECTION_REACH=b.defineInRange("selection_reach",8.0,1.0,32.0);
        b.pop().push("anvil");
        RESTRICT_ANVIL=b.comment("Require the result to preserve base enchantments. False restores vanilla enchantment transfers/merges.").define("restrict_enchantment_changes",true);
        b.pop().push("inspection");
        ENABLE_LENS=b.define("enable_lsbc_lens",true);
        ENABLE_BOOK=b.define("enable_book_fallback",true);
        INSPECTION_TICKS=b.defineInRange("analysis_ticks",40,1,1200);
        INSPECTION_REACH=b.defineInRange("reach",4.0,1.0,16.0);
        BOOK_COOLDOWN=b.defineInRange("book_cooldown_ticks",20,0,1200);
        b.pop().push("visuals");
        RENDER_ITEMS = b.define("render_items", true);
        PARTICLES = b.define("particles", true);
        PHASE_VISUALS = b.comment("Phase motion, directional transfer and short completion fade.").define("phase_ritual_effects", true);
        DYNAMIC_LIGHT = b.comment("MVP: full-bright ritual items only; never changes world light or blocks.")
                .define("dynamic_light", false);
        TARGET_SCALE=b.comment("Multiplier for the table target, using item_scale as its base; pedestal_item_scale is independent.").defineInRange("target_scale",2.0,0.1,8.0);
        PEDESTAL_ITEM_SCALE=b.defineInRange("pedestal_item_scale",.80,.05,4.0);
        ITEM_SCALE=b.defineInRange("item_scale",.55,.05,4.0);
        TARGET_HEIGHT=b.defineInRange("target_height",1.65,0.0,5.0);
        PEDESTAL_HEIGHT=b.defineInRange("pedestal_height",1.15,0.0,5.0);
        IDLE_ROTATION=b.defineInRange("idle_rotation_degrees_per_tick",.8,0.0,30.0);
        RUNNING_ROTATION=b.defineInRange("running_rotation_degrees_per_tick",3.0,0.0,30.0);
        IDLE_BOB=b.defineInRange("idle_bob_height",.035,0.0,1.0);
        RUNNING_BOB=b.defineInRange("running_bob_height",.065,0.0,1.0);
        BOB_SPEED=b.defineInRange("bob_speed",.08,0.0,2.0);
        RENDER_DISTANCE=b.defineInRange("target_render_distance",64.0,8.0,256.0);
        PARTICLE_INTERVAL=b.defineInRange("particle_interval_ticks",5,1,100);
        b.pop().push("sounds");
        SOUNDS=b.define("enabled",true);
        SOUND_INTERVAL=b.defineInRange("running_interval_ticks",20,1,1200);
        SOUND_VOLUME=b.comment("Multiplier for all ritual sounds; zero mutes.").defineInRange("volume_multiplier",1.0,0.0,4.0);
        INFUSION_START_SOUND=sound(b,"infusion_start","minecraft:block.beacon.activate");
        INFUSION_AMBIENT_SOUND=sound(b,"infusion_ambient","minecraft:block.beacon.ambient");
        EXTRACTION_START_SOUND=sound(b,"extraction_start","minecraft:block.enchantment_table.use");
        RUNNING_SOUND=sound(b,"running","minecraft:block.amethyst_block.chime");
        INFUSION_COMPLETE_SOUND=sound(b,"infusion_complete","minecraft:entity.player.levelup");
        EXTRACTION_COMPLETE_SOUND=sound(b,"extraction_complete","minecraft:item.book.page_turn");
        EXTRACTION_FAILURE_SOUND=sound(b,"extraction_failure","minecraft:block.fire.extinguish");
        VALIDATION_SOUND=sound(b,"validation","minecraft:item.book.page_turn");
        ERROR_SOUND=sound(b,"invalid_ritual","minecraft:block.note_block.bass");
        EXTRACTION_TRANSFER_SOUND=sound(b,"extraction_transfer","minecraft:item.book.page_turn");
        b.pop().push("knowledge");
        KNOWLEDGE_PICKUP=b.define("learn_on_pickup",true);
        KNOWLEDGE_TRADE=b.define("learn_on_trade",true);
        KNOWLEDGE_INVENTORY=b.define("learn_on_inventory_acquisition",true);
        KNOWLEDGE_EXTRACTION=b.define("learn_on_successful_extraction",true);
        KNOWLEDGE_INTERVAL=b.defineInRange("inventory_scan_interval_ticks",10,1,1200);
        LSBC_KNOWLEDGE=b.define("lsbc_word_integration",true);
        JEI_RECIPES=b.define("jei_recipe_integration",true);
        b.pop().push("loot");
        LOOT_INJECTION=b.define("enable_loot_injection",true);
        CATALYST_LOOT=b.define("enable_catalyst_loot",true);
        BOOK_LOOT=b.define("enable_book_loot_injection",true);
        LOOT_MULTIPLIER=b.comment("Multiplier for pool roll chance, clamped to 1 after multiplication.").defineInRange("loot_multiplier",1.0,0.0,10.0);
        TREASURE_LOOT_MULTIPLIER=b.comment("Additional multiplier for pools marked treasure=true.").defineInRange("treasure_loot_multiplier",1.0,0.0,10.0);
        b.pop(); SPEC = b.build();
    }
    private static ModConfigSpec.ConfigValue<String> sound(ModConfigSpec.Builder b,String name,String value){return b.comment("Registered sound event ID; unknown IDs fall back to the vanilla sound.").define(name,value,v->v instanceof String s && net.minecraft.resources.ResourceLocation.tryParse(s)!=null);}
    public record Settings(int bookshelfRadius, int chiseledRadius, int pedestalRadius, int maxPedestals, int verticalRadius, boolean infusion, boolean extraction) {}
    public static Settings settings() {
        return new Settings(BOOKSHELF_RADIUS.get(), CHISELED_RADIUS.get(), PEDESTAL_RADIUS.get(), MAX_PEDESTALS.get(),VERTICAL_RADIUS.get(),ENABLE_INFUSION.get(),ENABLE_EXTRACTION.get());
    }
    private InscribedConfig() {}
}
