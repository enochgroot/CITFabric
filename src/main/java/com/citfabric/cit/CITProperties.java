package com.citfabric.cit;

import net.minecraft.resources.ResourceLocation;
import java.io.*;
import java.util.*;

/**
 * Parses OptiFine/CIT Resewn .properties files into CITRule objects.
 *
 * Supported keys:
 *   type=item          (required)
 *   items=minecraft:mace minecraft:sword  (space or comma separated)
 *   enchantments=minecraft:breach         (space separated)
 *   enchantmentIDs=minecraft:breach       (alias)
 *   name=Stormbreaker                     (substring match)
 *   texture=stormbreaker                  (texture name, resolved relative to .properties file)
 *   model=...                             (optional model path)
 */
public class CITProperties {

    public static CITRule parse(String propertiesContent,
                                 ResourceLocation propertiesLocation) {
        Properties props = new Properties();
        try {
            props.load(new StringReader(propertiesContent));
        } catch (IOException e) {
            System.err.println("[CITFabric] Failed to parse " + propertiesLocation + ": " + e.getMessage());
            return null;
        }

        String type = props.getProperty("type", "item").trim();
        if (!type.equals("item")) return null; // only support item type for now

        // Parse items=
        Set<ResourceLocation> items = parseIdSet(
            props.getProperty("items", props.getProperty("matchItems", "")));

        // Parse enchantments=
        Set<ResourceLocation> enchants = parseIdSet(
            props.getProperty("enchantments",
            props.getProperty("enchantmentIDs",
            props.getProperty("enchantmentIds", ""))));
        // Normalize enchantment IDs (strip levels like "minecraft:breach:1-127" → "minecraft:breach")
        Set<ResourceLocation> normalizedEnchants = new LinkedHashSet<>();
        for (ResourceLocation e : enchants) {
            String path = e.getPath();
            int colonIdx = path.lastIndexOf(':');
            if (colonIdx >= 0) {
                try {
                    Integer.parseInt(path.substring(colonIdx + 1));
                    path = path.substring(0, colonIdx);
                } catch (NumberFormatException ignored) {}
            }
            normalizedEnchants.add(ResourceLocation.fromNamespaceAndPath(e.getNamespace(), path));
        }

        // Parse name=
        String name = props.getProperty("name", "").trim();

        // Parse texture= (resolve relative to the .properties file)
        String textureStr = props.getProperty("texture", "").trim();
        ResourceLocation texturePath = null;
        if (!textureStr.isEmpty()) {
            texturePath = resolveRelative(propertiesLocation, textureStr, ".png");
        }

        // Parse model=
        String modelStr = props.getProperty("model", "").trim();
        ResourceLocation modelPath = null;
        if (!modelStr.isEmpty()) {
            modelPath = resolveRelative(propertiesLocation, modelStr, ".json");
        }

        if (texturePath == null && modelPath == null) {
            System.err.println("[CITFabric] No texture or model in " + propertiesLocation + ", skipping");
            return null;
        }

        return new CITRule(items, normalizedEnchants, name.isEmpty() ? null : name,
                           texturePath, modelPath,
                           propertiesLocation.getPath());
    }

    private static Set<ResourceLocation> parseIdSet(String raw) {
        Set<ResourceLocation> result = new LinkedHashSet<>();
        if (raw == null || raw.isBlank()) return result;
        String[] parts = raw.trim().split("[\s,]+");
        for (String part : parts) {
            if (part.isBlank()) continue;
            try {
                // Handle both "mace" (vanilla default ns) and "minecraft:mace"
                if (!part.contains(":")) part = "minecraft:" + part;
                result.add(ResourceLocation.parse(part));
            } catch (Exception e) {
                System.err.println("[CITFabric] Invalid ID: " + part);
            }
        }
        return result;
    }

    /**
     * Resolve a texture/model path relative to the .properties file location.
     * e.g. properties at "optifine/cit/stormbreaker/stormbreaker.properties"
     *      texture = "stormbreaker"
     *      → "optifine/cit/stormbreaker/stormbreaker"
     */
    private static ResourceLocation resolveRelative(
            ResourceLocation propertiesLocation, String ref, String extension) {
        // If ref contains a colon it's already absolute
        if (ref.contains(":")) {
            try { return ResourceLocation.parse(ref); } catch (Exception ignored) {}
        }
        // Strip extension if present
        if (ref.endsWith(".png")) ref = ref.substring(0, ref.length() - 4);
        if (ref.endsWith(".json")) ref = ref.substring(0, ref.length() - 5);

        // Get directory of the .properties file
        String propPath = propertiesLocation.getPath();
        int lastSlash = propPath.lastIndexOf('/');
        String dir = lastSlash >= 0 ? propPath.substring(0, lastSlash + 1) : "";

        String ns = propertiesLocation.getNamespace();
        String fullPath = dir + ref;
        return ResourceLocation.fromNamespaceAndPath(ns, fullPath);
    }
}
