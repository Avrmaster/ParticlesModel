package ua.leskivproduction.particlesmodel.Model;

import com.sun.istack.internal.NotNull;

public class ModelEvent implements Comparable<ModelEvent> {
    public final double time;
    public final Particle2D a, b; // particles involved in event
    public final int initCountA, initCountB; // collision counts for a and b
    public final CollisionTypes type;

    public enum CollisionTypes {
        PARTICLES,
        HORIZONTAL_WALL,
        VERTICAL_WALL,
        DEPTH_WALL
    }

    public ModelEvent(double t, @NotNull Particle2D a, CollisionTypes type) {
        this.time = t;
        this.a = a;
        this.initCountA = a.getCollisionsCount();
        if (type != CollisionTypes.PARTICLES)
            this.type = type;
        else
            throw new IllegalArgumentException("You cannot specify particles collision event with only 1 particle! ");

        this.b = null;
        this.initCountB = -1;
    }

    public ModelEvent(double t, @NotNull Particle2D a, @NotNull Particle2D b) {
        this.time = t;
        this.a = a;
        this.b = b;
        this.initCountA = a.getCollisionsCount();
        this.initCountB = b.getCollisionsCount();
        this.type = CollisionTypes.PARTICLES;
    }

    public boolean isValid() {
        return (initCountA == a.getCollisionsCount() && (b == null || initCountB == b.getCollisionsCount()));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (type == CollisionTypes.PARTICLES) {
            builder.append("Particles collision between ");
            builder.append(a);
            builder.append( " and ");
            builder.append(b);
        } else {
            builder.append(type);
            builder.append(" collision of ");
            builder.append(a);
        }
        builder.append(" at ");
        builder.append(time);
        return builder.toString();
    }

    @Override
    public int compareTo(ModelEvent that) {
        double dt = this.time - that.time;
        if (dt < 0)
            return -1;
        if (dt > 0)
            return 1;
        return 0;
    }
}
