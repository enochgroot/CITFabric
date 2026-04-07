package com.citfabric.cit;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import java.util.*;

/**
 * One CIT override rule parsed from a .properties file.
 * Supported conditions: type=item, items=..., enchantments=..., nbt.display.Name=...
 */
public class CITRule {
    // Conditions
    public final Set<ResourceLocation> matchItems;      // items= (empty = any)
    public final Set<ResourceLocation> matchEnchants;   // enchantments= (empty = any)
    public final String matchName;                      // name= (null = any)
    // Override
    public final ResourceLocation texturePath;          // full path to texture PNG
    public final ResourceLocation modelPath;            // optional: full path to model
    // Display name for debugging
    public final String debugName;
    // Loaded item model (set after baking)
    public volatile net.minecraft.client.renderer.item.ItemModel bakedModel = null;

    public CITRule(Set<ResourceLocation> items, Set<ResourceLocation> enchants,
                   String name, ResourceLocation texture, ResourceLocation model,
                   String debugName) {
        this.matchItems    = Collections.unmodifiableSet(items);
        this.matchEnchants = Collections.unmodifiableSet(enchants);
        this.matchName     = name;
        this.texturePath   = texture;
        this.modelPath     = model;
        this.debugName     = debugName;
    }

    public boolean matches(ItemStack stack) {
        if (stack.isEmpty()) return false;

        // Check item type
        if (!matchItems.isEmpty()) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (!matchItems.contains(itemId)) return false;
        }

        // Check enchantments
        if (!matchEnchants.isEmpty()) {
            var enchMap = EnchantmentHelper.getEnchantmentsForCrafting(stack);
            boolean hasAll = matchEnchants.stream().allMatch(enchId ->
                enchMap.keySet().stream().anyMatch(holder ->
                    holder.unwrapKey().map(k -> k.location().equals(enchId)).orElse(false)));
            if (!hasAll) return false;
        }

        // Check display name
        if (matchName != null && !matchName.isEmpty()) {
            if (!stack.hasCustomHoverName()) return false;
            String name = stack.getHoverName().getString();
            if (!name.contains(matchName) && !name.equalsIgnoreCase(matchName)) return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "CITRule[" + debugName + "]";
    }
}
