package dev.inscribed;

import dev.inscribed.altar.AltarInteraction;
import dev.inscribed.command.InscribedCommands;
import dev.inscribed.config.InscribedConfig;
import dev.inscribed.recipe.InscribedRecipeRegistry;
import dev.inscribed.registry.InscribedRegistries;
import dev.inscribed.ritual.RitualController;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;

@Mod(Inscribed.MOD_ID)
public final class Inscribed {
    public static final String MOD_ID = "inscribed";
    public Inscribed(IEventBus bus, ModContainer container) {
        InscribedRegistries.register(bus);
        bus.addListener(dev.inscribed.knowledge.KnowledgeNetworking::register);
        NeoForge.EVENT_BUS.addListener(dev.inscribed.knowledge.KnowledgeCommands::register);
        NeoForge.EVENT_BUS.addListener(dev.inscribed.knowledge.KnowledgeEvents::pickup);
        NeoForge.EVENT_BUS.addListener(dev.inscribed.knowledge.KnowledgeEvents::trade);
        NeoForge.EVENT_BUS.addListener(dev.inscribed.knowledge.KnowledgeEvents::login);
        NeoForge.EVENT_BUS.addListener(dev.inscribed.knowledge.KnowledgeEvents::respawn);
        NeoForge.EVENT_BUS.addListener(dev.inscribed.knowledge.KnowledgeEvents::dimension);
        NeoForge.EVENT_BUS.addListener(dev.inscribed.knowledge.KnowledgeEvents::logout);
        NeoForge.EVENT_BUS.addListener(dev.inscribed.knowledge.KnowledgeEvents::stop);
        NeoForge.EVENT_BUS.addListener(dev.inscribed.knowledge.KnowledgeEvents::tick);

        bus.addListener(dev.inscribed.inspection.InspectionNetworking::register);
        container.registerConfig(ModConfig.Type.SERVER, InscribedConfig.SPEC);
        NeoForge.EVENT_BUS.addListener(AltarInteraction::rightClick);
        NeoForge.EVENT_BUS.addListener(InscribedRecipeRegistry::registerReload);
        NeoForge.EVENT_BUS.addListener(dev.inscribed.loot.LootInjection::registerReload);
        NeoForge.EVENT_BUS.addListener(InscribedCommands::register);
        NeoForge.EVENT_BUS.addListener(RitualController::tick);
        NeoForge.EVENT_BUS.addListener(RitualController::chunkUnload);
        NeoForge.EVENT_BUS.addListener(RitualController::levelUnload);
        NeoForge.EVENT_BUS.addListener(RitualController::stop);
        NeoForge.EVENT_BUS.addListener(RitualController::leave);
        NeoForge.EVENT_BUS.addListener(dev.inscribed.inspection.AltarInspection::tick);
        NeoForge.EVENT_BUS.addListener(dev.inscribed.inspection.AltarInspection::leave);
        NeoForge.EVENT_BUS.addListener(dev.inscribed.inspection.AltarInspection::stop);
        NeoForge.EVENT_BUS.addListener(dev.inscribed.ritual.RitualSelection::tick);
        NeoForge.EVENT_BUS.addListener(dev.inscribed.ritual.RitualSelection::leave);
        NeoForge.EVENT_BUS.addListener(dev.inscribed.ritual.RitualSelection::stop);
    }
}
