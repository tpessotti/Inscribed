package dev.inscribed.mixin;

import com.google.gson.JsonElement;
import dev.inscribed.config.ConfigRecipeStore;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

/** Inject configured crafting JSON before vanilla validates, builds and synchronizes recipes. */
@Mixin(RecipeManager.class)
public abstract class ConfigCraftingRecipesMixin {
    @ModifyVariable(method="apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",at=@At("HEAD"),argsOnly=true)
    private Map<ResourceLocation,JsonElement> inscribed$configCrafting(Map<ResourceLocation,JsonElement> data) {
        return ConfigRecipeStore.merge(ConfigRecipeStore.load(),data,"crafting_recipes");
    }
}
