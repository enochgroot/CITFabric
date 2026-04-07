package com.citfabric.model;

import com.citfabric.cit.CITManager;
import com.citfabric.cit.CITRule;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import java.util.List;

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
                       ClientLevel level, ItemOwner owner, int seed) {
        for (CITRule rule : rules) {
            if (rule.matches(stack) && rule.bakedModel != null) {
                rule.bakedModel.update(state, stack, resolver, context, level, owner, seed);
                return;
            }
        }
        if (original != null) original.update(state, stack, resolver, context, level, owner, seed);
    }
}
