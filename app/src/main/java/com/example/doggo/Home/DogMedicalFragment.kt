package com.example.doggo.Home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.doggo.databinding.FragmentDogMedicalBinding

class DogMedicalFragment : Fragment() {

    private var _binding: FragmentDogMedicalBinding? = null
    private val binding get() = _binding!!

    private var dogProfile: DogProfile? = null

    companion object {
        private const val ARG_DOG_PROFILE = "dog_profile"

        fun newInstance(dogProfile: DogProfile): DogMedicalFragment {
            val fragment = DogMedicalFragment()
            val args = Bundle()
            args.putSerializable(ARG_DOG_PROFILE, dogProfile)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            dogProfile = it.getSerializable(ARG_DOG_PROFILE) as? DogProfile
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDogMedicalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupButtons()
        loadMedicalRecords()
    }

    private fun setupRecyclerView() {
        binding.rvMedicalRecords.layoutManager = LinearLayoutManager(requireContext())
        // TODO: Set adapter when medical records model is ready
    }

    private fun setupButtons() {
        binding.btnAddMedical.setOnClickListener {
            // TODO: Navigate to add medical record screen
            Toast.makeText(requireContext(), "Add Medical Record (Coming Soon)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadMedicalRecords() {
        // TODO: Load medical records from API/database
        // For now, show empty state
        showEmptyState(true)
    }

    private fun showEmptyState(show: Boolean) {
        if (show) {
            binding.emptyStateMedical.visibility = View.VISIBLE
            binding.rvMedicalRecords.visibility = View.GONE
        } else {
            binding.emptyStateMedical.visibility = View.GONE
            binding.rvMedicalRecords.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}