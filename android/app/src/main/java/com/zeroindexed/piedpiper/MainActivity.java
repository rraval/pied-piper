package com.zeroindexed.piedpiper;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.casualcoding.reedsolomon.EncoderDecoder;
import com.google.zxing.common.reedsolomon.ReedSolomonEncoder;
import com.google.zxing.common.reedsolomon.Util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;


public class MainActivity extends ActionBarActivity implements ToneThread.ToneCallback {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int FEC_BYTES = 1;

    ImageView picture_preview;
    View play_tone;
    ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.take_picture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });
        picture_preview = (ImageView) findViewById(R.id.picture_preview);

        play_tone = findViewById(R.id.play_tone);
        progress = (ProgressBar) findViewById(R.id.progress);
        play_tone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] payload = new byte[]{
                        13, 56, 81, 89, 107, 19, (byte) 251,
                };

                EncoderDecoder encoder = new EncoderDecoder();
                final byte[] fec_payload;
                try {
                    fec_payload = encoder.encodeData(payload, FEC_BYTES);
                } catch (EncoderDecoder.DataTooLargeException e) {
                    return;
                }

                ByteArrayInputStream bis = new ByteArrayInputStream(fec_payload);

                play_tone.setEnabled(false);
                ToneThread.ToneIterator tone = new BitstreamToneGenerator(bis, 7);
                new ToneThread(tone, MainActivity.this).start();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            final Bitmap bm = (Bitmap) extras.get("data");
            picture_preview.setImageBitmap(bm);

            final File dir = MainActivity.this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            Log.i("DEBUG", "got bitmap: " + bm.getWidth() + "x" + bm.getHeight());
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        File output = File.createTempFile("photo-", ".jpg", dir);
                        FileOutputStream fos = new FileOutputStream(output);
                        bm.compress(Bitmap.CompressFormat.JPEG, 50, fos);
                        fos.close();

                        Log.e("DEBUG", "file size: " + output.length());
                    } catch (IOException e) {
                        Log.e("DEBUG", "fail", e);
                    }
                    return null;
                }
            }.execute();
        }
    }
}
