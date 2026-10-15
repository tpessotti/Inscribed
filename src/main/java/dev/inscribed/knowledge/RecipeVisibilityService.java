package dev.inscribed.knowledge;
import dev.inscribed.recipe.EnchantmentRecipe;
public final class RecipeVisibilityService {
    public static boolean visible(EnchantmentRecipe r, KnowledgeData k) {
        return switch(r.visibility()) {
            case ALWAYS -> true;
            case ON_ENCHANTMENT_DISCOVERY -> k.knownEnchantments().contains(r.enchantment());
            case EXPLICIT_DISCOVERY -> k.knownRecipes().contains(r.id());
        };
    }
    public static String reason(EnchantmentRecipe r, KnowledgeData k) {
        return (visible(r,k)?"visible: ":"hidden: ")+r.visibility().name();
    }
    private RecipeVisibilityService() {}
}
