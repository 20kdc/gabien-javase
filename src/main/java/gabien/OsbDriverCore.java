/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package gabien;

import gabien.backendhelp.Blender;
import gabien.backendhelp.INativeImageHolder;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Created on 04/06/17.
 */
public class OsbDriverCore extends AWTImage implements IWindowGrBackend {
    public Graphics2D bufGraphics;
    private final boolean alpha;
    public OsbDriverCore(int w, int h, boolean a) {
        alpha = a;
        resize(w, h);
    }

    @Override
    public void flush() {
        // Not needed
    }

    public void resize(int w, int h) {
        buf = new BufferedImage(w, h, alpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        bufGraphics = buf.createGraphics();
    }

    @Override
    public void blitImage(int srcx, int srcy, int srcw, int srch, int x, int y, IImage i) {
        INativeImageHolder nih = (INativeImageHolder) i;
        Runnable[] r = nih.getLockingSequence();
        if (r != null)
            r[0].run();
        bufGraphics.drawImage((BufferedImage) nih.getNative(), x, y, (x + srcw), (y + srch), srcx, srcy, (srcx + srcw), (srcy + srch), null);
        if (r != null)
            r[1].run();
    }

    @Override
    public void blitScaledImage(int srcx, int srcy, int srcw, int srch, int x, int y, int acw, int ach, IImage i) {
        INativeImageHolder nih = (INativeImageHolder) i;
        Runnable[] r = nih.getLockingSequence();
        if (r != null)
            r[0].run();
        bufGraphics.drawImage((BufferedImage) nih.getNative(), x, y, (x + acw), (y + ach), srcx, srcy, (srcx + srcw), (srcy + srch), null);
        if (r != null)
            r[1].run();
    }

    @Override
    public void blitRotatedScaledImage(int srcx, int srcy, int srcw, int srch, int x, int y, int acw, int ach, int angle, IImage i) {
        AffineTransform workTransform = new AffineTransform();
        workTransform.translate(x + (acw / 2.0d), y + (ach / 2.0d));
        workTransform.rotate((-angle / 360.0d) * (Math.PI * 2.0d));
        workTransform.translate(-(acw / 2.0d), -(ach / 2.0d));
        bufGraphics.setTransform(workTransform);
        INativeImageHolder nih = (INativeImageHolder) i;
        Runnable[] r = nih.getLockingSequence();
        if (r != null)
            r[0].run();
        bufGraphics.drawImage((BufferedImage) nih.getNative(), 0, 0, acw, ach, srcx, srcy, (srcx + srcw), (srcy + srch), null);
        if (r != null)
            r[1].run();
        bufGraphics.setTransform(new AffineTransform());
    }

    @Override
    public void blendRotatedScaledImage(int srcx, int srcy, int srcw, int srch, int x, int y, int acw, int ach, int angle, IImage i, boolean blendSub) {
        Blender.blendRotatedScaledImage(this, srcx, srcy, srcw, srch, x, y, acw, ach, angle, i, blendSub);
    }

    protected static Font getFont(int textSize) {
        try {
            Font f = new Font(GaBIEnImpl.getPresentFont(), Font.PLAIN, textSize - (textSize / 8));
            return f;
        } catch (Exception ex) {
        }
        return null;
    }

    @Override
    public void drawText(int x, int y, int r, int cg, int b, int textSize, String text) {
        try {
            Font f = getFont(textSize);
            if (f != null)
                bufGraphics.setFont(f);
            bufGraphics.setColor(new Color(r, cg, b));
            bufGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            // --- NOTE before changing this. Offset of +1 causes underscore to be hidden on some fonts.
            bufGraphics.drawString(text, x, y + (textSize - (textSize / 4)));
        } catch (Exception ex) {
        }
    }

    @Override
    public void clearAll(int i, int i0, int i1) {
        bufGraphics.setColor(new Color(i, i0, i1));
        bufGraphics.fillRect(0, 0, buf.getWidth(), buf.getHeight());
    }

    @Override
    public void clearRect(int i, int i0, int i1, int x, int y, int w, int h) {
        bufGraphics.setColor(new Color(i, i0, i1));
        bufGraphics.fillRect(x, y, w, h);
    }

    @Override
    public void shutdown() {
        buf = null;
        bufGraphics = null;
    }

}
