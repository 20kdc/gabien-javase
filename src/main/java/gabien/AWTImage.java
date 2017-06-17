/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package gabien;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

/**
 * Created on 04/06/17.
 */
public class AWTImage implements IGrInDriver.IImage, IAWTImageLike {
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

    @Override
    public byte[] createPNG() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(buf, "PNG", baos);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return baos.toByteArray();
    }

    @Override
    public BufferedImage getImage() {
        return buf;
    }
}
