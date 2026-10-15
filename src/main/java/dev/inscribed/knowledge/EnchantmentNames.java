package dev.inscribed.knowledge;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.enchantment.Enchantment;
public final class EnchantmentNames {
    public static Component component(Holder<Enchantment> enchantment){
        var original=enchantment.value().description();
        if(!(original.getContents() instanceof TranslatableContents key)||key.getFallback()!=null)return original;
        String fallback=enchantment.unwrapKey().map(k->friendly(k.location().getPath())).orElse("Enchantment");
        var result=Component.translatableWithFallback(key.getKey(),fallback,key.getArgs()).setStyle(original.getStyle());for(var sibling:original.getSiblings())result.append(sibling.copy());return result;
    }
    private static String friendly(String path){var result=new StringBuilder();for(var word:path.replace('_',' ').replace('-',' ').replace('/',' ').split(" +"))if(!word.isBlank()){if(!result.isEmpty())result.append(' ');result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));}return result.toString();}
    private EnchantmentNames(){}
}
