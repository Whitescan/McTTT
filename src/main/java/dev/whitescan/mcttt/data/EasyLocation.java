package dev.whitescan.mcttt.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 * A simple org.bukkit.Location wrapper
 *
 * @author Whitescan
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
public class EasyLocation {

    @NonNull
    private String world;

    private double x;

    private double y;

    private double z;

    private float yaw = 0;

    private float pitch = 0;

    public EasyLocation(Location location) {

        this.world = location.getWorld().getName();

        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();

        this.yaw = location.getYaw();
        this.pitch = location.getPitch();

    }

    public Location getLocation() {
        return new Location(Bukkit.getWorld(getWorld()), getX(), getY(), getZ(), getYaw(), getPitch());
    }

}
