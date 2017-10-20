package ua.leskivproduction.particlesmodel.Model;

import static java.lang.Double.POSITIVE_INFINITY;
import static java.lang.Math.sqrt;

public class Particle2D {

    private final static double WALL_BOUNCE_COEF = 0.98;
    private final static double MAX_ORTH_SPEED = 0.3;

    private final int number;
    private double x, y;
    private double vx, vy;
    private final double radius;
    private int collisionsCount;

    private final int BORDER_X;
    private final int BORDER_Y;

    public Particle2D(final int partN, final int PARTICLES_COUNT, final int MAX_X, final int MAX_Y) {
        if (MAX_X <= 0 || MAX_Y <= 0)
            throw new NullPointerException("Initialize BORDER_X and BORDER_Y with positive integers first!");

        BORDER_X = MAX_X;
        BORDER_Y = MAX_Y;

        this.number = partN;

        this.x = BORDER_X * (Math.random()*0.4+0.3);
        this.y = BORDER_Y * (Math.random()*0.4+0.3);
//        this.x = MAX_X*partN/PARTICLES_COUNT;
//        this.y = MAX_Y*partN/PARTICLES_COUNT;

        this.vx = BORDER_X * (0.3*MAX_ORTH_SPEED+0.7*MAX_ORTH_SPEED*Math.random()*(Math.random()<0.5? -1 : 1));
        this.vy = BORDER_Y * (0.3*MAX_ORTH_SPEED+0.7*MAX_ORTH_SPEED*Math.random()*(Math.random()<0.5? -1 : 1));
        this.radius = BORDER_X * (0.001+Math.min(0.05, 2*Math.random()/PARTICLES_COUNT));
        constrainSpeed();
        constrainPosition();
    }


    private void constrainSpeed() {
        double maxSpeed = MAX_ORTH_SPEED * BORDER_X;

        if (Math.abs(vx) > maxSpeed)
            vx = Math.signum(vx)*maxSpeed;
        if (Math.abs(vy) > maxSpeed)
            vy = Math.signum(vy)*maxSpeed;
    }

    public boolean constrainPosition() {
        boolean somethingChanged = false;

        if (x < radius) {
            x = radius;
            somethingChanged = true;
        }
        if (x > BORDER_X - radius) {
            x = BORDER_X - radius;
            somethingChanged = true;
        }
        if (y < radius) {
            y = radius;
            somethingChanged = true;
        }
        if (y > BORDER_Y - radius) {
            y = BORDER_Y - radius;
            somethingChanged = true;
        }

        if (somethingChanged)
            this.collisionsCount++;
        return somethingChanged;
    }

    public void updatePosition(double deltaTime) {
        x += vx*deltaTime;
        y += vy*deltaTime;
    }


    public double timeToHit(Particle2D that) {
        if (this == that)
            return POSITIVE_INFINITY;

        //position distances
        double dx = that.x - this.x,
                dy = that.y - this.y;

        //velocities towards each other
        double dvx = that.vx - this.vx,
                dvy = that.vy - this.vy;

        double dvdr = dx*dvx + dy*dvy; //delta velocity/distance
        if (dvdr > 0) //they're not even moving towards each other
            return POSITIVE_INFINITY;

        double dvdv = dvx*dvx + dvy*dvy;
        double drdr = dx*dx + dy*dy;

        double sigma = this.radius + that.radius;

        double d = (dvdr*dvdr) - dvdv * (drdr - sigma*sigma);
        if (d < 0) return POSITIVE_INFINITY;
        return -(dvdr + Math.sqrt(d)) / dvdv;
    }

    public double timeToHitHorizontalWall() {
        return timeToHitWall(y, vy, BORDER_Y);
    }

    public double timeToHitVerticalWall() {
        return timeToHitWall(x, vx, BORDER_X);
    }

    /**
     * @param orthPos - projection of a position on a line perpendicular to a wall
     * @param orthSpeed - projection of speed on a line perpendicular to a wall
     * @param orthLength - the length of the perpendicular
     * @return time to hit wall depending on direction
     */
    private double timeToHitWall(double orthPos, double orthSpeed, double orthLength) {
        if (orthSpeed == 0)
            return POSITIVE_INFINITY;
        if (orthSpeed > 0)
            return (orthLength - radius - orthPos) / orthSpeed;
        else
            return (-radius + orthPos) / -orthSpeed;
    }

    public void bounceOff(Particle2D that) {
        //we assume, that particle's mass is quadratically proportional to it's radius
        double thisM = this.radius*this.radius;
        double thatM = that.radius*that.radius;

        double dx = that.x - this.x, dy = that.y - this.y;
        double dvx = that.vx - this.vx, dvy = that.vy - this.vy;

        double dvdr = dx*dvx + dy*dvy;
        double dist = this.radius + that.radius;
        double J = 2 * thisM * thatM * dvdr / ((thisM + thatM) * dist);

        double Jx = J * dx / dist;
        double Jy = J * dy / dist;

        this.vx += Jx / thisM;
        this.vy += Jy / thisM;
        that.vx -= Jx / thatM;
        that.vy -= Jy / thatM;

        this.constrainSpeed();
        that.constrainSpeed();

        this.collisionsCount++;
        that.collisionsCount++;
    }

    public void bounceOffVerticalWall() {
        vx *= -WALL_BOUNCE_COEF;
        collisionsCount++;
    }

    public void bounceOffHorizontalWall() {
        vy *= -WALL_BOUNCE_COEF;
        collisionsCount++;
    }

    public float getX() {
        return (float) x;
    }

    public float getY() {
        return (float)y;
    }

    public float getRadius() {
        return (float)radius;
    }

    public int getCollisionsCount() {
        return collisionsCount;
    }

    public int getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return "Particle2D №"+number;
    }
}
