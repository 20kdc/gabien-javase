/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package gabien;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Hashtable;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Wow, this code dates back a long time.
 * (See: very early versions of IkachanMapEdit)
 */
final class GrInDriver implements IGrInDriver {
    public JFrame frame;
    public JPanel panel;
    public String typed = "";
    public Graphics2D g;
    public BufferedImage bi;
    private boolean[] keys = new boolean[IGrInDriver.KEYS];
    private boolean[] keysjd = new boolean[IGrInDriver.KEYS];
    private int sc;
    private int realWidth, realHeight, mouseX = 0, mouseY = 0, mouseB = 0;
    private boolean mouseDown = false, mouseJustDown = false;

    public GrInDriver(String name, int scale, boolean resizable, int rw, int rh) {
        realWidth = rw;
        realHeight = rh;
        sc = scale;
        frame = new JFrame(name);
        panel = new JPanel();
        panel.setBackground(Color.black);
        frame.setResizable(resizable);

        panel.setPreferredSize(new Dimension(rw * scale, rh * scale));
        frame.setSize(rw * scale, rh * scale);

        frame.add(panel, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent arg0) {

            }

            @Override
            public void focusLost(FocusEvent arg0) {
                for (int p = 0; p < keys.length; p++)
                    keys[p] = false;
            }
        });

        panel.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent me) {
            }

            @Override
            public void mousePressed(MouseEvent me) {
                mouseX = me.getX() / sc;
                mouseY = me.getY() / sc;
                mouseB = me.getButton();
                mouseDown = true;
                mouseJustDown = true;
            }

            @Override
            public void mouseReleased(MouseEvent me) {
                mouseX = me.getX() / sc;
                mouseY = me.getY() / sc;
                mouseDown = false;
                // justdown is a click checker.
                // as in, even if the mouse is
                // released, register the click
            }

            @Override
            public void mouseEntered(MouseEvent me) {
            }

            @Override
            public void mouseExited(MouseEvent me) {
            }
        });
        panel.addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent me) {
                mouseX = me.getX() / sc;
                mouseY = me.getY() / sc;

            }

            @Override
            public void mouseMoved(MouseEvent me) {
                mouseX = me.getX() / sc;
                mouseY = me.getY() / sc;
            }
        });
        frame.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent ke) {
                if ((ke.getKeyChar() >= 32) || (ke.getKeyChar() == 10))
                    typed += ke.getKeyChar();
            }

            public int[] keymap = {KeyEvent.VK_ESCAPE, KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3, KeyEvent.VK_4, KeyEvent.VK_5, KeyEvent.VK_6, KeyEvent.VK_7, KeyEvent.VK_8, KeyEvent.VK_9, KeyEvent.VK_0, KeyEvent.VK_MINUS, KeyEvent.VK_EQUALS, KeyEvent.VK_BACK_SPACE, KeyEvent.VK_TAB, KeyEvent.VK_Q, KeyEvent.VK_W, KeyEvent.VK_E, KeyEvent.VK_R, KeyEvent.VK_T, KeyEvent.VK_Y, KeyEvent.VK_U, KeyEvent.VK_I, KeyEvent.VK_O, KeyEvent.VK_P, KeyEvent.VK_OPEN_BRACKET, KeyEvent.VK_CLOSE_BRACKET, KeyEvent.VK_ENTER, KeyEvent.VK_CONTROL, KeyEvent.VK_A, KeyEvent.VK_S, KeyEvent.VK_D, KeyEvent.VK_F, KeyEvent.VK_G, KeyEvent.VK_H, KeyEvent.VK_J, KeyEvent.VK_K, KeyEvent.VK_L, KeyEvent.VK_SEMICOLON, KeyEvent.VK_QUOTE, 0,// Afraid
                    // I
                    // can't
                    // map
                    // this
                    // key.
                    KeyEvent.VK_SHIFT, KeyEvent.VK_BACK_SLASH, KeyEvent.VK_Z, KeyEvent.VK_X, KeyEvent.VK_C, KeyEvent.VK_V, KeyEvent.VK_B, KeyEvent.VK_N, KeyEvent.VK_M, KeyEvent.VK_COMMA, KeyEvent.VK_PERIOD, KeyEvent.VK_SLASH, 0,// this
                    // does
                    // not
                    // map
                    KeyEvent.VK_ALT, KeyEvent.VK_SPACE, KeyEvent.VK_CAPS_LOCK, KeyEvent.VK_F1, KeyEvent.VK_F2, KeyEvent.VK_F3, KeyEvent.VK_F4, KeyEvent.VK_F5, KeyEvent.VK_F6, KeyEvent.VK_F7, KeyEvent.VK_F8, KeyEvent.VK_F9, KeyEvent.VK_F10, KeyEvent.VK_NUM_LOCK, KeyEvent.VK_SCROLL_LOCK, KeyEvent.VK_NUMPAD7, KeyEvent.VK_NUMPAD8, KeyEvent.VK_NUMPAD9, 0, KeyEvent.VK_NUMPAD4, KeyEvent.VK_NUMPAD5, KeyEvent.VK_NUMPAD6, 0, KeyEvent.VK_NUMPAD1, KeyEvent.VK_NUMPAD2, KeyEvent.VK_NUMPAD3, KeyEvent.VK_NUMPAD0, KeyEvent.VK_PERIOD, KeyEvent.VK_F11, KeyEvent.VK_F12, 0, 0, KeyEvent.VK_ALT_GRAPH, 0, KeyEvent.VK_UP, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN, KeyEvent.VK_INSERT,};

            public int filterKey(KeyEvent ke) {
                for (int p = 0; p < keymap.length; p++) {
                    if (ke.getKeyCode() == keymap[p])
                        return p;
                }
                return -1;
            }

            @Override
            public void keyPressed(KeyEvent ke) {
                int KeyNum = filterKey(ke);
                if (KeyNum != -1) {
                    if (!keys[KeyNum])
                        keysjd[KeyNum] = true;
                    keys[KeyNum] = true;
                }
            }

            @Override
            public void keyReleased(KeyEvent ke) {
                int KeyNum = filterKey(ke);
                if (KeyNum != -1)
                    keys[KeyNum] = false;
            }
        });

        if (resizable) {
            panel.addComponentListener(new ComponentListener() {
                @Override
                public void componentResized(ComponentEvent componentEvent) {
                    realWidth = panel.getWidth() / sc;
                    realHeight = panel.getHeight() / sc;
                    bi = new BufferedImage(realWidth, realHeight, BufferedImage.TYPE_INT_RGB);
                    g = bi.createGraphics();
                }

                @Override
                public void componentMoved(ComponentEvent componentEvent) {
                }

                @Override
                public void componentShown(ComponentEvent componentEvent) {
                }

                @Override
                public void componentHidden(ComponentEvent componentEvent) {
                }
            });
        }

        bi = new BufferedImage(realWidth, realHeight, BufferedImage.TYPE_INT_RGB);
        g = bi.createGraphics();
    }

    @Override
    public void flush() {
        panel.getGraphics().drawImage(bi, 0, 0, realWidth * sc, realHeight * sc, null);
    }

    @Override
    public void clearAll(int i, int i0, int i1) {
        g.setColor(new Color(i, i0, i1));
        g.fillRect(0, 0, realWidth, realHeight);
    }

    @Override
    public void clearRect(int i, int i0, int i1, int x, int y, int w, int h) {
        g.setColor(new Color(i, i0, i1));
        g.fillRect(x * sc, y * sc, w * sc, h * sc);
    }

    @Override
    public int getWidth() {
        return realWidth;
    }

    @Override
    public int getHeight() {
        return realHeight;
    }

    @Override
    public void drawText(int x, int y, int r, int cg, int b, int textSize, String text) {
        try {
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, textSize));
            g.setColor(new Color(r, cg, b));
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.drawString(text, x, y + (textSize - (textSize / 4)));
        } catch (Exception ex) {
        }
    }

    @Override
    public int getMouseX() {
        return mouseX;
    }

    @Override
    public int getMouseY() {
        return mouseY;
    }

    @Override
    public int getMouseButton() {
        return mouseB;
    }

    @Override
    public boolean stillRunning() {
        return frame.isVisible();
    }

    @Override
    public boolean isKeyDown(int KEYID) {
        return keys[KEYID];
    }

    @Override
    public boolean isKeyJustPressed(int KEYID) {
        boolean b = keysjd[KEYID];
        keysjd[KEYID] = false;
        return b;
    }

    @Override
    public boolean getMouseDown() {
        return mouseDown;
    }

    @Override
    public boolean getMouseJustDown() {
        boolean b = mouseJustDown;
        mouseJustDown = false;
        return b;
    }

    @Override
    public void clearKeys() {
        for (int p = 0; p < keysjd.length; p++) {
            keysjd[p] = false;
            keys[p] = false;
        }
        mouseJustDown = false;
        typed = "";
    }

    @Override
    public void setTypeBuffer(String s) {
        typed = s;
    }

    @Override
    public String getTypeBuffer() {
        return typed;
    }

    @Override
    public void shutdown() {
        frame.setVisible(false);
    }

    public static class Image_AWT implements IImage {
        protected BufferedImage img, bckimg;

        @Override
        public int getWidth() {
            return img.getWidth();
        }

        @Override
        public int getHeight() {
            return img.getHeight();
        }

        @Override
        public int[] getPixels() {
            int[] arr = new int[img.getWidth() * img.getHeight()];
            img.getRGB(0, 0, img.getWidth(), img.getHeight(), arr, 0, img.getWidth());
            return arr;
        }
    }

    @Override
    public void blitImage(int srcx, int srcy, int srcw, int srch, int x, int y, IImage i) {
        g.drawImage(((Image_AWT) i).img, x, y, (x + srcw), (y + srch), srcx, srcy, (srcx + srcw), (srcy + srch), null);
    }

    @Override
    public void blitBCKImage(int srcx, int srcy, int srcw, int srch, int x, int y, IImage i) {
        g.drawImage(((Image_AWT) i).bckimg, x, y, (x + srcw), (y + srch), srcx, srcy, (srcx + srcw), (srcy + srch), null);
    }
}
