package com.citfabric;
import net.fabricmc.api.ClientModInitializer;
// Test 1: common classes VaultSniper uses
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
// Test 2: package we need
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.enchantment.ItemEnchantments;
// Test 3: ResourceLocation — try alternate paths
// import net.minecraft.resources.ResourceLocation;        // standard path
// import net.minecraft.ResourceLocation;                  // shorter path?
import net.minecraft.resources.ResourceKey;               // related class
public class CITFabric implements ClientModInitializer {
    public static final String MOD_ID = "citfabric";
    @Override
    public void onInitializeClient() {
        BlockPos p = new BlockPos(0,64,0);
        System.out.println("[CITFabric] BlockPos: " + p);
    }
}
