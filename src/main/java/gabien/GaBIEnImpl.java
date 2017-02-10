/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package gabien;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.sound.sampled.LineUnavailableException;

/**
 * Graphics and Basic Intput-english Subsystems should be initialized in this
 * order: graphics,sound

 */
public final class GaBIEnImpl implements IGaBIEn {
    public Hashtable<String, IGrInDriver.IImage> loadedImages = new Hashtable<String, IGrInDriver.IImage>();

    private long startup = System.currentTimeMillis();

    private double lastDt = getTime();

    private IRawAudioDriver sound = null;

    public double getTime() {
        return (System.currentTimeMillis() - startup) / 1000.0;
    }

    public double timeDelta(boolean reset) {
        double dt = getTime() - lastDt;
        if (reset)
            lastDt = getTime();
        return dt;
    }

    public InputStream getResource(String resource) {
        return ClassLoader.getSystemClassLoader().getResourceAsStream("assets/" + resource);
    }

    public InputStream getFile(String FDialog) {
        try {
            return new FileInputStream(FDialog);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public OutputStream getOutFile(String FDialog) {
        try {
            return new FileOutputStream(FDialog);
        } catch (Exception e) {
            return null;
        }
    }

    public IGrInDriver makeGrIn(String name, int w, int h, WindowSpecs ws) {
        return new gabien.GrInDriver(name, ws.scale, ws.resizable, w, h);
    }

    public boolean singleWindowApp() {
        return false;
    }

    public void ensureQuit() {
        System.exit(0);
    }

    public IRawAudioDriver getRawAudio() {
        if (sound == null) {
            try {
                sound = new RawSoundDriver();
            } catch (LineUnavailableException ex) {
                Logger.getLogger(GaBIEnImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return sound;
    }

    @Override
    public WindowSpecs defaultWindowSpecs(String name, int w, int h) {
        WindowSpecs ws = new WindowSpecs();
        ws.scale = ((w > 400) || (h > 300)) ? 1 : 2;
        ws.resizable = false;
        return ws;
    }

    @Override
    public IGrInDriver.IImage getImage(String a, int tr, int tg, int tb) {
        String ki = a + "_" + tr + "_" + tg + "_" + tb;
        if (loadedImages.containsKey(ki))
            return loadedImages.get(ki);
        try {
            GrInDriver.Image_AWT img = new GrInDriver.Image_AWT();
            try {
                img.img = ImageIO.read(GaBIEn.getFile(a));
            } catch (Exception e) {
                img.img = ImageIO.read(GaBIEn.getResource(a));
            }
            img.bckimg = new BufferedImage(img.img.getWidth(), img.img.getHeight(), BufferedImage.TYPE_INT_ARGB);
            for (int px = 0; px < img.img.getWidth(); px++) {
                for (int py = 0; py < img.img.getHeight(); py++) {
                    int c = img.img.getRGB(px, py);
                    if ((c & 0xFFFFFF) != (tb | (tg << 8) | (tr << 16))) {
                        img.bckimg.setRGB(px, py, c | 0xFF000000);
                    } else {
                        img.bckimg.setRGB(px, py, 0);
                    }
                }
            }
            loadedImages.put(ki, img);
            return img;
        } catch (Exception ex) {
            System.err.println("COULDN'T GET IMAGE:" + a);
            ex.printStackTrace();

            GrInDriver.Image_AWT img = new GrInDriver.Image_AWT();

            img.img = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
            img.bckimg = img.img;
            img.img.setRGB(0, 0, 0xFF00FF);
            img.img.setRGB(1, 0, 0xFF00FF);
            img.img.setRGB(0, 1, 0xFF00FF);
            img.img.setRGB(1, 1, 0xFF00FF);
            img.img.setRGB(0 + 2, 0 + 2, 0xFF00FF);
            img.img.setRGB(1 + 2, 0 + 2, 0xFF00FF);
            img.img.setRGB(0 + 2, 1 + 2, 0xFF00FF);
            img.img.setRGB(1 + 2, 1 + 2, 0xFF00FF);
            loadedImages.put(ki, img);
            return img;
        }
    }
    @Override
    public IGrInDriver.IImage createImage(int[] colours, int width, int height) {
        GrInDriver.Image_AWT ia = new GrInDriver.Image_AWT();
        ia.img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        ia.img.setRGB(0, 0, width, height, colours, 0, width);
        ia.bckimg = ia.img;
        return ia;
    }

    @Override
    public void hintFlushAllTheCaches() {
        loadedImages.clear();
    }

    @Override
    public int measureText(int i, String text) {
        Font f = new Font(Font.SANS_SERIF, Font.PLAIN, i);
        Rectangle r = f.getStringBounds(text, new FontRenderContext(AffineTransform.getTranslateInstance(0, 0), true, true)).getBounds();
        return (int) r.getMaxX();
    }
}
