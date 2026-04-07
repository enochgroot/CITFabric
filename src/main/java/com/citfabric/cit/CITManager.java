package com.citfabric.cit;

import com.citfabric.model.CITConditionalModel;
import com.citfabric.model.CITItemModel;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

// Loads CIT rules from resource packs and creates ItemModel objects for each rule.
// Scans: optifine/cit, citresewn/cit, mcpatcher/cit directories.
public class CITManager implements SimpleSynchronousResourceReloadListener {

    public static final CITManager INSTANCE = new CITManager();
    private static final Identifier ID = Identifier.fromNamespaceAndPath("citfabric","cit_loader");
    private final List<CITRule> rules = new CopyOnWriteArrayList<>();

    private CITManager() {}

    @Override public Identifier getFabricId() { return ID; }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        // Release old textures
        Minecraft mc = Minecraft.getInstance();
        for (CITRule rule : rules) {
            if (rule.bakedModel instanceof CITItemModel) {
                Identifier tid = ((CITItemModel)rule.bakedModel).getTextureId();
                try { mc.getTextureManager().release(tid); } catch (Exception ignored) {}
            }
        }
        rules.clear();

        int count = 0;
        for (String prefix : new String[]{"optifine/cit","citresewn/cit","mcpatcher/cit"}) {
            Map<Identifier, net.minecraft.server.packs.resources.Resource> found =
                manager.listResources(prefix, id -> id.getPath().endsWith(".properties"));
            for (Map.Entry<Identifier, net.minecraft.server.packs.resources.Resource> e : found.entrySet()) {
                try (InputStream is = e.getValue().open()) {
                    String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    CITRule rule = CITProperties.parse(content, e.getKey());
                    if (rule != null) {
                        // Load the texture and create an ItemModel immediately
                        if (loadModel(rule, manager, mc)) {
                            rules.add(rule);
                            count++;
                            System.out.println("[CITFabric] Loaded: " + rule.debugName);
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("[CITFabric] Error " + e.getKey() + ": " + ex.getMessage());
                }
            }
        }
        System.out.println("[CITFabric] Loaded " + count + " CIT rules with models");
    }

    private boolean loadModel(CITRule rule, ResourceManager manager, Minecraft mc) {
        if (rule.texturePath == null) return false;
        try {
            // Texture is at assets/<ns>/optifine/cit/.../<name>.png
            // ResourceManager looks up assets/<ns>/<path>
            Identifier resourceId = Identifier.fromNamespaceAndPath(
                rule.texturePath.getNamespace(),
                rule.texturePath.getPath() + ".png"
            );
            var res = manager.getResource(resourceId);
            if (res.isEmpty()) {
                System.err.println("[CITFabric] Texture not found: " + resourceId);
                return false;
            }
            NativeImage img;
            try (InputStream is = res.get().open()) {
                img = NativeImage.read(is);
            }
            // Register as DynamicTexture
            String texId = rule.texturePath.getPath()
                .replaceAll("[^a-z0-9_./]", "_").toLowerCase();
            Identifier textureKey = Identifier.fromNamespaceAndPath("citfabric", "cit/" + texId);
            mc.getTextureManager().register(textureKey, new DynamicTexture(img));
            // Create ItemModel
            rule.bakedModel = new CITItemModel(textureKey);
            return true;
        } catch (Exception e) {
            System.err.println("[CITFabric] Model load failed for " + rule.debugName + ": " + e.getMessage());
            return false;
        }
    }

    public CITRule findMatch(net.minecraft.world.item.ItemStack stack) {
        for (CITRule rule : rules) { if (rule.matches(stack)) return rule; }
        return null;
    }
    public List<CITRule> getAllRules() { return Collections.unmodifiableList(rules); }
    public boolean hasRules() { return !rules.isEmpty(); }
}
