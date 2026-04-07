package com.citfabric.cit;

import com.citfabric.model.CITItemModel;
import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class CITManager implements SimpleSynchronousResourceReloadListener {

    public static final CITManager INSTANCE = new CITManager();
    private static final Identifier ID = Identifier.fromNamespaceAndPath("citfabric","cit_loader");
    private final List<CITRule> rules = new CopyOnWriteArrayList<>();

    private CITManager() {}

    @Override public Identifier getFabricId() { return ID; }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        Minecraft mc = Minecraft.getInstance();
        for (CITRule rule : rules) {
            if (rule.bakedModel instanceof CITItemModel m) {
                try { mc.getTextureManager().release(m.getTextureId()); } catch (Exception ignored) {}
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
                    if (rule != null && loadModel(rule, manager, mc)) {
                        rules.add(rule);
                        count++;
                        System.out.println("[CITFabric] Loaded: " + rule.debugName);
                    }
                } catch (Exception ex) {
                    System.err.println("[CITFabric] Error " + e.getKey() + ": " + ex.getMessage());
                }
            }
        }
        System.out.println("[CITFabric] Loaded " + count + " CIT rules");
    }

    private boolean loadModel(CITRule rule, ResourceManager manager, Minecraft mc) {
        if (rule.texturePath == null) return false;
        try {
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
            String texPath = rule.texturePath.getPath()
                .replaceAll("[^a-z0-9_./]","_").toLowerCase();
            Identifier textureKey = Identifier.fromNamespaceAndPath("citfabric","cit/"+texPath);
            // DynamicTexture in 1.21.11: (Supplier<String> debugLabel, NativeImage pixels)
            mc.getTextureManager().register(textureKey,
                new DynamicTexture(() -> "citfabric_cit", img));
            rule.bakedModel = new CITItemModel(textureKey);
            return true;
        } catch (Exception e) {
            System.err.println("[CITFabric] Texture load failed for "+rule.debugName+": "+e.getMessage());
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
