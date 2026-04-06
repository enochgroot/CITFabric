package com.citfabric;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.resources.ResourceLocation;
public class CITFabric implements ClientModInitializer {
    public static final String MOD_ID = "citfabric";
    @Override
    public void onInitializeClient() {
        // Minimal test — just check ResourceLocation compiles
        ResourceLocation testId = ResourceLocation.fromNamespaceAndPath(MOD_ID, "test");
        System.out.println("[CITFabric] Loaded — id: " + testId);
    }
}
