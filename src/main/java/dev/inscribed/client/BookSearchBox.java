package dev.inscribed.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.GuiGraphics;

import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.components.Tooltip;
public final class BookSearchBox extends EditBox {
    private final int outerX,outerY,outerWidth;
    private final Font measuringFont;
    public BookSearchBox(Font font,int x,int y,int width,String query,java.util.function.Consumer<String> changed) {
        super(font,x+4,y+5,width-8,8,Component.translatable("guide.inscribed.search"));outerX=x;outerY=y;outerWidth=width;
        measuringFont=font;
        setBordered(false);setTextColor(BookTheme.INK);setTextShadow(false);setMaxLength(256);setValue(query);
        setHint(Component.translatable("guide.inscribed.search"));setResponder(changed);
    }
    @Override public void setHint(Component hint) {
        super.setHint(Component.literal(measuringFont.plainSubstrByWidth(hint.getString(),Math.max(1,getWidth()-2))));
        setTooltip(Tooltip.create(GuideScreen.safeNarration(hint)));
    }
    @Override public boolean isMouseOver(double x,double y) {return x>=outerX&&x<outerX+outerWidth&&y>=outerY&&y<outerY+18;}
    @Override public boolean mouseClicked(double x,double y,int button) {
        if(!isMouseOver(x,y))return false;
        return super.mouseClicked(Math.clamp(x,getX(),getX()+getWidth()-1),getY()+2,button);
    }
    @Override public void renderWidget(GuiGraphics g,int mx,int my,float partial) {
        g.fill(outerX,outerY,outerX+outerWidth,outerY+18,isFocused()?BookTheme.INK:BookTheme.BORDER);
        g.fill(outerX+1,outerY+1,outerX+outerWidth-1,outerY+17,BookTheme.DOCUMENT);
        super.renderWidget(g,mx,my,partial);
    }
}
