package dev.whitescan.mcttt.config;

import dev.whitescan.mcttt.McTTT;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * This Service handles all the messages of this plugin that are sent to users.
 *
 * @author Whitescan
 * @since 1.0.0
 */
public class MessageService {

    @Getter
    private static Logger logger;

    @Getter
    private static final Properties messages = new Properties();

    // Messages

    public static String PREFIX;

    public static String COMMAND_PERMISSION_ERROR;

    public static String COMMAND_SYNTAX_ERROR;

    public static String COMMAND_PLAYER_ONLY_ERROR;

    public static String COMMAND_PLAYER_NOT_FOUND_ERROR;

    public static String COMMAND_WORLD_NOT_FOUND_ERROR;

    public static String COMMAND_HELP;

    public static String COMMAND_HELP_CONFIG;

    public static String COMMAND_CONFIG_SET;

    public static String COMMAND_CONFIG_ADDSPAWN;

    public static String COMMAND_CONFIG_REMOVESPAWN;

    public static String GAME_LAUNCHING;

    public static String ROLE_SELECTED;

    public static String GAME_LAUNCH;

    public static String GAME_LAUNCH_NO_DETECTIVES;

    public static String GAME_LAUNCH_TRAITORS;

    public static String ELIMINATED;

    public static String GAME_WIN_INNOCENT;

    public static String GAME_WIN_TRAITOR;

    public static String TRUTH;

    public static String TRUTH_END;

    public static String COUNTDOWN;

    public static String COUNTDOWN_ABORT;

    public static void init(final @NotNull McTTT mcTTT) {
        MessageService.logger = mcTTT.getLogger();

        final File messagesFile = new File(mcTTT.getDataFolder(), "messages.properties");

        try {

            if (!messagesFile.exists()) {
                Files.copy(mcTTT.getResource(messagesFile.getName()), messagesFile.getAbsoluteFile().toPath());
                getLogger().warning("Messages file not be found. Fallback to default...");
            }

            final FileInputStream in = new FileInputStream(messagesFile);
            messages.load(in);
            in.close();

        } catch (Exception e) {
            getLogger().severe("Failed to read messages file. See error below:");
            throw new RuntimeException(e);
        }

        read();

    }

    private static void read() {

        PREFIX = getRawMessage("prefix", "&7[&6TTT&7]");

        COMMAND_PERMISSION_ERROR = getMessage("command_permission_error", "&cYou are not allowed to do that.");
        COMMAND_SYNTAX_ERROR = getMessage("command_syntax_error", "&cYour syntax is incorrect. &aTry /ttt help for more information.");
        COMMAND_PLAYER_ONLY_ERROR = getMessage("command_player_only_error", "&cThis command can only be executed as a player.");
        COMMAND_PLAYER_NOT_FOUND_ERROR = getMessage("command_player_not_found", "&cThe target player could not be found. Make sure the player is online and double-check your spelling.");
        COMMAND_WORLD_NOT_FOUND_ERROR = getMessage("command_world_not_found", "&cThe target world could not be found. Make sure the world exists and double-check your spelling.");

        COMMAND_HELP = getMessage("command_help", "&7[&a?&7] &cCommand Help &7[&a?&7]\n&a/ttt help &7- &eShows this page\n&a/ttt quickstart &7- &eAttempt to quickstart the next round\n&c/ttt config <key> <value> &7- &eEdit the config\n&c/ttt forcestart &7- Forcestart the round\n&c/ttt debug &7- &eDisplay debug Information");
        COMMAND_HELP_CONFIG = getMessage("command_help_config", "&7[&a?&7] &cCommand Help &7[&a?&7]\n&a/ttt config set <key> <value> &7- &eAlter config values\n&a/ttt config addspawn <key> (location) &7- &eAdd a spawn\n&c/ttt config removespawn <key> &7- &eRemove a spawn");

        COMMAND_CONFIG_SET = getMessage("command_config_set", "&eConfig value of &c%key% &ehas been set to &a%value%&e.");
        COMMAND_CONFIG_ADDSPAWN = getMessage("command_config_addspawn", "&eSpawn &c%spawn% &ehas been added.");
        COMMAND_CONFIG_REMOVESPAWN = getMessage("command_config_removespawn", "&eSpawn &c%key% &ehas been deleted.");

        GAME_LAUNCHING = getMessage("game_launching", "&ePreparing for next round, please wait...");
        ROLE_SELECTED = getMessage("role_selected", "&eYou have been selected to be a: %role%");

        GAME_LAUNCH = getMessage("game_launch", "&eNext round has started! Detectives are: %detectives%");
        GAME_LAUNCH_NO_DETECTIVES = getMessage("game_launch_no_detectives", "&eNext round has started! &cThere will be no Detectives this round.");
        GAME_LAUNCH_TRAITORS = getMessage("game_launch_traitors", "&cTraitors are: %traitors%");

        ELIMINATED = getMessage("eliminated", "&cYou have been eliminated by &4%deathcause%");

        GAME_WIN_INNOCENT = getMessage("game_win_innocent", "&aAll Traitors have been eliminated!");
        GAME_WIN_TRAITOR = getMessage("game_win_traitor", "&cAll Innocents have been eliminated!");

        TRUTH = getMessage("truth", "&cYou cannot deal damage while truth is active.");
        TRUTH_END = getMessage("truth_end", "&cTruth has been disabled. &aGood Luck!");

        COUNTDOWN = getMessage("countdown", "&eStart in &a%countdown% seconds&e.");
        COUNTDOWN_ABORT = getMessage("countdown_abort", "&cLaunch has been aborted. Not enough players.");

    }

    public static String getRawMessage(String key, String defaultValue) {
        return getMessages().getProperty(key, defaultValue).replace("&", "§");
    }

    public static String getMessage(String key, String defaultValue) {
        return PREFIX + " " + getRawMessage(key, defaultValue);
    }

}
