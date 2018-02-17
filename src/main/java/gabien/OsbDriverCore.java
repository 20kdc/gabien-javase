/*
 * gabien-javase - gabien backend for desktop Java
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
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
    private Font lastFont;
    private int lastFontSize;
    public int scissorX, scissorY, scissorXT, scissorYT, scissorW, scissorH;

    public OsbDriverCore(int w, int h, boolean a) {
        alpha = a;
        resize(w, h);
    }

    @Override
    public void flush() {

    }

    public void resize(int w, int h) {
        buf = new BufferedImage(w, h, alpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        bufGraphics = buf.createGraphics();
        scissorX = 0;
        scissorY = 0;
        scissorXT = 0;
        scissorYT = 0;
        scissorW = w;
        scissorH = h;
    }

    @Override
    public void blitImage(int srcx, int srcy, int srcw, int srch, int x, int y, IImage i) {
        INativeImageHolder nih = (INativeImageHolder) i;
        bufGraphics.drawImage((BufferedImage) nih.getNative(), x, y, (x + srcw), (y + srch), srcx, srcy, (srcx + srcw), (srcy + srch), null);
    }

    @Override
    public void blitScaledImage(int srcx, int srcy, int srcw, int srch, int x, int y, int acw, int ach, IImage i) {
        INativeImageHolder nih = (INativeImageHolder) i;
        bufGraphics.drawImage((BufferedImage) nih.getNative(), x, y, (x + acw), (y + ach), srcx, srcy, (srcx + srcw), (srcy + srch), null);
    }

    @Override
    public void blitRotatedScaledImage(int srcx, int srcy, int srcw, int srch, int x, int y, int acw, int ach, int angle, IImage i) {
        AffineTransform workTransform = new AffineTransform();
        workTransform.translate(x + (acw / 2.0d), y + (ach / 2.0d));
        workTransform.rotate((-angle / 360.0d) * (Math.PI * 2.0d));
        workTransform.translate(-(acw / 2.0d), -(ach / 2.0d));
        bufGraphics.setTransform(workTransform);
        INativeImageHolder nih = (INativeImageHolder) i;
        bufGraphics.drawImage((BufferedImage) nih.getNative(), 0, 0, acw, ach, srcx, srcy, (srcx + srcw), (srcy + srch), null);
        bufGraphics.setTransform(new AffineTransform());
    }

    @Override
    public void blendRotatedScaledImage(int srcx, int srcy, int srcw, int srch, int x, int y, int acw, int ach, int angle, IImage i, boolean blendSub) {
        // There is no way to do this in Java directly
        Blender.blendRotatedScaledImage(this, srcx, srcy, srcw, srch, x, y, acw, ach, angle, i, blendSub);
    }

    protected static Font getFont(int textSize) {
        try {
            String s = FontManager.fontOverride;
            if (s == null)
                s = Font.SANS_SERIF;
            Font f = new Font(s, Font.PLAIN, textSize - (textSize / 8));
            return f;
        } catch (Exception ex) {
        }
        return null;
    }

    @Override
    public void drawText(int x, int y, int r, int cg, int b, int textSize, String text) {
        try {
            Font f = lastFont;
            if (f != null) {
                if (lastFontSize != textSize) {
                    lastFont = f = getFont(textSize);
                    lastFontSize = textSize;
                }
            } else {
                lastFont = f = getFont(textSize);
                lastFontSize = textSize;
            }
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
        bufGraphics.fillRect(0, 0, scissorW, scissorH);
    }

    @Override
    public void clearRect(int i, int i0, int i1, int x, int y, int w, int h) {
        bufGraphics.setColor(new Color(i, i0, i1));
        bufGraphics.fillRect(x, y, w, h);
    }

    @Override
    public void clearScissoring() {
        // change origin back to 0, 0
        bufGraphics.translate(-scissorXT, -scissorYT);
        int w = buf.getWidth(), h = buf.getHeight();
        scissorX = 0;
        scissorY = 0;
        scissorXT = 0;
        scissorYT = 0;
        scissorW = w;
        scissorH = h;
        bufGraphics.setClip(null);
    }

    @Override
    public void adjustScissoring(int x, int y, int tx, int ty, int w, int h) {
        scissorX += x;
        scissorY += y;
        scissorXT += tx;
        scissorYT += ty;
        scissorW += w;
        scissorH += h;
        bufGraphics.translate(tx, ty);
        bufGraphics.setClip(scissorX - scissorXT, scissorY - scissorYT, scissorW, scissorH);
    }

    @Override
    public void shutdown() {
        buf = null;
        bufGraphics = null;
    }

}
