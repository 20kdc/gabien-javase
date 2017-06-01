/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package gabien;

import java.awt.*;
import java.awt.image.BufferedImage;

abstract class Main {

    /**
     * Use reflection to find and run the application.
     *
     * @param args
     */
    public static void main(String[] args) {
        // Firstly, try to make java2d use OpenGL if possible.
        // This means HW-accelerated drawing for not much in the way of price.
        boolean tryForceOpenGL = true;
        if (args.length > 0)
            if (args[0].equalsIgnoreCase("noForceOpenGL"))
                tryForceOpenGL = false;
        if (tryForceOpenGL) {
            System.setProperty("sun.java2d.opengl", "true");
            System.setProperty("sun.java2d.xrender", "true");
        }
        new Thread() {
            @Override
            public void run() {
                Font f = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
                BufferedImage scratch = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
                Graphics g = scratch.createGraphics();
                g.setFont(f);
                g.drawString("Flutter", 0, 0);
                System.err.println("FONT:Font has preloaded");
            }
        }.start();
        GaBIEn.internal = new GaBIEnImpl();
        try {
            Class.forName("gabienapp.Application").getDeclaredMethod("gabienmain").invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
            GaBIEn.ensureQuit();
        }
    }

}
