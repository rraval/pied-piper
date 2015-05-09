package com.zeroindexed.piedpiper;

import java.io.InputStream;
import java.util.Iterator;

public class BitstreamToneGenerator implements ToneThread.ToneIterator {
    final static int START_HZ = 1024;
    final static int STEP_HZ = 256;
    final static int BITS = 4;

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
            @Override
            public boolean hasNext() {
                return bits_iterator.hasNext();
            }

            @Override
            public Integer next() {
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
        return Math.round(file_size * ((float) Byte.SIZE) / BITS);
    }
}
