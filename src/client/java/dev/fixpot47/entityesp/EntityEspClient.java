/*
 * EntityESP
 * Copyright (C) 2026 fixpot47
 *
 * ESP behavior is adapted from Wurst Client by Wurst-Imperium and contributors.
 * Original project: https://github.com/Wurst-Imperium/Wurst7
 *
 * This program is free software under GNU GPL v3 or later.
 */
package dev.fixpot47.entityesp;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public final class EntityEspClient implements ClientModInitializer {
    public static final String MOD_ID = "entityesp";

    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(MOD_ID, "controls")
    );

    private static KeyMapping openMenuKey;

    @Override
    public void onInitializeClient() {
        EntityEspConfig.load();
        EntityEspRenderer.register();

        openMenuKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.entityesp.open_menu",
                InputConstants.Type.KEYBOARD,
                InputConstants.KEY_F8,
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.consumeClick()) {
                if (client.screen instanceof EntityEspScreen) {
                    client.setScreenAndShow(null);
                } else {
                    client.setScreenAndShow(new EntityEspScreen());
                }
            }

            EntityEspRenderer.tick(client);
        });
    }
}
