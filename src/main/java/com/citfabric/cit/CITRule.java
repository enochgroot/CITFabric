package com.citfabric.cit;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import java.util.*;

public class CITRule {
    public final Set<Identifier> matchItems;
    public final Set<Identifier> matchEnchants;
    public final String matchName;   // null = any name
    public final Identifier texturePath;
    public final String debugName;
    public volatile net.minecraft.client.renderer.item.ItemModel bakedModel = null;

    public CITRule(Set<Identifier> items, Set<Identifier> enchants, String name,
                   Identifier texture, String debugName) {
        this.matchItems    = Collections.unmodifiableSet(items);
        this.matchEnchants = Collections.unmodifiableSet(enchants);
        this.matchName     = name;
        this.texturePath   = texture;
        this.debugName     = debugName;
    }

    public boolean matches(ItemStack stack) {
        if (stack.isEmpty()) return false;

        // Check item type
        if (!matchItems.isEmpty()) {
            Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (itemId == null || !matchItems.contains(itemId)) return false;
        }

        // Check enchantments
        if (!matchEnchants.isEmpty()) {
            ItemEnchantments enchantments = stack.getEnchantments();
            if (enchantments.isEmpty()) return false;
            boolean hasAll = matchEnchants.stream().allMatch(enchId ->
                enchantments.keySet().stream().anyMatch(holder ->
                    holder.unwrapKey().map(k -> k.identifier().equals(enchId)).orElse(false)));
            if (!hasAll) return false;
        }

        // Check name (custom name set by anvil)
        if (matchName != null && !matchName.isEmpty()) {
            // Try custom name first (anvil rename)
            net.minecraft.network.chat.Component customName = stack.getCustomName();
            String itemName = customName != null ? customName.getString()
                                                 : stack.getHoverName().getString();
            // Case-insensitive, allow partial match
            if (!itemName.trim().equalsIgnoreCase(matchName.trim())
                    && !itemName.contains(matchName)) {
                return false;
            }
        }

        return true;
    }

    @Override public String toString() { return "CITRule[" + debugName + "]"; }
}
