/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package gabien;

import java.awt.*;

/**
 * Used to plot relative coordinates
 * 1   2
 *
 * X6 9
 *  7 8
 * 4   3
 *
 * X: 5 and 10
 * Created on 01/06/17.
 */
public class ClipBoundHelper {
    int x = 0;
    int y = 0;
    Polygon p = new Polygon();

    public void point(int i, int i1) {
        x += i;
        y += i1;
        p.addPoint(x, y);
    }
}
