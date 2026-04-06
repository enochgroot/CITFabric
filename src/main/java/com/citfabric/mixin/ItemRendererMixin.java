package com.citfabric.mixin;
import com.citfabric.CITManager;
import com.citfabric.CITRule;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.item.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {

    @Inject(method = "renderStatic", at = @At("HEAD"), cancellable = true)
    private void cit$renderStatic(ItemStack stack, ItemDisplayContext ctx,
                                   int packedLight, int packedOverlay,
                                   PoseStack poseStack, MultiBufferSource bufferSource,
                                   Level level, int seed, CallbackInfo ci) {
        CITRule rule = CITManager.getInstance().findRule(stack);
        if (rule == null) return;
        ci.cancel();

        Minecraft mc = Minecraft.getInstance();
        BakedModel model = mc.getItemRenderer().getModel(stack, level, mc.player, seed);
        poseStack.pushPose();
        boolean isLeft = ctx == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                      || ctx == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
        model.getTransforms().getTransform(ctx).apply(isLeft, poseStack);
        poseStack.translate(-0.5, -0.5, -0.5);

        RenderType rt = RenderType.itemEntityTranslucentCull(rule.textureKey);
        VertexConsumer vc = bufferSource.getBuffer(rt);
        PoseStack.Pose pose = poseStack.last();

        // In 1.21.11 setNormal takes PoseStack.Pose, not Matrix3f
        vc.addVertex(pose.pose(),0f,0f,0.005f).setColor(-1).setUv(0f,1f).setOverlay(packedOverlay).setLight(packedLight).setNormal(pose,0f,0f,1f);
        vc.addVertex(pose.pose(),1f,0f,0.005f).setColor(-1).setUv(1f,1f).setOverlay(packedOverlay).setLight(packedLight).setNormal(pose,0f,0f,1f);
        vc.addVertex(pose.pose(),1f,1f,0.005f).setColor(-1).setUv(1f,0f).setOverlay(packedOverlay).setLight(packedLight).setNormal(pose,0f,0f,1f);
        vc.addVertex(pose.pose(),0f,1f,0.005f).setColor(-1).setUv(0f,0f).setOverlay(packedOverlay).setLight(packedLight).setNormal(pose,0f,0f,1f);

        vc.addVertex(pose.pose(),1f,0f,-0.005f).setColor(-1).setUv(0f,1f).setOverlay(packedOverlay).setLight(packedLight).setNormal(pose,0f,0f,-1f);
        vc.addVertex(pose.pose(),0f,0f,-0.005f).setColor(-1).setUv(1f,1f).setOverlay(packedOverlay).setLight(packedLight).setNormal(pose,0f,0f,-1f);
        vc.addVertex(pose.pose(),0f,1f,-0.005f).setColor(-1).setUv(1f,0f).setOverlay(packedOverlay).setLight(packedLight).setNormal(pose,0f,0f,-1f);
        vc.addVertex(pose.pose(),1f,1f,-0.005f).setColor(-1).setUv(0f,0f).setOverlay(packedOverlay).setLight(packedLight).setNormal(pose,0f,0f,-1f);

        poseStack.popPose();
    }
}
