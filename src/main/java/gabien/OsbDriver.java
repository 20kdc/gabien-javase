/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package gabien;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Created on 04/06/17.
 */
public class OsbDriver extends AWTImage implements IOsbDriver {
    public Graphics2D bufGraphics;

    public OsbDriver(int w, int h) {
        size(w, h);
    }

    public void size(int w, int h) {
        buf = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        bufGraphics = buf.createGraphics();
    }

    @Override
    public void blitImage(int srcx, int srcy, int srcw, int srch, int x, int y, IGrInDriver.IImage i) {
        bufGraphics.drawImage(((AWTImage) i).buf, x, y, (x + srcw), (y + srch), srcx, srcy, (srcx + srcw), (srcy + srch), null);
    }

    @Override
    public void blitScaledImage(int srcx, int srcy, int srcw, int srch, int x, int y, int acw, int ach, IGrInDriver.IImage i) {
        bufGraphics.drawImage(((AWTImage) i).buf, x, y, (x + acw), (y + ach), srcx, srcy, (srcx + srcw), (srcy + srch), null);
    }

    @Override
    public void blitRotatedScaledImage(int srcx, int srcy, int srcw, int srch, int x, int y, int acw, int ach, int angle, IGrInDriver.IImage i) {
        AffineTransform workTransform = new AffineTransform();
        workTransform.translate(x + (acw / 2.0d), y + (ach / 2.0d));
        workTransform.rotate((-angle / 360.0d) * (Math.PI * 2.0d));
        workTransform.translate(-(acw / 2.0d), -(ach / 2.0d));
        bufGraphics.setTransform(workTransform);
        bufGraphics.drawImage(((AWTImage) i).buf, 0, 0, acw, ach, srcx, srcy, (srcx + srcw), (srcy + srch), null);
        bufGraphics.setTransform(new AffineTransform());
    }

    protected static Font getFont(int textSize) {
        try {
            Font f = new Font(Font.SANS_SERIF, Font.PLAIN, textSize - (textSize / 8));
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

}
