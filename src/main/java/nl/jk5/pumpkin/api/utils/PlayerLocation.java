package nl.jk5.pumpkin.api.utils;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Objects;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.extent.Extent;

public final class PlayerLocation {

    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;

    public PlayerLocation(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
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

    public PlayerLocation setX(double x){
        return new PlayerLocation(x, y, z, yaw, pitch);
    }

    public PlayerLocation setY(double y){
        return new PlayerLocation(x, y, z, yaw, pitch);
    }

    public PlayerLocation setZ(double z){
        return new PlayerLocation(x, y, z, yaw, pitch);
    }

    public PlayerLocation setYaw(float yaw){
        return new PlayerLocation(x, y, z, yaw, pitch);
    }

    public PlayerLocation setPitch(float pitch){
        return new PlayerLocation(x, y, z, yaw, pitch);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlayerLocation that = (PlayerLocation) o;

        if (Double.compare(that.x, x) != 0) return false;
        if (Double.compare(that.y, y) != 0) return false;
        if (Double.compare(that.z, z) != 0) return false;
        if (Float.compare(that.yaw, yaw) != 0) return false;
        return Float.compare(that.pitch, pitch) == 0;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(z);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (yaw != +0.0f ? Float.floatToIntBits(yaw) : 0);
        result = 31 * result + (pitch != +0.0f ? Float.floatToIntBits(pitch) : 0);
        return result;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("x", x)
                .add("y", y)
                .add("z", z)
                .add("yaw", yaw)
                .add("pitch", pitch)
                .toString();
    }

    public Vector3i toVector3i(){
        return new Vector3i(x, y, z);
    }

    public Vector3d getRotation() {
        return new Vector3d(this.pitch, this.yaw, 0);
    }

    public <E extends Extent> Location<E> toLocation(E extent) {
        return new Location<E>(extent, this.x, this.y, this.z);
    }
}
