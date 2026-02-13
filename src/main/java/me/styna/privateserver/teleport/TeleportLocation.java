package me.styna.privateserver.teleport;

import net.minecraft.entity.player.EntityPlayerMP;

public class TeleportLocation {
    private final int dimension;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;

    public TeleportLocation(int dimension, double x, double y, double z, float yaw, float pitch) {
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public static TeleportLocation fromPlayer(EntityPlayerMP player) {
        return new TeleportLocation(
                player.dimension,
                player.posX,
                player.posY,
                player.posZ,
                player.rotationYaw,
                player.rotationPitch
        );
    }

    public int getDimension() {
        return dimension;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }
}
