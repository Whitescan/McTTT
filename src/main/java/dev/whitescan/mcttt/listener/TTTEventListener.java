package dev.whitescan.mcttt.listener;

import dev.whitescan.mcttt.McTTT;
import dev.whitescan.mcttt.config.MessageService;
import dev.whitescan.mcttt.data.GamePhase;
import lombok.RequiredArgsConstructor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@RequiredArgsConstructor
public class TTTEventListener implements Listener {

    private final McTTT mcTTT;

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.getPlayer().setGameMode(GameMode.SPECTATOR);
        mcTTT.attemptStart();
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (mcTTT.getGamePhase() != GamePhase.RUNNING)
            e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        if (e.getInventory().getType() != InventoryType.PLAYER && e.getPlayer().getGameMode() != GameMode.CREATIVE)
            e.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (e.getPlayer().getGameMode() != GameMode.CREATIVE)
            e.setCancelled(true);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        final Player actor = e.getPlayer();
        actor.setGameMode(GameMode.SPECTATOR);

        final String deathCause;
        if (actor.getKiller() != null) {
            deathCause = actor.getKiller().getName() + " §7(" + mcTTT.getRole(actor.getKiller()) + "§7)";
        } else if (actor.getLastDamageCause() != null) {
            deathCause = actor.getLastDamageCause().getCause().toString();
        } else {
            deathCause = "§5Unknown";
        }

        actor.sendMessage(MessageService.ELIMINATED.replace("%deathcause%", deathCause));
        e.deathMessage(null);
        mcTTT.endRoundCheck();

    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (mcTTT.isTruth())
            e.setCancelled(true);
    }

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent e) {
        if (mcTTT.isTruth() && e.getDamager() instanceof Player) {
            e.setCancelled(true);
            e.getDamager().sendMessage(MessageService.TRUTH);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        mcTTT.endRoundCheck();
    }

}
