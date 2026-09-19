/*
 * Rendering approach adapted from Wurst Client's ESP render layers.
 * Wurst Client is licensed under GNU GPL v3 or later.
 */
package dev.fixpot47.entityesp;

import java.util.Optional;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

public final class EntityEspRenderTypes {
    private EntityEspRenderTypes() {}

    private static final RenderPipeline.Snippet ESP_LINES_SNIPPET =
            RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
                    .withVertexShader(Identifier.parse("entityesp:core/fogless_lines"))
                    .withFragmentShader(Identifier.parse("entityesp:core/fogless_lines"))
                    .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                    .withCull(false)
                    .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH)
                    .withPrimitiveTopology(PrimitiveTopology.LINES)
                    .buildSnippet();

    private static final RenderPipeline ESP_LINES_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(ESP_LINES_SNIPPET)
                    .withLocation(Identifier.parse("entityesp:pipeline/esp_lines"))
                    .withDepthStencilState(Optional.empty())
                    .build()
    );

    public static final RenderType ESP_LINES = RenderType.create(
            "entityesp:esp_lines",
            RenderSetup.builder(ESP_LINES_PIPELINE)
                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    .createRenderSetup()
    );
}
