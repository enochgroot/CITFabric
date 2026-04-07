package com.citfabric.mixin;

import com.citfabric.cit.CITManager;
import com.citfabric.cit.CITRule;
import com.citfabric.model.CITModelRegistry;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepts ItemModelResolver.appendItemLayers() to apply CIT overrides.
 * This is called every frame for every visible item — keep it fast.
 *
 * Confirmed in 1.21.11 mappings:
 *   void appendItemLayers(ItemStackRenderState, ItemStack, ItemDisplayContext, int, Level)
 */
@Mixin(targets = "net.minecraft.client.renderer.item.ItemModelResolver")
public class ItemModelResolverMixin {

    @Inject(method = "appendItemLayers",
            at = @At("HEAD"),
            cancellable = true)
    private void citfabric$applyCIT(
            ItemStackRenderState renderState,
            ItemStack stack,
            ItemDisplayContext displayContext,
            int seed,
            Level level,
            CallbackInfo ci) {

        if (!CITManager.INSTANCE.hasRules()) return;

        CITRule rule = CITManager.INSTANCE.findMatch(stack);
        if (rule == null) return;

        // Get the baked ItemModel for this rule
        ItemModel citModel = CITModelRegistry.getModelForRule(rule);
        if (citModel == null) return; // model not yet baked, fall back to vanilla

        // Clear any existing state and apply CIT model
        renderState.clear();
        citModel.update(renderState, stack, null, displayContext, seed, level);
        ci.cancel();
    }
}
