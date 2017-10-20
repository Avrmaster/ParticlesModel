package ua.leskivproduction.particlesmodel.Model;

import static java.lang.Double.POSITIVE_INFINITY;
import static java.lang.Math.abs;

public class Particle {

    private final static double MAX_SPEED = 0.5/7;

    public final int number;
    private double x, y;
    private double vx, vy;
    private final double radius;
    private int count;

    private static int SCREEN_WIDTH;
    private static int SCREEN_HEIGHT;

    public static void setScreenWidth(int screenWidth) {
        if (screenWidth > 0)
            SCREEN_WIDTH = screenWidth;
    }
    public static void setScreenHeight(int screenHeight) {
        if (screenHeight > 0)
            SCREEN_HEIGHT = screenHeight;
    }

    public Particle(final int partN, final int PARTICLES_COUNT) {
        if (SCREEN_WIDTH <= 0 || SCREEN_HEIGHT <= 0)
            throw new NullPointerException("Initialize SCREEN_WIDTH and SCREEN_HEIGHT with positive integers first!");

        this.number = partN;

        this.x = SCREEN_WIDTH*(Math.random()*0.4+0.3);
        this.y = SCREEN_HEIGHT*(Math.random()*0.4+0.3);
        this.vx = SCREEN_WIDTH*(Math.random()-0.5)/7;
        this.vy = SCREEN_HEIGHT*(Math.random()-0.5)/7;
        this.radius = SCREEN_WIDTH*Math.min(0.02, 10*Math.random()/PARTICLES_COUNT);
    }

    public void update(double deltaTime) {
        x += vx*deltaTime;
        y += vy*deltaTime;
    }

    public double timeToHit(Particle that) {
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
        if (vy == 0)
            return POSITIVE_INFINITY;

        if (y - radius <= 0 || y >= SCREEN_HEIGHT-radius)
            return 0;

        if (vy > 0) {
            return (SCREEN_HEIGHT - radius - y) / vy;
        } else {
            return (radius - y) / vy;
        }
    }

    public double timeToHitVerticalWall() {
        if (vx == 0)
            return POSITIVE_INFINITY;

        if (x - radius <= 0 || x >= SCREEN_WIDTH-radius)
            return 0;

        if (vx > 0) {
            return (SCREEN_WIDTH - radius - x) / vx;
        } else {
            return (radius - x) / vx;
        }
    }

    public void bounceOff(Particle that) {
        //we assume, that particle's mass is quadratically proportional to it's radius
        double thisM = Math.pow(this.radius, 2);
        double thatM = Math.pow(that.radius, 2);

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

        this.count++;
        that.count++;
    }

    private void constrainSpeed() {
        double maxSpeed = MAX_SPEED*SCREEN_WIDTH;

        if (Math.abs(vx) > maxSpeed)
            vx /= Math.abs(vx)/maxSpeed;
        if (Math.abs(vy) > maxSpeed)
            vy /= Math.abs(vy)/maxSpeed;
    }

    public void bounceOffVerticalWall() {
        vx *= -1;
        count++;
    }

    public void bounceOffHorizontalWall() {
        vy *= -1;
        count++;
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

    public int getCount() {
        return count;
    }

    @Override
    public String toString() {
        return "Particle №"+number;
    }
}
