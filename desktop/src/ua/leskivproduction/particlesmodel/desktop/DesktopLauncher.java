package ua.leskivproduction.particlesmodel.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import ua.leskivproduction.particlesmodel.ParticlesModel;

import java.awt.*;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();

		cfg.title = "Particles Model";
        cfg.useGL30 = true;

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

//        cfg.width = 1920;
//        cfg.width = 1920;
        cfg.width = screenSize.width;
        cfg.height = screenSize.height;
        cfg.fullscreen=true;

		new LwjglApplication(new ParticlesModel(), cfg);
	}
}
