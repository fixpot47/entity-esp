package dev.fixpot47.entityesp.mixin;

import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.buffers.GpuBufferSlice;

import dev.fixpot47.entityesp.EntityEspRenderer;
import net.minecraft.util.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;

import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    // Minecraft 26.2 LevelRenderer.render uses DeltaTracker and Matrix4fc.
    // The 26.3 render descriptor crashes during mixin application on 26.2.
    @Inject(method = "render", at = @At("RETURN"))
    private void entityesp$render(
            GraphicsResourceAllocator allocator,
            DeltaTracker deltaTracker,
            boolean renderBlockOutline,
            CameraRenderState cameraState,
            Matrix4fc modelViewMatrix,
            GpuBufferSlice terrainFog,
            Vector4f fogColor,
            boolean shouldRenderSky,
            CallbackInfo ci) {
        PoseStack poseStack = new PoseStack();
        poseStack.mulPose(cameraState.viewRotationMatrix);
        EntityEspRenderer.render(poseStack, 0.0F);
    }
}
