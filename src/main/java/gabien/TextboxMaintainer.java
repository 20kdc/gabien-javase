/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package gabien;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;

/**
 * Maintains a textbox.
 * Created on 01/06/17.
 */
public class TextboxMaintainer {
    public final JPanel parent;
    public JTextField target;
    private KeyListener kl;
    // null means unmaintained!
    public String maintainedString = null;
    public boolean maintainedThisFrame = false;

    public TextboxMaintainer(JPanel panel, KeyListener k) {
        parent = panel;
        kl = k;
    }

    public void newFrame() {
        if (!maintainedThisFrame)
            clear();
    }

    public String maintain(int x, int y, int width, String text) {
        if (target == null) {
            // wait as long as possible because of font loading perf.
            // IDK if it's loading every font on the system or something but this is a real issue...
            target = new JTextField();
            parent.add(target);
            // apparently it's not capable of setting sensible defaults
            target.setBounds(0, 0, 32, 17);
            // use
            target.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
            target.addKeyListener(kl);
        }
        boolean needToMove = false;
        if (target.getX() != x)
            needToMove = true;
        if (target.getY() != (y - (target.getHeight() / 2)))
            needToMove = true;
        if (target.getWidth() != width)
            needToMove = true;
        if (needToMove) {
            target.setLocation(x, y - (target.getHeight() / 2));
            target.setSize(width, target.getHeight());
        }

        if (maintainedString != null) {
            if (!maintainedString.equals(text))
                target.setText(text);
        } else {
            target.setText(text);
            target.setVisible(true);
            target.grabFocus();
        }

        maintainedThisFrame = true;
        maintainedString = target.getText();
        return maintainedString;
    }

    public void clear() {
        maintainedThisFrame = false;
        maintainedString = null;
        if (target != null)
            target.setVisible(false);
    }
}
