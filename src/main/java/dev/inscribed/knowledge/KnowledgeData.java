package dev.inscribed.knowledge;

import java.util.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

/** Private server attachment. Stable identifiers, independent of LSBC and displayed language. */
public record KnowledgeData(Set<ResourceLocation> knownEnchantments, Set<ResourceLocation> knownRecipes) {
    public static final Codec<KnowledgeData> CODEC = RecordCodecBuilder.create(i -> i.group(
        ResourceLocation.CODEC.listOf().optionalFieldOf("known_enchantments", List.of()).forGetter(k -> List.copyOf(k.knownEnchantments)),
        ResourceLocation.CODEC.listOf().optionalFieldOf("known_recipes", List.of()).forGetter(k -> List.copyOf(k.knownRecipes))
    ).apply(i, (e,r) -> new KnowledgeData(new HashSet<>(e),new HashSet<>(r))));
    public KnowledgeData { knownEnchantments=Set.copyOf(knownEnchantments); knownRecipes=Set.copyOf(knownRecipes); }
    public static KnowledgeData empty() { return new KnowledgeData(Set.of(),Set.of()); }
    public KnowledgeData change(ResourceLocation id, boolean enchantment, boolean learn) {
        var e=new HashSet<>(knownEnchantments); var r=new HashSet<>(knownRecipes);
        var set=enchantment?e:r; if(learn)set.add(id);else set.remove(id);
        return new KnowledgeData(e,r);
    }
}
