package ua.leskivproduction.particlesmodel;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import ua.leskivproduction.particlesmodel.Model.ModelEvent;
import ua.leskivproduction.particlesmodel.Model.Particle;
import ua.leskivproduction.particlesmodel.utils.MinQueue;


public class ParticlesModel extends ApplicationAdapter {

//    private final static boolean DEBUG = true;
    private final static boolean DEBUG = false;

	private ShapeRenderer shapeRenderer;
	private OrthographicCamera mainCam;
	private OrthographicCamera epicCam;
	private SpriteBatch batch;
    private BitmapFont font;

    private final static int PARTICLES_COUNT = 300;
    private final static int MAX_COMPUTATIONS_PER_FRAME = 100;


    private final MinQueue<ModelEvent> eventMinQueue = new MinQueue<ModelEvent>((int)(PARTICLES_COUNT*Math.log(PARTICLES_COUNT)));
    private final Particle[] particles = new Particle[PARTICLES_COUNT];

    private float timeWarp = 3;

	@Override
	public void create () {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        mainCam = newCamera();
        epicCam = newCamera();

        font = new BitmapFont(Gdx.files.internal("core/assets/font.fnt"),
                Gdx.files.internal("core/assets/font.png"), false);
        font.setColor(Color.WHITE);

        Particle.setScreenWidth(Gdx.graphics.getWidth());
        Particle.setScreenHeight(Gdx.graphics.getHeight());

        for (int i = 0; i < particles.length; i++) {
            particles[i] = new Particle(i, PARTICLES_COUNT);
            enqueueEventsFor(particles[i]);
            System.out.println("Loading.. "+((i+1)*100/PARTICLES_COUNT));
        }
	}


    private double time;
	private void enqueueEventsFor(Particle p) {
	    if (p == null)
	        return;
	    for (Particle b : particles) {
            if (b != null) {
                enqueueEvent(new ModelEvent(time+p.timeToHit(b), b, p));
            }
        }
        enqueueEvent(new ModelEvent(time+p.timeToHitHorizontalWall(), p, null));
        enqueueEvent(new ModelEvent(time+p.timeToHitVerticalWall(), null, p));
    }

    private void enqueueEvent(ModelEvent e) {
	    if (e.getTime() != Double.POSITIVE_INFINITY) {
	        eventMinQueue.add(e);
        }
    }

	@Override
	public void render () {
        handleInput();

	    Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glDisable(GL20.GL_BLEND);




        OrthographicCamera currentCam = mainCam;
        if (Gdx.input.isKeyPressed(Input.Keys.G)) {
            Particle tracked = particles[0];
            currentCam = newCamera();
            currentCam.translate(tracked.getX()-Gdx.graphics.getWidth()/2, tracked.getY()-Gdx.graphics.getHeight()/2);
            currentCam.zoom = 0.2f;
        }

        currentCam.update();
        batch.setProjectionMatrix(currentCam.combined);
        shapeRenderer.setProjectionMatrix(currentCam.combined);

//        float deltaTime = Gdx.graphics.getDeltaTime()*timeWarp;
        float deltaTime = 0.001f;

        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            deltaTime /= 20;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.P)) {
            deltaTime = 0;
        }

        time += deltaTime;

        int performedCnt = 0;
        System.out.println("Queue size: "+eventMinQueue.size());
        while (eventMinQueue.size() > 0 && ++performedCnt < MAX_COMPUTATIONS_PER_FRAME) {
            ModelEvent event = eventMinQueue.getMin();

            if (event.getTime() > time)
                break;

            if (event.isValid()) {
                Particle a = event.getA();
                Particle b = event.getB();

                if (a != null)
                    a.update(event.getTime()-time);
                if (b != null)
                    b.update(event.getTime()-time);

                if (a != null && b == null) {
                    a.bounceOffHorizontalWall();
                } else if (b != null && a == null) {
                    b.bounceOffVerticalWall();
                } else if (a != null){ //&& b != null
                    a.bounceOff(b);
                }

                if (a != null) {
                    a.update(time-event.getTime());
                }
                if (b != null) {
                    b.update(time-event.getTime());
                }

                enqueueEventsFor(a);
                enqueueEventsFor(b);
            }

            eventMinQueue.removeMin();
        }

        for (Particle p : particles)
            p.update(deltaTime);

        drawParticles();
        drawOutline();
	}

	private void drawOutline() {
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

    private void drawParticles() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(Color.BLUE);

        int counter = 0;
        for (Particle p : particles) {
            float x = p.getX();
            float y = p.getY();
            float radius =  p.getRadius();

            shapeRenderer.circle(x, y, radius);

            if (DEBUG) {
                batch.begin();
                font.draw(batch, ""+(counter++)+"", x+radius, y);
                batch.end();
            }
        }
        shapeRenderer.end();
	}

    private void handleInput() {
	    int dx = Gdx.graphics.getWidth()/100;
	    int dy = Gdx.graphics.getHeight()/100;

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            mainCam.zoom += 0.02;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
            mainCam.zoom -= 0.02;
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
            mainCam = newCamera();
        }

    }

    private OrthographicCamera newCamera() {
        OrthographicCamera cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(cam.viewportWidth/2, cam.viewportHeight/2, 0);
        cam.zoom *= 1.005; //to see outline
        return cam;
    }

	@Override
	public void dispose () {
		batch.dispose();
	}
}
