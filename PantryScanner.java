package com.example.pantryscanner;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.exifinterface.media.ExifInterface;


import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.ExecutionException;

public class PantryScanner extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private ImageCapture imageCapture;
    private TextView blinkText;
    private final Handler handler = new Handler();
    private boolean isVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pantryscanner_activity);

        blinkText = findViewById(R.id.blinkText);
        startBlinking();

        Button newSuggestionButton = findViewById(R.id.newSuggestionButton);
        newSuggestionButton.setOnClickListener(v ->
                Toast.makeText(PantryScanner.this, "New suggestion clicked", Toast.LENGTH_SHORT).show()
        );

        // Check camera permission
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissions(new String[]{android.Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        }
    }

    private void startBlinking() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                blinkText.setVisibility(isVisible ? View.INVISIBLE : View.VISIBLE);
                isVisible = !isVisible;
                handler.postDelayed(this, 500);
            }
        }, 500);
    }

    private void startCamera() {
        PreviewView previewView = findViewById(R.id.previewView);

        previewView.setOnClickListener(v -> {
            takePhoto();
            v.animate().alpha(0f).setDuration(80).withEndAction(() ->
                    v.animate().alpha(1f).setDuration(150)
            );
        });

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                cameraProvider.unbindAll();

                previewView.post(() -> {
                    int rotation = previewView.getDisplay().getRotation();

                    Preview preview = new Preview.Builder()
                            .setTargetRotation(rotation)
                            .build();

                    imageCapture = new ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .setTargetRotation(rotation)
                            .build();

                    preview.setSurfaceProvider(previewView.getSurfaceProvider());

                    CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                    cameraProvider.bindToLifecycle(
                            this,
                            cameraSelector,
                            preview,
                            imageCapture
                    );
                });

            } catch (ExecutionException | InterruptedException e) {
                Log.e("PantryScanner", "Camera initialization failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture == null) {
            Toast.makeText(this, "Camera not ready", Toast.LENGTH_SHORT).show();
            return;
        }

        File photoFile = new File(getExternalFilesDir(null),
                "photo_" + System.currentTimeMillis() + ".jpg");

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        correctImageRotation(photoFile.getAbsolutePath());
                        Toast.makeText(PantryScanner.this,
                                "Image saved: " + photoFile.getAbsolutePath(),
                                Toast.LENGTH_SHORT).show();
                        Log.d("PantryScanner", "Saved image: " + photoFile.getAbsolutePath());
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(PantryScanner.this,
                                "Failed to save image: " + exception.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        Log.e("PantryScanner", "Image capture failed", exception);
                    }
                });
    }

    private void correctImageRotation(String imagePath) {
        try {
            androidx.exifinterface.media.ExifInterface exif =
                    new androidx.exifinterface.media.ExifInterface(imagePath);

            int orientation = exif.getAttributeInt(
                    androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION,
                    androidx.exifinterface.media.ExifInterface.ORIENTATION_UNDEFINED
            );

            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

            Bitmap rotatedBitmap = switch (orientation) {
                case androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90);
                case androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180);
                case androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270);
                default -> bitmap;
            };

            try (FileOutputStream out = new FileOutputStream(imagePath)) {
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            }

            Log.d("PantryScanner", "Rotation corrected for " + imagePath);

        } catch (Exception e) {
            Log.e("PantryScanner", "Failed to correct rotation", e);
        }
    }
    private Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }
}