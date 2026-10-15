package dev.inscribed.ritual;

import dev.inscribed.config.InscribedConfig;
import dev.inscribed.recipe.RitualMatch;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;

public final class RitualVisuals {
    public static void start(ServerLevel level, BlockPos pos,RitualType type) {
        sound(level,pos,InscribedConfig.VALIDATION_SOUND,SoundEvents.BOOK_PAGE_TURN,.22f,1.2f);
        sound(level,pos,type==RitualType.EXTRACTION ? InscribedConfig.EXTRACTION_START_SOUND : InscribedConfig.INFUSION_START_SOUND,SoundEvents.ENCHANTMENT_TABLE_USE,.45f,type==RitualType.EXTRACTION ? .55f : .9f);
        if (InscribedConfig.PARTICLES.get()) level.sendParticles(ParticleTypes.ENCHANT,pos.getX()+.5,pos.getY()+1.55,pos.getZ()+.5,4,.12,.1,.12,.015);
    }
    public static void running(ServerLevel level, BlockPos table, RitualMatch match, int ticks, int duration) {
        double progress=ticks/(double)Math.max(1,duration);
        int interval=Math.max(8,(int)(InscribedConfig.SOUND_INTERVAL.get()*(InscribedConfig.PHASE_VISUALS.get() ? 1-.45*progress : 1)));
        if(ticks%interval==0 && ticks<duration) sound(level,table,match.type()==RitualType.EXTRACTION ? InscribedConfig.EXTRACTION_TRANSFER_SOUND : InscribedConfig.RUNNING_SOUND,
                match.type()==RitualType.EXTRACTION ? SoundEvents.BOOK_PAGE_TURN : SoundEvents.AMETHYST_BLOCK_CHIME,(float)(.15+.2*progress),match.type()==RitualType.EXTRACTION ? (float)(1.1-.35*progress) : (float)(.8+.45*progress));
        if(!InscribedConfig.PARTICLES.get() || ticks%InscribedConfig.PARTICLE_INTERVAL.get()!=0)return;
        var phase=RitualPhase.active(progress);
        if(InscribedConfig.PHASE_VISUALS.get() && (phase==RitualPhase.VALIDATION || phase==RitualPhase.AWAKENING))return;
        var core=new net.minecraft.world.phys.Vec3(table.getX()+.5,table.getY()+1.05,table.getZ()+.5);
        var target=core.add(0,.7,0);
        // Two source pedestals per emission; rotate through all reserved sources.
        int sourceIndex=0,selected=ticks/Math.max(1,InscribedConfig.PARTICLE_INTERVAL.get());
        for(var ingredient:match.ingredients()) {
            int index=sourceIndex++;
            if(Math.floorMod(index-selected,Math.max(1,match.ingredients().size()))>1)continue;
            var pos=ingredient.pedestal().pos();
            path(level,new net.minecraft.world.phys.Vec3(pos.getX()+.5,pos.getY()+1.2,pos.getZ()+.5),core,ticks+pos.hashCode(),false);
        }
        if(phase==RitualPhase.RESONANCE)return;
        boolean reverse=match.type()==RitualType.EXTRACTION;
        path(level,reverse ? target : core,reverse ? core : target,ticks,reverse);
        // Only actual reserved book slots participate. At most three shelf paths.
        match.books().stream().map(b->b.slot().pos()).distinct().limit(3).forEach(pos -> {
            var shelf=new net.minecraft.world.phys.Vec3(pos.getX()+.5,pos.getY()+.8,pos.getZ()+.5);
            path(level,reverse ? core : shelf,reverse ? shelf : core,ticks,reverse);
            if(ticks%20==0)level.sendParticles(ParticleTypes.ENCHANT,shelf.x,shelf.y,shelf.z,1,.1,.1,.1,.015);
        });
        if(phase==RitualPhase.CRESCENDO)level.sendParticles(ParticleTypes.ENCHANT,target.x,target.y,target.z,1,.07,.08,.07,.005);
    }
    private static void path(ServerLevel level,net.minecraft.world.phys.Vec3 from,net.minecraft.world.phys.Vec3 to,int ticks,boolean reverse) {
        double t=Math.floorMod(ticks,40)/40.0;
        var point=from.lerp(to,t).add(0,.12*Math.sin(t*Math.PI),0);
        var velocity=to.subtract(from).normalize().scale(.018);
        // Marching small glyphs establish direction without long-lived white
        // END_ROD trails obscuring the table and participating bookshelf.
        level.sendParticles(reverse ? ParticleTypes.REVERSE_PORTAL : ParticleTypes.ENCHANT,point.x,point.y,point.z,0,velocity.x,velocity.y,velocity.z,1);
    }
    public static void invalid(ServerLevel level,BlockPos pos,net.minecraft.world.entity.player.Player player) {
        if(!InscribedConfig.SOUNDS.get() || player instanceof net.neoforged.neoforge.common.util.FakePlayer)return;
        var event=net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.getOptional(net.minecraft.resources.ResourceLocation.parse(InscribedConfig.ERROR_SOUND.get())).orElse(SoundEvents.BOOK_PAGE_TURN);
        if(player instanceof net.minecraft.server.level.ServerPlayer server)server.playNotifySound(event,SoundSource.BLOCKS,.15f*InscribedConfig.SOUND_VOLUME.get().floatValue(),.7f);
    }
    public static void complete(ServerLevel level, BlockPos pos,RitualMatch match) {
        sound(level,pos,match.type()==RitualType.EXTRACTION ? InscribedConfig.EXTRACTION_COMPLETE_SOUND : InscribedConfig.INFUSION_COMPLETE_SOUND,match.type()==RitualType.EXTRACTION ? SoundEvents.BOOK_PAGE_TURN : SoundEvents.PLAYER_LEVELUP,.5f,1.3f);
        if(match.type()==RitualType.EXTRACTION && InscribedConfig.PARTICLES.get()) {var book=match.book().pos();level.sendParticles(ParticleTypes.ENCHANT,book.getX()+.5,book.getY()+.8,book.getZ()+.5,8,.12,.12,.12,.02);}
        if (InscribedConfig.PARTICLES.get()) level.sendParticles(ParticleTypes.END_ROD, pos.getX() + .5, pos.getY() + 1.6, pos.getZ() + .5, 2, .1, .1, .1, .005);
    }
    public static void failed(ServerLevel level,BlockPos pos) {
        sound(level,pos,InscribedConfig.EXTRACTION_FAILURE_SOUND,SoundEvents.FIRE_EXTINGUISH,.6f,.7f);
        if(InscribedConfig.PARTICLES.get())level.sendParticles(ParticleTypes.SMOKE,pos.getX()+.5,pos.getY()+1.7,pos.getZ()+.5,8,.15,.12,.15,.01);
    }
    private static void sound(ServerLevel level,BlockPos pos,net.neoforged.neoforge.common.ModConfigSpec.ConfigValue<String> configured,SoundEvent fallback,float volume,float pitch) {
        if(!InscribedConfig.SOUNDS.get())return;
        var event=net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.getOptional(net.minecraft.resources.ResourceLocation.parse(configured.get())).orElse(fallback);
        level.playSound(null,pos,event,SoundSource.BLOCKS,volume*InscribedConfig.SOUND_VOLUME.get().floatValue(),pitch);
    }
    private RitualVisuals() {}
}
