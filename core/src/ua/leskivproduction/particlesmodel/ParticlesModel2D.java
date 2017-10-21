package ua.leskivproduction.particlesmodel;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.sun.org.apache.xpath.internal.operations.Or;
import ua.leskivproduction.particlesmodel.Model.ModelEvent;
import ua.leskivproduction.particlesmodel.Model.Particle2D;
import ua.leskivproduction.particlesmodel.utils.MinQueue;


import java.text.DecimalFormat;

import static ua.leskivproduction.particlesmodel.Model.ModelEvent.CollisionTypes.*;


public class ParticlesModel2D extends ApplicationAdapter {

//    protected final static boolean DEBUG = true;
    private final static boolean DEBUG = false;

    private ShapeRenderer shapeRenderer;
    private Camera mainCam;
    private SpriteBatch batch;
    private BitmapFont font;

    private final static int PARTICLES_COUNT = 500;


    private final MinQueue<ModelEvent> eventMinQueue = new MinQueue<ModelEvent>((int)(PARTICLES_COUNT*Math.log(PARTICLES_COUNT)));
    private Particle2D[] particles;
    private float timeWarp = 1f;

	@Override
	public void create () {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        mainCam = newOrthCamera();

        font = new BitmapFont(Gdx.files.internal("core/assets/font.fnt"),
                Gdx.files.internal("core/assets/font.png"), false);
        font.setColor(Color.WHITE);

        final int screenWidth = Gdx.graphics.getWidth();
        final int screenHeight = Gdx.graphics.getHeight();

        spawnParticles(screenWidth, screenHeight);
	}

	protected void spawnParticles(final int screenWidth, final int screenHeight) {
        particles = new Particle2D[PARTICLES_COUNT];
	    for (int i = 0; i < particles.length; i++) {
            particles[i] = new Particle2D(i, PARTICLES_COUNT, screenWidth, screenHeight);
        }
        for (int i = 0; i < particles.length; i++) {
            enqueueEventsFor(particles[i]);
        }
    }

    private double modelTime;
    private void enqueueEventsFor(Particle2D p) {
	    if (p == null)
	        return;
        enqueueEvent(new ModelEvent(modelTime +p.timeToHitHorizontalWall(), p, HORIZONTAL_WALL));
        enqueueEvent(new ModelEvent(modelTime +p.timeToHitVerticalWall(), p, VERTICAL_WALL));

        for (Particle2D b : particles) {
            enqueueEvent(new ModelEvent(modelTime +p.timeToHit(b), p, b));
        }
    }

    private void enqueueEvent(ModelEvent e) {
	    if (e.time != Double.POSITIVE_INFINITY) {
            eventMinQueue.add(e);
	    }
    }

	@Override
	public void render () {
        handleInput();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        drawBackground();

        Camera currentCam = mainCam;
        if (Gdx.input.isKeyPressed(Input.Keys.G) && particles.length > 0) {
            currentCam = obtainFollowCamera(particles[0]);
        }

        float deltaTime = Math.min(0.3f, Gdx.graphics.getDeltaTime() * timeWarp);

        if (currentCam instanceof OrthographicCamera) {
            currentCam.update();
            batch.setProjectionMatrix(currentCam.combined);
            shapeRenderer.setProjectionMatrix(currentCam.combined);


            if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
                deltaTime /= 20;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                deltaTime *= 20;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.P)) {
                deltaTime = 0;
            }
        }

        modelTime += deltaTime;

        if (batch != null) {
            batch.begin();
            font.draw(batch, new DecimalFormat("##.#").format(modelTime), 0, (float)(Gdx.graphics.getHeight()*0.015));
            batch.end();
        }

        for (Particle2D p : particles) {
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

            Particle2D a = event.a;
            Particle2D b = event.b;

            double rollBackTime = event.time-modelTime;
            a.updatePosition(rollBackTime);
            if (b != null)
                b.updatePosition(rollBackTime);

            processEvent(event);

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
        drawOutline();
	}

	private OrthographicCamera obtainFollowCamera(Particle2D tracked) {
        OrthographicCamera trackCam = newOrthCamera();
        if (tracked != null) {
            trackCam.translate(tracked.getX()-Gdx.graphics.getWidth()/2, tracked.getY()-Gdx.graphics.getHeight()/2);
            trackCam.zoom = 20*tracked.getRadius()/(Gdx.graphics.getWidth());
        }
        return trackCam;
    }

    protected void processEvent(ModelEvent event) {
	    Particle2D a = event.a;
	    Particle2D b = event.b;
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
            default:
                break;
        }
    }

    protected void drawBackground() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.2f);
        shapeRenderer.rect(-25000, -25000, 50000, 50000);
        shapeRenderer.end();
    }

    protected void drawOutline() {
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();

	    shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        Color c1 = Color.valueOf("#77A9C2");
        Color c2 = Color.RED;
        Color c3 = Color.ORANGE;
        Color c4 = Color.GREEN;

        shapeRenderer.line(0, 0, screenWidth, 0, c1, c2);
        shapeRenderer.line(screenWidth, 0, screenWidth, screenHeight, c2, c3);
        shapeRenderer.line(screenWidth, screenHeight, 0, screenHeight, c3, c4);
        shapeRenderer.line(0, screenHeight,  0, 0, c4, c1);

        shapeRenderer.end();
    }

    protected void drawParticles() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

//        shapeRenderer.setColor(Color.BLUE);

        int counter = 0;
        for (Particle2D p : particles) {
            float x = p.getX();
            float y = p.getY();
            float radius =  p.getRadius();

            shapeRenderer.setColor(0, (float)p.getNumber()/particles.length, 1-(float)p.getNumber()/particles.length, 1);
            shapeRenderer.circle(x, y, radius);

            if (DEBUG) {
                batch.begin();
                font.draw(batch, ""+(counter++)+"", x+radius, y);
                batch.end();
            }
        }
        shapeRenderer.end();
	}

    protected void handleInput() {
	    int dx = Gdx.graphics.getWidth()/100;
	    int dy = Gdx.graphics.getHeight()/100;

        if (mainCam instanceof OrthographicCamera &&
                Gdx.input.isKeyPressed(Input.Keys.A)) {
            ((OrthographicCamera)mainCam).zoom += 0.02;
        }
        if (mainCam instanceof OrthographicCamera &&
                Gdx.input.isKeyPressed(Input.Keys.Q)) {
            ((OrthographicCamera)mainCam).zoom -= 0.02;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            mainCam.translate(-dx, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            mainCam.translate(dx, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            mainCam.translate(0, -dy, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            mainCam.translate(0, dy, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            mainCam.rotate(-0.5f, 0, 0, 1);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.E)) {
            mainCam.rotate(0.5f, 0, 0, 1);
        }

        if (Gdx.input.isKeyPressed(Input.Keys.R)) {
            mainCam = newOrthCamera();
        }

    }

    private OrthographicCamera newOrthCamera() {
        OrthographicCamera cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(cam.viewportWidth/2, cam.viewportHeight/2, 0);
        cam.zoom *= 1.005; //to see outline
        return cam;
    }

	@Override
	public void dispose () {
		batch.dispose();
		shapeRenderer.dispose();
		font.dispose();
	}
}
