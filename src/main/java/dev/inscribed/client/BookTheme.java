package dev.inscribed.client;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/** Opaque paper and four semantic text levels; native GUI scale determines actual pixel size. */
public final class BookTheme {
    public static final int INK=0xFF21180F, METADATA=0xFF54442D, PAPER=0xFFF2E5C4,
            DOCUMENT=0xFFFFF3D9, BORDER=0xFF705338, SELECTED=0xFFD8BE84,
            HOVER=0xFFE9D7AC, RIBBON=0xFFE2CE9A, ACTIVE_RIBBON=0xFF963D32;
    private BookTheme() {}
    public static Component title(Component value) {return value.copy().withStyle(ChatFormatting.BOLD);}
}
