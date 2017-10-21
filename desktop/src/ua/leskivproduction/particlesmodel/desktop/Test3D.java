package ua.leskivproduction.particlesmodel.desktop;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

import java.util.LinkedList;
import java.util.List;

public class Test3D extends ApplicationAdapter {

    CameraInputController camController;
    PerspectiveCamera camera;
    ModelBatch modelBatch;
    Model model;
    List<ModelInstance> modelInstances = new LinkedList<ModelInstance>();

    @Override
    public void create() {
        modelBatch = new ModelBatch();

        camera = new PerspectiveCamera(75, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(5f, 5f, 5f);
        camera.lookAt(0, 0, 0);
        camera.near = 0.1f;
        camera.far = 300.0f;
        camera.update();

        camController = new CameraInputController(camera);
        camController.pinchZoomFactor = 10000;
        Gdx.input.setInputProcessor(camController);

//        ModelLoader<?> loader = new ObjLoader();
//        model = loader.loadModel(Gdx.files.internal("core/assets/sphere.obj"), new TextureProvider.FileTextureProvider().load(""));

        final Texture texture = new Texture(Gdx.files.internal("core/assets/particleTexture.jpg"));
        final Material material = new Material(TextureAttribute.createDiffuse(texture), ColorAttribute.createSpecular(1, 1, 1, 1),
                FloatAttribute.createShininess(8f));
        final long attributes = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates;

        ModelBuilder builder = new ModelBuilder();

        model = builder.createSphere(6, 6, 6, 24, 24, material, attributes);
        modelInstances.add(new ModelInstance(model));
        modelInstances.add(new ModelInstance(model));
        modelInstances.add(new ModelInstance(model));
//        modelInstances.add(new ModelInstance(builder.createBox(10, 10, 10, material, attributes)));
        int counter = 0;
        for (ModelInstance mi : modelInstances) {
            mi.transform.setTranslation(2*counter++, counter++, 0);
        }
    }

    @Override
    public void render() {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT|GL20.GL_DEPTH_BUFFER_BIT);

//        camera.rotate(Gdx.graphics.getDeltaTime(), 0, 1, 0);
        camera.update();

        modelBatch.begin(camera);
        modelBatch.render(modelInstances);
        modelBatch.end();
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        model.dispose();
    }
}
