package com.citfabric;

import com.citfabric.cit.CITManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;

public class CITFabricMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES)
            .registerReloadListener(CITManager.INSTANCE);
        System.out.println("[CITFabric] v1.0.0 loaded");
    }
}
