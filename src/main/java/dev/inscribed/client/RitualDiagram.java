package dev.inscribed.client;
import dev.inscribed.registry.InscribedRegistries;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.*;
/** Uses resource-pack-aware vanilla sprites; no recipe knowledge or hidden names are exposed. */
public final class RitualDiagram {
    public static void render(GuiGraphics g,int x,int y,int width,boolean extraction){
        int step=Math.max(28,(width-16)/2);
        g.renderItem(new ItemStack(extraction?Items.DIAMOND_SWORD:Items.CHISELED_BOOKSHELF),x,y+5);
        if(!extraction)g.renderItem(new ItemStack(InscribedRegistries.PEDESTAL.get()),x,y+24);
        g.renderItem(new ItemStack(Items.ENCHANTING_TABLE),x+step,y+12);
        g.renderItem(new ItemStack(extraction?Items.ENCHANTED_BOOK:Items.DIAMOND_SWORD),x+step*2,y+12);
        int color=extraction?0xFF9360A4:0xFFAB8436;
        arrow(g,x+20,y+20,x+step-3,color);arrow(g,x+step+20,y+20,x+step*2-3,color);
    }
    private static void arrow(GuiGraphics g,int x,int y,int end,int color){g.hLine(x,end,y,color);for(int n=0;n<4;n++){g.fill(end-n,y-n,end-n+1,y-n+1,color);g.fill(end-n,y+n,end-n+1,y+n+1,color);}}
    private RitualDiagram(){}
}
