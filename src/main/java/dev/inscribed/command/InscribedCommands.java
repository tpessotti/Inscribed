package dev.inscribed.command;

import dev.inscribed.altar.*;
import dev.inscribed.recipe.InfusionMatcher;
import dev.inscribed.ritual.RitualController;
import net.minecraft.commands.*;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class InscribedCommands {
    public static void register(RegisterCommandsEvent event) {
        var altar = Commands.literal("altar");
        for (String action : new String[]{"scan", "recipes", "start", "cancel", "errors"}) {
            altar.then(Commands.literal(action).executes(c -> execute(c.getSource(), lookedAt(c.getSource()), action))
                    .then(Commands.argument("pos", BlockPosArgument.blockPos()).executes(c -> execute(c.getSource(), BlockPosArgument.getLoadedBlockPos(c, "pos"), action))));
        }
        event.getDispatcher().register(Commands.literal("inscribed").requires(s -> s.hasPermission(2))
                .then(Commands.literal("debug").then(altar)
                        .then(Commands.literal("recipe").then(Commands.literal("audit").executes(c -> audit(c.getSource()))))
                        .then(Commands.literal("loot").executes(c -> {
                            c.getSource().sendSuccess(() -> Component.literal("Inscribed loot: " + dev.inscribed.loot.LootInjection.path()
                                    + " | enabled=" + dev.inscribed.config.InscribedConfig.LOOT_INJECTION.get()
                                    + " | books=" + dev.inscribed.config.InscribedConfig.BOOK_LOOT.get()
                                    + " | catalysts=" + dev.inscribed.config.InscribedConfig.CATALYST_LOOT.get()),false);
                            for(var pool:dev.inscribed.loot.LootInjection.pools()) c.getSource().sendSuccess(() -> Component.literal(pool.id()+" | tables="+pool.tables()+" | chance="+pool.chance()+" | rolls="+pool.minRolls()+".."+pool.maxRolls()+" | weights="+pool.entries().stream().map(e->e.item()+"/"+e.enchantment()+":"+e.weight()).toList()),false);
                            return dev.inscribed.loot.LootInjection.pools().size();
                        }).then(Commands.literal("table").then(Commands.argument("id",net.minecraft.commands.arguments.ResourceLocationArgument.id()).executes(c -> {
                            var id=net.minecraft.commands.arguments.ResourceLocationArgument.getId(c,"id");int count=0;
                            for(var pool:dev.inscribed.loot.LootInjection.pools())if(pool.tables().contains(id)){count++;c.getSource().sendSuccess(() -> Component.literal(pool.id()+" | "+pool.theme()+" | host tier="+pool.structureTier()+" | chance="+pool.chance()+" | entries="+pool.entries()),false);}return count;
                        }))).then(Commands.literal("enchantment").then(Commands.argument("id",net.minecraft.commands.arguments.ResourceLocationArgument.id()).executes(c -> {
                            var sources=dev.inscribed.recipe.ProgressionAudit.sources(net.minecraft.commands.arguments.ResourceLocationArgument.getId(c,"id"));for(var value:sources)c.getSource().sendSuccess(() -> Component.literal(value),false);return sources.size();
                        }))))
                        .then(Commands.literal("balance").then(Commands.argument("recipe",net.minecraft.commands.arguments.ResourceLocationArgument.id()).executes(c -> {
                            var id=net.minecraft.commands.arguments.ResourceLocationArgument.getId(c,"recipe");var recipe=dev.inscribed.recipe.InscribedRecipeRegistry.recipes().stream().filter(r->r.id().equals(id)).findFirst();
                            if(recipe.isEmpty()){c.getSource().sendFailure(Component.literal("Unknown Inscribed recipe "+id));return 0;}
                            var r=recipe.get();c.getSource().sendSuccess(() -> Component.literal(r.id()+" | "+r.enchantment()+" "+r.level()+" | "+r.tier()+" | power="+r.minimumPower()+" | infusion="+r.infusion()+" | extraction="+r.extraction()+" | sources="+dev.inscribed.recipe.ProgressionAudit.sources(r.enchantment())),false);return 1;
                        })))
                        .then(Commands.literal("progression").then(Commands.literal("report").executes(c -> {
                            var warnings=dev.inscribed.recipe.ProgressionAudit.warnings();var counts=new java.util.EnumMap<dev.inscribed.recipe.ProgressionTier,Long>(dev.inscribed.recipe.ProgressionTier.class);
                            for(var recipe:dev.inscribed.recipe.InscribedRecipeRegistry.recipes())counts.merge(recipe.tier(),1L,Long::sum);
                            c.getSource().sendSuccess(() -> Component.literal("Inscribed progression: "+counts+" | warnings="+warnings.size()),false);for(var warning:warnings.stream().limit(32).toList())c.getSource().sendSuccess(() -> Component.literal(warning),false);return warnings.isEmpty() ? 1 : 0;
                        }))))
                .then(Commands.literal("config").executes(c->{c.getSource().sendSuccess(()->Component.literal("Inscribed recipes: "+dev.inscribed.config.ConfigRecipeStore.path()+" | options: inscribed-server.toml | /inscribed reload"),false);return 1;}))
                .then(Commands.literal("reload").executes(c->{
                    var source=c.getSource();var server=source.getServer();
                    source.sendSuccess(()->Component.translatable("config.inscribed.reloading"),false);
                    server.reloadResources(server.getPackRepository().getSelectedIds()).whenComplete((unused,error)->server.execute(()->{
                        if(error!=null)source.sendFailure(Component.translatable("config.inscribed.reload_failed"));
                        else source.sendSuccess(()->Component.translatable("config.inscribed.reloaded",dev.inscribed.recipe.InscribedRecipeRegistry.recipes().size()),false);
                    }));return 1;
                })));
    }
    private static BlockPos lookedAt(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player = source.getPlayerOrException(); var eye = player.getEyePosition();
        var hit = source.getLevel().clip(new ClipContext(eye, eye.add(player.getLookAngle().scale(8)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        return hit.getType() == HitResult.Type.BLOCK ? hit.getBlockPos() : player.blockPosition();
    }
    private static int execute(CommandSourceStack source, BlockPos pos, String action) {
        var level = source.getLevel();
        if (!level.hasChunkAt(pos) || !level.getBlockState(pos).is(Blocks.ENCHANTING_TABLE)) { source.sendFailure(Component.translatable("message.inscribed.missing_table")); return 0; }
        if (action.equals("start")) {
            if (source.getEntity() == null) { source.sendFailure(Component.literal("Start requires a player.")); return 0; }
            var result = RitualController.start(level, pos, source.getEntity().getUUID());
            source.sendSuccess(() -> Component.translatable("message.inscribed." + result.reason(), result.matches()), false); return result.started() ? 1 : 0;
        }
        if (action.equals("cancel")) { RitualController.cancelAt(level, pos, "admin_cancelled"); return 1; }
        var altar = AltarScanner.scan(level, pos); var matches = dev.inscribed.recipe.RitualMatcher.matches(level, altar);
        if(action.equals("errors")) {
            var failures=dev.inscribed.recipe.RitualDiagnostics.failures(level,altar);
            source.sendSuccess(() -> Component.translatable("message.inscribed."+dev.inscribed.recipe.RitualDiagnostics.reason(level,altar)),false);
            for(var failure:failures.stream().limit(12).toList())source.sendSuccess(() -> Component.literal(failure.mode()+":"+failure.recipe()+" | "+failure.reason()),false);
            return failures.size();
        }
        source.sendSuccess(() -> Component.literal("Altar " + pos.toShortString() + " | state=" + RitualController.state(level, pos)
                + " | target=" + altar.target() + " | power=" + altar.power() + " | failure=" + altar.failure()), false);
        source.sendSuccess(() -> Component.literal("Pedestals=" + altar.pedestals().stream().map(p -> p.pos().toShortString() + ": " + p.stack()).toList()), false);
        source.sendSuccess(() -> Component.literal("Chiseled bookshelves=" + altar.shelves().size() + " | enchanted books="
                + altar.books().enchanted().stream().map(b -> b.pos().toShortString() + "/" + b.slot() + ": " + b.stack()).toList()
                + " | empty books=" + altar.books().empty().size()), false);
        source.sendSuccess(() -> Component.literal("Matches=" + matches.stream().map(m -> m.type()+":"+m.recipe().id()).toList()), false);
        return matches.size();
    }
    private InscribedCommands() {}
    private static int audit(CommandSourceStack source) {
        int invalid=0;
        for(var recipe:dev.inscribed.recipe.InscribedRecipeRegistry.recipes()) {
            boolean valid=recipe.infusion().map(i->dev.inscribed.recipe.RecipeConstraints.valid(i.ingredients())).orElse(true)
                    && recipe.extraction().map(i->dev.inscribed.recipe.RecipeConstraints.valid(i.ingredients())).orElse(true);
            if(!valid){invalid++;source.sendFailure(Component.literal("Invalid pedestal constraints: "+recipe.id()));}
        }
        final int count=invalid;
        source.sendSuccess(() -> Component.literal("Inscribed recipe audit: "+dev.inscribed.recipe.InscribedRecipeRegistry.recipes().size()+" loaded | invalid="+count+" | maximum 8 ingredients, one item per pedestal"),false);
        return invalid==0 ? 1 : 0;
    }
}
