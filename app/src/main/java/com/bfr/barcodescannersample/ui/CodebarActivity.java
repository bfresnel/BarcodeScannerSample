package com.bfr.barcodescannersample.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class CodebarActivity extends AppCompatActivity implements ZBarScannerView.ResultHandler {
    private ZBarScannerView scannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scannerView = new ZBarScannerView(this);
        setContentView(scannerView);

    }

    @Override
    protected void onResume() {
        super.onResume();
        scannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        scannerView.startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        scannerView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        // If you would like to resume scanning, call this method below:
        scannerView.resumeCameraPreview(this);
    }
}
