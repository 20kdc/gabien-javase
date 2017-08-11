/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package gabien;

/**
 * This interface covers both OsbDrivers.
 * This is it's purpose, really, so that GrInDriver can receive either one.
 * Created on 08/06/17.
 */
public interface IWindowGrBackend extends IGrDriver, IAWTImageLike {
    // Locks the backend until the next command.
    void flush();
    // Resizes the backend (only use after a flush before further commands)
    void resize(int wantedRW, int wantedRH);
}
