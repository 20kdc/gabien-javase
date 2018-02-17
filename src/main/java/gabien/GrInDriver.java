/*
 * gabien-javase - gabien backend for desktop Java
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package gabien;

import gabien.backendhelp.ProxyGrDriver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Wow, this code dates back a long time.
 * (See: very early versions of IkachanMapEdit)
 * (Though now it's been split up for OsbDriver - Jun 4 2017)
 */
final class GrInDriver extends ProxyGrDriver<IWindowGrBackend> implements IGrInDriver {
    public JFrame frame;
    public JPanel panel;
    public TextboxMaintainer tm;
    private boolean[] keys = new boolean[IGrInDriver.KEYS];
    private boolean[] keysjd = new boolean[IGrInDriver.KEYS];
    private int sc;
    private int mouseX = 0, mouseY = 0, mouseB = 0;
    private HashSet<Integer> mouseDown = new HashSet<Integer>();
    private HashSet<Integer> mouseJustDown = new HashSet<Integer>();
    private HashSet<Integer> mouseJustUp = new HashSet<Integer>();
    private ReentrantLock mouseLock = new ReentrantLock();
    private boolean mousewheelDir = false;
    private int mousewheelMovements = 0;
    private int shadowScissorX, shadowScissorY;
    Random fuzzer = new Random();

    public GrInDriver(String name, WindowSpecs ws, IWindowGrBackend t) {
        super(t);
        sc = ws.scale;
        frame = new JFrame(name);
        panel = new JPanel();
        panel.setBackground(Color.black);
        frame.setResizable(ws.resizable && (!ws.fullscreen));

        int rw = t.getWidth();
        int rh = t.getHeight();

        panel.setPreferredSize(new Dimension(rw * sc, rh * sc));
        frame.setSize(rw * sc, rh * sc);

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
                mouseLock.lock();
                mouseX = me.getX() / sc;
                mouseY = me.getY() / sc;
                mouseB = me.getButton();
                if (GaBIEnImpl.mobileEmulation) {
                    mouseB = 1;
                    fuzzXY();
                }

                mouseDown.add(mouseB);
                mouseJustDown.add(mouseB);
                mouseLock.unlock();
            }

            @Override
            public void mouseReleased(MouseEvent me) {
                mouseLock.lock();
                mouseX = me.getX() / sc;
                mouseY = me.getY() / sc;
                if (GaBIEnImpl.mobileEmulation)
                    fuzzXY();
                mouseDown.remove(mouseB);
                mouseJustUp.add(mouseB);
                mouseLock.unlock();
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
                mouseLock.lock();
                mouseX = me.getX() / sc;
                mouseY = me.getY() / sc;
                if (GaBIEnImpl.mobileEmulation)
                    fuzzXY();
                mouseLock.unlock();
            }

