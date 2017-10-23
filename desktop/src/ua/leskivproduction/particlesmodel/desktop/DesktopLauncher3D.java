package ua.leskivproduction.particlesmodel.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import ua.leskivproduction.particlesmodel.ParticlesModel3D;

import java.awt.*;

public class DesktopLauncher3D {
    public static void main (String[] arg) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();

        cfg.title = "Particles Model 3D";
        cfg.useGL30 = true;

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

//        cfg.width = screenSize.width;
//        cfg.height = screenSize.height;
//        cfg.fullscreen=true;
//
        cfg.width = screenSize.width/2;
        cfg.height = screenSize.height/2;

        new LwjglApplication(new ParticlesModel3D(), cfg);
    }
}
