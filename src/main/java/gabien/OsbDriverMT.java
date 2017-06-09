/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package gabien;

import gabien.backendhelp.ThreadForwardingGrDriver;

import java.awt.image.BufferedImage;

/**
 * Created on 08/06/17.
 */
public class OsbDriverMT extends ThreadForwardingGrDriver<OsbDriverCore> implements IWindowGrBackend {
    public OsbDriverMT(int w, int h) {
        super(new OsbDriverCore(w, h));
        clientWidth = w;
        clientHeight = h;
    }

    @Override
    public int[] getPixels() {
        flushCmdBuf();
        return target.getPixels();
    }

    @Override
    public BufferedImage getImage() {
        // Note that this is always run inside the lock.
        return target.getImage();
    }

    @Override
    public void flush() {
        flushCmdBuf();
    }

    @Override
    public void resize(int wantedRW, int wantedRH) {
        target.resize(wantedRW, wantedRH);
    }
}
