package dev.fixpot47.entityesp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import net.fabricmc.loader.api.FabricLoader;

public final class EntityEspConfig {
    private static final Path PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("entity-esp.properties");

    private static boolean playerEsp;
    private static boolean mobEsp;
    private static boolean chestEsp;

    private EntityEspConfig() {}

    public static boolean playerEsp() {
        return playerEsp;
    }

    public static boolean mobEsp() {
        return mobEsp;
    }

    public static boolean chestEsp() {
        return chestEsp;
    }

    public static void togglePlayerEsp() {
        playerEsp = !playerEsp;
        save();
    }

    public static void toggleMobEsp() {
        mobEsp = !mobEsp;
        save();
    }

    public static void toggleChestEsp() {
        chestEsp = !chestEsp;
        save();
    }

    public static boolean anyEnabled() {
        return playerEsp || mobEsp || chestEsp;
    }

    public static void load() {
        Properties p = new Properties();
        if (Files.isRegularFile(PATH)) {
            try (InputStream in = Files.newInputStream(PATH)) {
                p.load(in);
            } catch (IOException ignored) {
            }
        }

        playerEsp = Boolean.parseBoolean(p.getProperty("playerESP", "false"));
        mobEsp = Boolean.parseBoolean(p.getProperty("mobESP", "false"));
        chestEsp = Boolean.parseBoolean(p.getProperty("chestESP", "false"));
    }

    private static void save() {
        Properties p = new Properties();
        p.setProperty("playerESP", Boolean.toString(playerEsp));
        p.setProperty("mobESP", Boolean.toString(mobEsp));
        p.setProperty("chestESP", Boolean.toString(chestEsp));

        try {
            Files.createDirectories(PATH.getParent());
            try (OutputStream out = Files.newOutputStream(PATH)) {
                p.store(out, "EntityESP settings");
            }
        } catch (IOException ignored) {
        }
    }
}
