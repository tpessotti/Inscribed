package dev.inscribed;

import com.google.gson.*;
import com.mojang.authlib.GameProfile;
import dev.inscribed.altar.*;
import dev.inscribed.config.InscribedConfig;
import dev.inscribed.pedestal.*;
import dev.inscribed.recipe.*;
import dev.inscribed.registry.InscribedRegistries;
import dev.inscribed.ritual.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.phys.*;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("inscribed") @PrefixGameTestTemplate(false)
public final class InscribedGameTests {
    private static final BlockPos TABLE = new BlockPos(10, 2, 10), P1 = TABLE.offset(-2, 0, 0), P2 = TABLE.offset(2, 0, 0), SHELF = TABLE.offset(0, 0, -3);
    private record Fixture(GameTestHelper h, FakePlayer player, EnchantingTableBlockEntity table, PedestalBlockEntity p1, PedestalBlockEntity p2, ChiseledBookShelfBlockEntity shelf) {
        BlockPos pos() { return h.absolutePos(TABLE); }
        void click(boolean shift, ItemStack stack) {
            player.setShiftKeyDown(shift); player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            var event = new PlayerInteractEvent.RightClickBlock(player, InteractionHand.MAIN_HAND, pos(), new BlockHitResult(Vec3.atCenterOf(pos()), Direction.UP, pos(), false));
            NeoForge.EVENT_BUS.post(event); h.assertTrue(event.isCanceled() && event.getCancellationResult().consumesAction(), "Table interception failed");
        }
        void ready(String enchantment, int level, ItemStack a, ItemStack b) {
            click(false, new ItemStack(Items.IRON_SWORD)); UnitPedestals.set(h,TABLE,List.of(a,b)); shelf.setItem(0, book(h, enchantment, level));
        }
        RitualController.StartResult start() { return RitualController.start(h.getLevel(), pos(), player.getUUID()); }
    }
    private static Fixture fixture(GameTestHelper h) {
        if (Boolean.getBoolean("inscribed.requireLsbc")) h.assertTrue(ModList.get().isLoaded("lsbc"), "Combined test lacks LSBC");
        h.setBlock(TABLE, Blocks.ENCHANTING_TABLE); h.setBlock(P1, InscribedRegistries.PEDESTAL.get()); h.setBlock(P2, InscribedRegistries.PEDESTAL.get());
        h.setBlock(SHELF, Blocks.CHISELED_BOOKSHELF); h.setBlock(TABLE.offset(0, 0, 3), Blocks.BOOKSHELF); h.setBlock(TABLE.offset(1, 0, 3), Blocks.BOOKSHELF);
        var player = new FakePlayer(h.getLevel(), new GameProfile(UUID.randomUUID(), "InscribedTest")); player.setPos(Vec3.atCenterOf(h.absolutePos(TABLE)).add(0, 1, 1));
        return new Fixture(h, player, (EnchantingTableBlockEntity)h.getBlockEntity(TABLE), (PedestalBlockEntity)h.getBlockEntity(P1),
                (PedestalBlockEntity)h.getBlockEntity(P2), (ChiseledBookShelfBlockEntity)h.getBlockEntity(SHELF));
    }
    private static Holder<Enchantment> enchant(GameTestHelper h, String name) { return h.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(ResourceLocation.parse("minecraft:" + name)).orElseThrow(); }
    private static ItemStack book(GameTestHelper h, String name, int level) { var stack = new ItemStack(Items.ENCHANTED_BOOK); stack.enchant(enchant(h, name), level); return stack; }
    private static int targetLevel(Fixture f, String enchantment) { return EnchantmentHelper.getEnchantmentsForCrafting(AltarStateService.get(f.table()).target()).getLevel(enchant(f.h(), enchantment)); }
    private static void sharpness(Fixture f) { f.ready("sharpness", 3, new ItemStack(Items.AMETHYST_SHARD, 2), new ItemStack(Items.IRON_INGOT)); }

