package dev.inscribed.client;

import dev.inscribed.inspection.InspectionPayload;
import java.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

/** Scrollable snapshot from the completed lens analysis, with no inventory or ritual controls. */
public final class AltarInspectionScreen extends Screen {
    private record Line(ItemStack icon, FormattedCharSequence text,Component tooltip) {}
    private final InspectionPayload snapshot;
    private final List<Line> lines = new ArrayList<>();
    private int scroll, left, top, panelWidth, panelHeight, visible;
    public AltarInspectionScreen(InspectionPayload snapshot) {
        super(Component.translatable("inspection.inscribed.title")); this.snapshot = snapshot;
    }
    @Override protected void init() {
        panelWidth = Math.min(420, width - 16); panelHeight = Math.min(380, height - 16);
        left = (width-panelWidth)/2; top = (height-panelHeight)/2;
        visible = Math.max(1, (panelHeight-114)/20); lines.clear();
        for (var row : snapshot.rows()) {
            var wrapped = font.split(row.text(), panelWidth-58);
            for (int i=0; i<wrapped.size(); i++) lines.add(new Line(i==0 ? row.icon() : ItemStack.EMPTY, wrapped.get(i),row.text()));
        }
        scroll = Math.clamp(scroll, 0, Math.max(0,lines.size()-visible));
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
                .bounds(left+panelWidth/2-45,top+panelHeight-26,90,20).build());
    }
    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        renderBackground(g,mouseX,mouseY,partial);
        g.fill(left,top,left+panelWidth,top+panelHeight,0xF21B1820);
        g.fill(left,top,left+panelWidth,top+2,0xFFE2BF78);
        g.drawCenteredString(font,title,width/2,top+9,0xFFE2BF78);
        var target = snapshot.target();
        if (!target.isEmpty()) { g.renderItem(target,left+12,top+29); g.renderItemDecorations(font,target,left+12,top+29); }
        var targetName = target.isEmpty() ? Component.translatable("inspection.inscribed.no_target") : target.getHoverName();
        g.drawString(font,font.split(Component.translatable("inspection.inscribed.target",targetName),panelWidth-50).getFirst(),left+34,top+32,0xFFF4DFC4);
        g.drawString(font,Component.translatable("inspection.inscribed.power",String.format(Locale.ROOT,"%.1f",snapshot.power())),left+12,top+51,0xFFE2BF78);
        g.drawString(font,Component.translatable("inspection.inscribed.snapshot"),left+12,top+65,0xFFAAA2B3);
        int y=top+83;
        for (int i=scroll; i<Math.min(lines.size(),scroll+visible); i++) {
            var line = lines.get(i);
            if (!line.icon().isEmpty()) { g.renderItem(line.icon(),left+12,y-3); g.renderItemDecorations(font,line.icon(),left+12,y-3); }
            g.drawString(font,line.text(),left+34,y,0xFFF4DFC4); y+=20;
        }
        if (lines.size()>visible) {
            int track = visible*20;
            g.fill(left+panelWidth-8,top+81,left+panelWidth-5,top+81+track,0xFF48414F);
            int thumb=Math.max(8,track*visible/lines.size());
            int offset=(track-thumb)*scroll/Math.max(1,lines.size()-visible);
            g.fill(left+panelWidth-8,top+81+offset,left+panelWidth-5,top+81+offset+thumb,0xFFE2BF78);
        }
        if (!snapshot.failure().isEmpty()) g.drawString(font,font.split(Component.translatable("message.inscribed."+snapshot.failure()),panelWidth-24).getFirst(),left+12,top+panelHeight-39,0xFFFF9988);
        // Screen.render() would blur the background a second time, including our own text.
        for (var child : children()) if (child instanceof net.minecraft.client.gui.components.Renderable widget)
            widget.render(g,mouseX,mouseY,partial);
        if (!target.isEmpty() && mouseX>=left+12 && mouseX<left+29 && mouseY>=top+29 && mouseY<top+46) g.renderTooltip(font,target,mouseX,mouseY);
        for (int i=scroll; i<Math.min(lines.size(),scroll+visible); i++) {
            var line=lines.get(i); int rowY=top+80+(i-scroll)*20;
            if (!line.icon().isEmpty() && mouseX>=left+12 && mouseX<left+29 && mouseY>=rowY && mouseY<rowY+16) g.renderTooltip(font,List.of(line.icon().getHoverName(),line.tooltip()),Optional.empty(),mouseX,mouseY);
        }
    }
    @Override public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
        scroll=Math.clamp(scroll-(int)Math.signum(vertical)*3,0,Math.max(0,lines.size()-visible)); return true;
    }
    @Override public boolean isPauseScreen() { return false; }
}
