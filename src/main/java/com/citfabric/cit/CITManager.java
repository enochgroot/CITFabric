package com.citfabric.cit;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

// Loads and manages all CIT rules from resource packs.
// Scans optifine/cit, citresewn/cit, mcpatcher/cit directories.
public class CITManager implements SimpleSynchronousResourceReloadListener {

    public static final CITManager INSTANCE = new CITManager();
    private static final Identifier ID = Identifier.fromNamespaceAndPath("citfabric","cit_loader");
    private final List<CITRule> rules = new CopyOnWriteArrayList<>();

    private CITManager() {}

    @Override public Identifier getFabricId() { return ID; }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        rules.clear();
        int count = 0;
        for (String prefix : new String[]{"optifine/cit","citresewn/cit","mcpatcher/cit"}) {
            Map<Identifier, net.minecraft.server.packs.resources.Resource> found =
                manager.listResources(prefix, id -> id.getPath().endsWith(".properties"));
            for (Map.Entry<Identifier, net.minecraft.server.packs.resources.Resource> e : found.entrySet()) {
                try (InputStream is = e.getValue().open()) {
                    String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    CITRule rule = CITProperties.parse(content, e.getKey());
                    if (rule != null) { rules.add(rule); count++; System.out.println("[CITFabric] Loaded: "+rule.debugName); }
                } catch (Exception ex) {
                    System.err.println("[CITFabric] Error "+e.getKey()+": "+ex.getMessage());
                }
            }
        }
        System.out.println("[CITFabric] Loaded "+count+" CIT rules");
    }

    public CITRule findMatch(net.minecraft.world.item.ItemStack stack) {
        for (CITRule rule : rules) { if (rule.matches(stack)) return rule; }
        return null;
    }

    public List<CITRule> getAllRules() { return Collections.unmodifiableList(rules); }
    public boolean hasRules() { return !rules.isEmpty(); }
}
