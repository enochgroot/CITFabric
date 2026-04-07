package com.citfabric.pack;

import com.citfabric.cit.CITManager;
import com.citfabric.cit.CITRule;
import com.citfabric.model.CITModelRegistry;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

/**
 * A virtual resource pack that generates item model JSON files for CIT rules on-the-fly.
 *
 * For each CIT rule, we generate:
 *   assets/citfabric/items/cit/<rule_id>.json  — item definition pointing to model
 *   assets/citfabric/models/item/cit/<rule_id>.json — generated item model
 *
 * These are plain "minecraft:item/generated" style models with the CIT texture.
 * For items with CIT rules, we also generate overriding mace.json etc. but those
 * are handled at runtime via the ItemModelResolverMixin — the virtual pack just
 * provides the CIT sub-models.
 */
public class CITVirtualPack extends AbstractPackResources {

    public static final CITVirtualPack INSTANCE = new CITVirtualPack();

    private CITVirtualPack() {
        super("citfabric_virtual", true); // isBuiltin=true means higher priority
    }

    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String... paths) {
        return null;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(PackType type, ResourceLocation id) {
        if (type != PackType.CLIENT_RESOURCES) return null;
        String path = id.getPath();
        String ns = id.getNamespace();
        if (!ns.equals("citfabric")) return null;

        // models/item/cit/<rule_id>.json — generated model JSON
        if (path.startsWith("models/item/cit/") && path.endsWith(".json")) {
            String ruleId = path.substring("models/item/cit/".length(), path.length() - 5);
            CITRule rule = findRuleById(ruleId);
            if (rule != null) {
                String json = generateModelJson(rule);
                byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
                return () -> new ByteArrayInputStream(bytes);
            }
        }

        return null;
    }

    @Override
    public void listResources(PackType type, String namespace, String prefix,
                              ResourceOutput output) {
        if (type != PackType.CLIENT_RESOURCES) return;
        if (!namespace.equals("citfabric")) return;

        // Expose all CIT model JSONs
        if (prefix.equals("models/item/cit") || prefix.isEmpty() || prefix.startsWith("models")) {
            for (CITRule rule : CITManager.INSTANCE.getAllRules()) {
                ResourceLocation loc = CITModelRegistry.getCITItemModelId(rule);
                // Convert item model ID to model path
                // citfabric:cit/<id> → models/item/cit/<id>.json
                String modelPath = "models/item/" + loc.getPath() + ".json";
                ResourceLocation modelLoc = ResourceLocation.fromNamespaceAndPath("citfabric", modelPath);
                output.accept(modelLoc, () -> getResource(type, modelLoc));
            }
        }
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        if (type == PackType.CLIENT_RESOURCES) return Set.of("citfabric");
        return Set.of();
    }

    @Override
    public <T> @Nullable T getMetadataSection(MetadataSectionType<T> deserializer) {
        return null;
    }

    @Override
    public String packId() { return "citfabric_virtual"; }

    @Override
    public void close() {}

    private CITRule findRuleById(String ruleId) {
        for (CITRule rule : CITManager.INSTANCE.getAllRules()) {
            ResourceLocation loc = CITModelRegistry.getCITItemModelId(rule);
            // loc.getPath() = "cit/<rule_id>"
            if (loc.getPath().equals("cit/" + ruleId)) return rule;
        }
        return null;
    }

    /**
     * Generate item model JSON for a CIT rule.
     * Uses minecraft:item/generated parent with layer0 = CIT texture.
     */
    private String generateModelJson(CITRule rule) {
        // Texture path in standard item texture format
        // CIT texture is at e.g. "assets/minecraft/optifine/cit/stormbreaker/stormbreaker.png"
        // We reference it as "minecraft:optifine/cit/stormbreaker/stormbreaker"
        String texRef;
        if (rule.texturePath != null) {
            String texPath = rule.texturePath.getPath();
            // Remove "textures/" prefix if present, remove extension
            if (texPath.startsWith("textures/")) texPath = texPath.substring("textures/".length());
            if (texPath.endsWith(".png")) texPath = texPath.substring(0, texPath.length() - 4);
            texRef = rule.texturePath.getNamespace() + ":" + texPath;
        } else {
            texRef = "minecraft:item/barrier"; // fallback
        }

        return "{\"parent\":\"minecraft:item/generated\",\"textures\":{\"layer0\":\"" + texRef + "\"}}";
    }
}
