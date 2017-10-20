package ua.leskivproduction.particlesmodel.Model;

public class ModelEvent implements Comparable<ModelEvent> {
    private double t;
    private Particle a, b; // particles involved in event
    private int countA, countB; // collision counts for a and b

    public ModelEvent(double t, Particle a, Particle b) {
        this.a = a;
        this.b = b;
        if (a != null)
            this.countA = a.getCount();
        if (b != null)
            this.countB = b.getCount();
        this.t = t;
    }

    public boolean isValid() {
        boolean isValid = true;

        if (a != null) {
            if (countA != a.getCount())
                isValid = false;
        }
        if (b != null) {
            if (countB != b.getCount())
                isValid = false;
        }

        return isValid;
    }

    public double getTime() {
        return t;
    }

    public Particle getA() {
        return a;
    }

    public Particle getB() {
        return b;
    }

    @Override
    public String toString() {
        if (a == null && b == null) {
            return "reset event at "+t;
        }
        if (a == null || b == null) {
            return "bounce wall event at "+t;
        }
        return "particle bounce event at "+t;
    }

    @Override
    public int compareTo(ModelEvent that) {
        double dt = this.t - that.t;
        if (dt < 0)
            return -1;
        if (dt > 0)
            return 1;
        return 0;
    }
}
