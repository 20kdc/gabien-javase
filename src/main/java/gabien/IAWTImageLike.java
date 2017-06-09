/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package gabien;

import java.awt.image.BufferedImage;

/**
 * Created on 08/06/17.
 */
public interface IAWTImageLike {
    // Notably, this is "behind" the threading layer, implemented by OsbDriver and such.
    // The locking is done before it reaches OsbDriverCore.
    BufferedImage getImage();
}
