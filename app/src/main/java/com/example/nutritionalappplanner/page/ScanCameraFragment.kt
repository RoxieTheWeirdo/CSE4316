package com.example.fitbite

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.fitbite.databinding.FragmentScanCameraBinding
import com.example.nutritionalappplanner.page.ScanResultFragment
import java.io.File

class ScanCameraFragment : Fragment() {

    private var _binding: FragmentScanCameraBinding? = null
    private val binding get() = _binding!!
    private fun setBottomNavVisible(visible: Boolean) {
        val bottomNav = requireActivity().findViewById<View>(R.id.bottom_nav)
        bottomNav?.visibility = if (visible) View.VISIBLE else View.GONE
    }
    override fun onResume() {
        super.onResume()
        setBottomNavVisible(false) // HIDE nav when camera opens
    }
    override fun onPause() {
        super.onPause()
        setBottomNavVisible(true) // SHOW nav when leaving
    }
    private var imageCapture: ImageCapture? = null

    // Register permission launcher
    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startCamera()
            } else {
                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check permissions using ActivityResult API
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        binding.captureButton.setOnClickListener {
            takePhoto()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder()
                .setJpegQuality(60)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                viewLifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            File(requireContext().cacheDir, "scan_${System.currentTimeMillis()}.jpg")
        ).build()

        imageCapture?.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val uri = output.savedUri
                    val file = File(uri?.path ?: return)

                    // Read bytes for Clarifai
                    val imageBytes = file.readBytes()

                    val fragment = ScanResultFragment.newInstance(
                        imageUri = uri.toString(),
                        imageBytes = imageBytes
                    )
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()
                }

                override fun onError(exc: ImageCaptureException) {
                    Log.e("SCAN", "Photo capture failed: ${exc.message}", exc)
                }
            }
        )
    }

    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        activity?.findViewById<View>(R.id.content_root)?.visibility = View.VISIBLE
    }
}
