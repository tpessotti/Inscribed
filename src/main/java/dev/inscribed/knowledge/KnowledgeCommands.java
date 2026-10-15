package dev.inscribed.knowledge;
import net.minecraft.commands.*;
import net.minecraft.commands.arguments.*;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
public final class KnowledgeCommands {
    public static void register(RegisterCommandsEvent e){
        var knowledge=Commands.literal("knowledge");
        knowledge.then(Commands.literal("status").then(Commands.argument("player",EntityArgument.player()).executes(c->{var p=EntityArgument.getPlayer(c,"player");c.getSource().sendSuccess(()->Component.literal(KnowledgeService.get(p).toString()),false);return 1;})));
        knowledge.then(Commands.literal("clear").then(Commands.argument("player",EntityArgument.player()).executes(c->{KnowledgeService.clear(EntityArgument.getPlayer(c,"player"));return 1;})));
        for(var action:new String[]{"learn","forget"}){
            var branch=Commands.literal(action);
            for(var kind:new String[]{"enchantment","recipe"})branch.then(Commands.literal(kind).then(Commands.argument("player",EntityArgument.player()).then(Commands.argument("id",ResourceLocationArgument.id()).executes(c->{
                var id=ResourceLocationArgument.getId(c,"id");var p=EntityArgument.getPlayer(c,"player");boolean enchantment=kind.equals("enchantment");
                boolean exists=enchantment?p.registryAccess().registryOrThrow(Registries.ENCHANTMENT).containsKey(id):dev.inscribed.recipe.InscribedRecipeRegistry.recipes().stream().anyMatch(r->r.id().equals(id));
                if(!exists&&action.equals("learn")){c.getSource().sendFailure(Component.literal("Unknown "+kind+": "+id));return 0;}
                KnowledgeService.change(p,id,enchantment,action.equals("learn"));c.getSource().sendSuccess(()->Component.literal(action+" "+kind+" "+id+" -> "+p.getScoreboardName()),false);return 1;
            }))));knowledge.then(branch);
        }
        e.getDispatcher().register(Commands.literal("inscribed").requires(s->s.hasPermission(2)).then(knowledge).then(Commands.literal("debug").then(Commands.literal("recipes").then(Commands.literal("visible").then(Commands.argument("player",EntityArgument.player()).executes(c->{
            var data=KnowledgeService.get(EntityArgument.getPlayer(c,"player"));for(var r:dev.inscribed.recipe.InscribedRecipeRegistry.recipes())c.getSource().sendSuccess(()->Component.literal(r.id()+" "+RecipeVisibilityService.reason(r,data)),false);return 1;
        }))))));
    }
}
