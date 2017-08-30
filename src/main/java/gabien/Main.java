/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package gabien;

import gabien.ui.UILabel;

import java.awt.*;
import java.awt.image.BufferedImage;

abstract class Main {

    /**
     * Use reflection to find and run the application.
     */
    public static void main(String[] args) {
        boolean tryForceOpenGL = false;
        boolean useMT = false;
        if (args.length > 0) {
            for (String s : args) {
                if (s.equalsIgnoreCase("forceOpenGL"))
                    tryForceOpenGL = true;
                if (s.equalsIgnoreCase("mt"))
                    useMT = true;
            }
        }
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
                // Default to system font because people like it for some reason.
                if (UILabel.fontOverride == null)
                    UILabel.fontOverride = GaBIEn.getFontOverrides()[0];
            }
        }.start();
        GaBIEn.internal = new GaBIEnImpl(useMT);
        try {
            Class.forName("gabienapp.Application").getDeclaredMethod("gabienmain").invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
            GaBIEn.ensureQuit();
        }
    }

}
