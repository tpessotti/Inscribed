package dev.inscribed.client;
import dev.inscribed.altar.AltarStateService;
import dev.inscribed.config.InscribedConfig;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
/** One positional loop per active infusion. Attachment changes stop it immediately. */
public final class RitualAmbientSound extends AbstractTickableSoundInstance {
    private static final Map<EnchantingTableBlockEntity,RitualAmbientSound> ACTIVE=new IdentityHashMap<>();
    private final EnchantingTableBlockEntity table;
    private RitualAmbientSound(EnchantingTableBlockEntity table){
        super(BuiltInRegistries.SOUND_EVENT.getOptional(ResourceLocation.parse(InscribedConfig.INFUSION_AMBIENT_SOUND.get())).orElse(SoundEvents.BEACON_AMBIENT),SoundSource.BLOCKS,RandomSource.create());
        this.table=table;BlockPos p=table.getBlockPos();x=p.getX()+.5;y=p.getY()+.5;z=p.getZ()+.5;looping=true;delay=0;volume=.18f*InscribedConfig.SOUND_VOLUME.get().floatValue();
    }
    public static void update(){
        var mc=Minecraft.getInstance();ACTIVE.entrySet().removeIf(e->{if(!e.getValue().valid()){mc.getSoundManager().stop(e.getValue());return true;}return false;});
        if(mc.level==null||!InscribedConfig.SOUNDS.get())return;
        mc.levelRenderer.iterateVisibleBlockEntities(be->{if(be instanceof EnchantingTableBlockEntity t&&AltarStateService.get(t).running()&&!AltarStateService.get(t).extraction()&&!ACTIVE.containsKey(t)){
            var sound=new RitualAmbientSound(t);if(sound.valid()){ACTIVE.put(t,sound);mc.getSoundManager().play(sound);}
        }});
    }
    private boolean valid(){var mc=Minecraft.getInstance();var state=AltarStateService.get(table);return mc.level!=null&&mc.level==table.getLevel()&&!table.isRemoved()&&state.running()&&!state.extraction()&&InscribedConfig.SOUNDS.get()&&InscribedConfig.SOUND_VOLUME.get()>0&&mc.player!=null&&mc.player.distanceToSqr(x,y,z)<256;}
    @Override public void tick(){if(!valid()){stop();return;}volume=.18f*InscribedConfig.SOUND_VOLUME.get().floatValue();pitch=1;}
    public static int activeCount(){return ACTIVE.size();}
}
