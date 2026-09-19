/*
 * Rendering approach adapted from Wurst Client's ESP render layers.
 * Wurst Client is licensed under GNU GPL v3 or later.
 */
package dev.fixpot47.entityesp;

import java.util.Optional;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;

public final class EntityEspRenderTypes {
    private EntityEspRenderTypes() {}

    private static final RenderPipeline ESP_LINES_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
                    .withLocation(Identifier.parse("entityesp:pipeline/esp_lines"))
                    .withDepthStencilState(Optional.empty())
                    .build()
    );

    private static final RenderPipeline ESP_QUADS_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                    .withLocation(Identifier.parse("entityesp:pipeline/esp_quads"))
                    .withDepthStencilState(Optional.empty())
                    .withCull(false)
                    .build()
    );

    public static final RenderType ESP_LINES = RenderType.create(
            "entityesp:esp_lines",
            RenderSetup.builder(ESP_LINES_PIPELINE)
                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    .createRenderSetup()
    );

    public static final RenderType ESP_QUADS = RenderType.create(
            "entityesp:esp_quads",
            RenderSetup.builder(ESP_QUADS_PIPELINE)
                    .sortOnUpload()
                    .createRenderSetup()
    );
}
