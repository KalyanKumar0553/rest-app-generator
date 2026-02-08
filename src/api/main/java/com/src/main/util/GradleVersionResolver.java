package com.src.main.util;

public final class GradleVersionResolver {
    private GradleVersionResolver() {}

    public static String forBoot(String bootVersion) {
        // very safe defaults; adjust when you tighten your matrix
        // Boot 3.2.x → Gradle 8.5+, Boot 3.3.x → 8.7+, 3.4.x → 8.10+
        if (bootVersion == null) return "8.10.2";
        String v = bootVersion.trim();
        if (v.startsWith("3.4")) return "8.10.2";
        if (v.startsWith("3.3")) return "8.10.2";
        if (v.startsWith("3.2")) return "8.5";
        return "8.10.2";
    }
}
