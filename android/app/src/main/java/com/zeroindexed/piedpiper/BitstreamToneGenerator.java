package com.zeroindexed.piedpiper;

import java.io.InputStream;
import java.util.Iterator;

public class BitstreamToneGenerator implements ToneThread.ToneIterator {
    final static int START_HZ = 1024;
    final static int STEP_HZ = 256;
    final static int BITS = 4;

    final static int HANDSHAKE_START_HZ = 8192;
    final static int HANDSHAKE_END_HZ = 8192 + 512;

    final int file_size;
    final InputStream stream;

    public BitstreamToneGenerator(InputStream stream, int file_size) {
        this.stream = stream;
        this.file_size = file_size;
    }

    @Override
    public Iterator<Integer> iterator() {
        final Iterator<Integer> bits_iterator = new BitstreamIterator(stream, BITS).iterator();
        return new Iterator<Integer>() {
            boolean yield_start = false;
            boolean yield_end = false;

            @Override
            public boolean hasNext() {
                if (!yield_start || !yield_end) {
                    return true;
                }

                return bits_iterator.hasNext();
            }

            @Override
            public Integer next() {
                if (!yield_start) {
                    yield_start = true;
                    return HANDSHAKE_START_HZ;
                }

                if (!yield_end && !bits_iterator.hasNext()) {
                    yield_end = true;
                    return HANDSHAKE_END_HZ;
                }

                Integer step = bits_iterator.next();
                return START_HZ + step * STEP_HZ;
            }

            @Override
            public void remove() {
            }
        };
    }

    @Override
    public int size() {
        // +2 for handshake
        return Math.round(file_size * ((float) Byte.SIZE) / BITS) + 2;
    }
}
