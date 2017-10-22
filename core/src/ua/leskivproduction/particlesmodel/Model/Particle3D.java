package ua.leskivproduction.particlesmodel.Model;

import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import static java.lang.Double.POSITIVE_INFINITY;

public class Particle3D extends Particle2D {

    final private ModelInstance modelInstance;
    protected double z;
    protected double vz;

    protected final int BORDER_Z;

    public Particle3D(int partN, int PARTICLES_COUNT, int MAX_X, int MAX_Y, int MAX_Z,
                      ModelBuilder builder, Material material, long modelAttributes) {
        super(partN, PARTICLES_COUNT, MAX_X, MAX_Y);

        radius *= 7;

        if (MAX_Z <= 0)
            throw new IllegalArgumentException("BORDER_Z must be positive integer!");

        BORDER_Z = MAX_Z;

        this.z = BORDER_Z * (Math.random()*0.4+0.3);
        this.vz = BORDER_Z * (0.3*MAX_ORTH_SPEED+0.7*MAX_ORTH_SPEED*Math.random()*(Math.random()<0.5? -1 : 1));

//        modelInstance = new ModelInstance(builder.createSphere((float)radius, (float)radius, (float)radius,
//                24, 24, material, modelAttributes),
//                (float)x, (float)y, (float)z);
        modelInstance = new ModelInstance(builder.createSphere(
                2*(float)radius, 2*(float)radius, 2*(float)radius,
                24, 24, material, modelAttributes),
                (float)x,(float)y, (float)z);
    }

    @Override
    public double timeToHit(Particle2D that2) {
        Particle3D that = (Particle3D)that2;

        if (this == that)
            return POSITIVE_INFINITY;

        //position distances
        double dx = that.x - this.x,
                dy = that.y - this.y,
                dz = that.z - this.z;

        //velocities towards each other
        double dvx = that.vx - this.vx,
                dvy = that.vy - this.vy,
                dvz = that.vz - this.vz;

        double dvdr = dx*dvx + dy*dvy + dz*dvz; //delta velocity/distance
        if (dvdr > 0) //they're not even moving towards each other
            return POSITIVE_INFINITY;

        double dvdv = dvx*dvx + dvy*dvy + dvz*dvz;
        double drdr = dx*dx + dy*dy + dz*dz;

        double sigma = this.radius + that.radius;

        double d = (dvdr*dvdr) - dvdv * (drdr - sigma*sigma);
        if (d < 0) return POSITIVE_INFINITY;
        return -(dvdr + Math.sqrt(d)) / dvdv;
    }

    @Override
    public void bounceOff(Particle2D that2) {
        Particle3D that = (Particle3D)that2;

        //we assume, that particle's mass is proportional radius^3
        double thisM = Math.pow(this.radius*10, 3);
        double thatM = Math.pow(that.radius*10, 3);

        double dx = that.x - this.x, dy = that.y - this.y, dz = that.z - this.z;
        double dvx = that.vx - this.vx, dvy = that.vy - this.vy, dvz = that.vz - this.vz;

        double dvdr = dx*dvx + dy*dvy + dz*dvz;
        double dist = this.radius + that.radius;
        double J = 2 * thisM * thatM * dvdr / ((thisM + thatM) * dist);

        double Jx = J * dx / dist;
        double Jy = J * dy / dist;
        double Jz = J * dz / dist;

        this.vx += Jx / thisM;
        this.vy += Jy / thisM;
        this.vz += Jz / thisM;
        that.vx -= Jx / thatM;
        that.vy -= Jy / thatM;
        that.vz -= Jz / thatM;

        this.constrainSpeed();
        that.constrainSpeed();

        this.collisionsCount++;
        that.collisionsCount++;
    }

    @Override
    public void updatePosition(double deltaTime) {
        super.updatePosition(deltaTime);
        z += vz*deltaTime;
        modelInstance.transform.setTranslation((float)x, (float)y, (float)z);
    }

    @Override
    protected void constrainSpeed() {
        super.constrainSpeed();

        double maxSpeed = MAX_ORTH_SPEED * BORDER_X;

        if (Math.abs(vz) > maxSpeed)
            vz = Math.signum(vz)*maxSpeed;
    }

    @Override
    public boolean constrainPosition() {
        boolean smthChanged = false;
        if (z < radius) {
            z = radius;
            smthChanged = true;
        }
        if (z > BORDER_Z - radius) {
            z = BORDER_Z - radius;
            smthChanged = true;
        }

        if (smthChanged)
            this.collisionsCount++;
        return super.constrainPosition() || smthChanged;
    }

    public double timeToHitDepthWall() {
        return timeToHitWall(z, vz, BORDER_Z);
    }

    public void bounceOffDepthWall() {
        vz *= - WALL_BOUNCE_COEF;
        collisionsCount++;
    }

    public float getZ() {
        return (float)z;
    }

    public ModelInstance getModelInstance() {
        return modelInstance;
    }

}
