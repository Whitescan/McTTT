package dev.whitescan.mcttt;

import dev.whitescan.mcttt.command.TTTCommand;
import dev.whitescan.mcttt.config.ConfigService;
import dev.whitescan.mcttt.config.MessageService;
import dev.whitescan.mcttt.data.GamePhase;
import dev.whitescan.mcttt.data.Role;
import dev.whitescan.mcttt.listener.TTTEventListener;
import dev.whitescan.mcttt.task.StartSequenceTimer;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

/**
 * This is the main class of this Plugin.
 *
 * @author Whitescan
 * @since 1.0.0
 */
public final class McTTT extends JavaPlugin {

    @Getter
    private static McTTT instance;

    @Getter
    private final Random random = new Random();

    @Getter
    private final Map<Player, Role> players = new HashMap<>();

    @Getter
    @Setter
    private int traitorAmount = 1;

    @Getter
    @Setter
    private int detectiveAmount = 0;

    @Getter
    @Setter
    private GamePhase gamePhase = GamePhase.WAIT;

    @Getter
    @Setter
    private boolean truth = true;

    @Getter
    @Setter
    private boolean startLocked = false;

    @Override
    public void onLoad() {
        McTTT.instance = this;
        MessageService.init(this);
        ConfigService.init(this);
    }

    @Override
    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new TTTEventListener(this), this);
        getCommand("ttt").setExecutor(new TTTCommand(this));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void attemptStart() {

        if (isStartLocked())
            return;

        if (getGamePhase() != GamePhase.WAIT)
            return;

        if (Bukkit.getOnlinePlayers().size() < traitorAmount + detectiveAmount + 1)
            return;

        StartSequenceTimer startSequence = new StartSequenceTimer(this);
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(this, startSequence, 20L, 20L); // Delay one second to avoid race condition with the cancelling
        startSequence.setTaskId(task.getTaskId());

    }

    public void launch() {

        // Announce new round being prepared

        Bukkit.broadcast(Component.text(MessageService.GAME_LAUNCHING));

        setGamePhase(GamePhase.PREP);

        // Clean

        for (World world : Bukkit.getWorlds())
            for (Entity entity : world.getEntities())
                if (entity.getType() != EntityType.PLAYER)
                    entity.remove();

        // Randomly select player roles

        getPlayers().clear();

        final List<Player> randomPlayerList = new ArrayList<>(Bukkit.getOnlinePlayers());
        Collections.shuffle(randomPlayerList);

        final List<String> detectives = new ArrayList<>();
        final List<String> traitors = new ArrayList<>();

        int selected = 0;
        for (final Player player : randomPlayerList) {
            player.spigot().respawn();
            player.getInventory().clear();

            final Role role;
            if (selected < getTraitorAmount()) {
                role = Role.TRAITOR;
                player.displayName(Component.text("§7" + player.getName()));
                traitors.add(player.getName());
            } else if (selected < getTraitorAmount() + getDetectiveAmount()) {
                role = Role.DETECTIVE;
                player.displayName(Component.text(Role.DETECTIVE + " §7| §9" + player.getName()));
                detectives.add(player.getName());
            } else {
                role = Role.INNOCENT;
                player.displayName(Component.text("§7" + player.getName()));
            }

            getPlayers().put(player, role);
            getLogger().info("DEBUG: " + selected + ". selection = " + player.getName() + " as " + role.getDisplayName());
            player.setGameMode(GameMode.ADVENTURE);
            player.teleport(new Location(Bukkit.getWorld("world_ttt"), 0, 64, 0));

            selected++;

        }

        // Launch

        setGamePhase(GamePhase.RUNNING);

        final String gameLaunchMessage;
        if (detectives.isEmpty()) {
            gameLaunchMessage = MessageService.GAME_LAUNCH_NO_DETECTIVES;
        } else {
            gameLaunchMessage = MessageService.GAME_LAUNCH.replace("%detectives%", String.join("\n§7- §9", detectives));
        }

        Bukkit.broadcast(Component.text(gameLaunchMessage));

        for (Entry<Player, Role> entry : getPlayers().entrySet()) {
            entry.getKey().sendMessage(MessageService.ROLE_SELECTED.replace("%role%", entry.getValue().getDisplayName()));
            if (entry.getValue() == Role.TRAITOR)
                entry.getKey().sendMessage(MessageService.GAME_LAUNCH_TRAITORS.replace("%traitors%", String.join("\n§7- §4", traitors)));
        }

        Bukkit.getScheduler().runTaskLater(this, new Runnable() {

            @Override
            public void run() {

                setTruth(false);

                final List<String> guns = ConfigService.GUNS;
                for (final Player player : getPlayers().keySet()) {
                    String gun = guns.get(getRandom().nextInt(guns.size()));
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "shot give " + player.getName() + " " + gun);
                }

                Bukkit.broadcast(Component.text(MessageService.TRUTH_END));

            }

        }, TimeUnit.MINUTES.toSeconds(1) * 20L);

    }

    public void endRoundCheck() {

        int traitorAlive = 0;
        int innocentAlive = 0;

        for (Map.Entry<Player, Role> entry : getPlayers().entrySet()) {

            if (entry.getKey().getGameMode() != GameMode.ADVENTURE || !entry.getKey().isConnected())
                continue;

            if (entry.getValue() == Role.TRAITOR) {
                traitorAlive++;
            } else {
                innocentAlive++;
            }

        }

        if (traitorAlive == 0) {
            Bukkit.broadcast(Component.text(MessageService.GAME_WIN_INNOCENT));
        } else if (innocentAlive == 0) {
            Bukkit.broadcast(Component.text(MessageService.GAME_WIN_TRAITOR));
        } else {
            return;
        }

        setTruth(true);
        setGamePhase(GamePhase.WAIT);
        setStartLocked(false);
        attemptStart();

    }

    public Role getRole(final Player player) {
        return getPlayers().get(player);
    }

    public void updateSpawnPoint(final @NotNull String key, final @NotNull Location location) {

    }

    public void addSpawn(final @NotNull String key, final @NotNull Location location) {
    }

    public void removeSpawn(final @NotNull String key) {
    }
}
