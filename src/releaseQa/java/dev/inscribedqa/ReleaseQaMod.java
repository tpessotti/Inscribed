package dev.inscribedqa;

import net.neoforged.fml.common.Mod;

/** Separate test module: production must be loaded from the candidate JAR. */
@Mod("inscribed_qa")
public final class ReleaseQaMod {
    public ReleaseQaMod(){
        var source=dev.inscribed.Inscribed.class.getProtectionDomain().getCodeSource().getLocation().toString();
        if(!source.contains("inscribed-0.7.2.jar"))throw new IllegalStateException("RC tests loaded production from source instead of candidate JAR: "+source);
        com.mojang.logging.LogUtils.getLogger().info("Inscribed RC harness verified production JAR origin");
    }
}
