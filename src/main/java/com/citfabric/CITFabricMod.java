package com.citfabric;

import com.citfabric.cit.CITManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;

public class CITFabricMod implements ClientModInitializer {
    public static final String VERSION = "4.0.0";

    @Override
    public void onInitializeClient() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES)
            .registerReloadListener(CITManager.INSTANCE);
        System.out.println("[CITFabric] v" + VERSION + " initialized for MC 1.21.11");
        System.out.println("[CITFabric] Will scan: optifine/cit, citresewn/cit, mcpatcher/cit");
    }
}
