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

// After ModelManager.apply() completes, wrap item models that have CIT rules
// with CITConditionalModel. At this point rule.bakedModel is already set
// by CITManager.onResourceManagerReload().
@Mixin(ModelManager.class)
public class ModelManagerMixin {

    @Shadow private Map<Identifier, ItemModel> bakedItemStackModels;

    @Inject(method = "apply", at = @At("TAIL"))
    private void citfabric$wrapCITModels(CallbackInfo ci) {
        if (!CITManager.INSTANCE.hasRules() || bakedItemStackModels == null) return;

        // Group rules by item type
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
            }
        }
        if (wrapped > 0)
            System.out.println("[CITFabric] Wrapped " + wrapped + " item model(s)");
    }
}
