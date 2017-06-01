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
    public final JTextField target = new JTextField();
    // null means unmaintained!
    public String maintainedString = null;
    public boolean maintainedThisFrame = false;

    public TextboxMaintainer(JPanel panel, KeyListener kl) {
        parent = panel;
        parent.add(target);
        // apparently it's not capable of setting sensible defaults
        target.setBounds(0, 0, 32, 17);
        // use
        target.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        target.addKeyListener(kl);
    }

    public void newFrame() {
        if (!maintainedThisFrame)
            clear();
    }

    public String maintain(int x, int y, int width, String text) {
        if (maintainedString != null) {
            if (!maintainedString.equals(text))
                target.setText(text);
        } else {
            target.setText(text);
            target.setLocation(x, y - (target.getHeight() / 2));
            target.setSize(width, target.getHeight());
            target.setVisible(true);
        }
        maintainedThisFrame = true;
        maintainedString = target.getText();
        return maintainedString;
    }

    public void clear() {
        maintainedThisFrame = false;
        maintainedString = null;
        target.setVisible(false);
    }
}
