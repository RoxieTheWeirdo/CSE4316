package com.example.fitbite;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class BarcodeScanner extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.barcode_scanner);

        Button barcodeScan = findViewById(R.id.scanButton);

        barcodeScan.setOnClickListener(v -> {
            Intent intent = new Intent(BarcodeScanner.this, BarcodeScannerAfter.class);
            startActivity(intent);
        });

    }
}
