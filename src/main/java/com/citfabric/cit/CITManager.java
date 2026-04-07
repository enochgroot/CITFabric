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
    private static final String[] SCAN_PATHS = {"optifine/cit", "citresewn/cit", "mcpatcher/cit"};

    private CITManager() {}

    @Override
    public Identifier getFabricId() { return ID; }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        Minecraft mc = Minecraft.getInstance();
        // Release old textures
        for (CITRule rule : rules) {
            if (rule.bakedModel instanceof CITItemModel m) {
                try { mc.getTextureManager().release(m.getTextureId()); }
                catch (Exception ignored) {}
            }
        }
        rules.clear();
        System.out.println("[CITFabric] Resource reload started");

        int ruleCount = 0;
        for (String prefix : SCAN_PATHS) {
            Map<Identifier, net.minecraft.server.packs.resources.Resource> found =
                manager.listResources(prefix, id -> id.getPath().endsWith(".properties"));
            System.out.println("[CITFabric] Scanning " + prefix + ": found " + found.size() + " .properties files");
            for (Map.Entry<Identifier, net.minecraft.server.packs.resources.Resource> e : found.entrySet()) {
                try (InputStream is = e.getValue().open()) {
                    String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    CITRule rule = CITProperties.parse(content, e.getKey());
                    if (rule != null && loadTexture(rule, manager, mc)) {
                        rules.add(rule);
                        ruleCount++;
                        System.out.println("[CITFabric] Loaded rule: " + rule.debugName);
                    }
                } catch (Exception ex) {
                    System.err.println("[CITFabric] Error loading " + e.getKey() + ": " + ex);
                    ex.printStackTrace();
                }
            }
        }
        System.out.println("[CITFabric] Reload complete: " + ruleCount + " rules loaded");
        if (ruleCount == 0) {
            System.out.println("[CITFabric] No rules found! Check resource pack is active.");
            System.out.println("[CITFabric] Resource packs loaded: " + manager.listPacks().count());
        }
    }

    private boolean loadTexture(CITRule rule, ResourceManager manager, Minecraft mc) {
        if (rule.texturePath == null) return false;
        Identifier resourceId = Identifier.fromNamespaceAndPath(
            rule.texturePath.getNamespace(),
            rule.texturePath.getPath() + ".png"
        );
        System.out.println("[CITFabric] Loading texture: " + resourceId);
        var res = manager.getResource(resourceId);
        if (res.isEmpty()) {
            System.err.println("[CITFabric] Texture NOT FOUND: " + resourceId);
            return false;
        }
        try {
            NativeImage img;
            try (InputStream is = res.get().open()) {
                img = NativeImage.read(is);
            }
            System.out.println("[CITFabric] Texture loaded: " + img.getWidth() + "x" + img.getHeight());
            String safeName = rule.texturePath.getPath().replaceAll("[^a-z0-9_./]","_").toLowerCase();
            Identifier textureKey = Identifier.fromNamespaceAndPath("citfabric", "cit/" + safeName);
            DynamicTexture dynTex = new DynamicTexture(() -> "citfabric_" + safeName, img);
            mc.getTextureManager().register(textureKey, dynTex);
            // Force GPU upload immediately
            dynTex.upload();
            rule.bakedModel = new CITItemModel(textureKey, img.getWidth(), img.getHeight());
            System.out.println("[CITFabric] Texture registered: " + textureKey);
            return true;
        } catch (Exception e) {
            System.err.println("[CITFabric] Texture load failed for " + rule.debugName + ": " + e);
            e.printStackTrace();
            return false;
        }
    }

    public CITRule findMatch(net.minecraft.world.item.ItemStack stack) {
        for (CITRule rule : rules) {
            if (rule.matches(stack)) return rule;
        }
        return null;
    }

    public List<CITRule> getAllRules() { return Collections.unmodifiableList(rules); }
    public boolean hasRules() { return !rules.isEmpty(); }
    public int getRuleCount() { return rules.size(); }
}
