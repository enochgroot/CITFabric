package com.citfabric;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

public class CITFabric implements ClientModInitializer {
    public static final String MOD_ID = "citfabric";
    @Override
    public void onInitializeClient() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(
            new SimpleSynchronousResourceReloadListener() {
                @Override public ResourceLocation getFabricId() {
                    return ResourceLocation.fromNamespaceAndPath(MOD_ID, "cit_loader");
                }
                @Override public void onResourceManagerReload(ResourceManager manager) {
                    CITManager.getInstance().reload(manager);
                }
            });
        ClientTickEvents.END_CLIENT_TICK.register(c -> CITManager.getInstance().tick());
        System.out.println("[CITFabric] Initialised");
    }
}
