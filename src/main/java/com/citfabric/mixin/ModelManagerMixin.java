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

// Fallback: wrap item models in CITConditionalModel after baking
@Mixin(value = ModelManager.class, priority = 900)
public class ModelManagerMixin {

    @Shadow private Map<Identifier, ItemModel> bakedItemStackModels;

    @Inject(method = "apply", at = @At("TAIL"), require = 0)
    private void citfabric$wrapCITModels(CallbackInfo ci) {
        try {
            if (!CITManager.INSTANCE.hasRules()) {
                System.out.println("[CITFabric] ModelManagerMixin.apply: no rules yet");
                return;
            }
            if (bakedItemStackModels == null) {
                System.err.println("[CITFabric] ModelManagerMixin.apply: bakedItemStackModels is null!");
                return;
            }
            Map<Identifier, List<CITRule>> byItem = new LinkedHashMap<>();
            for (CITRule rule : CITManager.INSTANCE.getAllRules()) {
                if (rule.bakedModel == null) continue;
                for (Identifier id : rule.matchItems) {
                    byItem.computeIfAbsent(id, k -> new ArrayList<>()).add(rule);
                }
            }
            int wrapped = 0;
            for (Map.Entry<Identifier, List<CITRule>> e : byItem.entrySet()) {
                ItemModel orig = bakedItemStackModels.get(e.getKey());
                if (orig != null && !(orig instanceof CITConditionalModel)) {
                    bakedItemStackModels.put(e.getKey(), new CITConditionalModel(orig, e.getValue()));
                    wrapped++;
                } else if (orig == null) {
                    System.err.println("[CITFabric] Item model not found for: " + e.getKey());
                }
            }
            System.out.println("[CITFabric] ModelManagerMixin.apply: wrapped " + wrapped + " item models");
        } catch (Exception ex) {
            System.err.println("[CITFabric] ModelManagerMixin error: " + ex.getMessage());
        }
    }
}
