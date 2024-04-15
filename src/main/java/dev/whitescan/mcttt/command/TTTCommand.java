package dev.whitescan.mcttt.command;

import dev.whitescan.mcttt.McTTT;
import dev.whitescan.mcttt.config.ConfigService;
import dev.whitescan.mcttt.config.MessageService;
import dev.whitescan.mcttt.data.GamePhase;
import dev.whitescan.mcttt.data.Role;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class TTTCommand implements CommandExecutor, TabExecutor {

    private final McTTT mcTTT;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {

        final String subCommand = args.length > 0 ? args[0].toLowerCase() : "help";

        switch (subCommand) {

            case "help":
                help(sender);
                return true;

            case "quickstart":
                quickstart(sender);
                return true;

            case "forcestart":
                forcestart(sender);
                return true;

            case "debug":
                debug(sender);
                return true;

            case "config":
                config(sender, args);
                return true;

            default:
                sender.sendMessage(MessageService.COMMAND_SYNTAX_ERROR);

        }

        return true;

    }

    private void config(CommandSender sender, String[] args) {

        if (!sender.hasPermission("ttt.admin.config")) {
            sender.sendMessage(MessageService.COMMAND_PERMISSION_ERROR);
            return;
        }

        final String subCommand = args.length > 1 ? args[1].toLowerCase() : "help";

        switch (subCommand) {

            case "help":
                sender.sendMessage(MessageService.COMMAND_HELP_CONFIG);

            case "set":

                if (args.length > 2) {

                    final String key = args[2];
                    final Object value;

                    if (args.length == 4 && "traitors".equalsIgnoreCase(key)) {
                        value = Integer.parseInt(args[3]);
                        mcTTT.setTraitorAmount((int) value);
                    } else if (args.length == 4 && "detectives".equalsIgnoreCase(key)) {
                        value = Integer.parseInt(args[3]);
                        mcTTT.setDetectiveAmount((int) value);
                    } else if ("spawn".equalsIgnoreCase(key)) {

                        if (args.length == 3) {

                            if (!(sender instanceof Player actor)) {
                                sender.sendMessage(MessageService.COMMAND_PLAYER_ONLY_ERROR);
                                return;
                            }

                            value = actor.getLocation();
                            mcTTT.updateSpawnPoint(key, (Location) value);

                        } else if (args.length == 4) {

                            final Player target = Bukkit.getPlayer(args[3]);
                            if (target == null) {
                                sender.sendMessage(MessageService.COMMAND_PLAYER_NOT_FOUND_ERROR);
                                return;
                            }

                            value = target.getLocation();
                            mcTTT.updateSpawnPoint(key, (Location) value);

                        } else if (args.length == 7) {

                            final World world = Bukkit.getWorld(args[3]);
                            if (world == null) {
                                sender.sendMessage(MessageService.COMMAND_WORLD_NOT_FOUND_ERROR);
                                return;
                            }

                            final double x = Double.parseDouble(args[4]);
                            final double y = Double.parseDouble(args[5]);
                            final double z = Double.parseDouble(args[6]);

                            value = new Location(world, x, y, z);
                            mcTTT.updateSpawnPoint(key, (Location) value);

                        } else if (args.length == 9) {

                            final World world = Bukkit.getWorld(args[3]);
                            if (world == null) {
                                sender.sendMessage(MessageService.COMMAND_WORLD_NOT_FOUND_ERROR);
                                return;
                            }

                            final double x = Double.parseDouble(args[4]);
                            final double y = Double.parseDouble(args[5]);
                            final double z = Double.parseDouble(args[6]);

                            final float yaw = Float.parseFloat(args[7]);
                            final float pitch = Float.parseFloat(args[8]);

                            value = new Location(world, x, y, z, yaw, pitch);
                            mcTTT.updateSpawnPoint(key, (Location) value);

                        } else {
                            sender.sendMessage(MessageService.COMMAND_SYNTAX_ERROR);
                            return;
                        }

                    } else {
                        sender.sendMessage(MessageService.COMMAND_SYNTAX_ERROR);
                        return;
                    }

                    String message = MessageService.COMMAND_CONFIG_SET;
                    message = message.replace("%key%", key.toLowerCase());
                    message = message.replace("%value%", value.toString());
                    sender.sendMessage(message);

                }

            case "addspawn":

                if (args.length == 3) {

                    if (!(sender instanceof Player actor)) {
                        sender.sendMessage(MessageService.COMMAND_PLAYER_ONLY_ERROR);
                        return;
                    }

                    final String key = args[2];
                    final Location location = actor.getLocation();
                    mcTTT.addSpawn(key, location);
                    actor.sendMessage(MessageService.COMMAND_CONFIG_ADDSPAWN.replace("%spawn%", location.toString()));

                }

            case "removespawn":

                if (args.length == 3) {
                    final String key = args[2];
                    mcTTT.removeSpawn(key);
                    sender.sendMessage(MessageService.COMMAND_CONFIG_REMOVESPAWN.replace("%key%", key));
                }

            default:
                sender.sendMessage(MessageService.COMMAND_SYNTAX_ERROR);

        }

    }

    private void debug(CommandSender sender) {

        if (!sender.hasPermission("ttt.admin.debug")) {
            sender.sendMessage(MessageService.COMMAND_PERMISSION_ERROR);
            return;
        }

        sender.sendMessage(MessageService.PREFIX + " Players: " + Bukkit.getOnlinePlayers().size() + "\nTraitors: " + mcTTT.getTraitorAmount() + "\nDetectives: " + mcTTT.getDetectiveAmount() + "\nWeapons: " + ConfigService.GUNS.size() + "\nGamePhase: " + mcTTT.getGamePhase().toString());

        if (mcTTT.getGamePhase() == GamePhase.RUNNING) {
            for (final Map.Entry<Player, Role> entry : mcTTT.getPlayers().entrySet()) {
                final String status = entry.getKey().getGameMode() == GameMode.ADVENTURE && entry.getKey().isConnected() ? "§aALIVE" : "§cDEAD";
                sender.sendMessage("§e" + entry.getKey().getName() + " §7(" + entry.getValue().getDisplayName() + "§7) §7- " + status);
            }
        }

    }

    private void forcestart(CommandSender sender) {

        if (!sender.hasPermission("ttt.admin.forcestart")) {
            sender.sendMessage(MessageService.COMMAND_PERMISSION_ERROR);
            return;
        }

        mcTTT.launch();

    }

    private void quickstart(CommandSender sender) {

        if (!sender.hasPermission("ttt.quickstart")) {
            sender.sendMessage(MessageService.COMMAND_PERMISSION_ERROR);
            return;
        }

        mcTTT.attemptStart();

    }

    private void help(CommandSender sender) {
        sender.sendMessage(MessageService.COMMAND_HELP);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {

        final List<String> complete = new ArrayList<>();

        if (args.length == 0) {

            complete.add("help");

            if (sender.hasPermission("ttt.quickstart"))
                complete.add("quickstart");

            if (sender.hasPermission("ttt.admin.forcestart"))
                complete.add("forcestart");

            if (sender.hasPermission("ttt.admin.debug"))
                complete.add("debug");

            if (sender.hasPermission("ttt.admin.config"))
                complete.add("config");

        } else {

            final String subCommand = args.length == 1 ? args[0].toLowerCase() : "help";

            switch (subCommand) {

                case "debug": {
                    if (sender.hasPermission("ttt.admin.debug")) {
                        complete.add("toggle");
                        complete.add("info");
                    }
                }

                case "config": {
                    if (sender.hasPermission("ttt.admin.config")) {
                        complete.add("traitors");
                        complete.add("detectives");
                        complete.add("gamephase");
                    }
                }

            }

        }

        return complete;

    }

}
