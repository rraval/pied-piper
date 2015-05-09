package com.zeroindexed.piedpiper;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class ToneThread extends Thread {
    public interface ToneCallback {
        public void onProgress(int current, int total);

        public void onDone();
    }

    public interface ToneIterator extends Iterable<Integer[]> {
        public int size();
    }

    static final int sample_rate = 44100;
    static final float duration = 0.1f;
    static final int sample_size = Math.round(duration * sample_rate);

    final ToneIterator frequencies;
    final ToneCallback callback;
    boolean callback_done = false;

    public ToneThread(ToneIterator frequencies, ToneCallback callback) {
        this.frequencies = frequencies;
        this.callback = callback;
        setPriority(Thread.MAX_PRIORITY);
    }

    @Override
    public void run() {
        final AudioTrack track = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                sample_rate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                2 * sample_size,
                AudioTrack.MODE_STREAM
        );

        final int total_samples = Math.round(frequencies.size() * sample_size);

        track.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
            @Override
            public void onMarkerReached(AudioTrack track) {
                if (!callback_done) {
                    callback.onDone();
                    callback_done = true;
                }
            }

            @Override
            public void onPeriodicNotification(AudioTrack track) {
                if (!callback_done) {
                    callback.onProgress(track.getPlaybackHeadPosition(), total_samples);
                }
            }
        });
        track.setPositionNotificationPeriod(sample_rate / 10);

        track.play();

        for (Integer[] freq_chunk : frequencies) {
            double[][] samples = new double[freq_chunk.length][];
            for (int i = 0; i < freq_chunk.length; ++i) {
                samples[i] = generate(freq_chunk[i]);
            }

            short[] merged_sample = new short[sample_size];
            for (int i = 0; i < merged_sample.length; ++i) {
                double s = 0;
                for (int j = 0; j < freq_chunk.length; ++j) {
                    s += samples[j][i];
                }
                merged_sample[i] = (short) ((s / freq_chunk.length) * Short.MAX_VALUE);
            }

            track.write(merged_sample, 0, merged_sample.length);
        }

        track.setNotificationMarkerPosition(sample_size);
    }

    static double[] generate(float frequency) {
        final double sample[] = new double[sample_size];
        final double increment = 2 * Math.PI * frequency / sample_rate;

        double angle = 0;
        for (int i = 0; i < sample.length; ++i) {
            sample[i] = Math.sin(angle);
            angle += increment;
        }

        return sample;
    }
}