    @GameTest(template="test_altar") public static void lensSnapshotRoundTripPreservesPhysicalInputs(GameTestHelper h) {
        var f = fixture(h); sharpness(f);
        f.shelf().setItem(1, book(h, "unbreaking", 2)); f.shelf().setItem(2, new ItemStack(Items.BOOK));
        var snapshot = dev.inscribed.inspection.AltarInspection.snapshot(AltarScanner.scan(h.getLevel(), f.pos()));
        h.assertTrue(snapshot.target().is(Items.IRON_SWORD) && snapshot.power() == 2, "Wrong target or actual altar power");
        h.assertTrue(snapshot.rows().stream().filter(r -> r.icon().is(Items.ENCHANTED_BOOK)).count() == 2, "Missing physical library books");
        h.assertTrue(snapshot.rows().stream().anyMatch(r -> r.icon().is(Items.AMETHYST_SHARD) && r.icon().getCount() == 1), "Ingredient count lost");
        var buffer = new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(), h.getLevel().registryAccess());
        try {
            dev.inscribed.inspection.InspectionPayload.STREAM_CODEC.encode(buffer, snapshot);
            var decoded = dev.inscribed.inspection.InspectionPayload.STREAM_CODEC.decode(buffer);
            h.assertTrue(ItemStack.matches(decoded.target(),snapshot.target()) && decoded.rows().size()==snapshot.rows().size() && decoded.power()==snapshot.power(), "Inspection network round trip lost data");
            for (int i=0;i<snapshot.rows().size();i++) h.assertTrue(ItemStack.matches(decoded.rows().get(i).icon(),snapshot.rows().get(i).icon()) && decoded.rows().get(i).text().equals(snapshot.rows().get(i).text()), "Book components or display text lost");
            decoded.target().shrink(1); decoded.rows().get(1).icon().shrink(5);
            h.assertTrue(f.p1().item().getCount()==1 && AltarStateService.get(f.table()).target().getCount()==1 && targetLevel(f,"sharpness")==0, "Read-only inspection changed physical inventory");
        } finally { buffer.release(); }
        h.succeed();
    }

    private static void aimAtTable(Fixture f) {
        var delta = Vec3.atCenterOf(f.pos()).subtract(f.player().getEyePosition());
        f.player().setYRot((float)Math.toDegrees(Math.atan2(-delta.x,delta.z)));
        f.player().setXRot((float)-Math.toDegrees(Math.atan2(delta.y,Math.sqrt(delta.x*delta.x+delta.z*delta.z))));
    }
    @GameTest(template="test_altar") public static void fallbackBookPhysicalClickPreservesNotesAndInputs(GameTestHelper h) {
        if (ModList.get().isLoaded("lsbc")) { h.succeed(); return; }
        var f=fixture(h); sharpness(f); aimAtTable(f);
        var book=new ItemStack(Items.WRITABLE_BOOK);
        book.set(net.minecraft.core.component.DataComponents.WRITABLE_BOOK_CONTENT,new net.minecraft.world.item.component.WritableBookContent(List.of(net.minecraft.server.network.Filterable.passThrough("My existing notes"))));
        f.click(true,book);
        var pages=book.get(net.minecraft.core.component.DataComponents.WRITABLE_BOOK_CONTENT).pages();
        h.assertTrue(pages.size()>1 && pages.getFirst().raw().equals("My existing notes"),"Book fallback overwrote notes or did not append");
        var text=String.join(" ",pages.stream().map(p->p.raw().replace('\n',' ')).toList());
        h.assertTrue(text.contains("Iron Sword") && text.contains("Amethyst Shard") && text.contains("Sharpness III") && text.contains("2.0"),"Report lacks physical target, ingredients, book level or power");
        h.assertTrue(book.is(Items.WRITABLE_BOOK) && f.p1().item().getCount()==1 && f.p2().item().getCount()==1 && targetLevel(f,"sharpness")==0 && RitualController.state(h.getLevel(),f.pos())!=RitualState.RUNNING,"Fallback signed book or modified altar");
        dev.inscribed.inspection.AltarInspection.writeBook(f.player(),f.pos(),InteractionHand.MAIN_HAND);
        h.assertTrue(book.get(net.minecraft.core.component.DataComponents.WRITABLE_BOOK_CONTENT).pages().size()==pages.size(),"Repeated click duplicated report immediately");h.succeed();
    }
    @GameTest(template="test_altar") public static void fallbackOffhandBookAndLsbcGate(GameTestHelper h) {
        var f=fixture(h); sharpness(f); aimAtTable(f);var book=new ItemStack(Items.WRITABLE_BOOK);
        h.assertTrue(dev.inscribed.inspection.AltarInspection.isBookFallback(book)==!ModList.get().isLoaded("lsbc"),"Fallback presence gate is incorrect");
        if(ModList.get().isLoaded("lsbc")){h.succeed();return;}
        f.player().setItemInHand(InteractionHand.OFF_HAND,book);f.click(true,ItemStack.EMPTY);
        h.assertTrue(!book.getOrDefault(net.minecraft.core.component.DataComponents.WRITABLE_BOOK_CONTENT,net.minecraft.world.item.component.WritableBookContent.EMPTY).pages().isEmpty() && RitualController.state(h.getLevel(),f.pos())!=RitualState.RUNNING,"Offhand book started ritual or lacks report");h.succeed();
    }
    @GameTest(template="test_altar") public static void fullBookRejectsReportWithoutLosingNotes(GameTestHelper h) {
        var f=fixture(h);sharpness(f);var book=new ItemStack(Items.WRITABLE_BOOK);
        var original=new net.minecraft.world.item.component.WritableBookContent(Collections.nCopies(100,net.minecraft.server.network.Filterable.passThrough("Preserve this")));
        book.set(net.minecraft.core.component.DataComponents.WRITABLE_BOOK_CONTENT,original);
        h.assertTrue(!dev.inscribed.inspection.AltarBookReport.append(book,dev.inscribed.inspection.AltarInspection.snapshot(AltarScanner.scan(h.getLevel(),f.pos())),"en_us") && book.get(net.minecraft.core.component.DataComponents.WRITABLE_BOOK_CONTENT).equals(original),"Full book was overwritten or partially changed");h.succeed();
    }
    @GameTest(template="test_altar") public static void bookReportPortugueseAndPageLimits(GameTestHelper h) {
        var f=fixture(h);sharpness(f);var book=new ItemStack(Items.WRITABLE_BOOK);
        h.assertTrue(dev.inscribed.inspection.AltarBookReport.append(book,dev.inscribed.inspection.AltarInspection.snapshot(AltarScanner.scan(h.getLevel(),f.pos())),"pt_br"),"Portuguese report failed");
        var pages=book.get(net.minecraft.core.component.DataComponents.WRITABLE_BOOK_CONTENT).pages();
        var text=String.join(" ",pages.stream().map(p->p.raw().replace('\n',' ')).toList());
        h.assertTrue(text.toLowerCase(Locale.ROOT).contains("espada de ferro") && text.contains("Afiação III") && text.contains("2.0"),"Portuguese names or available book level lost: "+text);
        h.assertTrue(pages.stream().allMatch(p->p.raw().length()<=1024 && p.raw().split("\n",-1).length<=12),"Pages exceed vanilla limits");h.succeed();
    }
    @GameTest(template="test_altar", timeoutTicks=80) public static void optionalLensClickAnalyzesWithoutStartingRitual(GameTestHelper h) {
        if (!ModList.get().isLoaded("lsbc")) { h.succeed(); return; }
        var f = fixture(h); sharpness(f); aimAtTable(f);
        var lens = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(ResourceLocation.parse("lsbc:magnifying_glass"));
        f.click(true,new ItemStack(lens));
        h.assertTrue(f.player().isUsingItem() && dev.inscribed.inspection.AltarInspection.lookingAt(f.player(),f.pos()), "Shift lens interaction did not begin inspection");
        h.assertTrue(RitualController.state(h.getLevel(),f.pos()) != RitualState.RUNNING,"Inspection accidentally started infusion");
        h.runAtTickTime(30, () -> {
            dev.inscribed.inspection.AltarInspection.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(f.player()));
            h.assertTrue(f.player().isUsingItem(),"Analysis completed before its duration");
        });
        h.runAtTickTime(45, () -> {
            dev.inscribed.inspection.AltarInspection.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(f.player()));
            h.assertTrue(!f.player().isUsingItem(),"Analysis did not complete");
            h.assertTrue(f.p1().item().getCount()==1 && f.p2().item().getCount()==1 && f.shelf().getItem(0).is(Items.ENCHANTED_BOOK) && targetLevel(f,"sharpness")==0,"Analysis consumed or enchanted input");
            h.succeed();
        });
    }
    @GameTest(template="test_altar") public static void lensLookAwayCancelsAnalysis(GameTestHelper h) {
        if (!ModList.get().isLoaded("lsbc")) { h.succeed(); return; }
        var f=fixture(h); sharpness(f); aimAtTable(f);
        var lens=net.minecraft.core.registries.BuiltInRegistries.ITEM.get(ResourceLocation.parse("lsbc:magnifying_glass"));
        f.click(true,new ItemStack(lens)); f.player().setXRot(-90);
        dev.inscribed.inspection.AltarInspection.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(f.player()));
        h.assertTrue(!f.player().isUsingItem() && f.p1().item().getCount()==1,"Looking away did not safely cancel analysis"); h.succeed();
    }
    @GameTest(template="test_altar") public static void offhandLensTakesPriorityOverShiftRitual(GameTestHelper h) {
        if (!ModList.get().isLoaded("lsbc")) { h.succeed(); return; }
        var f=fixture(h); sharpness(f); aimAtTable(f);
        var lens=net.minecraft.core.registries.BuiltInRegistries.ITEM.get(ResourceLocation.parse("lsbc:magnifying_glass"));
        f.player().setItemInHand(InteractionHand.OFF_HAND,new ItemStack(lens)); f.click(true,ItemStack.EMPTY);
        h.assertTrue(f.player().isUsingItem() && f.player().getUsedItemHand()==InteractionHand.OFF_HAND
                && RitualController.state(h.getLevel(),f.pos()) != RitualState.RUNNING,"Offhand lens did not override shift ritual");
        f.player().stopUsingItem();dev.inscribed.inspection.AltarInspection.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(f.player()));h.succeed();
    }

    @GameTest(template="test_altar",timeoutTicks=140) public static void physicalShiftClickPipeline(GameTestHelper h) {
        var f = fixture(h); sharpness(f);
        h.assertTrue(f.player().getMainHandItem().isEmpty(), "Target was copied instead of transferred");
        f.click(true, ItemStack.EMPTY); h.assertTrue(RitualController.state(h.getLevel(), f.pos()) == RitualState.RUNNING, "Shift-click did not start");
        h.assertTrue(f.p1().item().getCount() == 1 && f.shelf().getItem(0).getCount() == 1, "Inputs consumed before completion");
        h.runAtTickTime(110, () -> {
            h.assertTrue(targetLevel(f, "sharpness") == 1, "Infusion result missing");
            h.assertTrue(f.p1().item().getCount() == 0 && f.p2().item().getCount() == 0, "Wrong ingredient consumption");
            h.assertTrue(!AltarStateService.get(f.table()).running() && !f.p1().running(), "Animation state leaked");
            f.click(false, ItemStack.EMPTY); h.assertTrue(AltarStateService.get(f.table()).target().isEmpty(), "Target retrieval failed");
            h.assertTrue(f.player().getInventory().items.stream().filter(s -> s.is(Items.IRON_SWORD)).count() == 1, "Result duplicated/lost"); h.succeed();
        });
    }
    @GameTest(template="test_altar",timeoutTicks=40) public static void certainBookConsumptionUsesOneDeterministicSlot(GameTestHelper h) {
        var f = fixture(h); f.ready("knockback", 2, new ItemStack(Items.DIAMOND, 2), ItemStack.EMPTY); f.shelf().setItem(1, book(h, "knockback", 1));
        h.assertTrue(f.start().started(), "Certain consumption recipe unavailable");
        h.runAtTickTime(20, () -> { h.assertTrue(targetLevel(f, "knockback") == 1 && f.shelf().getItem(0).isEmpty() && !f.shelf().getItem(1).isEmpty(), "Wrong book consumed");
            h.assertTrue(!f.shelf().getBlockState().getValue(ChiseledBookShelfBlock.SLOT_OCCUPIED_PROPERTIES.getFirst()), "Occupied slot state stale"); h.succeed(); });
    }
    @GameTest(template="test_altar",timeoutTicks=40) public static void zeroChancePreservesBookAndDoesNotReplay(GameTestHelper h) {
        var f = fixture(h); f.ready("unbreaking", 3, new ItemStack(Items.REDSTONE, 2), ItemStack.EMPTY); var original = f.shelf().getItem(0).copy();
        h.assertTrue(f.start().started(), "Zero chance recipe unavailable");
        h.runAtTickTime(20, () -> { h.assertTrue(targetLevel(f, "unbreaking") == 3 && ItemStack.matches(original, f.shelf().getItem(0)), "Zero chance consumed book");
            h.assertTrue(!f.start().started() && f.p1().item().getCount() == 0, "Already applied enchantment replayed"); h.succeed(); });
    }
    @GameTest(template="test_altar",timeoutTicks=40) public static void tagsOverlapAndPedestalOrderDoesNotMatter(GameTestHelper h) {
        var f = fixture(h); f.ready("smite", 1, new ItemStack(Items.IRON_INGOT), new ItemStack(Items.AMETHYST_SHARD));
        h.assertTrue(f.start().started(), "Overlapping tag produced greedy false negative");
        h.runAtTickTime(20, () -> { h.assertTrue(targetLevel(f, "smite") == 1 && f.p1().item().isEmpty() && f.p2().item().isEmpty(), "Tag allocation/consumption wrong"); h.succeed(); });
    }
    @GameTest(template="test_altar",timeoutTicks=40) public static void ingredientsCanBeSplitAcrossPedestals(GameTestHelper h) {
        var f = fixture(h); f.ready("knockback", 1, new ItemStack(Items.DIAMOND), new ItemStack(Items.DIAMOND));
        h.assertTrue(f.start().started(), "Split ingredient failed"); h.runAtTickTime(20, () -> { h.assertTrue(f.p1().item().isEmpty() && f.p2().item().isEmpty(), "Split stacks not consumed"); h.succeed(); });
    }
    @GameTest(template="test_altar",timeoutTicks=20) public static void ambiguousMatchesNeverPickSilently(GameTestHelper h) {
        var f = fixture(h); f.ready("fire_aspect", 1, new ItemStack(Items.GOLD_INGOT), ItemStack.EMPTY);
        var result = f.start(); h.assertTrue(!result.started() && result.reason().equals("ambiguous") && result.matches() == 2, "Ambiguous recipe picked");
        h.assertTrue(f.p1().item().getCount() == 1 && !f.shelf().getItem(0).isEmpty(), "Ambiguous validation consumed inputs"); h.succeed();
    }
    @GameTest(template="test_altar",timeoutTicks=20) public static void compatibilityExistingEnchantmentsAndBookLevels(GameTestHelper h) {
        var f = fixture(h); sharpness(f);
        AltarStateService.target(f.table(), new ItemStack(Items.IRON_PICKAXE)); h.assertTrue(InfusionMatcher.matches(h.getLevel(), AltarScanner.scan(h.getLevel(), f.pos())).isEmpty(), "Sharpness accepted pickaxe");
        var sword = new ItemStack(Items.IRON_SWORD); sword.enchant(enchant(h, "smite"), 1); AltarStateService.target(f.table(), sword);
        h.assertTrue(!f.start().started(), "Conflicting enchantments accepted");
        f.ready("unbreaking", 2, new ItemStack(Items.REDSTONE, 2), ItemStack.EMPTY); h.assertTrue(!f.start().started(), "Lower-level book authorized III"); h.succeed();
    }
    @GameTest(template="test_altar",timeoutTicks=40) public static void ordinaryBookBecomesEnchantedBook(GameTestHelper h) {
        var f = fixture(h); f.ready("knockback", 1, new ItemStack(Items.DIAMOND, 2), ItemStack.EMPTY); AltarStateService.target(f.table(), new ItemStack(Items.BOOK));
        h.assertTrue(f.start().started(), "Blank target book rejected"); h.runAtTickTime(20, () -> { h.assertTrue(AltarStateService.get(f.table()).target().is(Items.ENCHANTED_BOOK) && targetLevel(f, "knockback") == 1, "Book target not converted"); h.succeed(); });
    }
    @GameTest(template="test_altar",timeoutTicks=20) public static void demandScanIsReadOnlyAndChiseledKnowledgeIsNotPower(GameTestHelper h) {
        var f = fixture(h); sharpness(f); f.shelf().setItem(2, new ItemStack(Items.BOOK)); var old = f.shelf().saveWithFullMetadata(h.getLevel().registryAccess());
        var snapshot = AltarScanner.scan(h.getLevel(), f.pos());
        h.assertTrue(snapshot.valid() && snapshot.power() == 2 && snapshot.books().enchanted().size() == 1 && snapshot.books().empty().size() == 1, "Scan classification/power wrong");
        h.assertTrue(old.equals(f.shelf().saveWithFullMetadata(h.getLevel().registryAccess())), "Scan mutated library");
        h.setBlock(TABLE.offset(0, 0, 3), Blocks.AIR); h.setBlock(TABLE.offset(1, 0, 3), Blocks.AIR);
        h.assertTrue(AltarScanner.scan(h.getLevel(), f.pos()).power() == 0 && !f.start().started(), "Chiseled shelf provided power"); h.succeed();
    }
    @GameTest(template="test_altar",timeoutTicks=30) public static void removingTargetCancelsWithoutConsuming(GameTestHelper h) {
        var f = fixture(h); sharpness(f); h.assertTrue(f.start().started(), "Start failed"); f.click(false, ItemStack.EMPTY);
        h.runAtTickTime(5, () -> { h.assertTrue(RitualController.state(h.getLevel(), f.pos()) == RitualState.IDLE && f.p1().item().getCount() == 1 && !f.shelf().getItem(0).isEmpty(), "Target cancellation consumed input"); h.succeed(); });
    }
    @GameTest(template="test_altar",timeoutTicks=30) public static void changingIngredientCancelsImmediately(GameTestHelper h) {
        var f = fixture(h); sharpness(f); h.assertTrue(f.start().started(), "Start failed"); f.p1().setItem(ItemStack.EMPTY);
        h.assertTrue(RitualController.state(h.getLevel(), f.pos()) == RitualState.IDLE && f.p2().item().getCount() == 1 && !f.shelf().getItem(0).isEmpty(), "Ingredient cancellation partially consumed"); h.succeed();
    }
    @GameTest(template="test_altar",timeoutTicks=30) public static void bookRemovalCancelsEvenWithEquivalentReplacementElsewhere(GameTestHelper h) {
        var f = fixture(h); sharpness(f); f.shelf().setItem(1, book(h, "sharpness", 3)); h.assertTrue(f.start().started(), "Start failed"); f.shelf().removeItem(0, 1);
        h.runAtTickTime(5, () -> { h.assertTrue(RitualController.state(h.getLevel(), f.pos()) == RitualState.IDLE && targetLevel(f, "sharpness") == 0 && f.p1().item().getCount() == 1 && !f.shelf().getItem(1).isEmpty(), "Ritual switched to an unreserved equivalent book"); h.succeed(); });
    }
    @GameTest(template="test_altar",timeoutTicks=30) public static void fallingPowerCancelsWithoutCost(GameTestHelper h) {
        var f = fixture(h); sharpness(f); h.assertTrue(f.start().started(), "Start failed"); h.setBlock(TABLE.offset(0, 0, 3), Blocks.AIR);
        h.runAtTickTime(5, () -> { h.assertTrue(RitualController.state(h.getLevel(), f.pos()) == RitualState.IDLE && targetLevel(f, "sharpness") == 0 && f.p1().item().getCount() == 1, "Power loss did not cancel"); h.succeed(); });
    }
    @GameTest(template="test_altar",timeoutTicks=30) public static void replacingTableDropsTargetOnce(GameTestHelper h) {
        var f = fixture(h); sharpness(f); h.assertTrue(f.start().started(), "Start failed"); var area = new AABB(f.pos()).inflate(3);
        int before = h.getLevel().getEntitiesOfClass(ItemEntity.class, area).stream().filter(e -> e.getItem().is(Items.IRON_SWORD)).mapToInt(e -> e.getItem().getCount()).sum();
        h.setBlock(TABLE, Blocks.STONE); h.setBlock(TABLE, Blocks.AIR);
        int after = h.getLevel().getEntitiesOfClass(ItemEntity.class, area).stream().filter(e -> e.getItem().is(Items.IRON_SWORD)).mapToInt(e -> e.getItem().getCount()).sum();
        h.assertTrue(after - before == 1 && f.p1().item().getCount() == 1 && !f.shelf().getItem(0).isEmpty(), "Removal hook lost/duplicated target or ingredients"); h.succeed();
    }
    @GameTest(template="test_altar",timeoutTicks=30) public static void breakingPedestalDropsStackAndCancels(GameTestHelper h) {
        var f = fixture(h); sharpness(f); h.assertTrue(f.start().started(), "Start failed"); h.setBlock(P1, Blocks.AIR);
        h.assertTrue(RitualController.state(h.getLevel(), f.pos()) == RitualState.IDLE && f.p2().item().getCount() == 1 && !f.shelf().getItem(0).isEmpty(), "Pedestal removal consumed inputs");
        h.assertTrue(h.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(h.absolutePos(P1)).inflate(2)).stream().filter(e -> e.getItem().is(Items.AMETHYST_SHARD)).mapToInt(e -> e.getItem().getCount()).sum() == 1, "Pedestal item duplicated/lost"); h.succeed();
    }
    @GameTest(template="test_altar",timeoutTicks=20) public static void unloadEventCancelsAndCanRestart(GameTestHelper h) {
        var f = fixture(h); sharpness(f); h.assertTrue(f.start().started(), "Start failed");
        NeoForge.EVENT_BUS.post(new ChunkEvent.Unload(h.getLevel().getChunkAt(f.pos())));
        h.assertTrue(RitualController.state(h.getLevel(), f.pos()) == RitualState.IDLE && f.p1().item().getCount() == 1, "Unload consumed inputs");
        h.assertTrue(f.start().started(), "Lease leaked after cancellation"); RitualController.cancelAt(h.getLevel(), f.pos(), "admin_cancelled"); h.succeed();
    }
    @GameTest(template="test_altar",timeoutTicks=20) public static void overlappingAltarsCannotReserveSameBook(GameTestHelper h) {
        var f = fixture(h); sharpness(f); var other = TABLE.offset(0, 0, 1); h.setBlock(other, Blocks.ENCHANTING_TABLE);
        AltarStateService.target((EnchantingTableBlockEntity)h.getBlockEntity(other), new ItemStack(Items.IRON_SWORD)); h.assertTrue(f.start().started(), "First start failed");
        var second = RitualController.start(h.getLevel(), h.absolutePos(other), f.player().getUUID()); h.assertTrue(!second.started() && second.reason().equals("resource_reserved"), "Concurrent altars accepted shared resources");
        RitualController.cancelAt(h.getLevel(), f.pos(), "admin_cancelled"); h.succeed();
    }
    @GameTest(template="test_altar",timeoutTicks=20) public static void persistenceRoundTripKeepsItemsAndClearsRunning(GameTestHelper h) {
        var f = fixture(h); sharpness(f); h.assertTrue(f.start().started(), "Start failed");
        var lookup = h.getLevel().registryAccess(); var tableTag = f.table().saveWithFullMetadata(lookup); var pedestalTag = f.p1().saveWithFullMetadata(lookup);
        var loadedTable = (EnchantingTableBlockEntity)BlockEntity.loadStatic(f.pos(), f.table().getBlockState(), tableTag, lookup);
        var loadedPedestal = (PedestalBlockEntity)BlockEntity.loadStatic(f.p1().getBlockPos(), f.p1().getBlockState(), pedestalTag, lookup);
        h.assertTrue(ItemStack.matches(AltarStateService.get(f.table()).target(), AltarStateService.get(loadedTable).target()) && !AltarStateService.get(loadedTable).running(), "Target attachment round trip failed");
        h.assertTrue(ItemStack.matches(loadedPedestal.item(), f.p1().item()) && !loadedPedestal.running(), "Pedestal round trip failed");
        h.assertTrue(f.p1().getUpdateTag(lookup).getBoolean("Running"), "Pedestal animation not networked");
        RitualController.cancelAt(h.getLevel(), f.pos(), "admin_cancelled"); h.succeed();
    }
    @GameTest(template="test_altar",timeoutTicks=20) public static void physicalPedestalInsertRejectMergeRetrieveAndCopies(GameTestHelper h) {
        var f = fixture(h); var held = new ItemStack(Items.AMETHYST_SHARD, 40); f.player().setItemInHand(InteractionHand.MAIN_HAND, held);
        InfusionPedestalBlock.interact(h.getLevel(), f.p1().getBlockPos(), f.player(), InteractionHand.MAIN_HAND);
        h.assertTrue(held.getCount()==39 && f.p1().item().getCount() == 1, "Insertion duplicated stack");
        f.player().setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.AMETHYST_SHARD, 40));
        InfusionPedestalBlock.interact(h.getLevel(), f.p1().getBlockPos(), f.player(), InteractionHand.MAIN_HAND);
        h.assertTrue(f.p1().item().getCount() == 1 && f.player().getMainHandItem().getCount() == 40, "Merge exceeded capacity");
        var copy = f.p1().item(); copy.setCount(0); h.assertTrue(f.p1().item().getCount() == 1, "Mutable inventory escaped");
        f.player().setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY); InfusionPedestalBlock.interact(h.getLevel(), f.p1().getBlockPos(), f.player(), InteractionHand.MAIN_HAND);
        h.assertTrue(f.p1().item().isEmpty() && f.player().getInventory().countItem(Items.AMETHYST_SHARD) == 1, "Retrieval duplicated/lost items"); h.succeed();
    }
    @GameTest(template="test_altar",timeoutTicks=20) public static void offhandAndRepeatedClicksDoNotDuplicate(GameTestHelper h) {
        var f = fixture(h); f.click(false, new ItemStack(Items.IRON_SWORD)); f.click(false, new ItemStack(Items.IRON_SWORD));
        h.assertTrue(f.player().getMainHandItem().getCount() == 1, "Occupied table consumed second target");
        f.player().setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.IRON_SWORD));
        NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(f.player(), InteractionHand.OFF_HAND, f.pos(), new BlockHitResult(Vec3.atCenterOf(f.pos()), Direction.UP, f.pos(), false)));
        h.assertTrue(f.player().getOffhandItem().getCount() == 1 && AltarStateService.get(f.table()).target().getCount() == 1, "Offhand duplicated target"); h.succeed();
    }
    @GameTest(template="test_altar",timeoutTicks=20) public static void operatorCommandsAndRecipeValidation(GameTestHelper h) throws Exception {
        var f = fixture(h); sharpness(f); var dispatcher = h.getLevel().getServer().getCommands().getDispatcher(); var plain = f.player().createCommandSourceStack().withPermission(0);
        String position = f.pos().getX() + " " + f.pos().getY() + " " + f.pos().getZ();
        try { dispatcher.execute("inscribed debug altar scan " + position, plain); h.fail("Non-admin accessed debug commands"); } catch (com.mojang.brigadier.exceptions.CommandSyntaxException expected) {}
        h.assertTrue(dispatcher.execute("inscribed debug altar recipes " + position, plain.withPermission(2)) == 1, "Operator recipe command failed");
        h.assertTrue(InscribedRecipeRegistry.recipes().stream().noneMatch(r -> r.id().getPath().startsWith("test_invalid")), "Invalid resources admitted");
        var recipe = InscribedRecipeRegistry.recipes().stream().filter(r -> r.id().getPath().equals("test_sharpness_1")).findFirst().orElseThrow(); h.assertTrue(recipe.extraction().isPresent(), "Future extraction schema not parsed"); h.succeed();
    }
    @GameTest(template="test_altar",timeoutTicks=20) public static void schemaRejectsInvalidNumericAndRegistryValues(GameTestHelper h) {
        var base = JsonParser.parseString("{\"enchantment\":\"minecraft:sharpness\",\"level\":1,\"minimum_bookshelf_power\":0,\"infusion\":{\"ingredients\":[{\"item\":\"minecraft:iron_ingot\",\"count\":1}],\"duration_ticks\":1,\"book_consumption_chance\":0}}").getAsJsonObject();
        for (String replacement : new String[]{"0", "1.5", "6"}) { var json = base.deepCopy(); json.add("level", JsonParser.parseString(replacement)); reject(h, json); }
        for (String replacement : new String[]{"-1", "0.5", "\"2\""}) { var json = base.deepCopy(); json.add("minimum_bookshelf_power", JsonParser.parseString(replacement)); reject(h, json); }
        for (String field : List.of("duration_ticks", "book_consumption_chance")) { var json = base.deepCopy(); json.getAsJsonObject("infusion").addProperty(field, -1); reject(h, json); }
        var unknown = base.deepCopy(); unknown.addProperty("enchantment", "inscribed:missing"); reject(h, unknown);
        var badItem = base.deepCopy(); badItem.getAsJsonObject("infusion").getAsJsonArray("ingredients").get(0).getAsJsonObject().addProperty("item", "inscribed:missing"); reject(h, badItem);
        var count = base.deepCopy(); count.getAsJsonObject("infusion").getAsJsonArray("ingredients").get(0).getAsJsonObject().addProperty("count", 0); reject(h, count);
        var extraction = base.deepCopy(); extraction.add("extraction", JsonParser.parseString("{\"ingredients\":[],\"duration_ticks\":10,\"empty_books_required\":0}")); reject(h, extraction); h.succeed();
    }
    @GameTest(template="test_altar",timeoutTicks=20) public static void exactBookLevelIsExplicitAndExtractionIsNotExecutable(GameTestHelper h) {
        var f=fixture(h); f.ready("looting",3,new ItemStack(Items.COPPER_INGOT),ItemStack.EMPTY);
        h.assertTrue(!f.start().started(),"Exact-level recipe accepted higher-level book");
        f.shelf().setItem(0,book(h,"looting",2)); h.assertTrue(f.start().started(),"Exact-level recipe rejected exact book");
        RitualController.cancelAt(h.getLevel(),f.pos(),"admin_cancelled");
        h.assertTrue(InscribedRecipeRegistry.recipes().stream().anyMatch(r -> r.id().getPath().equals("test_extraction_only")&&r.infusion().isEmpty()&&r.extraction().isPresent()),"Extraction-only data rejected"); h.succeed();
    }
    @GameTest(template="test_altar",timeoutTicks=140) public static void finalStructureRevalidationRejectsExcessPedestals(GameTestHelper h) {
        var f=fixture(h); sharpness(f); h.assertTrue(f.start().started(),"Start failed");
        h.runAtTickTime(85,() -> { for(var offset:List.of(new BlockPos(-3,0,-1),new BlockPos(-3,0,1),new BlockPos(-1,0,2),new BlockPos(0,0,2),new BlockPos(1,0,2),new BlockPos(3,0,1),new BlockPos(3,0,-1))) h.setBlock(TABLE.offset(offset),InscribedRegistries.PEDESTAL.get()); });
        h.runAtTickTime(110,() -> { h.assertTrue(RitualController.state(h.getLevel(),f.pos())==RitualState.IDLE&&targetLevel(f,"sharpness")==0&&f.p1().item().getCount()==1&&!f.shelf().getItem(0).isEmpty(),"Final validation partially committed an invalid structure"); h.succeed(); });
    }
    @GameTest(template="test_altar",timeoutTicks=80) public static void scannerNeverForceLoadsNeighborChunks(GameTestHelper h) {
        var level=h.getLevel(); var pos=new BlockPos(20000000,80,20000000); level.getChunkAt(pos); level.setBlock(pos,Blocks.ENCHANTING_TABLE.defaultBlockState(),3);
        var neighbors=List.of(new ChunkPos(pos.offset(-1,0,0)),new ChunkPos(pos.offset(0,0,-1)),new ChunkPos(pos.offset(-1,0,-1)));
        var absent=neighbors.stream().filter(c -> !level.hasChunk(c.x,c.z)).toList(); h.assertTrue(!absent.isEmpty(),"No unloaded neighbor in isolated test fixture");
        var snapshot=AltarScanner.scan(level,pos); h.assertTrue(!snapshot.valid()&&snapshot.failure().equals("unloaded_chunk"),"Unloaded scan did not reject");
        for(var c:absent) h.assertTrue(!level.hasChunk(c.x,c.z),"Scanner force-loaded a neighbor"); level.setBlock(pos,Blocks.AIR.defaultBlockState(),3); h.succeed();
    }
    @GameTest(template="test_altar",batch="zy_config",timeoutTicks=20) public static void configRadiiAndOptionalShiftPreserveRetrieval(GameTestHelper h) {
        var f=fixture(h); sharpness(f); int oldRadius=InscribedConfig.BOOKSHELF_RADIUS.get(); boolean oldShift=InscribedConfig.REQUIRE_SHIFT.get();
        try {
            InscribedConfig.BOOKSHELF_RADIUS.set(2); h.assertTrue(AltarScanner.scan(h.getLevel(),f.pos()).power()==0,"Configured power radius ignored");
            InscribedConfig.BOOKSHELF_RADIUS.set(oldRadius); InscribedConfig.REQUIRE_SHIFT.set(false);
            f.click(false,new ItemStack(Items.STICK)); h.assertTrue(RitualController.state(h.getLevel(),f.pos())==RitualState.RUNNING,"Optional Shift did not permit held-item activation");
            f.click(false,ItemStack.EMPTY); h.assertTrue(AltarStateService.get(f.table()).target().isEmpty()&&RitualController.state(h.getLevel(),f.pos())==RitualState.IDLE,"Optional Shift disabled empty-hand retrieval");
        } finally { InscribedConfig.BOOKSHELF_RADIUS.set(oldRadius); InscribedConfig.REQUIRE_SHIFT.set(oldShift); }
        h.succeed();
    }
    @GameTest(template="test_altar",timeoutTicks=100) public static void completeCatalogHasEveryLegalLevelAndMatchesPhysicalInputs(GameTestHelper h) {
        var f=fixture(h); var extra=TABLE.offset(0,0,1);
        for(int x=-4;x<=4;x++) for(int z=-4;z<=4;z++) {
            var rel=TABLE.offset(x,0,z);
            if(x*x+z*z<=25&&!rel.equals(TABLE)&&!rel.equals(P1)&&!rel.equals(P2)&&!rel.equals(SHELF)&&!rel.equals(extra)) h.setBlock(rel,Blocks.BOOKSHELF);
        }
        var catalog=InscribedRecipeRegistry.recipes().stream().filter(r -> r.id().getPath().startsWith("minecraft/")||r.id().getPath().startsWith("create/")||r.id().getPath().equals("test_sharpness_1")).toList();
        h.assertTrue(catalog.size()==(ModList.get().isLoaded("create")?131:125),"Incomplete catalog or optional Create condition ignored: "+catalog.size());
        var registry=h.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        for(var holder:registry.holders().toList()) if(holder.unwrapKey().orElseThrow().location().getNamespace().equals("minecraft")||holder.unwrapKey().orElseThrow().location().getNamespace().equals("create")) {
            var name=holder.unwrapKey().orElseThrow().location();
            for(int level=1;level<=holder.value().getMaxLevel();level++) { final int n=level; h.assertTrue(catalog.stream().filter(r -> r.enchantment().equals(name)&&r.level()==n).count()==1,"Missing/duplicate catalog level: "+name+" "+level); }
        }
        for(var recipe:catalog) {
            AltarStateService.target(f.table(),new ItemStack(Items.BOOK)); var book=new ItemStack(Items.ENCHANTED_BOOK); book.enchant(registry.getHolder(recipe.enchantment()).orElseThrow(),recipe.level()); f.shelf().setItem(0,book);
            var pedestals=UnitPedestals.set(h,TABLE,List.of());
            var costs=recipe.infusion().orElseThrow().ingredients();
            for(int i=0;i<costs.size();i++) { var stack=costs.get(i).ingredient().getItems()[0].copyWithCount(costs.get(i).count()); pedestals.get(i).setItem(stack); }
            var matches=InfusionMatcher.matches(h.getLevel(),AltarScanner.scan(h.getLevel(),f.pos()));
            h.assertTrue(matches.stream().anyMatch(m -> m.recipe().id().equals(recipe.id())),"Authored recipe cannot match its physical inputs: "+recipe.id());
        }
        h.succeed();
    }
    @GameTest(template="test_altar",timeoutTicks=200) public static void actualCreateCapacityInfusion(GameTestHelper h) { createInfusion(h,"capacity","copper_backtank","copper_sheet"); }
    @GameTest(template="test_altar",timeoutTicks=200) public static void actualCreatePotatoRecoveryInfusion(GameTestHelper h) { createInfusion(h,"potato_recovery","potato_cannon",null); }
    private static void createInfusion(GameTestHelper h,String enchantment,String item,String theme) {
        if(!ModList.get().isLoaded("create")) { h.assertTrue(!Boolean.getBoolean("inscribed.requireCreate"),"Create test run lacks Create"); h.succeed(); return; }
        var f=fixture(h); var extra=TABLE.offset(0,0,1); h.setBlock(extra,InscribedRegistries.PEDESTAL.get());
        var p3=(PedestalBlockEntity)h.getBlockEntity(extra); var registry=h.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        for(int x=-3;x<=3;x++)h.setBlock(TABLE.offset(x,1,3),Blocks.BOOKSHELF);
        var enchantmentHolder=registry.getHolder(ResourceLocation.parse("create:"+enchantment)).orElseThrow();
        var target=new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(ResourceLocation.parse("create:"+item)));
        h.assertTrue(!target.isEmpty()&&target.supportsEnchantment(enchantmentHolder),"Real Create target does not support "+enchantment);
        AltarStateService.target(f.table(),target); var book=new ItemStack(Items.ENCHANTED_BOOK); book.enchant(enchantmentHolder,3); f.shelf().setItem(0,book);
        f.p1().setItem(new ItemStack(Items.AMETHYST_SHARD)); f.p2().setItem(new ItemStack(Items.COPPER_INGOT));
        p3.setItem(theme==null?new ItemStack(Items.POTATO):new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(ResourceLocation.parse("create:"+theme))));
        var fourth=TABLE.offset(-2,0,-1);h.setBlock(fourth,InscribedRegistries.PEDESTAL.get());((PedestalBlockEntity)h.getBlockEntity(fourth)).setItem(new ItemStack(Items.AMETHYST_SHARD));
        var start=f.start(); h.assertTrue(start.started(),"Create recipe did not start: "+start.reason());
        h.runAtTickTime(110,() -> { h.assertTrue(EnchantmentHelper.getEnchantmentsForCrafting(AltarStateService.get(f.table()).target()).getLevel(enchantmentHolder)==1,"Create enchantment not applied");
            h.assertTrue(f.p1().item().isEmpty()&&f.p2().item().isEmpty()&&p3.item().isEmpty(),"Create costs not consumed"); h.succeed(); });
    }
    private static void reject(GameTestHelper h, JsonObject json) {
        try { RecipeParser.parse(ResourceLocation.parse("inscribed:invalid"), json, h.getLevel().registryAccess()); h.fail("Invalid schema accepted: " + json); }
        catch (IllegalArgumentException | IllegalStateException expected) {}
    }
    @GameTest(template="test_altar",batch="zz_reload",timeoutTicks=300) public static void actualDatapackReloadCancelsActiveRitual(GameTestHelper h) {
        var f = fixture(h); sharpness(f); h.assertTrue(f.start().started(), "Start failed"); long generation = InscribedRecipeRegistry.generation();
        h.getLevel().getServer().reloadResources(h.getLevel().getServer().getPackRepository().getSelectedIds());
        h.succeedWhen(() -> { h.assertTrue(InscribedRecipeRegistry.generation() > generation, "Reload pending");
            h.assertTrue(RitualController.state(h.getLevel(), f.pos()) == RitualState.IDLE && f.p1().item().getCount() == 1 && targetLevel(f, "sharpness") == 0, "Reload did not cancel without cost"); });
    }
}
