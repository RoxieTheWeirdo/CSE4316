package com.example.fitbite;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.util.Log;
import android.widget.Toast;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class BarcodeScanner extends AppCompatActivity {

    private ActivityResultLauncher<ScanOptions> barLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.barcode_scanner);

        Button barcodeScan = findViewById(R.id.scanButton);

        // Register scanner result
        barLauncher = registerForActivityResult(new ScanContract(), result -> {

            if (result.getContents() != null) {
                try {
                    String barcode = result.getContents();
                    Log.d("BARCODE", "Scanned: " + barcode);

                    //  Send barcode to BarcodeMoreInfo
                    Intent intent = new Intent(BarcodeScanner.this, BarcodeMoreInfo.class);
                    intent.putExtra("data", barcode);

                    startActivity(intent);

                    // optional (keep or remove depending on flow)
                    finish();

                } catch (Exception e) {
                    Log.e("BarcodeScanner", "Error handling scan", e);
                }

            } else {
                // user canceled scan
                Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
            }
        });

        barcodeScan.setOnClickListener(v -> scanCode());
    }

    private void scanCode() {
        ScanOptions options = new ScanOptions();

        options.setPrompt("Press volume up button to enable flash");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);

        barLauncher.launch(options);
    }
}