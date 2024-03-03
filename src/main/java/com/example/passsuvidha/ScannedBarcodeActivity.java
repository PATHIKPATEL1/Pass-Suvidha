package com.example.passsuvidha;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;

public class ScannedBarcodeActivity extends AppCompatActivity {


    private SurfaceView qr_code_scan_surfaceView;
    private TextView txtBarcodeValue;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private StorageReference mStorageRef;
    private ProgressDialog progressDialog_qr_code_scan;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    private String intentData = "",pass_user_info[],url;
    private int temp=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanned_barcode);


        Toolbar toolbar = findViewById(R.id.qr_code_scan_toolbar);
        toolbar.setTitle("Scan QR Code");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mStorageRef = FirebaseStorage.getInstance().getReference();
        initViews();
    }

    private void initViews() {
        txtBarcodeValue = findViewById(R.id.txtBarcodeValue);
        qr_code_scan_surfaceView = findViewById(R.id.qr_code_scan_surfaceView);
    }

    private void initialiseDetectorsAndSources() {

        Toast.makeText(getApplicationContext(), "QR Code scanner started", Toast.LENGTH_SHORT).show();
        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true)
                .build();

        qr_code_scan_surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(ScannedBarcodeActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(qr_code_scan_surfaceView.getHolder());
                    } else {
                        ActivityCompat.requestPermissions(ScannedBarcodeActivity.this, new
                                String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
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
                    txtBarcodeValue.post(new Runnable() {
                        @Override
                        public void run() {

                                intentData = barcodes.valueAt(0).displayValue;

                                if(intentData.contains("_")){
                                    pass_user_info=intentData.split("_");
                                    getPassPDF();
                                }
                                else {
                                    Toast.makeText(getApplicationContext(), "Invalid QR Code", Toast.LENGTH_SHORT).show();
                                }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                finish();
                startActivity(getIntent());
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        cameraSource.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initialiseDetectorsAndSources();
    }

    private void getPassPDF(){
        if(temp==1){
            temp=0;
        progressbar_show();
        StorageReference reference=mStorageRef.child("Passes/"+pass_user_info[0]+"/pass_"+pass_user_info[1]+".pdf");

        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                url = uri.toString();
                openPdf();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ScannedBarcodeActivity.this, "Pass is not available", Toast.LENGTH_LONG).show();
                progressDialog_qr_code_scan.dismiss();
                finish();
            }
        });
        }
    }

    private void openPdf(){
        try
        {
            Intent intent=new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(url),"application/pdf");
            startActivity(Intent.createChooser(intent,"Choose an Application:"));
        }catch (ActivityNotFoundException e){
            Toast.makeText(ScannedBarcodeActivity.this,"Application is not available to view PDF",Toast.LENGTH_LONG).show();
        }
        progressDialog_qr_code_scan.dismiss();
        finish();
    }


    private void progressbar_show()
    {
        progressDialog_qr_code_scan= ProgressDialog.show(this,"Please wait...","Loading Please wait...",true,false);
    }

}