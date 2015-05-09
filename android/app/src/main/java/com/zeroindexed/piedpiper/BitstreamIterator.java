package com.zeroindexed.piedpiper;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public class BitstreamIterator implements Iterable<Integer> {
    final InputStream stream;
    final int bits;

    public BitstreamIterator(InputStream stream, int bits) {
        this.stream = stream;
        this.bits = bits;
    }

    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {
            final byte[] buffer = new byte[8192];
            int buffer_size = 0;

            int next_read_byte = 0;
            int next_read_bit = 0;

            @Override
            public boolean hasNext() {
                if (next_read_byte == buffer_size) {
                    next_read_byte = 0;
                    try {
                        buffer_size = stream.read(buffer, 0, buffer.length);
                    } catch (IOException e) {
                        Log.e("DEBUG", "IO fail", e);
                        buffer_size = 0;
                    }
                }

                return buffer_size > 0;
            }

            @Override
            public Integer next() {
                int out = 0;
                int bits_left = bits;

                while (bits_left > 0) {
                    if (!hasNext()) {
                        break;
                    }

                    int can_fill = Byte.SIZE - next_read_bit;
                    int to_fill = Math.min(can_fill, bits_left);
                    int offset = Byte.SIZE - next_read_bit - to_fill;

                    out <<= to_fill;
                    int shifted_bits =  buffer[next_read_byte] & (((1 << to_fill) - 1) << offset);
                    out |= shifted_bits >> offset;
                    bits_left -= to_fill;
                    next_read_bit += to_fill;

                    if (next_read_bit >= Byte.SIZE) {
                        ++next_read_byte;
                        next_read_bit -= Byte.SIZE;
                    }
                }

                //Log.i("BitStream", "yield -> " + out);
                return out;
            }

            @Override
            public void remove() {
            }
        };
    }
}
