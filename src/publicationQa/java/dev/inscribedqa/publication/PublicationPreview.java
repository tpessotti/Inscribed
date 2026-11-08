package dev.inscribedqa.publication;

import com.google.gson.*;
import dev.inscribed.altar.AltarStateService;
import dev.inscribed.config.InscribedConfig;
import dev.inscribed.recipe.*;
import dev.inscribed.registry.InscribedRegistries;
import dev.inscribed.ritual.*;
import dev.inscribed.pedestal.PedestalBlockEntity;
import dev.inscribed.trade.CatalystTrades;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.*;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.lwjgl.glfw.GLFW;

/** Actual vanilla/Inscribed rendering, interactions and server rituals in a new disposable world. */
@Mod("inscribed_publication_qa")
public final class PublicationPreview {
    private static final Path OUT=Path.of("screenshots/publication");
    private static final BlockPos TABLE=new BlockPos(0,-60,0),SHELF=TABLE.offset(0,0,-3);
    private static final int[][] OFFSETS={{-2,-2},{0,-2},{2,-2},{2,0},{2,2},{0,2},{-2,2},{-2,0}};
    private boolean opened,finished,working;private int phase,ticks;private long deadline;
    private Villager librarian;private EnchantmentRecipe recipe;private final JsonArray images=new JsonArray();
    private boolean purchased,ritualStarted;
    public PublicationPreview(){NeoForge.EVENT_BUS.register(this);}
    private static void check(boolean value,String why){if(!value)throw new AssertionError(why);}
    private ServerPlayer player(){var mc=Minecraft.getInstance();return mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());}
    private void server(Runnable task,int next){working=true;Minecraft.getInstance().getSingleplayerServer().execute(()->{
        try{task.run();Minecraft.getInstance().execute(()->{working=false;phase=next;ticks=0;});}
        catch(Throwable error){Minecraft.getInstance().execute(()->finish(error));}
    });}
    @SubscribeEvent public void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p)p.getAbilities().invulnerable=true;}
    @SubscribeEvent public void tick(ClientTickEvent.Post event){
        if(finished||!Boolean.getBoolean("inscribed.publicationQA"))return;
        var mc=Minecraft.getInstance();GLFW.glfwHideWindow(mc.getWindow().getWindow());
        mc.getToasts().clear();
        mc.options.hideGui=true;
        try{
            if(opened&&System.currentTimeMillis()>deadline)throw new AssertionError("Publication preview deadline at phase "+phase);
            if(mc.getOverlay()!=null||working)return;
            if(!opened&&mc.screen instanceof TitleScreen){
                opened=true;Files.createDirectories(OUT);deadline=System.currentTimeMillis()+600_000;mc.options.pauseOnLostFocus=false;
                var config=new WorldDataConfiguration(new DataPackConfig(List.of("vanilla"),List.of()),FeatureFlags.DEFAULT_FLAGS);
                var rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);
                String name="Inscribed-Publication-"+System.currentTimeMillis();
                mc.createWorldOpenFlows().createFreshLevel(name,new LevelSettings(name,GameType.SURVIVAL,false,Difficulty.PEACEFUL,false,rules,config),new WorldOptions(5L,false,false),r->r.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),mc.screen);return;
            }
            if(mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;ticks++;
            if(phase==0){server(()->{
                var p=player();var l=p.serverLevel();p.getAbilities().invulnerable=true;p.getAbilities().mayfly=true;p.getAbilities().flying=true;p.onUpdateAbilities();p.getFoodData().setFoodLevel(20);
                l.setDayTime(6000);l.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,l.getServer());l.setWeatherParameters(6000,0,false,false);
                for(int x=-12;x<=12;x++)for(int z=-12;z<=12;z++){l.setBlock(new BlockPos(x,-61,z),Blocks.SPRUCE_PLANKS.defaultBlockState(),3);for(int y=-60;y<=-53;y++)l.setBlock(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState(),3);}
                for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++)if((Math.abs(x)==4||Math.abs(z)==4)&&x*x+z*z<=25)l.setBlock(TABLE.offset(x,0,z),Blocks.BOOKSHELF.defaultBlockState(),3);
                l.setBlock(TABLE,Blocks.ENCHANTING_TABLE.defaultBlockState(),3);l.setBlock(SHELF,Blocks.CHISELED_BOOKSHELF.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.SOUTH),3);
                recipe=InscribedRecipeRegistry.recipes().stream().filter(r->r.enchantment().toString().equals("minecraft:unbreaking")&&r.level()==1).findFirst().orElseThrow();
                var book=new ItemStack(Items.ENCHANTED_BOOK);book.enchant(l.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(recipe.enchantment()).orElseThrow(),1);
                ((ChiseledBookShelfBlockEntity)l.getBlockEntity(SHELF)).setItem(0,book);p.getInventory().clearContent();p.getInventory().setItem(1,book.copy());p.getInventory().setItem(0,new ItemStack(Items.EMERALD,64));
                for(int i=0;i<8;i++){var pos=TABLE.offset(OFFSETS[i][0],0,OFFSETS[i][1]);l.setBlock(pos,InscribedRegistries.PEDESTAL.get().defaultBlockState(),3);var costs=recipe.infusion().orElseThrow().ingredients();((PedestalBlockEntity)l.getBlockEntity(pos)).setItem(i<costs.size()?costs.get(i).ingredient().getItems()[0].copyWithCount(1):ItemStack.EMPTY);}
                AltarStateService.target((EnchantingTableBlockEntity)l.getBlockEntity(TABLE),new ItemStack(Items.DIAMOND_PICKAXE));
                p.connection.teleport(6,-56.5,7.5,142,28);
                librarian=new Villager(EntityType.VILLAGER,l);librarian.setVillagerData(librarian.getVillagerData().setProfession(VillagerProfession.LIBRARIAN).setLevel(4));librarian.setNoAi(true);librarian.setPos(8,-60,1);l.addFreshEntity(librarian);
            },1);return;}
            if(phase==1&&ticks>40){mc.options.hideGui=true;capture("altar-overview.png");server(()->{
                var p=player();var result=RitualController.startSelected(p.serverLevel(),TABLE,p.getUUID(),recipe.id(),RitualType.INFUSION);check(result.started(),"Actual catalog ritual did not start: "+result);ritualStarted=true;
            },2);return;}
            if(phase==2&&ticks>25){capture("altar-infusion.png");mc.options.hideGui=false;server(()->{
                var p=player();librarian.setTradingPlayer(p);librarian.openTradingScreen(p,Component.translatable("entity.minecraft.villager.librarian"),4);
            },3);return;}
            if(phase==3){if(!(mc.screen instanceof MerchantScreen screen)){if(ticks>100)throw new AssertionError("Merchant screen did not open");return;}
                if(ticks<15)return;var offers=screen.getMenu().getOffers();int index=-1;for(int i=0;i<offers.size();i++)if(CatalystTrades.catalyst(offers.get(i).getResult()))index=i;
                check(index>=0&&index<7,"Native catalyst offer missing/needs scrolling");
                check(screen.mouseClicked(screen.getGuiLeft()+35,screen.getGuiTop()+18+index*20+5,0),"Native merchant selection failed");phase=4;ticks=0;return;}
            if(phase==4&&ticks>15){
                var menu=((MerchantScreen)mc.screen).getMenu();check(CatalystTrades.catalyst(menu.getSlot(2).getItem()),"Selected native trade has no catalyst result");capture("librarian-trade.png");
                mc.gameMode.handleInventoryMouseClick(menu.containerId,2,0,ClickType.PICKUP,mc.player);server(()->{},5);return;
            }
            if(phase==5&&ticks>15){server(()->{
                var p=player();check(CatalystTrades.catalyst(p.containerMenu.getCarried()),"Native purchase did not grant catalyst");purchased=true;
                p.closeContainer();p.getInventory().setItem(0,new ItemStack(InscribedRegistries.GUIDE_BOOK.get()));
            },6);return;}
            if(phase==6&&ticks>20){if(mc.screen!=null)mc.screen.onClose();mc.gameMode.useItem(mc.player,InteractionHand.MAIN_HAND);phase=7;ticks=0;return;}
            if(phase==7){if(!(mc.screen instanceof dev.inscribed.client.GuideScreen)){if(ticks>120)throw new AssertionError("Native Guide did not open");return;}
                if(ticks<15)return;capture("guide-book.png");mc.screen.onClose();mc.getWindow().setWindowed(512,512);mc.options.guiScale().set(1);mc.resizeDisplay();mc.setScreen(new IconScreen());phase=8;ticks=0;return;}
            if(phase==8&&ticks>20){capture("project-icon.png");finish(null);}
        }catch(Throwable error){finish(error);}
    }
    private void capture(String name)throws Exception{var mc=Minecraft.getInstance();try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(OUT.resolve(name));}images.add(name);}
    private void finish(Throwable error){if(finished)return;finished=true;
        try{var report=new JsonObject();report.addProperty("status",error==null?"PASS":"FAIL");if(error!=null)report.addProperty("error",error.toString());report.addProperty("native_purchase",purchased);report.addProperty("actual_catalog_ritual",ritualStarted);report.addProperty("disposable_world",true);report.addProperty("optional_runtime_mods",0);report.add("images",images);Files.createDirectories(OUT);Files.writeString(OUT.resolve("results.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));}catch(Exception e){e.printStackTrace();}Minecraft.getInstance().stop();
    }
    private static final class IconScreen extends Screen {
        IconScreen(){super(Component.literal("Inscribed pedestal"));}
        @Override public void renderBackground(GuiGraphics g,int x,int y,float d){}
        @Override public void render(GuiGraphics g,int mx,int my,float d){g.fill(0,0,width,height,0xFF292131);float scale=Math.min(width,height)/22F;g.pose().pushPose();g.pose().translate((width-16*scale)/2,(height-16*scale)/2,0);g.pose().scale(scale,scale,1);g.renderItem(new ItemStack(InscribedRegistries.PEDESTAL_ITEM.get()),0,0);g.pose().popPose();}
    }
}
