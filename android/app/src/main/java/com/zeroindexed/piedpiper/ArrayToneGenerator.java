package com.zeroindexed.piedpiper;

import java.util.Iterator;

public class ArrayToneGenerator /*implements ToneThread.ToneIterator*/ {
    final int[] frequencies;

    public ArrayToneGenerator(int[] frequencies) {
        this.frequencies = frequencies;
    }

    public int size() {
        return frequencies.length;
    }

    public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                return i < frequencies.length;
            }

            @Override
            public Integer next() {
                return frequencies[i++];
            }

            @Override
            public void remove() {
            }
        };
    }
}
