package com.example.cameron.barcodescansample1;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.util.jar.Manifest;

public class MainActivity extends AppCompatActivity {

    private TextView barcodeInfo;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private SurfaceView cameraView;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //check if permissions, get them if they do not exist
        if(PackageManager.PERMISSION_DENIED == ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.CAMERA)){
            requestCameraPermission(MainActivity.this);
        } else {
            doTheWork();
        }
    }

    protected void doTheWork(){
        cameraView = (SurfaceView) findViewById(R.id.cameraView);
        barcodeInfo = (TextView) findViewById(R.id.code_info);

        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.UPC_A | Barcode.UPC_E | Barcode.EAN_8 | Barcode.EAN_13)
                .build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(720, 640)
                .setAutoFocusEnabled(true)
                .build();

        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    cameraSource.start(cameraView.getHolder());
                } catch (IOException e) {
                    Log.e("Camera Source Error", e.getMessage());
                } catch (SecurityException se) {
                    Log.e("Security Exception", se.getMessage());
                    Toast.makeText(MainActivity.this, "Permissions Issue", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                if (barcodes.size() != 0) {
                    barcodeInfo.post(new Runnable() {
                        public void run() {
                            barcodeInfo.setText(barcodes.valueAt(0).displayValue);
                        }
                    });
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraSource.release();
        barcodeDetector.release();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    doTheWork();
                } else {
                    Log.e("Permissions denied", "");
                    System.exit(0);
                }
            }
        }
    }

    private static void requestCameraPermission(final Context context){
        if(ActivityCompat.shouldShowRequestPermissionRationale((MainActivity)context,android.Manifest.permission.CAMERA)) {
            new AlertDialog.Builder(context)
                    .setMessage("This app needs permission to use the Camera")
                    .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((MainActivity) context,
                                    new String[]{android.Manifest.permission.CAMERA},
                                    MY_PERMISSIONS_REQUEST_CAMERA);
                        }
                    }).show();

        }else {
            ActivityCompat.requestPermissions((MainActivity) context, new String[]{android.Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);
        }
    }
}
