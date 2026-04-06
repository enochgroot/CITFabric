package com.citfabric;
import net.minecraft.resources.ResourceLocation;
public class CITRule {
    public final String itemId;
    public final String enchantment;
    public final ResourceLocation textureKey;
    public final boolean animated;
    public CITRule(String itemId, String enchantment, ResourceLocation textureKey, boolean animated) {
        this.itemId = itemId;
        this.enchantment = enchantment;
        this.textureKey = textureKey;
        this.animated = animated;
    }
}
