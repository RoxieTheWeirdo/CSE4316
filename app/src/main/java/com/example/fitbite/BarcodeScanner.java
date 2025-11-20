package com.example.fitbite;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.util.Log;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class BarcodeScanner extends AppCompatActivity {
        
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.barcode_scanner);

        Button barcodeScan = findViewById(R.id.scanButton);

        barcodeScan.setOnClickListener(v -> {
            scanCode();
        });
    }

    private void scanCode() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Press volume up button to enable flash");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result -> {
        Intent intent = new Intent(BarcodeScanner.this, BarcodeScannerAfter.class);

        if(result.getContents() != null) {
            try {
                intent.putExtra("data", result.getContents());
                startActivity(intent);
                finish();
            }
            catch (Exception e) {
                Log.e("BarcodeScanner", "An error occurred", e);
            }
        }
    });           
}
