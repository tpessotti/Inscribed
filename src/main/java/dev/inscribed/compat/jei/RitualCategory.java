package dev.inscribed.compat.jei;
import java.util.*;
import dev.inscribed.knowledge.GuideSnapshot;
import mezz.jei.api.recipe.*;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.gui.builder.*;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.resources.ResourceLocation;

public final class RitualCategory implements IRecipeCategory<InscribedJeiPlugin.DisplayRecipe> {
    private final RecipeType<InscribedJeiPlugin.DisplayRecipe> type;private final boolean extraction;private final IDrawable icon;
    public RitualCategory(RecipeType<InscribedJeiPlugin.DisplayRecipe> t,boolean e,IGuiHelper gui){type=t;extraction=e;icon=gui.createDrawableItemLike(e?Items.BOOK:Items.ENCHANTED_BOOK);}
    public RecipeType<InscribedJeiPlugin.DisplayRecipe> getRecipeType(){return type;}
    public Component getTitle(){return Component.translatable("guide.inscribed."+(extraction?"extraction":"infusion"));}
    public IDrawable getIcon(){return icon;}
    public int getWidth(){return 180;}
    public int getHeight(){return 210;}
    public ResourceLocation getRegistryName(InscribedJeiPlugin.DisplayRecipe r){return r.view.id();}
    private static ItemStack enchanted(ItemStack base,GuideSnapshot.Recipe r){
        var stack=base.copy();var mc=Minecraft.getInstance();if(mc.level!=null)mc.level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(r.enchantment()).ifPresent(h->stack.enchant(h,r.level()));return stack;
    }
    private static void protectedTooltip(IRecipeSlotBuilder slot,InscribedJeiPlugin.DisplayRecipe row){slot.addTooltipCallback((view,tooltip)->{tooltip.clear();view.getDisplayedItemStack().ifPresent(s->tooltip.add(s.getHoverName()));tooltip.add(row.view.name());});}
    public void setRecipe(IRecipeLayoutBuilder b,InscribedJeiPlugin.DisplayRecipe row,IFocusGroup focus){
        var r=row.view;var targets=r.targets().isEmpty()?List.of(new ItemStack(Items.BOOK)):r.targets();
        var input=b.addInputSlot(4,24).setStandardSlotBackground().addItemStacks(extraction?targets.stream().map(s->enchanted(s,r)).toList():targets);
        if(extraction)protectedTooltip(input,row);
        var book=b.addInputSlot(28,24).setStandardSlotBackground().addItemStack(extraction?new ItemStack(Items.BOOK,r.emptyBooks()):enchanted(new ItemStack(Items.ENCHANTED_BOOK),r));if(!extraction)protectedTooltip(book,row);
        var output=b.addOutputSlot(150,24).setOutputSlotBackground().addItemStacks(extraction?List.of(enchanted(new ItemStack(Items.ENCHANTED_BOOK),r)):targets.stream().map(s->enchanted(s,r)).toList());protectedTooltip(output,row);
        for(int i=0;i<r.costs().size();i++){var c=r.costs().get(i);var stacks=c.alternatives().stream().map(s->{var copy=s.copy();copy.setCount(Math.min(c.count(),copy.getMaxStackSize()));return copy;}).toList();var slot=b.addInputSlot(4+(i%8)*22,51+(i/8)*20).setStandardSlotBackground().addItemStacks(stacks);slot.addTooltipCallback((v,t)->t.add(Component.translatable("guide.inscribed.quantity",c.count())));}
    }
    public void draw(InscribedJeiPlugin.DisplayRecipe row,IRecipeSlotsView slots,GuiGraphics g,double x,double y){
        var r=row.view;var font=Minecraft.getInstance().font;g.drawString(font,r.name(),3,2,0xFF303030,false);
        g.drawString(font,Component.literal("→"),110,27,0xFF303030,false);
        int baseline=Math.max(76,55+((r.costs().size()+7)/8)*20);
        var lines=List.of(Component.translatable("guide.inscribed.power",r.power()),Component.translatable("guide.inscribed.duration",r.ticks(),r.ticks()/20.0),Component.translatable("guide.inscribed."+(extraction?"failure":"consumption"),Math.round(r.chance()*100)),extraction?Component.translatable("guide.inscribed.books",r.emptyBooks()):Component.translatable("guide.inscribed."+(r.exact()?"book_exact":"book_minimum")));
        for(var line:lines)for(var wrapped:font.split(line,174)){g.drawString(font,wrapped,3,baseline,0xFF303030,false);baseline+=11;}
        for(var wrapped:font.split(Component.translatable("guide.inscribed.pedestal_constraints"),174)){g.drawString(font,wrapped,3,baseline,0xFF303030,false);baseline+=11;}
    }
}
