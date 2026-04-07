package com.citfabric.model;

import com.citfabric.cit.CITManager;
import com.citfabric.cit.CITRule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.resources.ResourceLocation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages ItemModel objects for CIT rules.
 * After model baking, CIT models are registered here indexed by rule.
 */
public class CITModelRegistry {

    public static final CITModelRegistry INSTANCE = new CITModelRegistry();
    private final Map<CITRule, ItemModel> models = new ConcurrentHashMap<>();

    private CITModelRegistry() {}

    public void register(CITRule rule, ItemModel model) {
        models.put(rule, model);
    }

    public ItemModel get(CITRule rule) {
        return models.get(rule);
    }

    public void clear() {
        models.clear();
    }

    /**
     * Get the ItemModel for a given CIT rule by looking it up from the ModelManager.
     * The model was registered via a virtual resource pack during model loading.
     * Returns null if not yet baked.
     */
    public static ItemModel getModelForRule(CITRule rule) {
        if (rule == null) return null;
        // First check our registry cache
        ItemModel cached = INSTANCE.get(rule);
        if (cached != null) return cached;
        // Try to get from ModelManager using the CIT item model ID
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return null;
        ResourceLocation citModelId = getCITItemModelId(rule);
        try {
            ItemModel model = mc.getModelManager().getItemModel(citModelId);
            if (model != null) {
                INSTANCE.register(rule, model);
                return model;
            }
        } catch (Exception e) {
            // Model not yet loaded
        }
        return null;
    }

    /**
     * Get the ResourceLocation for the virtual item model we register for this rule.
     * Format: citfabric:cit/<hash_of_debug_name>
     */
    public static ResourceLocation getCITItemModelId(CITRule rule) {
        String id = rule.debugName
            .replaceAll("[^a-z0-9_/.-]", "_")
            .toLowerCase()
            .replaceAll("__+", "_");
        // Strip leading/trailing slashes and underscores
        id = id.replaceAll("^[/_]+|[/_]+$", "");
        return ResourceLocation.fromNamespaceAndPath("citfabric", "cit/" + id);
    }
}