            @Override
            public void mouseMoved(MouseEvent me) {
                mouseLock.lock();
                if (!GaBIEnImpl.mobileEmulation) {
                    mouseX = me.getX() / sc;
                    mouseY = me.getY() / sc;
                    if (GaBIEnImpl.mobileEmulation)
                        fuzzXY();
                }
                if (mouseDown.size() > 0) {
                    mouseJustUp.addAll(mouseDown);
                    mouseDown.clear();
                }
                mouseLock.unlock();
            }
        });
        if (!GaBIEnImpl.mobileEmulation) {
            panel.addMouseWheelListener(new MouseWheelListener() {
                @Override
                public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
                    mouseLock.lock();
                    int n = mouseWheelEvent.getWheelRotation();
                    if (mousewheelMovements == 0) {
                        mousewheelMovements = Math.abs(n);
                        mousewheelDir = n < 0;
                    } else {
                        if ((n < 0) == mousewheelDir) {
                            mousewheelMovements += Math.abs(n);
                        } else {
                            mousewheelMovements -= Math.abs(n);
                            if (mousewheelMovements < 0) {
                                mousewheelMovements = -mousewheelMovements;
                                mousewheelDir = !mousewheelDir;
                            }
                        }
                    }
                    mouseLock.unlock();
                }
            });
        }

        KeyListener commonKeyListener = new KeyListener() {

            @Override
            public void keyTyped(KeyEvent ke) {
            }

            public int[] keymap = {KeyEvent.VK_ESCAPE, KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3, KeyEvent.VK_4, KeyEvent.VK_5, KeyEvent.VK_6, KeyEvent.VK_7, KeyEvent.VK_8, KeyEvent.VK_9, KeyEvent.VK_0, KeyEvent.VK_MINUS, KeyEvent.VK_EQUALS, KeyEvent.VK_BACK_SPACE, KeyEvent.VK_TAB, KeyEvent.VK_Q, KeyEvent.VK_W, KeyEvent.VK_E, KeyEvent.VK_R, KeyEvent.VK_T, KeyEvent.VK_Y, KeyEvent.VK_U, KeyEvent.VK_I, KeyEvent.VK_O, KeyEvent.VK_P, KeyEvent.VK_OPEN_BRACKET, KeyEvent.VK_CLOSE_BRACKET, KeyEvent.VK_ENTER, KeyEvent.VK_CONTROL, KeyEvent.VK_A, KeyEvent.VK_S, KeyEvent.VK_D, KeyEvent.VK_F, KeyEvent.VK_G, KeyEvent.VK_H, KeyEvent.VK_J, KeyEvent.VK_K, KeyEvent.VK_L, KeyEvent.VK_SEMICOLON, KeyEvent.VK_QUOTE,
                    // VK_HASH/VK_TILDE
                    0,
                    KeyEvent.VK_SHIFT, KeyEvent.VK_BACK_SLASH, KeyEvent.VK_Z, KeyEvent.VK_X, KeyEvent.VK_C, KeyEvent.VK_V, KeyEvent.VK_B, KeyEvent.VK_N, KeyEvent.VK_M, KeyEvent.VK_COMMA, KeyEvent.VK_PERIOD, KeyEvent.VK_SLASH,
                    // VK_KP_MULTIPLY
                    0,
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
        };

        if (!GaBIEnImpl.mobileEmulation)
            frame.addKeyListener(commonKeyListener);

        tm = new TextboxMaintainer(panel, commonKeyListener);

        if (ws.fullscreen)
            GaBIEnImpl.getFSDevice().setFullScreenWindow(frame);

        GaBIEnImpl.activeDriverLock.lock();
        GaBIEnImpl.activeDrivers.add(this);
        GaBIEnImpl.activeDriverLock.unlock();
    }

    @Override
    public boolean flush() {
        // Through the actual render, the target is locked.
        Runnable[] l = target.getLockingSequenceN();
        if (l != null)
            l[0].run();

        tm.newFrame();
        Graphics pg = panel.getGraphics();
        if (tm.maintainedString != null) {
            int txX = tm.target.getX();
            int txY = tm.target.getY();
            int txW = tm.target.getWidth();
            int txH = tm.target.getHeight();
            ClipBoundHelper cbh = new ClipBoundHelper();
            cbh.point(0, 0);
            cbh.point(getWidth() * sc, 0);
            cbh.point(0, getHeight() * sc);
            cbh.point(-(getWidth() * sc), 0);
            // Alternatively, maybe go up to 0, 0 then to txX, txY, and follow the points that way? depends on how much harm would be caused by non-orthogonal lines
            cbh.point(0, txY - (getHeight() * sc));
            cbh.point(txX, 0);
            cbh.point(0, txH);
            cbh.point(txW, 0);
            cbh.point(0, -txH);
            cbh.point(-(txX + txW), 0);
            pg.setClip(cbh.p);
            pg.drawImage((BufferedImage) target.getNative(), 0, 0, getWidth() * sc, getHeight() * sc, null);
        } else {
            pg.setClip(null);
            pg.drawImage((BufferedImage) target.getNative(), 0, 0, getWidth() * sc, getHeight() * sc, null);
        }

        if (l != null)
            l[1].run();

        int wantedRW = panel.getWidth() / sc;
        int wantedRH = panel.getHeight() / sc;
        if ((getWidth() != wantedRW) || (getHeight() != wantedRH)) {
            target.resize(wantedRW, wantedRH);
            shadowScissorX = 0;
            shadowScissorY = 0;
            return true;
        }

        return false;
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
    public boolean stillRunning() {
        boolean res = frame.isVisible();
        if (!res)
            shutdown();
        return res;
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
    public HashSet<Integer> getMouseDown() {
        mouseLock.lock();
        HashSet<Integer> backup = new HashSet<Integer>(mouseDown);
        mouseLock.unlock();
        return backup;
    }

    @Override
    public HashSet<Integer> getMouseJustDown() {
        mouseLock.lock();
        HashSet<Integer> b = mouseJustDown;
        mouseJustDown = new HashSet<Integer>();
        mouseLock.unlock();
        return b;
    }

    @Override
    public HashSet<Integer> getMouseJustUp() {
        mouseLock.lock();
        HashSet<Integer> b = mouseJustUp;
        mouseJustUp = new HashSet<Integer>();
        mouseLock.unlock();
        return b;
    }

    // the locks here are semi-meaningless under some circumstances. Oh well.
    @Override
    public boolean getMousewheelJustDown() {
        mouseLock.lock();
        boolean b = mousewheelMovements > 0;
        if (b)
            mousewheelMovements -= 1;
        mouseLock.unlock();
        return b;
    }

    @Override
    public boolean getMousewheelDir() {
        mouseLock.lock();
        boolean dir = mousewheelDir;
        mouseLock.unlock();
        return dir;
    }

    @Override
    public void clearKeys() {
        for (int p = 0; p < keysjd.length; p++) {
            keysjd[p] = false;
            keys[p] = false;
        }
        mouseLock.lock();
        mouseJustDown.clear();
        mouseJustUp.clear();
        mouseLock.unlock();
        tm.clear();
    }

    @Override
    public HashSet<Integer> activeKeys() {
        HashSet<Integer> keysH = new HashSet<Integer>();
        for (int i = 0; i < keys.length; i++)
            if (keys[i])
                keysH.add(i);
        return keysH;
    }

    @Override
    public void shutdown() {
        GaBIEnImpl.activeDriverLock.lock();
        GaBIEnImpl.lastClosureDevice = frame.getGraphicsConfiguration().getDevice();
        GaBIEnImpl.activeDrivers.remove(this);
        GaBIEnImpl.activeDriverLock.unlock();
        super.shutdown();
        frame.setVisible(false);
    }

    @Override
    public void clearScissoring() {
        shadowScissorX = 0;
        shadowScissorY = 0;
        super.clearScissoring();
    }

    @Override
    public void adjustScissoring(int x, int y, int xt, int yt, int w, int h) {
        shadowScissorX += xt;
        shadowScissorY += yt;
        super.adjustScissoring(x, y, xt, yt, w, h);
    }

    @Override
    public String maintain(int x, int y, int width, String text) {
        return tm.maintain((shadowScissorX + x) * sc, (shadowScissorY + y) * sc, width * sc, text);
    }

    private void fuzzXY() {
        // Emulate difficulties positioning correctly with a touch interface
        mouseX += fuzzer.nextInt(17) - 8;
        mouseY += fuzzer.nextInt(17) - 8;
    }
}
