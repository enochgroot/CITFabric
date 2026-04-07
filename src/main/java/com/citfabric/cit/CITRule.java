package com.citfabric.cit;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import java.util.*;

public class CITRule {
    public final Set<Identifier> matchItems;
    public final Set<Identifier> matchEnchants;
    public final String matchName;
    public final Identifier texturePath;
    public final Identifier modelPath;
    public final String debugName;

    // Set after model baking
    public volatile net.minecraft.client.renderer.item.ItemModel bakedModel = null;

    public CITRule(Set<Identifier> items, Set<Identifier> enchants,
                   String name, Identifier texture, Identifier model, String debugName) {
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
            Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (!matchItems.contains(itemId)) return false;
        }

        // Check enchantments using ItemStack.getEnchantments() (1.21.11 API)
        if (!matchEnchants.isEmpty()) {
            ItemEnchantments enchantments = stack.getEnchantments();
            if (enchantments.isEmpty()) return false;
            boolean hasAll = matchEnchants.stream().allMatch(enchId ->
                enchantments.keySet().stream().anyMatch(holder ->
                    holder.unwrapKey()
                        .map(k -> k.location().equals(enchId))
                        .orElse(false)));
            if (!hasAll) return false;
        }

        // Check display name (getCustomName() returns null if no custom name in 1.21.11)
        if (matchName != null && !matchName.isEmpty()) {
            net.minecraft.network.chat.Component customName = stack.getCustomName();
            if (customName == null) return false;
            String name = customName.getString();
            if (!name.contains(matchName) && !name.equalsIgnoreCase(matchName)) return false;
        }

        return true;
    }

    @Override public String toString() { return "CITRule[" + debugName + "]"; }
}
