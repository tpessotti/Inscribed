package dev.inscribed.client;
import dev.inscribed.knowledge.*;
import java.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

public final class GuideScreen extends Screen {
    private final Screen parent;
    private int tab,page,scroll;private boolean extraction;private String query="";
    private Object detail;private ResourceLocation filter;
    private EditBox search;private BookGeometry geometry;private int left,top,panelWidth,panelHeight;
    private List<Component> lines=List.of();
    private record Route(int tab,int page,int scroll,boolean extraction,String query,Object detail,ResourceLocation filter) {}
    private final Deque<Route> back=new ArrayDeque<>(),forward=new ArrayDeque<>();
    private Route route(){return new Route(tab,page,scroll,extraction,query,detail,filter);}
    private void visit(Runnable action){back.push(route());forward.clear();action.run();}
    private void restore(Route r){
        tab=r.tab();page=r.page();scroll=r.scroll();extraction=r.extraction();query=r.query();detail=r.detail();filter=r.filter();
        if(detail instanceof GuideSnapshot.KnownEnchantment e)detail=ClientKnowledge.snapshot().enchantments().stream().filter(v->v.id().equals(e.id())).findFirst().orElse(null);
        if(detail instanceof GuideSnapshot.Recipe recipe)detail=ClientKnowledge.snapshot().recipes().stream().filter(v->v.id().equals(recipe.id())&&v.extraction()==recipe.extraction()).findFirst().orElse(null);
        refresh();
    }
    public GuideScreen(Screen parent){super(Component.translatable("item.inscribed.guide_book"));this.parent=parent;}
    public BookGeometry bookGeometry(){return geometry;}
    public void refresh(){if(minecraft!=null)rebuildWidgets();}
    private static Component text(String key){return Component.translatable("guide.inscribed."+key);}
    private BookButton button(Component label,int x,int y,int w,Runnable action){
        var widget=new BookButton(label,x,y,w,action);addRenderableWidget(widget);return widget;
    }
    private final class BookButton extends Button {
        BookButton(Component label,int x,int y,int w,Runnable action){super(x,y,w,x==left+panelWidth?geometry.ribbonHeight(5):18,label,b->action.run(),s->safeNarration(label).copy());setTooltip(Tooltip.create(safeNarration(label)));}
        @Override protected void renderWidget(GuiGraphics g,int mx,int my,float dt){
            int x=getX(),y=getY();boolean ribbon=x==left+panelWidth,chosen=ribbon&&getMessage().getString().equals(text(new String[]{"guide","enchantments","recipes"}[tab]).getString());
            int end=x+width-(ribbon&&!chosen?5:0);
            g.fill(x,y,end,y+height,BookTheme.BORDER);g.fill(x+1,y+1,end-1,y+height-1,chosen?BookTheme.ACTIVE_RIBBON:isHoveredOrFocused()?BookTheme.HOVER:ribbon?BookTheme.RIBBON:BookTheme.PAPER);
            if(chosen)g.fill(x+2,y+3,x+4,y+height-3,0xFFFFE0A1);
            g.enableScissor(x+4,y+1,end-3,y+height-1);
            if(ribbon){float scale=Math.min(1f,(end-x-12f)/Math.max(1,font.width(getMessage())));g.pose().pushPose();g.pose().translate(x+6,y+(height-9*scale)/2,0);g.pose().scale(scale,scale,1);g.drawString(font,getMessage(),0,0,chosen?0xFFFFF1CC:BookTheme.INK,false);g.pose().popPose();}
            else{var label=net.minecraft.locale.Language.getInstance().getVisualOrder(font.substrByWidth(getMessage(),(int)((width-8)/BookTheme.TEXT_SCALE)));g.pose().pushPose();g.pose().translate(x+4,y+5,0);g.pose().scale(BookTheme.TEXT_SCALE,BookTheme.TEXT_SCALE,1);g.drawString(font,label,0,0,active?BookTheme.INK:BookTheme.METADATA,false);g.pose().popPose();}
            g.disableScissor();

        }
    }
    /** Narration never speaks the logical text of glyph-font spans. */
    public static Component safeNarration(Component c){var out=Component.empty();c.visit((style,s)->{out.append(style.getFont().equals(ResourceLocation.parse("lsbc:unknown"))||style.getFont().equals(ResourceLocation.parse("minecraft:alt"))?Component.translatable("guide.inscribed.unknown_word"):Component.literal(s));return Optional.<Void>empty();},Style.EMPTY);return out;}
    @Override protected void init(){
        geometry=BookGeometry.calculate(width,height);panelWidth=geometry.width();panelHeight=geometry.height();left=geometry.x();top=geometry.y();
        int leaf=geometry.leafWidth(),lx=geometry.leftX(),rx=geometry.rightX();
        for(int n=0;n<3;n++){int selected=n;button(text(new String[]{"guide","enchantments","recipes"}[n]),left+panelWidth,geometry.ribbonY(5)+n*(geometry.ribbonHeight(5)+2),geometry.ribbonWidth(),()->visit(()->{tab=selected;page=scroll=0;detail=null;filter=null;query="";refresh();}));}
        button(Component.literal("X"),left+panelWidth,geometry.ribbonY(5)+3*(geometry.ribbonHeight(5)+2),geometry.ribbonWidth(),this::onClose);
        var previous=button(Component.literal("←"),left+panelWidth+4,geometry.ribbonY(5)-22,26,()->{forward.push(route());restore(back.pop());});previous.active=!back.isEmpty();previous.setTooltip(Tooltip.create(text("back")));
        var next=button(Component.literal("→"),left+panelWidth+34,geometry.ribbonY(5)-22,26,()->{back.push(route());restore(forward.pop());});next.active=!forward.isEmpty();next.setTooltip(Tooltip.create(text("forward")));
        search=addRenderableWidget(new BookSearchBox(font,lx,top+geometry.inset()+14,leaf,query,value->{query=value;page=0;rebuildEntries();}));
        if(tab==2){button(text(extraction?"extraction":"infusion"),rx,top+geometry.inset()+14,filter!=null?leaf-38:leaf,()->{extraction=!extraction;page=0;refresh();});if(filter!=null)button(text("all"),rx+leaf-34,top+geometry.inset()+14,34,()->{filter=null;page=0;refresh();});}
        populate();
        if(detail==null&&tab==0&&!GuideContentProvider.chapters().isEmpty())detail=GuideContentProvider.chapters().getFirst();
        lines=detail==null?List.of(text("empty")):detailLines();
    }
    private void rebuildEntries(){query=search.getValue();refresh();if(search!=null){setFocused(search);search.setCursorPosition(query.length());}}
    private void populate(){
        int start=top+geometry.inset()+38,capacity=Math.max(1,(geometry.footerY()-4-start)/20);
        String q=query.toLowerCase(Locale.ROOT);var labels=new ArrayList<Component>();var values=new ArrayList<Object>();
        if(tab==0)for(var c:GuideContentProvider.chapters())if((c.title()+" "+c.body()).toLowerCase(Locale.ROOT).contains(q)){labels.add(Component.literal(c.title()));values.add(c);}
        if(tab==1)for(var e:ClientKnowledge.snapshot().enchantments())if(e.searchable().getString().toLowerCase(Locale.ROOT).contains(q)){labels.add(e.name());values.add(e);}
        if(tab==2)for(var r:ClientKnowledge.snapshot().recipes())if(r.extraction()==extraction&&(filter==null||filter.equals(r.enchantment()))&&r.searchable().getString().toLowerCase(Locale.ROOT).contains(q)){labels.add(r.name());values.add(r);}
        int pages=Math.max(1,(values.size()+capacity-1)/capacity);page=Math.min(page,pages-1);
        for(int i=page*capacity;i<Math.min(values.size(),(page+1)*capacity);i++){var selected=values.get(i);button(labels.get(i),geometry.leftX(),start+(i-page*capacity)*20,geometry.leafWidth(),()->visit(()->{detail=selected;scroll=0;refresh();}));}
        int right=geometry.rightX()+geometry.leafWidth(),fy=geometry.footerY();
        button(Component.literal("<"),geometry.leftX(),fy,18,()->{if(page>0){page--;refresh();}}).active=page>0;
        button(Component.literal(">"),right-18,fy,18,()->{if(page+1<pages){page++;refresh();}}).active=page+1<pages;
        totalPages=pages;
        lines=values.isEmpty()?List.of(text("empty")):List.of();
    }
    private List<Component> detailLines(){
        var result=new ArrayList<Component>();
        if(detail instanceof GuideContentProvider.Chapter c){result.add(Component.literal(c.title()));result.add(Component.literal(c.body()));}
        if(detail instanceof GuideSnapshot.KnownEnchantment e){
            result.add(e.name());result.add(Component.translatable("guide.inscribed.max_level",e.maxLevel()));
            button(text("related_recipes"),geometry.rightX(),top+geometry.inset(),geometry.leafWidth(),()->visit(()->{filter=e.id();tab=2;extraction=false;detail=null;page=0;query="";refresh();}));
        }
        if(detail instanceof GuideSnapshot.Recipe r){
            result.add(r.name());result.add(text(r.extraction()?"extraction":"infusion"));
            result.add(Component.translatable("guide.inscribed.power",r.power()));result.add(Component.translatable("guide.inscribed.duration",r.ticks(),r.ticks()/20.0));
            result.add(Component.translatable("guide.inscribed."+(r.extraction()?"failure":"consumption"),Math.round(r.chance()*100)));
            result.add(r.extraction()?Component.translatable("guide.inscribed.books",r.emptyBooks()):text(r.exact()?"book_exact":"book_minimum"));
            result.add(text("pedestal_constraints"));
            result.add(text("ingredients"));for(var cost:r.costs()){var alternatives=Component.empty();for(var stack:cost.alternatives()){if(!alternatives.getString().isEmpty())alternatives.append(" / ");alternatives.append(stack.getHoverName());}result.add(Component.literal(cost.count()+" × ").append(alternatives));}
            result.add(text("targets"));for(var stack:r.targets())result.add(stack.getHoverName());
            var enchant=ClientKnowledge.snapshot().enchantments().stream().filter(e->e.id().equals(r.enchantment())).findFirst();
            enchant.ifPresent(e->button(text("enchantment"),geometry.rightX(),top+geometry.inset()+36,geometry.leafWidth(),()->visit(()->{detail=e;scroll=0;refresh();})));
        }
        return result;
    }
    @Override public void render(GuiGraphics g,int mx,int my,float delta){
        g.fill(0,0,width,height,0x48000000);OpenBookBackground.render(g,left,top,panelWidth,panelHeight);
        g.drawString(font,BookTheme.title(text(new String[]{"guide","enchantments","recipes"}[tab])),geometry.leftX(),top+geometry.inset(),BookTheme.INK,false);
        super.render(g,mx,my,delta);
        int x=geometry.rightX(),y=top+geometry.inset()+38,w=geometry.leafWidth()-5,bottom=geometry.footerY()-4;
        if(detail instanceof GuideSnapshot.Recipe)y+=22;
        var counter=Component.literal((page+1)+" / "+totalPages);g.pose().pushPose();g.pose().translate(geometry.rightX(),geometry.footerY()+5,0);g.pose().scale(BookTheme.TEXT_SCALE,BookTheme.TEXT_SCALE,1);g.drawString(font,counter,0,0,BookTheme.INK,false);g.pose().popPose();
        boolean diagram=detail instanceof GuideContentProvider.Chapter c&&(c.id().equals("infusion")||c.id().equals("extraction"));
        if(diagram&&bottom-y>=48){RitualDiagram.render(g,x,y,w,((GuideContentProvider.Chapter)detail).id().equals("extraction"));y+=48;}
        var wrapped=new ArrayList<net.minecraft.util.FormattedCharSequence>();for(var line:lines){wrapped.addAll(font.split(line,(int)(w/BookTheme.TEXT_SCALE)));wrapped.add(net.minecraft.util.FormattedCharSequence.EMPTY);}
        int visible=Math.max(1,(bottom-y)/10);maxScroll=Math.max(0,wrapped.size()-visible);scroll=Math.clamp(scroll,0,maxScroll);
        g.enableScissor(x,y,x+w,bottom);
        for(int n=scroll;n<Math.min(wrapped.size(),scroll+visible);n++){g.pose().pushPose();g.pose().translate(x,y+(n-scroll)*10,0);g.pose().scale(BookTheme.TEXT_SCALE,BookTheme.TEXT_SCALE,1);g.drawString(font,wrapped.get(n),0,0,BookTheme.INK,false);g.pose().popPose();}
        g.disableScissor();
        if(maxScroll>0){int track=bottom-y,thumb=Math.max(8,track*visible/wrapped.size()),sy=y+(track-thumb)*scroll/maxScroll;g.fill(x+w+2,y,x+w+3,bottom,0x507C6342);g.fill(x+w+1,sy,x+w+4,sy+thumb,0xFF96784E);}
    }
    private int maxScroll,totalPages;
    @Override public boolean mouseScrolled(double x,double y,double dx,double dy){if(x>=left+panelWidth/2){scroll=Math.clamp(scroll+(dy<0?3:-3),0,maxScroll);return true;}return super.mouseScrolled(x,y,dx,dy);}
    @Override public void renderBackground(GuiGraphics g,int mx,int my,float delta) {}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){minecraft.setScreen(parent);}
}
