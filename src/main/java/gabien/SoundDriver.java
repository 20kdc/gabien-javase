/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */


package gabien;

import gabien.ISoundDriver;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * Note that it may be a good idea to move part of this into GaBIEn-Common, and
 * make ISoundDriver take raw data.
 */
final class SoundDriver implements ISoundDriver, Runnable {
    SourceDataLine sdl;

    public SoundDriver() throws LineUnavailableException {
        AudioFormat af = new AudioFormat(22050, 16, 2, true, true);
        sdl = AudioSystem.getSourceDataLine(af);
        sdl.open(af, 8820);
        soundthread.start();
    }

    private Thread soundthread = new Thread(this);
    private JAVAX_Channel[] channels = new JAVAX_Channel[128];

    public class JAVAX_Channel implements IChannel {
        short[] data;
        double pos = 0;
        double VL = 0, VR = 0, P = 1;
        boolean looping = true;

        public short WrappingGetIndex(double index) {
            return (short) lerp(data[wrapind((int) Math.floor(index))],
                    data[wrapind(((int) Math.floor(index)) + 1)],
                    index - Math.floor(index));
        }

        private int wrapind(int i) {
            while (i < 0)
                i += data.length;
            while (i >= data.length)
                i -= data.length;
            return i;
        }

        public short Get() {
            try {
                if (data == null)
                    return 0;

                short a = WrappingGetIndex(pos);
                pos += P;
                while (pos < 0)
                    pos += data.length;
                if (pos >= data.length) {
                    pos = pos % data.length;
                    if (!looping) {
                        VL = 0;
                        VR = 0;
                    }
                }
                return a;
            } catch (Exception e) {
                pos = 0;
                return 0;
            }
        }

        public JAVAX_Channel() {

        }

        public short Scale(short a, double V) {
            double b = a * V;
            if (b > 32767)
                b = 32767;
            if (b < -32768)
                b = -32768;
            return (short) b;
        }

        public short[] CreateData(int amount) {
            short[] s = new short[amount * 2];
            for (int px = 0; px < amount; px++) {
                short V = Get();
                s[(px * 2) + 0] = Scale(V, VL);
                s[(px * 2) + 1] = Scale(V, VR);
            }
            return s;
        }

        @Override
        public void playSound(double Pitch, double VolL, double VolR,
                              short[] sound, boolean isLooping) {
            VL = VolL;
            VR = VolR;
            // Sanity check
            if (P <= 0)
                P = 1;
            if (P == Double.POSITIVE_INFINITY)
                P = 1;
            if (P == Double.NEGATIVE_INFINITY)
                P = -1;
            P = Pitch;
            data = new short[sound.length];
            for (int p = 0; p < data.length; p++)
                data[p] = sound[p];
            pos = 0;
            looping = isLooping;
        }

        @Override
        public void setVolume(double VolL, double VolR) {
            VL = VolL;
            VR = VolR;
        }

        @Override
        public double volL() {
            return VL;
        }

        @Override
        public double volR() {
            return VR;
        }

        private double lerp(double s, double s0, double d) {
            double diff = s0 - s;
            diff *= d;
            return s + diff;
        }

    }

    @Override
    public IChannel createChannel() {
        JAVAX_Channel jc = new JAVAX_Channel();
        for (int p = 0; p < channels.length; p++) {
            if (channels[p] == null) {
                channels[p] = jc;
                break;
            }
        }
        return jc;
    }

    public byte[] CreateData(int amount) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            int[] L = new int[amount];
            int[] R = new int[amount];
            for (JAVAX_Channel js : channels) {
                if (js == null)
                    continue;
                short[] r = js.CreateData(amount);
                for (int px = 0; px < amount; px++) {
                    L[px] += r[(px * 2)];
                    R[px] += r[(px * 2) + 1];
                }
            }
            for (int px = 0; px < amount; px++) {
                if (L[px] > 32767)
                    L[px] = 32767;
                if (L[px] < -32768)
                    L[px] = -32768;
                if (R[px] > 32767)
                    R[px] = 32767;
                if (R[px] < -32768)
                    R[px] = -32768;
                dos.writeShort(L[px]);
                dos.writeShort(R[px]);
            }
            dos.flush();
            byte[] bytes = baos.toByteArray();
            return bytes;
        } catch (IOException ex) {
            return null;
        }
    }

    private boolean alive = true;

    @Override
    public void run() {
        int buf = 800; // Increase this to decrease CPU usage, but at a cost of latency
        while (alive) {
            int a = (sdl.available() / 4);
            // a is in sample frames now.
            // So a/2205 == amount of 10-millisecond blocks you can sleep in.
            while (a < buf) {
                try {
                    Thread.sleep((2205 - a) / 221);
                } catch (InterruptedException e) {
                }
                a = (sdl.available() / 4);
            }
            a = (sdl.available() / 4);
            if (a <= 0) {
                continue;
            }
            byte[] bytes = CreateData(a);

            sdl.write(bytes, 0, bytes.length);
            if (!sdl.isRunning()) {
                System.err.println("SOUND:needed restart...");
                sdl.start();
            }
        }

    }

    @Override
    public void deleteChannel(IChannel ic) {

        for (int p = 0; p < channels.length; p++) {
            if (channels[p] == ic)
                channels[p] = null;
        }
    }

}
