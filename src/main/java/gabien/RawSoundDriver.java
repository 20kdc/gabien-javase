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
import java.util.concurrent.atomic.AtomicReference;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * Note that it may be a good idea to move part of this into GaBIEn-Common, and
 * make ISoundDriver take raw data.
 */
final class RawSoundDriver implements IRawAudioDriver, Runnable {
    SourceDataLine sdl;

    public RawSoundDriver() throws LineUnavailableException {
        AudioFormat af = new AudioFormat(22050, 16, 2, true, true);
        sdl = AudioSystem.getSourceDataLine(af);
        sdl.open(af, 8820);
        soundthread.start();
    }

    private Thread soundthread = new Thread(this);
    private AtomicReference<IRawAudioSource> source = new AtomicReference<IRawAudioSource>(new IRawAudioSource() {
        @Override
        public short[] pullData(int samples) {
            return new short[samples * 2];
        }
    });

    public byte[] createData(int amount) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            int[] L = new int[amount];
            int[] R = new int[amount];
            short[] r = source.get().pullData(amount);
            for (int px = 0; px < amount; px++) {
                L[px] += r[(px * 2)];
                R[px] += r[(px * 2) + 1];
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
            byte[] bytes = createData(a);

            sdl.write(bytes, 0, bytes.length);
            if (!sdl.isRunning()) {
                System.err.println("SOUND:needed restart...");
                sdl.start();
            }
        }
    }

    @Override
    public void setRawAudioSource(IRawAudioSource src) {
        source.set(src);
    }
}
