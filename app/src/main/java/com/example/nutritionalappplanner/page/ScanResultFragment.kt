package com.example.nutritionalappplanner.page

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitbite.FoodItem
import com.example.fitbite.databinding.FragmentScanResultBinding
import kotlinx.coroutines.launch

class ScanResultFragment : Fragment() {

    private var _binding: FragmentScanResultBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ScanResultViewModel by viewModels()

    private var imageUri: String? = null
    private var imageBytes: ByteArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            imageUri = it.getString("image_uri")
            imageBytes = it.getByteArray("image_bytes")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Display the captured image
        imageUri?.let { uri ->
            binding.capturedImageView.setImageURI(Uri.parse(uri))
        }

        // Adapter with click listener
        val adapter = FoodListAdapter { item ->
            onFoodClicked(item)
        }
        binding.foodRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.foodRecyclerView.adapter = adapter

        // Observe ViewModel state
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is ScanState.Idle -> Unit

                    is ScanState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }

                    is ScanState.ClarifaiResult -> {
                        binding.progressBar.visibility = View.GONE
                        binding.detectedFoodName.text = "Detected: ${state.foodName}"
                    }

                    is ScanState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        adapter.submitList(state.foods)
                    }

                    is ScanState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Auto-trigger scan
        imageBytes?.let { bytes ->
            viewModel.scanImage(bytes)
        }
    }

    private fun onFoodClicked(item: FoodItem) {
        val fragment = FoodDetailFragment().apply {
            arguments = Bundle().apply {
                putString("foodName", item.name)
                putInt("calories", item.calories)
                putDouble("fat", item.fat)
                putDouble("carbs", item.carbs)
                putDouble("protein", item.protein)
            }
        }

        parentFragmentManager.beginTransaction()
            .replace(com.example.fitbite.R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(imageUri: String, imageBytes: ByteArray) = ScanResultFragment().apply {
            arguments = Bundle().apply {
                putString("image_uri", imageUri)
                putByteArray("image_bytes", imageBytes)
            }
        }
    }
}
