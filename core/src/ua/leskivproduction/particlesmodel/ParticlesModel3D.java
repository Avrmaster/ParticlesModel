package ua.leskivproduction.particlesmodel;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import ua.leskivproduction.particlesmodel.Model.ModelEvent;
import ua.leskivproduction.particlesmodel.Model.Particle2D;
import ua.leskivproduction.particlesmodel.Model.Particle3D;
import ua.leskivproduction.particlesmodel.utils.MinQueue;
import ua.leskivproduction.particlesmodel.utils.PerspectiveCameraMover;

import static ua.leskivproduction.particlesmodel.Model.ModelEvent.CollisionTypes.DEPTH_WALL;
import static ua.leskivproduction.particlesmodel.Model.ModelEvent.CollisionTypes.HORIZONTAL_WALL;
import static ua.leskivproduction.particlesmodel.Model.ModelEvent.CollisionTypes.VERTICAL_WALL;

public class ParticlesModel3D extends ApplicationAdapter {

    private CameraInputController camController;
    private PerspectiveCameraMover cameraMover;
    private PerspectiveCamera mainCam;
    private ModelBatch modelBatch;
    private Environment environment;

    private final static int PARTICLES_COUNT = 500;
    private final MinQueue<ModelEvent> eventMinQueue = new MinQueue<ModelEvent>((int)(PARTICLES_COUNT*Math.log(PARTICLES_COUNT)));
    private Particle3D[] particles;

    private float timeWarp = 0.2f;

    private Music backgroundMusic;

    @Override
    public void create() {
        modelBatch = new ModelBatch();

        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("core/assets/earthsong.mp3"));

        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        int screenDepth = Math.max(screenWidth, screenHeight);

        environment = new Environment();
        float intencity = 0.01f;
        Color color = new Color(1.0f*intencity, 0.9f*intencity, 0.7f*intencity, 0.0f);
        environment.add(new DirectionalLight().set(color, new Vector3(1, 1, 1)));
        environment.add(new DirectionalLight().set(color, new Vector3(-1, -1, -1)));
        environment.add(new DirectionalLight().set(color, new Vector3(1, -1, 1)));
        environment.add(new DirectionalLight().set(color, new Vector3(-1, 1, -1)));

        mainCam = newPerspectiveCamera();
        cameraMover = new PerspectiveCameraMover(mainCam);
        cameraMover.addCameraEvent(10, 900);
        cameraMover.addCameraEvent(5, new Vector3(screenWidth, screenHeight, 0));
//        cameraMover.addCameraEvent(4, 450);
        cameraMover.addCameraEvent(5,
                new Vector3(screenWidth/3, screenHeight/3, screenDepth/3),
                new Vector3(0,0,0));
        cameraMover.addCameraEvent(2,
                new Vector3(-screenWidth/3, screenHeight/3, screenDepth/3));
        cameraMover.addCameraEvent(9, 0);

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
        for (Particle3D p : particles) {
            enqueueEventsFor(p);
        }
    }

    private double modelTime;
    private void enqueueEventsFor(Particle3D p) {
        if (p == null)
            return;
        enqueueEvent(new ModelEvent(modelTime +p.timeToHitHorizontalWall(), p, HORIZONTAL_WALL));
        enqueueEvent(new ModelEvent(modelTime +p.timeToHitVerticalWall(), p, VERTICAL_WALL));
        enqueueEvent(new ModelEvent(modelTime +p.timeToHitDepthWall(), p, DEPTH_WALL));

        for (Particle3D b : particles) {
            enqueueEvent(new ModelEvent(modelTime +p.timeToHit(b), p, b));
        }
    }


    private void enqueueEvent(ModelEvent e) {
        if (e.time != Double.POSITIVE_INFINITY) {
            eventMinQueue.add(e);
        }
    }

    private float musicVolume = 0;

    @Override
    public void render() {
        handleInput();
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        drawBackground();

        float deltaTime = Gdx.graphics.getDeltaTime() * timeWarp;

        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            deltaTime /= 20;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.C)) {
            deltaTime *= 20;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.P)) {
            deltaTime = 0;
        }

        cameraMover.update(Gdx.graphics.getDeltaTime()); //without timeWarping
        mainCam.update();
        modelTime += deltaTime;

        for (Particle3D p : particles) {
            p.updatePosition(deltaTime);
            boolean smthChanged = p.constrainPosition();
            if (smthChanged)
                enqueueEventsFor(p);
        }

        drawParticles();

//        if (modelTime < 15*timeWarp) {
//            zoom(5);
//            rotate(0.05f);
//        }

        if (musicVolume < 1) {
            musicVolume += deltaTime;
            backgroundMusic.setVolume((float)Math.pow(musicVolume, 3));
        }
        if (!backgroundMusic.isPlaying()) {
            backgroundMusic.setLooping(true);
            backgroundMusic.play();
            backgroundMusic.setPosition(87.5f);
        }

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
        Gdx.gl.glClearColor(0, 0, 0, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT|GL20.GL_DEPTH_BUFFER_BIT);
    }

    private void drawParticles() {
        modelBatch.begin(mainCam);
        for (Particle2D p2 : particles) {
            modelBatch.render(((Particle3D)p2).getModelInstance(), environment);
        }
        modelBatch.end();
    }

    private void handleInput() {
        int dx = Gdx.graphics.getWidth()/100;
        int dy = Gdx.graphics.getHeight()/100;

        float speed = 10;

        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
            speed = 100;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.H)) {
            zoom(speed);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            zoom(-speed);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            rotate(-0.5f);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            rotate(0.5f);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
            float x = mainCam.position.x;
            float y = mainCam.position.y;
            float z = mainCam.position.z;
            mainCam.position.set(x, (float)(y*0.98), z);
        }

        if (Gdx.input.isKeyPressed(Input.Keys.E)) {
            float x = mainCam.position.x;
            float y = mainCam.position.y;
            float z = mainCam.position.z;
            mainCam.position.set(x, (float) (y * 1.02), z);
        }

        if (Gdx.input.isKeyPressed(Input.Keys.R)) {
            mainCam = newPerspectiveCamera();
        }
    }

    private void zoom(float amount) {
        float dzx = mainCam.direction.x*amount;
        float dzy = mainCam.direction.y*amount;
        float dzz = mainCam.direction.z*amount;
        mainCam.position.add(dzx, dzy, dzz);
    }

    private void rotate(float amount) {
        mainCam.rotate(mainCam.direction, amount);
    }

    private PerspectiveCamera newPerspectiveCamera() {
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        int screenDepth = Math.max(screenWidth, screenHeight);


        PerspectiveCamera cam;
        cam = new PerspectiveCamera(75, screenWidth, screenHeight);
        cam.position.set(screenDepth*1.2f, screenDepth*1.2f, screenDepth*1.2f);
        cam.lookAt(screenWidth/2, screenHeight/2, screenDepth/2);
        cam.near = 0.1f;
        cam.far = screenDepth*2;
        cam.update();
        camController = new CameraInputController(cam);
        Gdx.input.setInputProcessor(camController);
        return cam;
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
    }
}
