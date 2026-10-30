package dev.inscribed.client;
import java.nio.file.*;
import java.util.*;
import dev.inscribed.knowledge.*;
import dev.inscribed.registry.InscribedRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import net.minecraft.world.InteractionHand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import org.lwjgl.glfw.GLFW;
/** Actual item-use packets, rendered guide/Codex/JEI and a saved-player restart. Not packaged. */
@EventBusSubscriber(modid="inscribed",value=Dist.CLIENT)
public final class GuideClientValidation {
    private static int phase,ticks,wait,diagramStage;private static volatile Throwable error;private static volatile boolean ready;
    private static boolean finished,opened;private static String capture;private static String originalConfig,originalDatapack;private static long oldGeneration;
    private static final Path OUTPUT=Path.of("screenshots","inscribed-guide");
    private static final Map<String,Object> REPORT=new LinkedHashMap<>();
    private static void check(boolean value,String msg){if(!value)throw new AssertionError(msg);}
    private static ResourceLocation id(String value){return ResourceLocation.parse(value);}
    private static void server(java.util.function.Consumer<net.minecraft.server.level.ServerPlayer> action){ready=false;var mc=Minecraft.getInstance();mc.getSingleplayerServer().execute(()->{try{action.accept(mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID()));ready=true;}catch(Throwable e){error=e;}});}
    private static void next(int p){phase=p;wait=0;}
    private static void useGuide(){var mc=Minecraft.getInstance();mc.setScreen(null);mc.gameMode.useItem(mc.player,InteractionHand.MAIN_HAND);}
    private static boolean known(){return ClientKnowledge.snapshot().enchantments().stream().anyMatch(e->e.id().equals(id("minecraft:fire_protection")));}
    private static void history(String arrow){var screen=Minecraft.getInstance().screen;var b=screen.children().stream().filter(w->w instanceof AbstractButton a&&a.getMessage().getString().equals(arrow)).map(AbstractButton.class::cast).findFirst().orElseThrow();check(b.active,"Guide history disabled: "+arrow);screen.mouseClicked(b.getX()+2,b.getY()+2,0);}
    private static void clickLabel(String key){var mc=Minecraft.getInstance();var label=net.minecraft.network.chat.Component.translatable("guide.inscribed."+key).getString();var button=mc.screen.children().stream().filter(w->w instanceof AbstractButton b&&b.getMessage().getString().equals(label)).map(w->(AbstractButton)w).findFirst().orElseThrow(()->new AssertionError("Button absent: "+label));mc.screen.mouseClicked(button.getX()+2,button.getY()+2,0);}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        if(!Boolean.getBoolean("inscribed.validateGuide")||finished)return;
        var mc=Minecraft.getInstance();GLFW.glfwHideWindow(mc.getWindow().getWindow());mc.options.pauseOnLostFocus=false;
        try{
            if(++ticks>3600)throw new AssertionError("Guide validation timed out phase "+phase);
            if(error!=null)throw new AssertionError("Server validation failed",error);
            if(mc.getOverlay()!=null)return;
            if(mc.level==null||mc.player==null){if(!opened&&mc.screen!=null){opened=true;mc.createWorldOpenFlows().openWorld("InscribedValidation",()->finish(new AssertionError("World cancelled")));}return;}
            if(++wait<10)return;
            switch(phase){
                case 0->{
                    mc.options.renderDistance().set(3);mc.options.guiScale().set(3);mc.options.hideGui=false;mc.options.enableVsync().set(false);mc.resizeDisplay();
                    REPORT.put("lsbc",ModList.get().isLoaded("lsbc"));REPORT.put("jei",ModList.get().isLoaded("jei"));
                    server(p->{
                        if(Boolean.getBoolean("inscribed.guidePersistence")){check(KnowledgeService.get(p).knownEnchantments().contains(id("minecraft:fire_protection")),"Saved knowledge missing without LSBC");REPORT.put("savedKnowledgeAfterRemovingLSBC","PASS");}
                        p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);p.getInventory().clearContent();p.getInventory().selected=0;KnowledgeService.clear(p);p.getInventory().setItem(0,new ItemStack(InscribedRegistries.GUIDE_BOOK.get()));p.inventoryMenu.broadcastChanges();
                    });next(1);
                }
                case 1->{if(!ready||wait<40||!mc.player.getMainHandItem().is(InscribedRegistries.GUIDE_BOOK.get()))return;check(ClientKnowledge.snapshot().enchantments().isEmpty(),"Inspecting shelves learned knowledge");check(ClientKnowledge.snapshot().recipes().stream().noneMatch(r->r.id().getPath().startsWith("minecraft/")),"Default recipes exposed before discovery");REPORT.put("unknownCatalogAndBookshelfNoLearning","PASS");useGuide();next(2);}
                case 2->{if(!(mc.screen instanceof GuideScreen)||wait<20)return;capture="01-guide-home.png";REPORT.put("guideItemRightClick","PASS");next(3);}
                case 3->{if(capture!=null)return;server(p->{var book=new ItemStack(Items.ENCHANTED_BOOK);var reg=p.registryAccess().registryOrThrow(Registries.ENCHANTMENT);for(var name:List.of("fire_protection","unbreaking"))book.enchant(reg.getHolder(id("minecraft:"+name)).orElseThrow(),2);p.getInventory().setItem(1,book);if(ModList.get().isLoaded("lsbc"))p.getInventory().setItem(2,new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(id("lsbc:codex"))));p.inventoryMenu.broadcastChanges();});next(4);}
                case 4->{if(!ready||!known()||wait<30)return;check(ClientKnowledge.snapshot().enchantments().size()==2,"Multi-enchantment acquisition did not learn all");check(ClientKnowledge.snapshot().recipes().stream().anyMatch(r->r.enchantment().equals(id("minecraft:fire_protection"))&&!r.extraction()),"Recipe discovery not refreshed");REPORT.put("multiEnchantmentInventoryAcquisitionAndRefresh","PASS");clickLabel("enchantments");capture="02-known-enchantments.png";next(5);}
                case 5->{if(capture!=null)return;if(ModList.get().isLoaded("lsbc")){server(GuideLsbcProbe::partial);next(6);}else next(7);}
                case 6->{if(!ready||wait<25)return;check(known(),"LSBC word forgetting erased Inscribed enchantment");REPORT.put("partialKnownWordsIdentitySearchAndIndependentKnowledge","PASS");capture="03-partial-knownwords.png";next(61);}
                case 61->{if(capture!=null)return;var input=mc.screen.children().stream().filter(w->w instanceof net.minecraft.client.gui.components.EditBox).map(w->(net.minecraft.client.gui.components.EditBox)w).findFirst().orElseThrow();var semantic=ClientKnowledge.snapshot().enchantments().stream().filter(v->v.id().equals(id("minecraft:fire_protection"))).findFirst().orElseThrow().searchable().getString();
                    var logical=ClientKnowledge.snapshot().enchantments().stream().filter(v->v.id().equals(id("minecraft:fire_protection"))).findFirst().orElseThrow().name().getString();var hidden=Arrays.stream(logical.split(" ")).filter(word->!word.isBlank()&&!semantic.contains(word)).findFirst().orElseThrow();input.setValue(hidden);
                    check(mc.screen.children().stream().noneMatch(w->w instanceof AbstractButton b&&b.getMessage().getString().equals(logical)),"Guide search exposed an unknown word");
                    mc.screen.children().stream().filter(w->w instanceof net.minecraft.client.gui.components.EditBox).map(w->(net.minecraft.client.gui.components.EditBox)w).findFirst().orElseThrow().setValue("");REPORT.put("guideSearchDoesNotMatchUnknownWords","PASS");next(7);}
                case 7->{if(capture!=null)return;server(p->KnowledgeService.change(p,id("minecraft:fire_protection"),true,false));next(8);}
                case 8->{if(!ready||known()||wait<25)return;check(ClientKnowledge.snapshot().recipes().stream().noneMatch(r->r.enchantment().equals(id("minecraft:fire_protection"))),"Forget did not hide recipes");if(ModList.get().isLoaded("jei")){if(!GuideJeiProbe.ready())return;GuideJeiProbe.check();}REPORT.put("forgetHidesRecipesWithoutInventoryRelearning","PASS");server(p->KnowledgeService.change(p,id("minecraft:fire_protection"),true,true));next(9);}
                case 9->{if(!ready||!known()||wait<25)return;clickLabel("recipes");capture="04-visible-infusion-recipes.png";next(10);}
                case 10->{if(capture!=null)return;var recipe=ClientKnowledge.snapshot().recipes().stream().filter(r->!r.extraction()&&r.enchantment().equals(id("minecraft:fire_protection"))&&r.level()==1).findFirst().orElseThrow();var button=mc.screen.children().stream().filter(w->w instanceof AbstractButton b&&b.getMessage().getString().equals(recipe.name().getString())).map(w->(AbstractButton)w).findFirst().orElseThrow();mc.screen.mouseClicked(button.getX()+2,button.getY()+2,0);next(91);}
                case 91->{capture="09-recipe-detail.png";next(92);}
                case 92->{if(capture!=null)return;clickLabel("enchantment");clickLabel("related_recipes");REPORT.put("bidirectionalEnchantmentRecipeNavigation","PASS");history("←");check(mc.screen.children().stream().anyMatch(w->w instanceof AbstractButton b&&b.getMessage().getString().equals(net.minecraft.network.chat.Component.translatable("guide.inscribed.related_recipes").getString())),"Back lost the enchantment detail");history("→");REPORT.put("externalBackForwardHistory","PASS");clickLabel("infusion");capture="05-visible-extraction-recipes.png";next(11);}
                case 11->{if(capture!=null)return;if(ModList.get().isLoaded("lsbc")){GuideLsbcProbe.openCodex();next(12);}else next(14);}
                case 12->{if(wait<25)return;var codexBounds=mc.screen.getClass().getMethod("bookBounds").invoke(mc.screen);var standalone=new GuideScreen(null);standalone.init(mc,mc.screen.width,mc.screen.height);var geo=standalone.bookGeometry();
                    for(var property:List.of("x","y","width","height"))check(codexBounds.getClass().getMethod(property).invoke(codexBounds).equals(geo.getClass().getMethod(property).invoke(geo)),"Codex/Guide geometry differs: "+property);REPORT.put("identicalCodexGuideBookBounds","PASS");check(mc.screen.children().stream().noneMatch(w->w instanceof AbstractButton b&&b.getMessage().getString().equals(net.minecraft.network.chat.Component.translatable("guide.inscribed.enchantments").getString())),"Redundant enchantment ribbon still present in Codex");capture="06-codex-without-enchantments.png";next(13);}
                case 13->{if(capture!=null)return;useGuide();next(131);}
                case 131->{if(!(mc.screen instanceof GuideScreen))return;check(GuideContentProvider.chapters().size()==9,"Standalone document chapters missing");REPORT.put("codexEnchantmentTabRemovedAndStandaloneGuideWorks","PASS");next(14);}
                case 14->{if(ModList.get().isLoaded("jei")){if(!GuideJeiProbe.ready())return;GuideJeiProbe.check();GuideJeiProbe.checkIntegrationToggle();REPORT.put("jeiIntegrationToggleDisableRestore","PASS");GuideJeiProbe.show(false);next(15);}else next(18);}
                case 15->{if(wait<30)return;check(mc.screen!=null&&mc.screen.getClass().getName().contains("jei"),"JEI infusion screen failed");capture="07-jei-infusion.png";next(16);}
                case 16->{if(capture!=null)return;GuideJeiProbe.show(true);next(17);}
                case 17->{if(wait<30)return;capture="08-jei-extraction.png";REPORT.put("jeiCategoriesAndPublicApiPerPlayerVisibility","PASS");next(171);}
                case 171->{if(capture!=null)return;GuideJeiProbe.showCatalyst();next(172);}
                case 172->{if(wait<30)return;capture="10-jei-catalyst-unit-pedestals.png";REPORT.put("jeiCatalystAndUnitConstraints","PASS");next(18);}
                case 18->{if(Boolean.getBoolean("inscribed.rcAuthoring")&&ModList.get().isLoaded("jei"))GuideJeiProbe.expectChangedVariant(id("example:sharpness_basic"));if(capture!=null)return;oldGeneration=ClientKnowledge.snapshot().generation();server(p->{try{
                        var path=dev.inscribed.config.ConfigRecipeStore.path();originalConfig=Files.readString(path);if(Boolean.getBoolean("inscribed.rcAuthoring")){var file=p.server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).resolve("datapacks/inscribed_example_datapack/data/example/inscribed/enchantment_recipes/sharpness_basic.json");originalDatapack=Files.readString(file);var edit=com.google.gson.JsonParser.parseString(originalDatapack).getAsJsonObject();edit.getAsJsonObject("infusion").addProperty("duration_ticks",42);Files.writeString(file,edit.toString());}var doc=com.google.gson.JsonParser.parseString(originalConfig).getAsJsonObject();
                        doc.getAsJsonObject("enchantment_recipes").getAsJsonObject("inscribed:minecraft/fire_protection_4").addProperty("enabled",false);
                        doc.getAsJsonObject("enchantment_recipes").add("inscribed:validation_explicit",com.google.gson.JsonParser.parseString("{\"enchantment\":\"minecraft:efficiency\",\"level\":1,\"minimum_bookshelf_power\":0,\"recipe_visibility\":\"EXPLICIT_DISCOVERY\",\"infusion\":{\"ingredients\":[],\"duration_ticks\":20,\"book_consumption_chance\":0}}"));
                        Files.writeString(path,new com.google.gson.Gson().toJson(doc));p.server.reloadResources(p.server.getPackRepository().getSelectedIds()).whenComplete((v,t)->{if(t!=null)error=t;});
                    }catch(Exception ex){throw new RuntimeException(ex);}});next(110);}
                case 110->{if(!ready||ClientKnowledge.snapshot().generation()==oldGeneration||wait<25)return;check(ClientKnowledge.snapshot().recipes().stream().noneMatch(r->r.id().equals(id("inscribed:minecraft/fire_protection_4"))||r.id().equals(id("inscribed:validation_explicit"))),"Reload removal or explicit hiding failed");if(ModList.get().isLoaded("jei"))GuideJeiProbe.check();REPORT.put("reloadRemovesRecipesAndRefreshesJei","PASS");if(Boolean.getBoolean("inscribed.rcAuthoring")){check(ClientKnowledge.snapshot().recipes().stream().anyMatch(r->r.id().equals(id("example:sharpness_basic"))&&r.ticks()==42),"Actual datapack edit did not update client/JEI");REPORT.put("exampleDatapackReloadAndJeiStructuralRefresh","PASS");}server(p->p.server.getCommands().performPrefixedCommand(p.server.createCommandSourceStack(),"inscribed knowledge learn recipe "+p.getScoreboardName()+" inscribed:validation_explicit"));next(111);}
                case 111->{if(!ready||ClientKnowledge.snapshot().recipes().stream().noneMatch(r->r.id().equals(id("inscribed:validation_explicit")))||wait<25)return;check(ClientKnowledge.snapshot().enchantments().size()==2,"Explicit recipe grant taught its enchantment");if(ModList.get().isLoaded("jei"))GuideJeiProbe.check();server(p->p.server.getCommands().performPrefixedCommand(p.server.createCommandSourceStack(),"inscribed knowledge forget recipe "+p.getScoreboardName()+" inscribed:validation_explicit"));next(112);}
                case 112->{if(!ready||ClientKnowledge.snapshot().recipes().stream().anyMatch(r->r.id().equals(id("inscribed:validation_explicit")))||wait<25)return;REPORT.put("explicitDiscoveryAdminCommandsAndJeiRefresh","PASS");oldGeneration=ClientKnowledge.snapshot().generation();server(p->{try{Files.writeString(dev.inscribed.config.ConfigRecipeStore.path(),originalConfig);if(originalDatapack!=null)Files.writeString(p.server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).resolve("datapacks/inscribed_example_datapack/data/example/inscribed/enchantment_recipes/sharpness_basic.json"),originalDatapack);p.server.reloadResources(p.server.getPackRepository().getSelectedIds()).whenComplete((v,t)->{if(t!=null)error=t;});}catch(Exception ex){throw new RuntimeException(ex);}});next(113);}
                case 113->{if(!ready||ClientKnowledge.snapshot().generation()==oldGeneration||wait<25)return;check(ClientKnowledge.snapshot().recipes().stream().anyMatch(r->r.id().equals(id("inscribed:minecraft/fire_protection_4"))),"Restored recipe missing after reload");if(ModList.get().isLoaded("jei"))GuideJeiProbe.check();server(p->{check(KnowledgeService.get(p).knownEnchantments().size()==2,"Server knowledge diverged");p.getInventory().clearContent();p.inventoryMenu.broadcastChanges();p.getServer().saveEverything(true,true,true);});next(19);}
                case 19->{if(ready&&wait>20){
                    check(ClientKnowledge.snapshot().recipes().stream().allMatch(r->r.costs().size()<=8 && r.costs().stream().allMatch(c->c.count()==1)),"Guide received non-unit/over-budget costs");
                    check(ClientKnowledge.snapshot().recipes().stream().filter(r->r.id().toString().equals("inscribed:minecraft/unbreaking_3")).anyMatch(r->r.costs().stream().flatMap(c->c.alternatives().stream()).anyMatch(s->s.is(InscribedRegistries.ENCHANTMENT_CATALYST.get()))),"Guide catalyst cost absent");
                    REPORT.put("guideCatalystAndUnitConstraints","PASS");next(200);
                }}
                case 200->{if(capture!=null)return;if(diagramStage>=8){REPORT.put("bookDiagramsAndFourGuiScales","PASS");next(210);return;}
                    mc.options.guiScale().set(new int[]{2,3,4,0}[diagramStage/2]);mc.resizeDisplay();mc.setScreen(new GuideScreen(null));
                    String chapter=diagramStage%2==0?"infusion":"extraction";String label=GuideContentProvider.chapters().stream().filter(c->c.id().equals(chapter)).findFirst().orElseThrow().title();
                    var input=mc.screen.children().stream().filter(net.minecraft.client.gui.components.EditBox.class::isInstance).map(net.minecraft.client.gui.components.EditBox.class::cast).findFirst().orElseThrow();input.setValue(label);
                    var btn=mc.screen.children().stream().filter(w->w instanceof AbstractButton b&&b.getMessage().getString().equals(label)).map(AbstractButton.class::cast).findFirst().orElseThrow();mc.screen.mouseClicked(btn.getX()+2,btn.getY()+2,0);
                    for(var widget:mc.screen.children())if(widget instanceof net.minecraft.client.gui.components.AbstractWidget w)check(w.getX()>=0&&w.getY()>=0&&w.getX()+w.getWidth()<=mc.screen.width&&w.getY()+w.getHeight()<=mc.screen.height,"Guide widget outside viewport");
                    var geometry=((GuideScreen)mc.screen).bookGeometry();
                    var previous=mc.screen.children().stream().filter(w->w instanceof AbstractButton b&&b.getMessage().getString().equals("<")).map(AbstractButton.class::cast).findFirst().orElseThrow();
                    var next=mc.screen.children().stream().filter(w->w instanceof AbstractButton b&&b.getMessage().getString().equals(">")).map(AbstractButton.class::cast).findFirst().orElseThrow();
                    check(previous.getX()==geometry.leftX()&&next.getX()==geometry.rightX()+geometry.leafWidth()-18&&previous.getY()==geometry.footerY()&&next.getY()==geometry.footerY(),"Page arrows not in lower outer corners");
                    for(var widget:mc.screen.children())if(widget instanceof AbstractButton b&&(b.getMessage().getString().equals("←")||b.getMessage().getString().equals("→")))check(b.getX()>=geometry.x()+geometry.width()&&b.getY()+b.getHeight()<geometry.ribbonY(5),"History not outside above ribbons");
                    for(var widget:mc.screen.children())if(widget instanceof net.minecraft.client.gui.components.AbstractWidget w&&w.getX()<geometry.x()+geometry.width()){
                        boolean inLeaf=(w.getX()>=geometry.leftX()&&w.getX()+w.getWidth()<=geometry.leftX()+geometry.leafWidth())||(w.getX()>=geometry.rightX()&&w.getX()+w.getWidth()<=geometry.rightX()+geometry.leafWidth());
                        check(inLeaf&&w.getY()>=geometry.y()+geometry.inset()&&w.getY()+w.getHeight()<=geometry.y()+geometry.height()-geometry.inset(),"Guide widget crosses leaf edge");
                    }
                    mc.screen.mouseScrolled(mc.screen.width*.65,mc.screen.height*.6,0,-100);mc.screen.mouseScrolled(mc.screen.width*.65,mc.screen.height*.6,0,100);
                    capture="visual-scale"+new int[]{2,3,4,0}[diagramStage/2]+"-"+chapter+".png";diagramStage++;wait=0;
                }
                case 210->{if(!ModList.get().isLoaded("create")){finish(null);return;}server(p->{var book=new ItemStack(Items.ENCHANTED_BOOK);book.enchant(p.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(id("create:capacity")).orElseThrow(),1);p.getInventory().setItem(1,book);p.inventoryMenu.broadcastChanges();});next(211);}
                case 211->{if(!ready||wait<30||ClientKnowledge.snapshot().enchantments().stream().noneMatch(v->v.id().equals(id("create:capacity"))))return;check(ClientKnowledge.snapshot().recipes().stream().anyMatch(v->v.enchantment().equals(id("create:capacity"))),"Create recipes missing from Guide");mc.setScreen(new GuideScreen(null));clickLabel("enchantments");capture="create-guide.png";REPORT.put("createRegistryKnowledgeGuide","PASS");next(212);}
                case 212->{if(capture!=null)return;if(ModList.get().isLoaded("jei")){GuideJeiProbe.check();GuideJeiProbe.showEnchantment("create:capacity");capture="create-jei.png";REPORT.put("createJei","PASS");}next(213);}
                case 213->{if(capture==null&&wait>20)finish(null);}
            }
        }catch(Throwable failure){finish(failure);}
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e){if(capture==null||finished)return;var mc=Minecraft.getInstance();try{Files.createDirectories(OUTPUT);try(var img=net.minecraft.client.Screenshot.takeScreenshot(mc.getMainRenderTarget())){img.writeToFile(OUTPUT.resolve(capture));}capture=null;}catch(Throwable failure){finish(failure);}}
    private static void finish(Throwable failure){if(finished)return;finished=true;REPORT.put("status",failure==null?"PASS":"FAIL");if(failure!=null){REPORT.put("error",failure.toString());com.mojang.logging.LogUtils.getLogger().error("Guide validation failed",failure);}try{Files.createDirectories(OUTPUT);Files.writeString(OUTPUT.resolve("results.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(REPORT));}catch(Exception ignored){}Minecraft.getInstance().stop();}
}
