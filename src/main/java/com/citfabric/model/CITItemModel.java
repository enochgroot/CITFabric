package com.citfabric.model;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import java.util.List;

public class CITItemModel implements ItemModel {

    private final Identifier textureId;
    private final List<BakedQuad> quads;

    public CITItemModel(Identifier textureId, int texWidth, int texHeight) {
        this.textureId = textureId;
        this.quads = buildQuads();
    }

    public Identifier getTextureId() { return textureId; }

    private static long packUV(float u, float v) {
        return (long) Float.floatToRawIntBits(u)
            | ((long) Float.floatToRawIntBits(v) << 32);
    }

    private List<BakedQuad> buildQuads() {
        BakedQuad front = new BakedQuad(
            new Vector3f(0f, 0f, 0.006f),
            new Vector3f(1f, 0f, 0.006f),
            new Vector3f(1f, 1f, 0.006f),
            new Vector3f(0f, 1f, 0.006f),
            packUV(0f, 1f), packUV(1f, 1f), packUV(1f, 0f), packUV(0f, 0f),
            -1, Direction.SOUTH, null, false, 0
        );
        BakedQuad back = new BakedQuad(
            new Vector3f(1f, 0f, -0.006f),
            new Vector3f(0f, 0f, -0.006f),
            new Vector3f(0f, 1f, -0.006f),
            new Vector3f(1f, 1f, -0.006f),
            packUV(1f, 1f), packUV(0f, 1f), packUV(0f, 0f), packUV(1f, 0f),
            -1, Direction.NORTH, null, false, 0
        );
        return List.of(front, back);
    }

    @Override
    public void update(ItemStackRenderState state, ItemStack stack,
                       ItemModelResolver resolver, ItemDisplayContext context,
                       ClientLevel level, ItemOwner owner, int seed) {
        ItemStackRenderState.LayerRenderState layer = state.newLayer();
        layer.setRenderType(RenderTypes.itemEntityTranslucentCull(textureId));
        layer.setUsesBlockLight(false);
        layer.prepareQuadList().addAll(quads);
    }
}
