package dev.inscribed.ritual;

/** Deterministic server-time curve shared by renderers and ritual effects. */
public enum RitualPhase {
    IDLE, VALIDATION, AWAKENING, RESONANCE, TRANSFER, CRESCENDO, COMPLETION, DISSIPATION;
    public static RitualPhase active(double progress) {
        return progress < .03 ? VALIDATION : progress < .15 ? AWAKENING : progress < .32 ? RESONANCE : progress < .82 ? TRANSFER : CRESCENDO;
    }
    public static double strength(double progress) {
        double p=Math.max(0,Math.min(1,progress));return .15+.65*p*p;
    }
    public static double fade(double remaining) { return Math.max(0,Math.min(1,remaining/20)); }
}
