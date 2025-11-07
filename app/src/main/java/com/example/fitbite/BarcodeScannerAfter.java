package com.example.fitbite;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;


public class BarcodeScannerAfter extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.barcode_scanner_after);

        Bundle bundle = getIntent().getExtras();

        String data = bundle.getString("data");

        TextView textView = (TextView) findViewById(R.id.textView);

        textView.setText(data);

        Button barcodeScan = findViewById(R.id.scanAnotherButton);

        barcodeScan.setOnClickListener(v -> {
            scanCode();
        });
    }

    private void scanCode() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Press volume up to enable flash");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result -> {
        Intent intent = new Intent(this, BarcodeScannerAfter.class);

        if(result.getContents() != null) {
            try {
                intent.putExtra("data", result.getContents());
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
            catch (Exception e) {
                Log.e("BarcodeScannerAfter", "An error occurred", e);
            }
        }
    });      
    
}
