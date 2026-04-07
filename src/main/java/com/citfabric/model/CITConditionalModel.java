package com.citfabric.model;

import com.citfabric.cit.CITManager;
import com.citfabric.cit.CITRule;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import java.util.List;

// Wraps the original item model and overrides with CIT model when conditions match.
// ItemModel.update signature confirmed from 1.21.11 mappings (BlockModelWrapper.update):
//   void update(ItemStackRenderState, ItemStack, ItemModelResolver, ItemDisplayContext, int, Level)
public class CITConditionalModel implements ItemModel {

    private final ItemModel original;
    private final List<CITRule> rules;

    public CITConditionalModel(ItemModel original, List<CITRule> rules) {
        this.original = original;
        this.rules = rules;
    }

    @Override
    public void update(ItemStackRenderState state, ItemStack stack,
                       ItemModelResolver resolver, ItemDisplayContext context,
                       int seed, Level level) {
        for (CITRule rule : rules) {
            if (rule.matches(stack) && rule.bakedModel != null) {
                rule.bakedModel.update(state, stack, resolver, context, seed, level);
                return;
            }
        }
        // No CIT match — use original model
        if (original != null) original.update(state, stack, resolver, context, seed, level);
    }
}
