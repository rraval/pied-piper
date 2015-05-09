package com.zeroindexed.piedpiper;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;


public class MainActivity extends ActionBarActivity {
    static final int sample_rate = 44100;
    static final float duration = 0.125f;
    static final int sample_size = Math.round(duration * sample_rate);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.play_tone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        setPriority(Thread.MAX_PRIORITY);

                        final AudioTrack track = new AudioTrack(
                                AudioManager.STREAM_MUSIC,
                                sample_rate,
                                AudioFormat.CHANNEL_OUT_MONO,
                                AudioFormat.ENCODING_PCM_16BIT,
                                2 * sample_size,
                                AudioTrack.MODE_STREAM
                        );
                        track.play();

                        float[] frequencies = new float[]{
                                1024, 2048, 4096, 8192, 12000,
                        };

                        for (float freq : frequencies) {
                            short[] samples = generate(freq);
                            track.write(samples, 0, samples.length);
                        }
                    }
                }.start();
            }
        });
    }

    static short[] generate(float frequency) {
        final short sample[] = new short[sample_size];
        final double increment = 2 * Math.PI * frequency / sample_rate;

        double angle = 0;
        for (int i = 0; i < sample.length; ++i) {
            sample[i] = (short) (Math.sin(angle) * Short.MAX_VALUE);
            angle += increment;
        }

        return sample;
    }
}
