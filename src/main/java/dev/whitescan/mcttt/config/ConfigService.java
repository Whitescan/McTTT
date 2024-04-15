package dev.whitescan.mcttt.config;

import dev.whitescan.mcttt.McTTT;
import dev.whitescan.mcttt.data.EasyLocation;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * This class provides all configurations used for the plugin.
 *
 * @author Whitescan
 * @since 1.0.0
 */
public class ConfigService {

    @Getter
    private static Logger logger;

    @Getter
    private static FileConfiguration config;

    // Configs

    public static List<String> GUNS;

    public static Map<EasyLocation, String> SPAWNS = new HashMap<>();

    public static void init(final @NotNull McTTT mcTTT) {

        ConfigService.logger = mcTTT.getLogger();
        ConfigService.config = mcTTT.getConfig();

        final File configFile = new File(mcTTT.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            mcTTT.saveDefaultConfig();
            getLogger().warning("Config file not found. Fallback to default...");
        }

        read();

    }

    private static void read() {

        GUNS = getConfig().getStringList("config.guns");

        for (final String map : getConfig().getConfigurationSection("config.maps").getKeys(false)) {

            for (final String key : getConfig().getConfigurationSection("config.maps." + map).getKeys(false)) {

                final double x = getConfig().getDouble("config.maps." + map + "." + key + ".x");
                final double y = getConfig().getDouble("config.maps." + map + "." + key + ".y");
                final double z = getConfig().getDouble("config.maps." + map + "." + key + ".z");

                final float yaw = (float) getConfig().getDouble("config.maps." + map + "." + key + ".yaw");
                final float pitch = (float) getConfig().getDouble("config.maps." + map + "." + key + ".pitch");

                SPAWNS.put(new EasyLocation(map, x, y, z, yaw, pitch), key);

            }

        }

    }

}
