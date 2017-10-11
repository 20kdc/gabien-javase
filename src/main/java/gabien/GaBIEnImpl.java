/*
 * gabien-javase - gabien backend for desktop Java
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package gabien;

import gabien.ui.UILabel;

import javax.imageio.ImageIO;
import javax.sound.sampled.LineUnavailableException;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Graphics and Basic Input Engine (? Or was it meant to be english? (Well, thank goodness that issue was fixed)
 * Look, I named this thing in 2014. 3 years, people.)
 * Subsystems should be initialized in this order: graphics,sound
 */
public final class GaBIEnImpl implements IGaBIEn {
    public static boolean mobileEmulation;
    private HashMap<String, IImage> loadedImages = new HashMap<String, IImage>();

    private final boolean useMultithread;

    private long startup = System.currentTimeMillis();

    private double lastDt = getTime();

    private RawSoundDriver sound = null;

    public GaBIEnImpl(boolean useMT) {
        useMultithread = useMT;
    }

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


    private IWindowGrBackend makeOffscreenBufferInt(int w, int h, boolean alpha) {
        // Note all the multithreading occurs in OsbDriverMT.
        if (w <= 0)
            return new NullOsbDriver();
        if (h <= 0)
            return new NullOsbDriver();
        if (useMultithread)
            return new OsbDriverMT(w, h, alpha);
        return new OsbDriverCore(w, h, alpha);
    }

    public IGrInDriver makeGrIn(String name, int w, int h, WindowSpecs ws) {
        if (mobileEmulation) {
            ws.resizable = false;
            w = 960;
            h = 540;
        }
        return new gabien.GrInDriver(name, ws.scale, ws.resizable, w, h, makeOffscreenBufferInt(w, h, false));
    }

    @Override
    public IGrDriver makeOffscreenBuffer(int w, int h, boolean alpha) {
        // Finalization wrapper as a just-in-case.
        return new ProxyOsbDriver(makeOffscreenBufferInt(w, h, alpha));
    }

    public boolean singleWindowApp() {
        return mobileEmulation;
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
    public void hintShutdownRawAudio() {
        if (sound != null)
            sound.shutdown();
        sound = null;
    }

    @Override
    public WindowSpecs defaultWindowSpecs(String name, int w, int h) {
        WindowSpecs ws = new WindowSpecs();
        ws.scale = ((w > 400) || (h > 300)) ? 1 : 2;
        ws.resizable = false;
        return ws;
    }

    @Override
    public IImage getImage(String a) {
        String ki = a + "_N_N_N";
        if (loadedImages.containsKey(ki))
            return loadedImages.get(ki);
        try {
            AWTImage img = new AWTImage();
            try {
                img.buf = ImageIO.read(GaBIEn.getFile(a));
                if (img.buf == null)
                    return null;
            } catch (Exception e) {
                img.buf = ImageIO.read(GaBIEn.getResource(a));
                if (img.buf == null)
                    return null;
            }
            loadedImages.put(ki, img);
            return img;
        } catch (Exception ex) {
            System.err.println("COULDN'T GET IMAGE:" + a);
            IImage img = GaBIEn.getErrorImage();
            loadedImages.put(ki, img);
            return img;
        }
    }
    @Override
    public IImage getImageCK(String a, int tr, int tg, int tb) {
        String ki = a + "_" + tr + "_" + tg + "_" + tb;
        if (loadedImages.containsKey(ki))
            return loadedImages.get(ki);
        try {
            AWTImage img = new AWTImage();
            BufferedImage tmp;
            try {
                tmp = ImageIO.read(GaBIEn.getFile(a));
                if (tmp == null)
                    return null;
            } catch (Exception e) {
                tmp = ImageIO.read(GaBIEn.getResource(a));
                if (tmp == null)
                    return null;
            }
            img.buf = new BufferedImage(tmp.getWidth(), tmp.getHeight(), BufferedImage.TYPE_INT_ARGB);
            for (int px = 0; px < tmp.getWidth(); px++) {
                for (int py = 0; py < tmp.getHeight(); py++) {
                    int c = tmp.getRGB(px, py);
                    if ((c & 0xFFFFFF) != (tb | (tg << 8) | (tr << 16))) {
                        img.buf.setRGB(px, py, c | 0xFF000000);
                    } else {
                        img.buf.setRGB(px, py, 0);
                    }
                }
            }
            loadedImages.put(ki, img);
            return img;
        } catch (Exception ex) {
            System.err.println("COULDN'T GET IMAGE:" + a);
            IImage img = GaBIEn.getErrorImage();
            loadedImages.put(ki, img);
            return img;
        }
    }

    @Override
    public IImage createImage(int[] colours, int width, int height) {
        if (width <= 0)
            return new NullOsbDriver();
        if (height <= 0)
            return new NullOsbDriver();
        AWTImage ia = new AWTImage();
        ia.buf = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        ia.buf.setRGB(0, 0, width, height, colours, 0, width);
        return ia;
    }

    @Override
    public void hintFlushAllTheCaches() {
        loadedImages.clear();
    }

    @Override
    public int measureText(int i, String text) {
        Font f = OsbDriverCore.getFont(i);
        if (f == null)
            return text.length() * (i / 2);
        Rectangle r = f.getStringBounds(text, new FontRenderContext(AffineTransform.getTranslateInstance(0, 0), true, true)).getBounds();
        return (int) r.getMaxX();
    }

    protected static String getPresentFont() {
        if (UILabel.fontOverride != null)
            return UILabel.fontOverride;
        return Font.SANS_SERIF;
    }

    @Override
    public String[] getFontOverrides() {
        String[] p = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        boolean foundFirst = false;
        for (int i = 0; i < p.length; i++) {
            if (p[i].equals(Font.SANS_SERIF)) {
                foundFirst = true;
                if (i != 0) {
                    String x = p[0];
                    p[0] = Font.SANS_SERIF;
                    p[i] = x;
                }
                break;
            }
        }
        if (!foundFirst) {
            String[] p2 = new String[p.length + 1];
            p2[0] = Font.SANS_SERIF;
            System.arraycopy(p, 0, p2, 1, p.length);
            return p2;
        }
        return p;
    }

    @Override
    public String[] listEntries(String s) {
        return new File(s).list();
    }

    @Override
    public void makeDirectories(String s) {
        new File(s).mkdirs();
    }

    @Override
    public boolean fileOrDirExists(String s) {
        return new File(s).exists();
    }

    @Override
    public boolean dirExists(String s) {
        return new File(s).isDirectory();
    }

    @Override
    public boolean tryStartTextEditor(String fpath) {
        try {
            Desktop.getDesktop().edit(new File(fpath));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
