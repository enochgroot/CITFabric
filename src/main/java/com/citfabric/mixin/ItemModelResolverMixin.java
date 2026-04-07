package com.citfabric.mixin;

import com.citfabric.cit.CITManager;
import com.citfabric.cit.CITRule;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Fallback CIT hook: intercepts appendItemLayers and applies CIT model if matched.
// The CITConditionalModel approach via ModelManagerMixin is preferred.
@Mixin(targets = "net.minecraft.client.renderer.item.ItemModelResolver")
public class ItemModelResolverMixin {

    @Inject(method = "appendItemLayers", at = @At("HEAD"), cancellable = true)
    private void citfabric$applyCIT(
            ItemStackRenderState renderState,
            ItemStack stack,
            ItemDisplayContext displayContext,
            int seed,
            Level level,
            CallbackInfo ci) {

        if (!CITManager.INSTANCE.hasRules()) return;
        CITRule rule = CITManager.INSTANCE.findMatch(stack);
        if (rule == null || rule.bakedModel == null) return;

        renderState.clear();
        // Pass 'this' as the ItemModelResolver (we are inside the resolver)
        rule.bakedModel.update(renderState, stack,
            (ItemModelResolver)(Object)this, displayContext, seed, level);
        ci.cancel();
    }
}
