package com.citfabric;

import com.citfabric.cit.CITManager;
import com.citfabric.pack.CITVirtualPack;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.minecraft.server.packs.PackType;

/**
 * CITFabric — Custom Item Textures for Fabric 1.21.11
 *
 * Reads OptiFine/CIT Resewn .properties files from resource packs and applies
 * custom textures to items based on enchantments, names, and item types.
 *
 * Supported .properties keys:
 *   type=item
 *   items=minecraft:mace
 *   enchantments=minecraft:breach
 *   name=<display name substring>
 *   texture=<texture name>
 *   model=<model path>
 */
public class CITFabricMod implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Register CIT rule loader as a resource reload listener
        // Phase: after base resource loading, before model baking
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES)
            .registerReloadListener(CITManager.INSTANCE);

        System.out.println("[CITFabric] v1.0.0 loaded");
    }
}
