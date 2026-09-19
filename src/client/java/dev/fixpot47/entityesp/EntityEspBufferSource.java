/*
 * Buffer upload/draw flow adapted from Wurst Client's WurstBufferSource.
 * Wurst Client is licensed under GNU GPL v3 or later.
 */
package dev.fixpot47.entityesp;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.commands.RenderPass;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.rendertype.RenderType;

public final class EntityEspBufferSource {
    private final StagedVertexBuffer stagedBuffer =
            new StagedVertexBuffer(() -> "EntityESP", 4 * 1024 * 1024);
    private final List<StagedVertexBuffer.Draw> draws = new ArrayList<>();
    private final List<RenderType> drawTypes = new ArrayList<>();

    public VertexConsumer getBuffer(RenderType type) {
        if (!drawTypes.isEmpty()
                && drawTypes.getLast() == type
                && type.canConsolidateConsecutiveGeometry()) {
            return stagedBuffer.getVertexBuilder(draws.getLast());
        }

        ProjectionType projection = RenderSystem.getProjectionType();
        VertexSorting sorting = type.sortOnUpload() ? projection.vertexSorting() : null;
        StagedVertexBuffer.Draw draw = stagedBuffer.appendDraw(
                type.format(), type.primitiveTopology(), sorting);

        draws.add(draw);
        drawTypes.add(type);
        return stagedBuffer.getVertexBuilder(draw);
    }

    public void uploadAndDraw() {
        if (draws.isEmpty()) {
            close();
            return;
        }

        try {
            stagedBuffer.upload();

            RenderTarget target = Minecraft.getInstance().gameRenderer.mainRenderTarget();
            CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();

            try (RenderPass pass = encoder.createRenderPass(
                    () -> "EntityESP",
                    target.getColorTextureView(),
                    Optional.empty(),
                    target.getDepthTextureView(),
                    OptionalDouble.empty())) {
                RenderSystem.bindDefaultUniforms(pass);

                for (int i = 0; i < draws.size(); i++) {
                    draw(drawTypes.get(i), draws.get(i), pass);
                }
            }

            stagedBuffer.endDraw();
        } finally {
            close();
        }
    }

    private void draw(RenderType type, StagedVertexBuffer.Draw draw, RenderPass pass) {
        StagedVertexBuffer.ExecuteInfo info = stagedBuffer.getExecuteInfo(draw);
        if (info != null) {
            type.prepare().drawFromBuffer(info, pass);
        }
    }

    private void close() {
        draws.clear();
        drawTypes.clear();
        stagedBuffer.close();
    }
}
