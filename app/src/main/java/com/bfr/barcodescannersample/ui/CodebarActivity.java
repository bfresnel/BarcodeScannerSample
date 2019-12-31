package com.bfr.barcodescannersample.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class CodebarActivity extends AppCompatActivity implements ZBarScannerView.ResultHandler {
    private ZBarScannerView scannerView;
    private boolean alertDisplayed = false;

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
        if (!alertDisplayed){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(rawResult.getContents())
                    .setTitle("Result")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            alertDisplayed = false;
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
            alertDisplayed = true;
        }
        scannerView.resumeCameraPreview(this);
    }
}
