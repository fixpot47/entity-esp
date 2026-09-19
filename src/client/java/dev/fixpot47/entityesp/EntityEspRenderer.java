/*
 * EntityESP rendering and target selection are adapted from the PlayerESP,
 * MobESP and ChestESP features of Wurst Client.
 *
 * Copyright (c) 2014-2026 Wurst-Imperium and contributors.
 * Modifications Copyright (c) 2026 fixpot47.
 *
 * Licensed under GNU GPL v3 or later.
 */
package dev.fixpot47.entityesp;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractChestBoat;
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;
import net.minecraft.world.entity.vehicle.minecart.MinecartHopper;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.DropperBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class EntityEspRenderer {
    private static final List<ColoredBox> CHEST_BOXES = new ArrayList<>();
    private static int chestScanCooldown;

    private static final int CHEST_GREEN = 0xFF00FF00;
    private static final int CHEST_ORANGE = 0xFFFF8000;
    private static final int CHEST_CYAN = 0xFF00FFFF;
    private static final int CHEST_MAGENTA = 0xFFFF00FF;
    private static final int CHEST_WHITE = 0xFFFFFFFF;
    private static final int CHEST_RED = 0xFFFF0000;
    private static final int CHEST_YELLOW = 0xFFFFFF00;

    private EntityEspRenderer() {}

    public static void register() {
        LevelRenderEvents.COLLECT_SUBMITS.register(EntityEspRenderer::render);
    }

    public static void tick(Minecraft client) {
        if (!EntityEspConfig.chestEsp() || client.level == null || client.player == null) {
            CHEST_BOXES.clear();
            chestScanCooldown = 0;
            return;
        }

        if (chestScanCooldown-- > 0) {
            return;
        }

        chestScanCooldown = 10;
        scanChests(client);
    }

    private static void render(LevelRenderContext context) {
        Minecraft client = Minecraft.getInstance();
        if (!EntityEspConfig.anyEnabled() || client.level == null || client.player == null) {
            return;
        }

        Camera camera = client.gameRenderer.mainCamera();
        if (!camera.isInitialized()) {
            return;
        }

        Vec3 cameraPos = camera.position();
        List<ColoredBox> boxes = new ArrayList<>();

        for (Entity entity : client.level.entitiesForRendering()) {
            if (!entity.isAlive() || entity.isRemoved()) {
                continue;
            }

            if (EntityEspConfig.playerEsp() && entity instanceof Player player && player != client.player) {
                boxes.add(new ColoredBox(entity.getBoundingBox().inflate(0.035D), distanceColor(client, player)));
                continue;
            }

            if (EntityEspConfig.mobEsp()
                    && entity instanceof LivingEntity living
                    && !(entity instanceof Player)) {
                boxes.add(new ColoredBox(entity.getBoundingBox().inflate(0.025D), distanceColor(client, living)));
                continue;
            }

            if (EntityEspConfig.chestEsp()) {
                if (entity instanceof MinecartChest
                        || entity instanceof AbstractChestBoat
                        || entity instanceof MinecartHopper) {
                    boxes.add(new ColoredBox(entity.getBoundingBox().inflate(0.035D), CHEST_YELLOW));
                }
            }
        }

        if (EntityEspConfig.chestEsp()) {
            boxes.addAll(CHEST_BOXES);
        }

        if (boxes.isEmpty()) {
            return;
        }

        submitBoxes(context, cameraPos, boxes, EntityEspRenderTypes.ESP_QUADS, true);
        submitBoxes(context, cameraPos, boxes, EntityEspRenderTypes.ESP_LINES, false);
    }

    private static void submitBoxes(LevelRenderContext context, Vec3 cameraPos,
                                    List<ColoredBox> boxes, RenderType renderType,
                                    boolean filled) {
        context.submitNodeCollector().submitCustomGeometry(
                context.poseStack(), renderType, (pose, consumer) -> {
                    for (ColoredBox colored : boxes) {
                        AABB box = colored.box.move(-cameraPos.x, -cameraPos.y, -cameraPos.z);
                        int color = filled ? withAlpha(colored.color, 0x35) : withAlpha(colored.color, 0xD0);
                        if (filled) {
                            drawSolidBox(pose, consumer, box, color);
                        } else {
                            drawOutlinedBox(pose, consumer, box, color);
                        }
                    }
                });
    }

    private static int distanceColor(Minecraft client, LivingEntity entity) {
        float f = client.player.distanceTo(entity) / 20.0F;
        float r = Mth.clamp(2.0F - f, 0.0F, 1.0F);
        float g = Mth.clamp(f, 0.0F, 1.0F);
        return 0xFF000000
                | ((int)(r * 255.0F) << 16)
                | ((int)(g * 255.0F) << 8);
    }

    private static int withAlpha(int argb, int alpha) {
        return (alpha << 24) | (argb & 0x00FFFFFF);
    }

    private static void scanChests(Minecraft client) {
        CHEST_BOXES.clear();

        int radius = Math.max(2, client.options.getEffectiveRenderDistance()) + 3;
        ChunkPos center = client.player.chunkPosition();

        for (int x = center.x() - radius; x <= center.x() + radius; x++) {
            for (int z = center.z() - radius; z <= center.z() + radius; z++) {
                if (!client.level.hasChunk(x, z)) {
                    continue;
                }

                LevelChunk chunk = client.level.getChunk(x, z);
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    ColoredBox box = chestBox(client, be);
                    if (box != null) {
                        CHEST_BOXES.add(box);
                    }
                }
            }
        }
    }

    private static ColoredBox chestBox(Minecraft client, BlockEntity be) {
        int color;

        if (be instanceof ChestBlockEntity) {
            color = be instanceof net.minecraft.world.level.block.entity.TrappedChestBlockEntity
                    ? CHEST_ORANGE : CHEST_GREEN;
        } else if (be instanceof EnderChestBlockEntity) {
            color = CHEST_CYAN;
        } else if (be instanceof BarrelBlockEntity || be instanceof DecoratedPotBlockEntity) {
            color = CHEST_GREEN;
        } else if (be instanceof ShulkerBoxBlockEntity) {
            color = CHEST_MAGENTA;
        } else if (be instanceof HopperBlockEntity
                || be instanceof DropperBlockEntity
                || be instanceof CrafterBlockEntity) {
            color = CHEST_WHITE;
        } else if (be instanceof DispenserBlockEntity) {
            color = CHEST_ORANGE;
        } else if (be instanceof AbstractFurnaceBlockEntity) {
            color = CHEST_RED;
        } else {
            return null;
        }

        AABB box = getBlockEntityBox(client, be);
        return box == null ? null : new ColoredBox(box, color);
    }

    private static AABB getBlockEntityBox(Minecraft client, BlockEntity be) {
        BlockPos pos = be.getBlockPos();

        if (be instanceof ChestBlockEntity chest) {
            BlockState state = chest.getBlockState();
            if (state.hasProperty(ChestBlock.TYPE)) {
                ChestType type = state.getValue(ChestBlock.TYPE);
                if (type == ChestType.LEFT) {
                    return null;
                }

                AABB box = new AABB(pos);
                if (type != ChestType.SINGLE) {
                    BlockPos connected = pos.relative(ChestBlock.getConnectedDirection(state));
                    if (client.level.getBlockState(connected).getBlock() instanceof ChestBlock) {
                        box = box.minmax(new AABB(connected));
                    }
                }
                return box;
            }
        }

        return new AABB(pos);
    }

    private static void drawSolidBox(PoseStack.Pose pose, VertexConsumer buffer, AABB box, int color) {
        float x1 = (float)box.minX;
        float y1 = (float)box.minY;
        float z1 = (float)box.minZ;
        float x2 = (float)box.maxX;
        float y2 = (float)box.maxY;
        float z2 = (float)box.maxZ;

        buffer.addVertex(pose, x1, y1, z1).setColor(color);
        buffer.addVertex(pose, x2, y1, z1).setColor(color);
        buffer.addVertex(pose, x2, y1, z2).setColor(color);
        buffer.addVertex(pose, x1, y1, z2).setColor(color);

        buffer.addVertex(pose, x1, y2, z1).setColor(color);
        buffer.addVertex(pose, x1, y2, z2).setColor(color);
        buffer.addVertex(pose, x2, y2, z2).setColor(color);
        buffer.addVertex(pose, x2, y2, z1).setColor(color);

        buffer.addVertex(pose, x1, y1, z1).setColor(color);
        buffer.addVertex(pose, x1, y2, z1).setColor(color);
        buffer.addVertex(pose, x2, y2, z1).setColor(color);
        buffer.addVertex(pose, x2, y1, z1).setColor(color);

        buffer.addVertex(pose, x2, y1, z1).setColor(color);
        buffer.addVertex(pose, x2, y2, z1).setColor(color);
        buffer.addVertex(pose, x2, y2, z2).setColor(color);
        buffer.addVertex(pose, x2, y1, z2).setColor(color);

        buffer.addVertex(pose, x1, y1, z2).setColor(color);
        buffer.addVertex(pose, x2, y1, z2).setColor(color);
        buffer.addVertex(pose, x2, y2, z2).setColor(color);
        buffer.addVertex(pose, x1, y2, z2).setColor(color);

        buffer.addVertex(pose, x1, y1, z1).setColor(color);
        buffer.addVertex(pose, x1, y1, z2).setColor(color);
        buffer.addVertex(pose, x1, y2, z2).setColor(color);
        buffer.addVertex(pose, x1, y2, z1).setColor(color);
    }

    private static void drawOutlinedBox(PoseStack.Pose pose, VertexConsumer buffer, AABB box, int color) {
        float x1 = (float)box.minX;
        float y1 = (float)box.minY;
        float z1 = (float)box.minZ;
        float x2 = (float)box.maxX;
        float y2 = (float)box.maxY;
        float z2 = (float)box.maxZ;

        line(pose, buffer, x1, y1, z1, x2, y1, z1, 1, 0, 0, color);
        line(pose, buffer, x1, y1, z2, x2, y1, z2, 1, 0, 0, color);
        line(pose, buffer, x1, y2, z1, x2, y2, z1, 1, 0, 0, color);
        line(pose, buffer, x1, y2, z2, x2, y2, z2, 1, 0, 0, color);

        line(pose, buffer, x1, y1, z1, x1, y1, z2, 0, 0, 1, color);
        line(pose, buffer, x2, y1, z1, x2, y1, z2, 0, 0, 1, color);
        line(pose, buffer, x1, y2, z1, x1, y2, z2, 0, 0, 1, color);
        line(pose, buffer, x2, y2, z1, x2, y2, z2, 0, 0, 1, color);

        line(pose, buffer, x1, y1, z1, x1, y2, z1, 0, 1, 0, color);
        line(pose, buffer, x2, y1, z1, x2, y2, z1, 0, 1, 0, color);
        line(pose, buffer, x1, y1, z2, x1, y2, z2, 0, 1, 0, color);
        line(pose, buffer, x2, y1, z2, x2, y2, z2, 0, 1, 0, color);
    }

    private static void line(PoseStack.Pose pose, VertexConsumer buffer,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float nx, float ny, float nz, int color) {
        buffer.addVertex(pose, x1, y1, z1).setColor(color)
                .setNormal(pose, nx, ny, nz).setLineWidth(2.0F);
        buffer.addVertex(pose, x2, y2, z2).setColor(color)
                .setNormal(pose, nx, ny, nz).setLineWidth(2.0F);
    }

    private record ColoredBox(AABB box, int color) {}
}
