package dev.inscribed.client;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
/** Two mirrored vanilla book leaves, cropped to their visible cover. */
public final class OpenBookBackground {
    private static final ResourceLocation BOOK=ResourceLocation.withDefaultNamespace("textures/gui/book.png");
    public static void render(GuiGraphics g,int x,int y,int width,int height) {
        g.fill(x+3,y+5,x+width+4,y+height+5,0x60000000);int half=width/2;
        g.blit(BOOK,x,y,half,height,168,0,-148,182,256,256);
        g.pose().pushPose();g.pose().translate(x+half,y,0);g.pose().scale((width-half)/148f,height/182f,1);g.blit(BOOK,0,0,20,0,148,182);g.pose().popPose();
        g.fill(x+half-2,y+7,x+half+2,y+height-8,0x806B4930);
    }
    private OpenBookBackground() {}
}
