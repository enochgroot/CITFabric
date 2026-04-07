package com.citfabric.pack;

import com.citfabric.cit.CITManager;
import com.citfabric.cit.CITRule;
import com.citfabric.model.CITModelRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Set;

// Virtual resource pack that provides generated item model JSONs for CIT rules.
public class CITVirtualPack extends AbstractPackResources {

    public static final CITVirtualPack INSTANCE = new CITVirtualPack();

    private CITVirtualPack() {
        super("citfabric_virtual", true);
    }

    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String... paths) { return null; }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(PackType type, ResourceLocation id) {
        if (type != PackType.CLIENT_RESOURCES) return null;
        if (!id.getNamespace().equals("citfabric")) return null;
        String path = id.getPath();

        // models/item/cit/<rule_id>.json
        if (path.startsWith("models/item/cit/") && path.endsWith(".json")) {
            String ruleId = path.substring("models/item/cit/".length(), path.length() - 5);
            CITRule rule = findRuleById(ruleId);
            if (rule != null) {
                byte[] bytes = generateModelJson(rule).getBytes(StandardCharsets.UTF_8);
                return () -> new ByteArrayInputStream(bytes);
            }
        }
        return null;
    }

    @Override
    public void listResources(PackType type, String namespace, String prefix, ResourceOutput output) {
        if (type != PackType.CLIENT_RESOURCES || !namespace.equals("citfabric")) return;
        if (!prefix.isEmpty() && !prefix.startsWith("models")) return;
        for (CITRule rule : CITManager.INSTANCE.getAllRules()) {
            ResourceLocation loc = CITModelRegistry.getCITItemModelId(rule);
            String modelPath = "models/item/" + loc.getPath() + ".json";
            ResourceLocation modelLoc = ResourceLocation.fromNamespaceAndPath("citfabric", modelPath);
            output.accept(modelLoc, () -> getResource(type, modelLoc));
        }
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return type == PackType.CLIENT_RESOURCES ? Set.of("citfabric") : Set.of();
    }

    @Override
    public <T> @Nullable T getMetadataSection(MetadataSectionType<T> deserializer) { return null; }

    @Override
    public String packId() { return "citfabric_virtual"; }

    @Override
    public void close() {}

    private CITRule findRuleById(String ruleId) {
        for (CITRule rule : CITManager.INSTANCE.getAllRules()) {
            ResourceLocation loc = CITModelRegistry.getCITItemModelId(rule);
            if (loc.getPath().equals("cit/" + ruleId)) return rule;
        }
        return null;
    }

    private String generateModelJson(CITRule rule) {
        String texRef;
        if (rule.texturePath != null) {
            String texPath = rule.texturePath.getPath();
            if (texPath.startsWith("textures/")) texPath = texPath.substring("textures/".length());
            if (texPath.endsWith(".png")) texPath = texPath.substring(0, texPath.length() - 4);
            texRef = rule.texturePath.getNamespace() + ":" + texPath;
        } else {
            texRef = "minecraft:item/barrier";
        }
        // Build JSON string carefully to avoid escaping issues
        return "{" +
            "\"parent\":\"minecraft:item/generated\"," +
            "\"textures\":{\"layer0\":\"" + texRef + "\"}" +
            "}";
    }
}
