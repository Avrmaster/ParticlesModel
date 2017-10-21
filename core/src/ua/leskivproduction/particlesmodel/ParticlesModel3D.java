package ua.leskivproduction.particlesmodel;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import ua.leskivproduction.particlesmodel.Model.ModelEvent;
import ua.leskivproduction.particlesmodel.Model.Particle2D;
import ua.leskivproduction.particlesmodel.Model.Particle3D;
import ua.leskivproduction.particlesmodel.utils.MinQueue;

import java.awt.*;

import static ua.leskivproduction.particlesmodel.Model.ModelEvent.CollisionTypes.DEPTH_WALL;
import static ua.leskivproduction.particlesmodel.Model.ModelEvent.CollisionTypes.HORIZONTAL_WALL;
import static ua.leskivproduction.particlesmodel.Model.ModelEvent.CollisionTypes.VERTICAL_WALL;

public class ParticlesModel3D extends ApplicationAdapter {

    PerspectiveCamera mainCam;
    CameraInputController camController;
    ModelBatch modelBatch;

    private final static int PARTICLES_COUNT = 5000;
    private final MinQueue<ModelEvent> eventMinQueue = new MinQueue<ModelEvent>((int)(PARTICLES_COUNT*Math.log(PARTICLES_COUNT)));
    private Particle3D[] particles;

    private float timeWarp = 1f;


    @Override
    public void create() {
        modelBatch = new ModelBatch();

        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        int screenDepth = Math.max(screenWidth, screenHeight);

        mainCam = new PerspectiveCamera(75, screenWidth, screenHeight);
        mainCam.position.set(screenDepth*1.2f, screenDepth*1.2f, screenDepth*1.2f);
        mainCam.lookAt(screenWidth/2, screenHeight/2, screenDepth/2);
        mainCam.near = 0.1f;
        mainCam.far = screenDepth*2;
        mainCam.update();

        camController = new CameraInputController(mainCam);
        System.out.println(camController.pinchZoomFactor);

        Gdx.input.setInputProcessor(camController);

        spawnParticles(screenWidth, screenHeight, screenDepth);
    }

    private void spawnParticles(int screenWidth, int screenHeight, int screenDepth) {
        final ModelBuilder builder = new ModelBuilder();
        final Texture texture = new Texture(Gdx.files.internal("core/assets/particleTexture.jpg"));
        final Material material = new Material(TextureAttribute.createDiffuse(texture), ColorAttribute.createSpecular(1, 1, 1, 1),
                FloatAttribute.createShininess(8f));
        final long attributes = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates;

        particles = new Particle3D[PARTICLES_COUNT];
        for (int i = 0; i < particles.length; i++) {
            particles[i] = new Particle3D(i, PARTICLES_COUNT, screenWidth, screenHeight, screenDepth,
                    builder, material, attributes);
        }
        for (int i = 0; i < particles.length; i++) {
            enqueueEventsFor(particles[i]);
        }
    }

    private double modelTime;
    private void enqueueEventsFor(Particle3D p) {
        if (p == null)
            return;
        enqueueEvent(new ModelEvent(modelTime +p.timeToHitHorizontalWall(), p, HORIZONTAL_WALL));
        enqueueEvent(new ModelEvent(modelTime +p.timeToHitVerticalWall(), p, VERTICAL_WALL));
        enqueueEvent(new ModelEvent(modelTime +p.timeToHitDepthWall(), p, DEPTH_WALL));

//        for (Particle3D b : particles) {
//            enqueueEvent(new ModelEvent(modelTime +p.timeToHit(b), p, b));
//        }
    }


    private void enqueueEvent(ModelEvent e) {
        if (e.time != Double.POSITIVE_INFINITY) {
            eventMinQueue.add(e);
        }
    }

    @Override
    public void render() {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        drawBackground();

        float deltaTime = Gdx.graphics.getDeltaTime() * timeWarp;


        mainCam.update();
        modelTime += deltaTime;

        for (Particle3D p : particles) {
            p.updatePosition(deltaTime);
            boolean smthChanged = p.constrainPosition();
            if (smthChanged)
                enqueueEventsFor(p);
        }

        drawParticles();

        while (eventMinQueue.size() > 0) {
            ModelEvent event = eventMinQueue.getMin();

            if (!event.isValid()) {
                eventMinQueue.removeMin();
                continue;
            }
            if (event.time > modelTime)
                break;

            Particle3D a = (Particle3D)event.a;
            Particle3D b = (Particle3D)event.b;

            double rollBackTime = event.time-modelTime;
            a.updatePosition(rollBackTime);
            if (b != null)
                b.updatePosition(rollBackTime);

            switch (event.type) {
                case PARTICLES:
                    a.bounceOff(b);
                    break;
                case HORIZONTAL_WALL:
                    a.bounceOffHorizontalWall();
                    break;
                case VERTICAL_WALL:
                    a.bounceOffVerticalWall();
                    break;
                case DEPTH_WALL:
                    a.bounceOffDepthWall();
                    break;
                default:
                    break;
            }

            a.updatePosition(rollBackTime);
            a.constrainPosition();
            if (b != null) {
                b.updatePosition(rollBackTime);
                b.constrainPosition();
            }

            enqueueEventsFor(a);
            enqueueEventsFor(b);
            eventMinQueue.removeMin();
        }

    }

    private void drawBackground() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT|GL20.GL_DEPTH_BUFFER_BIT);
    }

    private void drawParticles() {
        modelBatch.begin(mainCam);
        for (Particle2D p2 : particles) {
            modelBatch.render(((Particle3D)p2).getModelInstance());
        }
        modelBatch.end();
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
    }
}
