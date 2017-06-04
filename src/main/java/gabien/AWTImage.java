package gabien;

import java.awt.image.BufferedImage;

/**
 * Created on 04/06/17.
 */
public class AWTImage implements IGrInDriver.IImage {
    protected BufferedImage buf;

    @Override
    public int getWidth() {
            return buf.getWidth();
        }

    @Override
    public int getHeight() {
            return buf.getHeight();
        }

    @Override
    public int[] getPixels() {
        int[] arr = new int[buf.getWidth() * buf.getHeight()];
        buf.getRGB(0, 0, buf.getWidth(), buf.getHeight(), arr, 0, buf.getWidth());
        return arr;
    }
}
