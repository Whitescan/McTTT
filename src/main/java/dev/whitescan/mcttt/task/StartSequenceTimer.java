package dev.whitescan.mcttt.task;

import dev.whitescan.mcttt.McTTT;
import dev.whitescan.mcttt.config.MessageService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;

@RequiredArgsConstructor
public class StartSequenceTimer implements Runnable {

    private final McTTT mcTTT;

    @Getter
    @Setter
    private int taskId;

    private int countdown = 10;

    @Override
    public void run() {

        if (Bukkit.getOnlinePlayers().size() < mcTTT.getTraitorAmount() + mcTTT.getDetectiveAmount() + 1) {
            Bukkit.broadcast(Component.text(MessageService.COUNTDOWN_ABORT));
            cancel();
            return;
        }

        if (countdown == 0) {
            mcTTT.launch();
            cancel();
            return;
        }

        Bukkit.broadcast(Component.text(MessageService.COUNTDOWN.replace("%countdown%", String.valueOf(countdown--))));

    }

    private void cancel() {
        Bukkit.getScheduler().cancelTask(taskId);
    }

}
