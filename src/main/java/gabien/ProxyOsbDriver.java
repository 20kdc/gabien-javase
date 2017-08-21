/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package gabien;

import gabien.backendhelp.ProxyGrDriver;

import java.awt.image.BufferedImage;

/**
 * Finalization helper.
 * Created on 08/06/17.
 */
public class ProxyOsbDriver extends ProxyGrDriver<IWindowGrBackend> implements IWindowGrBackend {
    public ProxyOsbDriver(IWindowGrBackend targ) {
        super(targ);
    }

    @Override
    public int[] getPixels() {
        return target.getPixels();
    }

    @Override
    public byte[] createPNG() {
        return target.createPNG();
    }

    @Override
    public void flush() {
        target.flush();
    }

    @Override
    public void resize(int wantedRW, int wantedRH) {
        target.resize(wantedRW, wantedRH);
    }
}
