package dev.inscribed.compat.lsbc;
import dev.inscribed.client.GuideScreen;
import dev.lsbc.client.codex.CodexScreen;
import dev.lsbc.client.codex.widget.CodexRibbonTab;
import dev.lsbc.codex.layout.CodexLayoutMetrics;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
public final class LSBCGuideClient {
    public static void initialize(){NeoForge.EVENT_BUS.addListener(LSBCGuideClient::init);}
    private static void init(ScreenEvent.Init.Post e){
        if(!(e.getScreen() instanceof CodexScreen screen))return;
        var slot=CodexLayoutMetrics.calculate(screen.width,screen.height,false).ribbons().get(4);
        e.addListener(new CodexRibbonTab(slot.x(),slot.y(),slot.width(),slot.height(),Component.translatable("guide.inscribed.enchantments"),false,()->GuideScreen.openFromCodex(screen)));
    }
    private LSBCGuideClient(){}
}
