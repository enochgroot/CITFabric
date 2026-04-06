package com.citfabric;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.core.BlockPos;
public class CITFabric implements ClientModInitializer {
    public static final String MOD_ID = "citfabric";
    @Override
    public void onInitializeClient() {
        BlockPos testPos = new BlockPos(0, 64, 0);
        System.out.println("[CITFabric] Loaded — test pos: " + testPos);
    }
}
