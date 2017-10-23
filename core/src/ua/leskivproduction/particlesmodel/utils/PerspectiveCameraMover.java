package ua.leskivproduction.particlesmodel.utils;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;

public class PerspectiveCameraMover {
    private final PerspectiveCamera camera;
    private float cameraTime;
    private MinQueue<CameraEvent> cameraEventMinQueue = new MinQueue<CameraEvent>();

    public PerspectiveCameraMover(PerspectiveCamera camera) {
        this.camera = camera;
    }


    public void addCameraEvent(float duration, Vector3 goalPos) {
        addCameraEvent(duration, goalPos, null);
    }

    public void addCameraEvent(float duration, Vector3 goalPos, Vector3 goalDir) {
        cameraEventMinQueue.add(new CameraEvent(duration, goalPos, goalDir));
    }

    public void addCameraEvent(float duration, float zoom) {
        float dx = camera.direction.x*zoom;
        float dy = camera.direction.y*zoom;
        float dz = camera.direction.z*zoom;
        cameraEventMinQueue.add(new CameraEvent(duration, copy(camera.position).add(dx, dy, dz), camera.direction));
    }

    public void update(float deltaTime) {
        cameraTime += deltaTime;

        if (cameraEventMinQueue.size() > 0) {
            CameraEvent nextEvent = cameraEventMinQueue.getMin();
            if (!nextEvent.started) {
                nextEvent.start(cameraTime, camera.position, camera.direction);
            }

            float eventProgress = (cameraTime-nextEvent.startTime)/(nextEvent.duration);

            Vector3 newPos = copy(nextEvent.initPosition).scl(1f-eventProgress).
                    add(copy(nextEvent.goalPosition).scl(eventProgress));
            camera.position.set(newPos);

            Vector3 newDir = copy(nextEvent.initDirection).scl(1f-eventProgress).
                    add(copy(nextEvent.goalDirection).scl(eventProgress));
            camera.direction.set(newDir);


            if (eventProgress >= 1) {
                cameraEventMinQueue.removeMin();
            }
        }

    }

    private Vector3 copy(Vector3 src) {
        if (src == null)
            return null;
        return new Vector3(src.x, src.y, src.z);
    }

    private int eventsCounter;
    private class CameraEvent implements Comparable<CameraEvent> {
        private final int num;
        float duration;
        float startTime;
        Vector3 initPosition, initDirection;
        Vector3 goalPosition, goalDirection;

        CameraEvent(float time, Vector3 goalPosition, Vector3 goalDirection) {
            num = eventsCounter++;
            this.duration = time;
            this.goalPosition = copy(goalPosition);
            this.goalDirection = copy(goalDirection);
        }

        boolean started = false;
        void start(float startTime, Vector3 initPosition, Vector3 initDirection) {
            this.startTime = startTime;
            this.initPosition = copy(initPosition);
            this.initDirection = copy(initDirection);
            if (this.goalPosition == null)
                this.goalPosition = copy(initPosition);
            if (this.goalDirection == null)
                this.goalDirection = copy(initDirection);
            started = true;
        }

        @Override
        public int compareTo(CameraEvent o) {
            return this.num - o.num;
        }
    }

}
