package dev.inscribed.compat.ponder;

import dev.inscribed.pedestal.PedestalBlockEntity;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.scene.*;
import net.createmod.ponder.api.PonderPalette;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.phys.Vec3;

/** Visual-only example of the shipped Sharpness I recipe; no server ritual/inventory mutation. */
public final class InscribedPonderScenes {
    private static ItemStack sharpness(ItemStack stack,net.minecraft.core.RegistryAccess registries){
        stack.enchant(registries.registryOrThrow(Registries.ENCHANTMENT).getHolder(ResourceLocation.parse("minecraft:sharpness")).orElseThrow(),1);return stack;
    }
    private static void pedestalItem(SceneBuilder scene,SceneBuildingUtil util,BlockPos pos,ItemStack stack){
        scene.world().modifyBlockEntityNBT(util.select().position(pos),PedestalBlockEntity.class,tag->{
            if(stack.isEmpty())tag.remove("Item");else tag.put("Item",stack.save(scene.world().getHolderLookupProvider()));
        });
    }
    public static void enchantingSetup(SceneBuilder scene,SceneBuildingUtil util){
        var table=new BlockPos(3,1,3);var library=new BlockPos(3,1,1);
        var left=new BlockPos(1,1,3);var right=new BlockPos(5,1,3);var front=new BlockPos(3,1,5);
        scene.title("enchanting_setup","Enchanting with a Physical Library");
        scene.configureBasePlate(0,0,7);scene.scaleSceneView(.8f);scene.showBasePlate();scene.idle(10);
        scene.world().showSection(util.select().position(table),Direction.DOWN);
        scene.overlay().showText(70).text("Use an Enchanting Table as the heart of your altar. This example shows the default Sharpness I recipe.").pointAt(Vec3.atCenterOf(table)).placeNearTarget();scene.idle(80);
        scene.addKeyframe();scene.world().showSection(util.select().position(1,1,1),Direction.DOWN);scene.world().showSection(util.select().position(5,1,1),Direction.DOWN);
        scene.overlay().showText(75).text("Two ordinary bookshelves provide the 2 Bookshelf Power needed here. Default scan: 5 blocks horizontally and 2 vertically.").pointAt(new Vec3(1.5,1.5,1.5)).placeNearTarget();scene.idle(85);
        scene.addKeyframe();scene.world().showSection(util.select().position(library),Direction.DOWN);
        var book=new ItemStack(Items.ENCHANTED_BOOK);
        scene.overlay().showControls(new Vec3(3.5,2.1,1.5),Pointing.DOWN,65).rightClick().withItem(book);
        scene.world().modifyBlockEntity(library,ChiseledBookShelfBlockEntity.class,shelf->shelf.setItem(0,sharpness(book.copy(),shelf.getLevel().registryAccess())));
        scene.overlay().showText(85).text("Store a Sharpness I or higher book in the Chiseled Bookshelf. Learning a recipe never replaces this physical reference book.").pointAt(Vec3.atCenterOf(library)).placeNearTarget();scene.idle(95);
        scene.addKeyframe();
        for(var pos:new BlockPos[]{left,right,front})scene.world().showSection(util.select().position(pos),Direction.DOWN);
        pedestalItem(scene,util,left,new ItemStack(Items.AMETHYST_SHARD));
        pedestalItem(scene,util,right,new ItemStack(Items.AMETHYST_SHARD));
        pedestalItem(scene,util,front,new ItemStack(Items.IRON_INGOT));
        scene.overlay().showText(95).text("Place one amethyst shard on each side pedestal and one iron ingot on the front pedestal. One item per pedestal; at most eight. Default pedestal radius: 4 blocks.").pointAt(Vec3.atCenterOf(front)).placeNearTarget();scene.idle(105);
        scene.addKeyframe();scene.overlay().showControls(new Vec3(3.5,2.3,3.5),Pointing.DOWN,55).rightClick().withItem(new ItemStack(Items.IRON_SWORD));
        var target=scene.world().createItemEntity(new Vec3(3.5,2.4,3.5),Vec3.ZERO,new ItemStack(Items.IRON_SWORD));
        scene.world().modifyEntity(target,entity->entity.setNoGravity(true));
        scene.overlay().showText(65).text("Right-click the table with a compatible item to place your target.").pointAt(new Vec3(3.5,2.4,3.5)).placeNearTarget();scene.idle(75);
        scene.addKeyframe();scene.overlay().showControls(new Vec3(3.5,2.3,3.5),Pointing.DOWN,65).rightClick().whileSneaking();
        scene.overlay().showText(80).text("Shift + right-click to start Infusion. This recipe takes 5 seconds and uses no XP or lapis.").pointAt(Vec3.atCenterOf(table)).placeNearTarget();
        for(var pos:new BlockPos[]{left,right,front}){scene.overlay().showLine(PonderPalette.GREEN,Vec3.atCenterOf(pos).add(0,1,0),new Vec3(3.5,2.4,3.5),90);scene.world().modifyBlockEntity(pos,PedestalBlockEntity.class,p->p.visual(100,false));}
        scene.idle(100);
        for(var pos:new BlockPos[]{left,right,front}){pedestalItem(scene,util,pos,ItemStack.EMPTY);scene.world().modifyBlockEntity(pos,PedestalBlockEntity.class,p->p.running(false));}
        scene.world().modifyEntity(target,entity->{if(entity instanceof ItemEntity item){var result=new ItemStack(Items.IRON_SWORD);item.setItem(sharpness(result,item.level().registryAccess()));}});
        scene.effects().indicateSuccess(table);
        scene.overlay().showText(85).text("The sword receives Sharpness I and the reagents are consumed. The reference book has a 10% consumption chance in this default recipe.").pointAt(new Vec3(3.5,2.4,3.5)).placeNearTarget();scene.idle(95);
        scene.addKeyframe();scene.overlay().showControls(new Vec3(3.5,2.3,3.5),Pointing.DOWN,55).rightClick();
        scene.overlay().showText(75).text("Right-click with an empty main hand to retrieve the enchanted item. Check the Guide or JEI: modpacks can change ingredients, power and chances.").pointAt(Vec3.atCenterOf(table)).placeNearTarget();scene.idle(80);
        scene.world().modifyEntity(target,entity->entity.discard());scene.markAsFinished();
    }
}
