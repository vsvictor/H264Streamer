package com.mpeg;

import android.hardware.Camera;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private String TAG = "MainActivity";
    private static final int WIDTH = 320;
    private static final int HEIGHT = 240;

    private BufferedOutputStream out;
    private Camera camera;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private AvcEncoder enc;
    private File F;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//Encoding prepare...

        try {
            F = new File(Environment.getExternalStorageDirectory(),"stream.mp4");
            F.createNewFile();
            out = new BufferedOutputStream(new FileOutputStream(F));
        } catch (IOException e) {
            e.printStackTrace();
        }
        enc = new AvcEncoder(WIDTH, HEIGHT, out);

        initSurface();
    }
    @Override
    protected void onResume(){
        super.onResume();
        camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.setDisplayOrientation(90);
            //camera.setPreviewCallbackWithBuffer(preview);
            camera.setPreviewCallback(preview);
            Camera.Parameters param = camera.getParameters();
            param.setPictureSize(WIDTH, HEIGHT);
            param.setPreviewSize(WIDTH, HEIGHT);
            camera.setParameters(param);
            camera.startPreview();
        } catch (IOException ex) {
            Log.d(TAG, ex.getMessage());
        }

    }
    @Override
    protected void onPause(){
        super.onPause();
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }
    private void initSurface() {
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);

        surfaceHolder = surfaceView.getHolder();

        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.d(TAG, "surfaceCreated");
                try {
                    camera.setPreviewDisplay(surfaceHolder);
                    camera.setDisplayOrientation(90);
                    camera.startPreview();
                } catch (Exception ex) {
                    Log.d(TAG, "surfaceCreated exception: " + ex.getMessage());
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.d(TAG, "surfaceChanged");
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.d(TAG, "surfaceDestroyed");
            }
        });
    }
    Camera.PreviewCallback preview = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            enc.offerEncoder(data);
            Log.i(TAG, "Frame saved");
        }
    };
}
