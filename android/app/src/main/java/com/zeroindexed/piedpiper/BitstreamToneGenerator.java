package com.zeroindexed.piedpiper;

import android.util.Log;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

public class BitstreamToneGenerator implements ToneThread.ToneIterator {
    final static String TAG = BitstreamToneGenerator.class.getName();

    final static int START_HZ = 1024;
    final static int STEP_HZ = 256;
    final static int BITS = 4;

    final static int HANDSHAKE_START_HZ = 8192;
    final static int HANDSHAKE_END_HZ = 8192 + 512;
    final static int FREQ_CONCURRENT = 2;

    final int file_size;
    final InputStream stream;

    public BitstreamToneGenerator(InputStream stream, int file_size) {
        this.stream = stream;
        this.file_size = file_size;
    }

    @Override
    public Iterator<Integer[]> iterator() {
        final Iterator<Integer> bits_iterator = new BitstreamIterator(stream, BITS).iterator();
        return new Iterator<Integer[]>() {
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
            public Integer[] next() {
                if (!yield_start) {
                    yield_start = true;
                    Log.i(TAG, "packet start");
                    return new Integer[] {HANDSHAKE_START_HZ};
                }

                if (!yield_end && !bits_iterator.hasNext()) {
                    yield_end = true;
                    Log.i(TAG, "packet end");
                    return new Integer[] {HANDSHAKE_END_HZ};
                }

                ArrayList<Integer> freq_chunk = new ArrayList<>();
                StringBuffer sb = new StringBuffer("chunk:");

                for (int i = 0; i < FREQ_CONCURRENT && bits_iterator.hasNext(); ++i) {
                    Integer step = bits_iterator.next();
                    Integer hz = START_HZ + step * STEP_HZ;
                    freq_chunk.add(hz);
                    sb.append(" ");
                    sb.append(step);
                    sb.append("[");
                    sb.append(hz);
                    sb.append(" hz]");
                }
                Log.i(TAG, sb.toString());

                // because toArray() can't be cast to Integer[]?
                Integer[] int_chunk = new Integer[freq_chunk.size()];
                for (int i = 0; i < int_chunk.length; ++i) {
                    int_chunk[i] = freq_chunk.get(i);
                }
                return int_chunk;
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
