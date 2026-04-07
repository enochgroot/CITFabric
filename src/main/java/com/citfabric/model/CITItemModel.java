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

// ItemModel that renders a flat textured quad using a DynamicTexture.
// Uses RenderTypes.itemEntityTranslucentCull so the atlas is NOT required.
// BakedQuad positions: model space 0,0,0 to 1,1,0 (standard item/generated extent).
// UV: 0,0 to 1,1 covering full DynamicTexture.
// sprite=null: BakedQuad sprite used only for sorting/particles, non-critical.
public class CITItemModel implements ItemModel {

    private final Identifier textureId;
    private final List<BakedQuad> quads;

    public CITItemModel(Identifier textureId) {
        this.textureId = textureId;
        this.quads = buildQuads();
    }

    // Pack two 32-bit floats into a 64-bit long (MC 1.21.11 BakedQuad UV format)
    private static long packUV(float u, float v) {
        return (long) Float.floatToRawIntBits(u)
            | ((long) Float.floatToRawIntBits(v) << 32);
    }

    private List<BakedQuad> buildQuads() {
        // Front face (facing +Z / SOUTH): counter-clockwise when looking at it
        BakedQuad front = new BakedQuad(
            new Vector3f(0f, 0f, 0.005f),
            new Vector3f(1f, 0f, 0.005f),
            new Vector3f(1f, 1f, 0.005f),
            new Vector3f(0f, 1f, 0.005f),
            packUV(0f, 1f), packUV(1f, 1f), packUV(1f, 0f), packUV(0f, 0f),
            -1, Direction.SOUTH, null, false, 0
        );
        // Back face (facing -Z / NORTH): reversed winding
        BakedQuad back = new BakedQuad(
            new Vector3f(1f, 0f, -0.005f),
            new Vector3f(0f, 0f, -0.005f),
            new Vector3f(0f, 1f, -0.005f),
            new Vector3f(1f, 1f, -0.005f),
            packUV(0f, 1f), packUV(1f, 1f), packUV(1f, 0f), packUV(0f, 0f),
            -1, Direction.NORTH, null, false, 0
        );
        return List.of(front, back);
    }

    @Override
    public void update(ItemStackRenderState state, ItemStack stack,
                       ItemModelResolver resolver, ItemDisplayContext context,
                       ClientLevel level, ItemOwner owner, int seed) {
        // Get a new render layer and configure it for our DynamicTexture
        ItemStackRenderState.LayerRenderState layer = state.newLayer();
        layer.setRenderType(RenderTypes.itemEntityTranslucentCull(textureId));
        layer.setUsesBlockLight(false);

        // Add flat-face quads (no atlas needed)
        List<BakedQuad> quadList = layer.prepareQuadList();
        quadList.addAll(quads);
    }
}
