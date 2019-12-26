package com.bfr.barcodescannersample.ui;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bfr.barcodescannersample.R;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = (Button) findViewById(R.id.button);
        final ImageView imageCodebar = (ImageView) findViewById(R.id.imageView);
        final TextView textToDisplay = (TextView) findViewById(R.id.txtContent);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Activate barcode detector
                BarcodeDetector detector = new BarcodeDetector.Builder(getApplicationContext())
                        .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
                        .build();

                if (!detector.isOperational()){
                    textToDisplay.setText("Unable to set the detector :(");
                    return;
                }

                // Load image
                Bitmap myBitmap = BitmapFactory.decodeResource(
                        getApplicationContext().getResources(),
                        R.drawable.puppy);
                imageCodebar.setImageBitmap(myBitmap);

                // Detect the barcode
                Frame frame = new Frame.Builder().setBitmap(myBitmap).build();
                SparseArray<Barcode> barcodes = detector.detect(frame);

                // Decode the barcode
                Barcode thisCode = barcodes.valueAt(0);
                textToDisplay.setText(thisCode.rawValue);
            }
        });
    }
}
