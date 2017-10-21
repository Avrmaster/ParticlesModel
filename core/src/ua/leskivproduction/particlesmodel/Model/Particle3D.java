package ua.leskivproduction.particlesmodel.Model;

import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

public class Particle3D extends Particle2D {

    final private ModelInstance modelInstance;
    protected double z;
    protected double vz;

    protected final int BORDER_Z;

    public Particle3D(int partN, int PARTICLES_COUNT, int MAX_X, int MAX_Y, int MAX_Z,
                      ModelBuilder builder, Material material, long modelAttributes) {
        super(partN, PARTICLES_COUNT, MAX_X, MAX_Y);

        if (MAX_Z <= 0)
            throw new IllegalArgumentException("BORDER_Z must be positive integer!");

        BORDER_Z = MAX_Z;

        this.z = BORDER_Z * (Math.random()*0.4+0.3);
        this.vz = BORDER_Z * (0.3*MAX_ORTH_SPEED+0.7*MAX_ORTH_SPEED*Math.random()*(Math.random()<0.5? -1 : 1));

//        modelInstance = new ModelInstance(builder.createSphere((float)radius, (float)radius, (float)radius,
//                24, 24, material, modelAttributes),
//                (float)x, (float)y, (float)z);
        modelInstance = new ModelInstance(builder.createSphere((float)radius, (float)radius, (float)radius,
                24, 24, material, modelAttributes),
                (float)x,(float)y, (float)z);
    }

    //TODO
    @Override
    public double timeToHit(Particle2D that) {
        return super.timeToHit(that);
    }

    //TODO
    @Override
    public void bounceOff(Particle2D that) {
        super.bounceOff(that);
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
