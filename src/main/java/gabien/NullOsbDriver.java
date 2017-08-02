package gabien;

import java.awt.image.BufferedImage;

/**
 * Created on 6/20/17.
 */
public class NullOsbDriver implements IWindowGrBackend {
    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public int[] getPixels() {
        return new int[0];
    }

    @Override
    public byte[] createPNG() {
        return new byte[0];
    }

    @Override
    public void blitImage(int srcx, int srcy, int srcw, int srch, int x, int y, IGrInDriver.IImage i) {

    }

    @Override
    public void blitScaledImage(int srcx, int srcy, int srcw, int srch, int x, int y, int acw, int ach, IGrInDriver.IImage i) {

    }

    @Override
    public void blitRotatedScaledImage(int srcx, int srcy, int srcw, int srch, int x, int y, int acw, int ach, int angle, IGrInDriver.IImage i) {

    }

    @Override
    public void blendRotatedScaledImage(int srcx, int srcy, int srcw, int srch, int x, int y, int acw, int ach, int angle, IGrInDriver.IImage i, boolean blendSub) {

    }

    @Override
    public void drawText(int x, int y, int r, int g, int b, int i, String text) {

    }

    @Override
    public void clearAll(int i, int i0, int i1) {

    }

    @Override
    public void clearRect(int r, int g, int b, int x, int y, int width, int height) {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public BufferedImage getImage() {
        // closest thing to a "no-op" image
        return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    }

    @Override
    public void flush() {

    }

    @Override
    public void resize(int wantedRW, int wantedRH) {

    }
}
