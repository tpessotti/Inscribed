package dev.inscribed.knowledge;
import java.util.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
/** Only permitted recipes and discovered enchantments cross the network. */
public record GuideSnapshot(long generation, List<KnownEnchantment> enchantments, List<Recipe> recipes) {
    public record KnownEnchantment(ResourceLocation id,Component name,Component searchable,int maxLevel) {}
    public record Cost(List<ItemStack> alternatives,int count) {public Cost {alternatives=List.copyOf(alternatives);}}
    public record Recipe(ResourceLocation id,ResourceLocation enchantment,int level,boolean extraction,
        Component name,Component searchable,int power,int ticks,double chance,int emptyBooks,boolean exact,
        List<Cost> costs,List<ItemStack> targets) {public Recipe {costs=List.copyOf(costs);targets=List.copyOf(targets);}}
    public static GuideSnapshot empty(){return new GuideSnapshot(-1,List.of(),List.of());}
    public GuideSnapshot {enchantments=List.copyOf(enchantments);recipes=List.copyOf(recipes);}
}
