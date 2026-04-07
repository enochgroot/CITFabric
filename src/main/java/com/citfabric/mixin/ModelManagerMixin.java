package com.citfabric.mixin;

import com.citfabric.cit.CITManager;
import com.citfabric.cit.CITRule;
import com.citfabric.model.CITConditionalModel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.*;

// After models are baked and applied, wrap item models that have CIT rules
// with CITConditionalModel.
@Mixin(ModelManager.class)
public class ModelManagerMixin {

    // Access the baked item models map via @Shadow
    // Confirmed field name from 1.21.11 mappings: bakedItemStackModels
    @Shadow private Map<Identifier, ItemModel> bakedItemStackModels;

    @Inject(method = "apply", at = @At("TAIL"))
    private void citfabric$wrapCITModels(CallbackInfo ci) {
        if (!CITManager.INSTANCE.hasRules() || bakedItemStackModels == null) return;

        // Find all item types that have CIT rules
        Map<Identifier, List<CITRule>> rulesByItem = new LinkedHashMap<>();
        for (CITRule rule : CITManager.INSTANCE.getAllRules()) {
            if (rule.matchItems.isEmpty()) continue; // skip wildcard rules for now
            for (Identifier itemId : rule.matchItems) {
                rulesByItem.computeIfAbsent(itemId, k -> new ArrayList<>()).add(rule);
            }
        }

        // Try to find and cache CIT sub-models, then wrap originals
        for (Map.Entry<Identifier, List<CITRule>> entry : rulesByItem.entrySet()) {
            Identifier itemId = entry.getKey();
            List<CITRule> rules = entry.getValue();

            // Look up CIT sub-models by their registered paths
            for (CITRule rule : rules) {
                if (rule.bakedModel == null) {
                    // Try to get from model manager — the model might have been registered
                    // via the atlas source injection + Fabric ModelLoadingPlugin
                    try {
                        Identifier citModelId = citModelId(rule);
                        ItemModel model = bakedItemStackModels.get(citModelId);
                        if (model != null) {
                            rule.bakedModel = model;
                            System.out.println("[CITFabric] Cached model for: " + rule.debugName);
                        }
                    } catch (Exception e) {
                        // Model not registered yet — will be picked up on next reload
                    }
                }
            }

            // Wrap the original item model
            ItemModel original = bakedItemStackModels.get(itemId);
            if (original != null && !(original instanceof CITConditionalModel)) {
                bakedItemStackModels.put(itemId, new CITConditionalModel(original, rules));
                System.out.println("[CITFabric] Wrapped model for item: " + itemId);
            }
        }
    }

    private static Identifier citModelId(CITRule rule) {
        String id = rule.debugName.replaceAll("[^a-z0-9_/.-]","_").toLowerCase()
            .replaceAll("__+","_").replaceAll("^[/_]+|[/_]+$","");
        return Identifier.fromNamespaceAndPath("citfabric","cit/"+id);
    }
}
