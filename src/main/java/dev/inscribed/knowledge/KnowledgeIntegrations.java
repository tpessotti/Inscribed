package dev.inscribed.knowledge;
import com.mojang.logging.LogUtils;
import net.neoforged.fml.ModList;
/** Optional classes are resolved only after the presence check. */
public final class KnowledgeIntegrations {
    private static KnowledgeIntegration instance;
    public static KnowledgeIntegration get() {
        if(!dev.inscribed.config.InscribedConfig.LSBC_KNOWLEDGE.get())return new NoKnowledgeIntegration();
        if(instance==null){
            instance=new NoKnowledgeIntegration();
            if(ModList.get().isLoaded("lsbc"))try{
                instance=(KnowledgeIntegration)Class.forName("dev.inscribed.compat.lsbc.LSBCKnowledgeIntegration").getConstructor().newInstance();
                instance.initialize();
            }catch(ReflectiveOperationException|LinkageError e){instance=new NoKnowledgeIntegration();LogUtils.getLogger().error("Cannot initialize optional LSBC knowledge bridge",e);}
        }
        return instance;
    }
    private KnowledgeIntegrations() {}
}
