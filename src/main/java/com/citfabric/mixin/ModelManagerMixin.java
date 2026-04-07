package com.citfabric.mixin;

import com.citfabric.cit.CITManager;
import com.citfabric.cit.CITRule;
import com.citfabric.model.CITModelRegistry;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * After models are applied (baked + stored in ModelManager),
 * resolve CIT item models from the model manager and store them.
 *
 * Confirmed in 1.21.11 mappings:
 *   void apply(ModelManager$ReloadState)
 */
@Mixin(ModelManager.class)
public class ModelManagerMixin {

    @Inject(method = "apply", at = @At("TAIL"))
    private void citfabric$cacheCITModels(CallbackInfo ci) {
        CITModelRegistry.INSTANCE.clear();
        ModelManager self = (ModelManager)(Object)this;

        int resolved = 0;
        for (CITRule rule : CITManager.INSTANCE.getAllRules()) {
            ResourceLocation modelId = CITModelRegistry.getCITItemModelId(rule);
            try {
                ItemModel model = self.getItemModel(modelId);
                if (model != null) {
                    CITModelRegistry.INSTANCE.register(rule, model);
                    resolved++;
                    System.out.println("[CITFabric] Resolved model for: " + rule.debugName);
                } else {
                    System.err.println("[CITFabric] Model not found for: " + rule.debugName
                        + " (id=" + modelId + ")");
                }
            } catch (Exception e) {
                System.err.println("[CITFabric] Error resolving model for " + rule.debugName
                    + ": " + e.getMessage());
            }
        }
        System.out.println("[CITFabric] Resolved " + resolved + "/" +
            CITManager.INSTANCE.getAllRules().size() + " CIT models");
    }
}
