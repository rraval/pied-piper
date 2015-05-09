package com.zeroindexed.piedpiper;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.casualcoding.reedsolomon.EncoderDecoder;
import com.google.zxing.common.reedsolomon.Util;


public class MainActivity extends ActionBarActivity implements ToneThread.ToneCallback {
    View play_tone;
    ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        play_tone = findViewById(R.id.play_tone);
        progress = (ProgressBar) findViewById(R.id.progress);
        play_tone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play_tone.setEnabled(false);
                new ToneThread(new float[]{1024, 2048, 1024, 2048, 1024, 2048}, MainActivity.this).start();
            }
        });

        findViewById(R.id.encode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    EncoderDecoder enc = new EncoderDecoder();
                    String message = new String("EncoderDecoder Example");
                    byte[] data = message.getBytes();
                    byte[] encoded_data = enc.encodeData(data, 5);
                    Log.i("DEBUG", "encoded: " + Util.toHex(encoded_data));
                } catch (EncoderDecoder.DataTooLargeException e) {
                    Log.e("DEBUG", "data too large", e);
                }
            }
        });
    }

    @Override
    public void onProgress(int current, int total) {
        progress.setMax(total);
        progress.setProgress(current);
    }

    @Override
    public void onDone() {
        play_tone.setEnabled(true);
        progress.setProgress(0);
    }
}
