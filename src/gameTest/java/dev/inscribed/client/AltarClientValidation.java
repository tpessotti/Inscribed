package dev.inscribed.client;

import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import dev.inscribed.altar.*;
import dev.inscribed.pedestal.PedestalBlockEntity;
import dev.inscribed.registry.InscribedRegistries;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import org.lwjgl.glfw.GLFW;

/** Real integrated world, client interaction packets, native BERs, attachment sync and saved-world restart. Test-only. */
@EventBusSubscriber(modid="inscribed", value=Dist.CLIENT)
public final class AltarClientValidation {
    private static final BlockPos TABLE = new BlockPos(0, 80, 0), P1 = TABLE.offset(-2, 0, 0), P2 = TABLE.offset(2, 0, 0), SHELF = TABLE.offset(0, 0, -3);
    private static final Path OUTPUT = Path.of("screenshots", "inscribed-altar");
    private static final Map<String,Object> REPORT = new LinkedHashMap<>();
    private static final List<String> CAPTURES = new ArrayList<>();
    private static int phase, ticks, phaseTicks, completionWait; private static boolean opened, finished;
    private static volatile boolean ready; private static volatile Throwable setupFailure;
    private static String captureRequest;
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if (Boolean.getBoolean("inscribed.validateAspect") || Boolean.getBoolean("inscribed.rcSmoke") || Boolean.getBoolean("inscribed.rcStress") || Boolean.getBoolean("inscribed.validatePolish") || Boolean.getBoolean("inscribed.validateGuide") || !Boolean.getBoolean("inscribed.clientValidation") || finished) return;
        var mc = Minecraft.getInstance(); GLFW.glfwHideWindow(mc.getWindow().getWindow());
        mc.options.pauseOnLostFocus = false; mc.options.enableVsync().set(false);
        try {
            if (++ticks > 2400) throw new AssertionError("Native altar test timed out in phase " + phase);
            if (setupFailure != null) throw new AssertionError("Server fixture failed", setupFailure);
            if (ticks % 100 == 0) LogUtils.getLogger().info("[INSCRIBED CLIENT TEST] phase={} screen={}", phase, mc.screen == null ? "game" : mc.screen.getClass().getName());
            if (mc.getOverlay() != null) return;
            if (mc.level == null || mc.player == null) {
                if (!opened && mc.screen != null) { opened = true; mc.createWorldOpenFlows().openWorld("InscribedValidation", () -> finish(new AssertionError("World opening cancelled"))); }
                return;
            }
            phaseTicks++;
            switch (phase) {
                case 0 -> {
                    check(!Boolean.getBoolean("inscribed.requireLsbc") || ModList.get().isLoaded("lsbc"), "Expected LSBC absent");
                    mc.options.renderDistance().set(4); mc.options.guiScale().set(3); mc.options.hideGui=true; mc.resizeDisplay();
                    ready = false; phase = 1; phaseTicks = 0;
                    mc.getSingleplayerServer().execute(() -> {
                        try {
                            var level = mc.getSingleplayerServer().overworld(); var player = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                            var configured=dev.inscribed.config.ConfigRecipeStore.load();
                            check(Files.isRegularFile(dev.inscribed.config.ConfigRecipeStore.path()) && configured.getAsJsonObject("enchantment_recipes").size()==131 && configured.getAsJsonObject("crafting_recipes").has("inscribed:infusion_pedestal"),"Editable config catalog not exported");
                            REPORT.put("configurationCatalogExported","PASS");
                            if(Boolean.getBoolean("inscribed.validateRitual02"))dev.inscribed.knowledge.KnowledgeService.clear(player);
                            if (Boolean.getBoolean("inscribed.requireClientPersistence")) {
                                check(level.getBlockEntity(TABLE) instanceof EnchantingTableBlockEntity, "Saved table missing");
                                var persisted = AltarStateService.get((EnchantingTableBlockEntity)level.getBlockEntity(TABLE));
                                check(persisted.target().is(Items.IRON_PICKAXE) && persisted.target().getEnchantments().getLevel(enchantment(level.registryAccess())) == 1 && !persisted.running(), "Saved target/enchants missing after JVM restart");
                                check(((PedestalBlockEntity)level.getBlockEntity(P1)).item().getCount() == 3 && ((PedestalBlockEntity)level.getBlockEntity(P2)).item().getCount() == 2
                                        && ((ChiseledBookShelfBlockEntity)level.getBlockEntity(SHELF)).getItem(0).isEmpty(), "Saved costs/library missing after JVM restart");
                                REPORT.put("previousJvmPersistence", "PASS");
                            }
                            for (int x=-8; x<=8; x++) for (int z=-8; z<=8; z++) { level.setBlock(new BlockPos(x,79,z), Blocks.SMOOTH_STONE.defaultBlockState(), 3); for(int y=80;y<84;y++) level.setBlock(new BlockPos(x,y,z), Blocks.AIR.defaultBlockState(), 3); }
                            level.setBlock(TABLE, Blocks.ENCHANTING_TABLE.defaultBlockState(), 3); level.setBlock(P1, InscribedRegistries.PEDESTAL.get().defaultBlockState(), 3); level.setBlock(P2, InscribedRegistries.PEDESTAL.get().defaultBlockState(), 3);
                            for (var entity : level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new AABB(TABLE).inflate(20))) entity.discard();
                            level.setBlock(SHELF, Blocks.CHISELED_BOOKSHELF.defaultBlockState(), 3); level.setBlock(TABLE.offset(0,0,3), Blocks.BOOKSHELF.defaultBlockState(), 3); level.setBlock(TABLE.offset(1,0,3), Blocks.BOOKSHELF.defaultBlockState(), 3);
                            var book = new ItemStack(Items.ENCHANTED_BOOK); book.enchant(enchantment(level.registryAccess()), 1); ((ChiseledBookShelfBlockEntity)level.getBlockEntity(SHELF)).setItem(0, book);
                            player.setGameMode(GameType.CREATIVE); player.teleportTo(3.5,80,3.5); player.setYRot(135); player.setXRot(17);
                            player.getInventory().clearContent(); player.getInventory().selected = 0; player.getInventory().setItem(0, new ItemStack(Items.IRON_PICKAXE)); player.inventoryMenu.broadcastChanges();
                            level.setDayTime(1000); ready = true;
                        } catch(Throwable error) { setupFailure = error; }
                    });
                }
                case 1 -> {
                    if (!ready || !(mc.level.getBlockEntity(TABLE) instanceof EnchantingTableBlockEntity) || !mc.player.getMainHandItem().is(Items.IRON_PICKAXE) || phaseTicks < 35) return;
                    mc.player.setYRot(135); mc.player.setXRot(17); click(TABLE); next(2);
                }
                case 2 -> {
                    var table = table(); if (table == null || !AltarStateService.get(table).target().is(Items.IRON_PICKAXE) || phaseTicks < 15) return;
                    check(mc.getBlockEntityRenderDispatcher().getRenderer(table) instanceof EnchantTableRenderer, "Vanilla table renderer was replaced");
                    check(mc.getBlockEntityRenderDispatcher().getRenderer(pedestal(P1)) instanceof PedestalRenderer, "Pedestal renderer missing");
                    REPORT.put("targetAttachmentAndVanillaRenderer", "PASS"); requestCapture("01-target-and-vanilla-book.png"); held(new ItemStack(Items.AMETHYST_SHARD,5)); next(3);
                }
                case 3 -> { if (ready && mc.player.getMainHandItem().is(Items.AMETHYST_SHARD)) { click(P1); next(4); } }
                case 4 -> { if (pedestal(P1) != null && pedestal(P1).item().getCount() == 5 && mc.player.getMainHandItem().isEmpty()) { held(new ItemStack(Items.IRON_INGOT,3)); next(5); } }
                case 5 -> { if (ready && mc.player.getMainHandItem().is(Items.IRON_INGOT)) { click(P2); next(6); } }
                case 6 -> {
                    if (pedestal(P2) == null || pedestal(P2).item().getCount() != 3 || !mc.player.getMainHandItem().isEmpty() || phaseTicks < 25) return;
                    requestCapture("02-prepared-altar.png"); next(Boolean.getBoolean("inscribed.validateBook") ? 70 : Boolean.getBoolean("inscribed.validateLens") ? 60 : 7);
                }
                case 70 -> {
                    if(captureRequest!=null)return;
                    check(!ModList.get().isLoaded("lsbc"),"Book fallback test must lack LSBC");held(new ItemStack(Items.WRITABLE_BOOK));
                    mc.getSingleplayerServer().execute(() -> mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID()).teleportTo(1.5,80,2.5));
                    mc.options.hideGui=false;next(71);
                }
                case 71 -> {
                    if(!ready || phaseTicks<30 || !mc.player.getMainHandItem().is(Items.WRITABLE_BOOK))return;
                    var delta=Vec3.atCenterOf(TABLE).subtract(mc.player.getEyePosition());
                    mc.player.setYRot((float)Math.toDegrees(Math.atan2(-delta.x,delta.z)));mc.player.setXRot((float)-Math.toDegrees(Math.atan2(delta.y,Math.sqrt(delta.x*delta.x+delta.z*delta.z))));
                    mc.getConnection().send(new net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Rot(mc.player.getYRot(),mc.player.getXRot(),mc.player.onGround()));
                    mc.player.setShiftKeyDown(true);mc.getConnection().send(new ServerboundPlayerCommandPacket(mc.player,ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY));click(TABLE);next(72);
                }
                case 72 -> {
                    if(!(mc.screen instanceof net.minecraft.client.gui.screens.inventory.BookEditScreen) || phaseTicks<20)return;
                    var content=mc.player.getMainHandItem().get(net.minecraft.core.component.DataComponents.WRITABLE_BOOK_CONTENT);
                    check(content!=null && content.pages().size()>1,"Written pages not synchronized to real client");
                    var text=String.join(" ",content.pages().stream().map(p->p.raw().replace('\n',' ')).toList());
                    check(text.contains("Iron Pickaxe") && text.contains("Amethyst Shard") && text.contains("Efficiency I") && text.contains("2.0"),"Fallback book report missing required info");
                    check(!AltarStateService.get(table()).running() && pedestal(P1).item().getCount()==5,"Book inspection consumed inputs");
                    REPORT.put("fallbackBookPhysicalClickPagesAndVanillaScreen","PASS");requestCapture("08-fallback-book.png");next(73);
                }
                case 73 -> {
                    if(captureRequest!=null || phaseTicks<10)return;
                    mc.setScreen(null);mc.player.setShiftKeyDown(false);mc.getConnection().send(new ServerboundPlayerCommandPacket(mc.player,ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));held(ItemStack.EMPTY);mc.options.hideGui=true;next(64);
                }
                case 60 -> {
                    if(captureRequest!=null) return;
                    check(ModList.get().isLoaded("lsbc"),"Lens validation needs LSBC");
                    held(new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(ResourceLocation.parse("lsbc:magnifying_glass"))));
                    mc.getSingleplayerServer().execute(() -> mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID()).teleportTo(1.5,80,2.5));
                    mc.options.hideGui=false; next(61);
                }
                case 61 -> {
                    if(!ready || phaseTicks<30 || !dev.inscribed.inspection.AltarInspection.isLens(mc.player.getMainHandItem()))return;
                    var delta=Vec3.atCenterOf(TABLE).subtract(mc.player.getEyePosition());
                    mc.player.setYRot((float)Math.toDegrees(Math.atan2(-delta.x,delta.z)));
                    mc.player.setXRot((float)-Math.toDegrees(Math.atan2(delta.y,Math.sqrt(delta.x*delta.x+delta.z*delta.z))));
                    mc.getConnection().send(new net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Rot(mc.player.getYRot(),mc.player.getXRot(),mc.player.onGround()));
                    mc.player.setShiftKeyDown(true);mc.getConnection().send(new ServerboundPlayerCommandPacket(mc.player,ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY));
                    mc.options.keyUse.setDown(true);
                    click(TABLE);next(62);
                }
                case 62 -> {
                    if(phaseTicks==15)requestCapture("06-lens-analysis.png");
                    if(!(mc.screen instanceof AltarInspectionScreen) || phaseTicks<45)return;
                    check(!AltarStateService.get(table()).running() && pedestal(P1).item().getCount()==5 && pedestal(P2).item().getCount()==3,"Lens started ritual/consumed inputs");
                    REPORT.put("lensPhysicalClickAndResultsScreen","PASS");requestCapture("07-lens-results.png");next(63);
                }
                case 63 -> {
                    if(captureRequest!=null || phaseTicks<10)return;
                    mc.options.keyUse.setDown(false);mc.setScreen(null);mc.player.setShiftKeyDown(false);mc.getConnection().send(new ServerboundPlayerCommandPacket(mc.player,ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));
                    held(ItemStack.EMPTY);mc.options.hideGui=true;next(64);
                }
                case 64 -> {if(ready && mc.player.getMainHandItem().isEmpty() && phaseTicks>10)next(7);}
                case 7 -> {
                    if (phaseTicks < 12 || captureRequest != null) return;
                    mc.player.setShiftKeyDown(true); mc.getConnection().send(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY));
                    click(TABLE); mc.player.setShiftKeyDown(false); mc.getConnection().send(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY)); next(8);
                }
                case 8 -> {
                    if (table() == null || !AltarStateService.get(table()).running()) return;
                    if (pedestal(P1)==null || pedestal(P2)==null || !pedestal(P1).running() || !pedestal(P2).running()) return;
                    check(pedestal(P1).item().getCount() == 5 && pedestal(P2).item().getCount() == 3, "Visual phase consumed early");
                    requestCapture("03-ritual-start.png"); REPORT.put("physicalShiftClickAndRunningSync", "PASS"); next(9);
                }
                case 9 -> {
                    if (phaseTicks == 50) requestCapture("04-knowledge-transfer.png");
                    if (table() == null || AltarStateService.get(table()).running() || phaseTicks < 110) return;
                    // Attachments and vanilla chunk BE packets are separate streams; wait for a coherent final view.
                    if (++completionWait < 10) return;
                    var result = AltarStateService.get(table()).target();
                    check(result.is(Items.IRON_PICKAXE) && result.getEnchantments().getLevel(enchantment(mc.level.registryAccess())) == 1, "Client result missing efficiency");
                    check(pedestal(P1).item().getCount() == 3 && pedestal(P2).item().getCount() == 2, "Client ingredient remainder incorrect");
                    REPORT.put("serverCommitAndResultSync", "PASS"); requestCapture("05-enchanted-result.png"); next(10);
                }
                case 10 -> { if (phaseTicks < 15 || captureRequest != null) return; click(TABLE); next(11); }
                case 11 -> {
                    if (table() == null || !AltarStateService.get(table()).target().isEmpty() || !mc.player.getMainHandItem().is(Items.IRON_PICKAXE)) return;
                    check(mc.player.getMainHandItem().getEnchantments().getLevel(enchantment(mc.level.registryAccess())) == 1, "Retrieved item lost enchantment");
                    REPORT.put("physicalRetrieval", "PASS"); click(TABLE); next(12);
                }
                case 12 -> {
                    if (table() == null || !AltarStateService.get(table()).target().is(Items.IRON_PICKAXE) || phaseTicks < 20) return;
                    ready = false; next(13); mc.getSingleplayerServer().execute(() -> {
                        try {
                            var level = mc.getSingleplayerServer().overworld(); var player = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                            check(((ChiseledBookShelfBlockEntity)level.getBlockEntity(SHELF)).getItem(0).isEmpty(), "Selected book not consumed at chance 1");
                            check(player.getInventory().countItem(Items.IRON_PICKAXE) == 0, "Reinsertion copied instead of transferred");
                            REPORT.put("bookConsumptionAndNoDuplicate", "PASS"); ready = true;
                        } catch(Throwable error) { setupFailure = error; }
                    });
                }
                case 13 -> {
                    if (!ready) return;
                    if (!Boolean.getBoolean("inscribed.validateRitual02")) { finish(null); return; }
                    ready=false; next(80);
                    mc.getSingleplayerServer().execute(() -> {
                        try {
                            var level=mc.getSingleplayerServer().overworld();
                            var target=new ItemStack(Items.IRON_PICKAXE);target.enchant(enchantment(level.registryAccess(),"fortune"),3);target.enchant(enchantment(level.registryAccess(),"efficiency"),4);target.enchant(enchantment(level.registryAccess(),"unbreaking"),3);
                            target.setDamageValue(42);target.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Kept name"));
                            AltarStateService.target((EnchantingTableBlockEntity)level.getBlockEntity(TABLE),target);
                            ((PedestalBlockEntity)level.getBlockEntity(P1)).setItem(new ItemStack(Items.PAPER,3));
                            ((PedestalBlockEntity)level.getBlockEntity(P2)).setItem(new ItemStack(Items.QUARTZ,3));
                            var shelf=(ChiseledBookShelfBlockEntity)level.getBlockEntity(SHELF);shelf.setItem(0,new ItemStack(Items.BOOK));shelf.setItem(1,new ItemStack(Items.BOOK));
                            var player=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());player.getInventory().setItem(0,ItemStack.EMPTY);player.inventoryMenu.broadcastChanges();player.teleportTo(1.5,80,2.5);
                            ready=true;
                        }catch(Throwable error){setupFailure=error;}
                    });
                }
                case 80 -> {
                    if(!ready || phaseTicks<35 || !mc.player.getMainHandItem().isEmpty() || AltarStateService.get(table()).target().getEnchantments().getLevel(enchantment(mc.level.registryAccess(),"fortune"))!=3)return;
                    mc.options.hideGui=false;mc.player.setShiftKeyDown(true);mc.getConnection().send(new ServerboundPlayerCommandPacket(mc.player,ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY));click(TABLE);
                    mc.player.setShiftKeyDown(false);mc.getConnection().send(new ServerboundPlayerCommandPacket(mc.player,ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));next(81);
                }
                case 81 -> {
                    if(!AltarStateService.get(table()).running())return;
                    check(mc.screen==null,"Extraction opened a selection UI");REPORT.put("extractionPhysicalClickNoSelectionScreen","PASS");requestCapture("09-extraction-no-screen.png");next(83);
                }
                case 83 -> {
                    if(table()==null || !AltarStateService.get(table()).running())return;
                    if(captureRequest!=null)return;
                    REPORT.put("extractionIngredientsSelectFortune","PASS");requestCapture("10-extraction-running.png");next(84);
                }
                case 84 -> {
                    if(phaseTicks<35 || AltarStateService.get(table()).running())return;
                    var result=AltarStateService.get(table()).target();
                    check(result.getEnchantments().getLevel(enchantment(mc.level.registryAccess(),"fortune"))==0 && result.getEnchantments().getLevel(enchantment(mc.level.registryAccess(),"efficiency"))==4 && result.getEnchantments().getLevel(enchantment(mc.level.registryAccess(),"unbreaking"))==3,"Extraction client enchantments incorrect");
                    check(result.getDamageValue()==42 && result.getHoverName().getString().equals("Kept name"),"Extraction lost components");
                    check(pedestal(P1).item().getCount()==2 && pedestal(P2).item().getCount()==2,"Extraction client costs incorrect");
                    ready=false;mc.getSingleplayerServer().execute(()->{
                        try{var level=mc.getSingleplayerServer().overworld();var shelf=(ChiseledBookShelfBlockEntity)level.getBlockEntity(SHELF);
                            check(shelf.getItem(0).is(Items.ENCHANTED_BOOK) && EnchantmentHelper.getEnchantmentsForCrafting(shelf.getItem(0)).getLevel(enchantment(level.registryAccess(),"fortune"))==3 && shelf.getItem(1).isEmpty(),"Extraction physical books incorrect");
                            var player=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                            check(dev.inscribed.knowledge.KnowledgeService.get(player).knownEnchantments().contains(ResourceLocation.parse("minecraft:fortune")),"Successful extraction did not teach unknown Fortune");
                            REPORT.put("successfulExtractionLearnsUnknownEnchantment","PASS");REPORT.put("extractionPhysicalBooksAndClientResult","PASS");ready=true;
                        }catch(Throwable error){setupFailure=error;}
                    });requestCapture("11-extraction-result.png");next(85);
                }
                case 85 -> {if(ready && captureRequest==null)finish(null);}
            }
        } catch(Throwable error) { finish(error); }
    }
    private static Holder<Enchantment> enchantment(RegistryAccess lookup) { return lookup.registryOrThrow(Registries.ENCHANTMENT).getHolder(ResourceLocation.parse("minecraft:efficiency")).orElseThrow(); }
    private static Holder<Enchantment> enchantment(RegistryAccess lookup,String name) { return lookup.registryOrThrow(Registries.ENCHANTMENT).getHolder(ResourceLocation.parse("minecraft:"+name)).orElseThrow(); }
    private static EnchantingTableBlockEntity table() { var be = Minecraft.getInstance().level.getBlockEntity(TABLE); return be instanceof EnchantingTableBlockEntity t ? t : null; }
    private static PedestalBlockEntity pedestal(BlockPos pos) { var be = Minecraft.getInstance().level.getBlockEntity(pos); return be instanceof PedestalBlockEntity p ? p : null; }
    private static void next(int value) { phase=value; phaseTicks=0; }
    private static void click(BlockPos pos) { var mc = Minecraft.getInstance(); mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false)); }
    private static void held(ItemStack stack) {
        var mc = Minecraft.getInstance(); ready = false;
        mc.getSingleplayerServer().execute(() -> { var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID()); p.getInventory().setItem(0,stack); p.inventoryMenu.broadcastChanges(); ready=true; });
    }
    private static void check(boolean value, String message) { if(!value) throw new AssertionError(message); }
    private static void requestCapture(String name) { captureRequest=name; }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post event) {
        if (!Boolean.getBoolean("inscribed.clientValidation") || finished || captureRequest == null) return;
        try { Files.createDirectories(OUTPUT); try(var image=Screenshot.takeScreenshot(Minecraft.getInstance().getMainRenderTarget())) { image.writeToFile(OUTPUT.resolve(captureRequest)); }
            CAPTURES.add(captureRequest); captureRequest=null;
        } catch(Throwable error) { finish(error); }
    }
    private static void finish(Throwable error) {
        if(finished)return; finished=true;
        REPORT.put("status", error == null ? "PASS" : "FAIL"); REPORT.put("phase",phase); REPORT.put("ticks",ticks); REPORT.put("captures",CAPTURES);
        REPORT.put("mods", ModList.get().getMods().stream().map(m -> m.getModId()).toList());
        if(error != null) { REPORT.put("error",error.toString()); LogUtils.getLogger().error("[INSCRIBED CLIENT TEST] FAIL",error); }
        try { Files.createDirectories(OUTPUT); Files.writeString(OUTPUT.resolve("results.json"), new GsonBuilder().setPrettyPrinting().create().toJson(REPORT)); }
        catch(Exception ex) { LogUtils.getLogger().error("Could not write client validation report",ex); }
        Minecraft.getInstance().stop();
    }
}
