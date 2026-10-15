package dev.inscribed.client;

import dev.inscribed.ritual.*;
import java.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public final class RitualSelectionScreen extends Screen {
    private final SelectionPayload payload;
    private int page,left,top,panelWidth,panelHeight,visible;
    private boolean sent;
    public RitualSelectionScreen(SelectionPayload payload){super(Component.translatable("selection.inscribed.title"));this.payload=payload;}
    @Override protected void init() {
        panelWidth=Math.min(360,width-16);panelHeight=Math.min(286,height-16);left=(width-panelWidth)/2;top=(height-panelHeight)/2;visible=Math.max(1,(panelHeight-68)/52);
        for(int row=0;row<visible;row++) {
            int index=page*visible+row;if(index>=payload.options().size())break;var option=payload.options().get(index);
            var label=Component.translatable("selection.inscribed."+option.type().name().toLowerCase(Locale.ROOT),option.name());
            addRenderableWidget(Button.builder(label,b->{sent=true;PacketDistributor.sendToServer(new SelectionChoice(payload.token(),index));minecraft.setScreen(null);})
                .createNarration(s->GuideScreen.safeNarration(label).copy()).bounds(left+8,top+28+row*52,panelWidth-16,48).tooltip(Tooltip.create(option.ingredients())).build());
        }
        addRenderableWidget(Button.builder(Component.literal("<"),b->{if(page>0){page--;rebuildWidgets();}}).bounds(left+8,top+panelHeight-27,30,20).build());
        addRenderableWidget(Button.builder(Component.literal(">"),b->{if((page+1)*visible<payload.options().size()){page++;rebuildWidgets();}}).bounds(left+panelWidth-38,top+panelHeight-27,30,20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"),b->onClose()).bounds(left+panelWidth/2-50,top+panelHeight-27,100,20).build());
    }
    @Override public void render(GuiGraphics g,int mx,int my,float partial) {
        renderBackground(g,mx,my,partial);g.fill(left,top,left+panelWidth,top+panelHeight,0xF028242B);g.drawCenteredString(font,title,width/2,top+9,0xFFE2BF78);
        for(var child:children())if(child instanceof Renderable r)r.render(g,mx,my,partial);
        for(int row=0;row<visible;row++) {int index=page*visible+row;if(index>=payload.options().size())break;var o=payload.options().get(index);
            var detail=Component.translatable("selection.inscribed.details",String.format(Locale.ROOT,"%.1f",o.duration()/20.0),o.power());
            if(o.chance()>=0)detail.append(Component.translatable("selection.inscribed.chance",Math.round(o.chance()*100)));
            g.drawCenteredString(font,detail,width/2,top+62+row*52,0xFFDED4BF);
        }
    }
    @Override public void onClose(){if(!sent){sent=true;PacketDistributor.sendToServer(new SelectionChoice(payload.token(),-1));}super.onClose();}
    @Override public boolean isPauseScreen(){return false;}
}
