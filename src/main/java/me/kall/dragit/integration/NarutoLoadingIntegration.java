package me.kall.dragit.integration;

import net.neoforged.fml.loading.FMLLoader;

public final class NarutoLoadingIntegration {
    private static final boolean INTEGRATABLE = FMLLoader.getLoadingModList().getModFileById("narutoloading") != null;

    public static boolean isIntegratable() {
        return INTEGRATABLE;
    }
}
