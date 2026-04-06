package com.citfabric;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.core.BlockPos;
// Try various ResourceLocation alternatives
import net.minecraft.resources.ResourceLocation;      // standard  -- FAILS
// import net.minecraft.ResourceLocation;             // root pkg?
// import com.mojang.rl.ResourceLocation;             // Mojang lib?
// How BuiltInRegistries returns keys -- test via var:
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Blocks;
public class CITFabric implements ClientModInitializer {
    public static final String MOD_ID = "citfabric";
    @Override
    public void onInitializeClient() {
        // Use the return type of getKey() to discover what ResourceLocation is
        var key = BuiltInRegistries.BLOCK.getKey(Blocks.AIR);
        System.out.println("[CITFabric] Key: " + key);
    }
}
