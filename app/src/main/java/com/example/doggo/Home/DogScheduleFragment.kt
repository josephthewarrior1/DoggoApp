package com.example.doggo.Home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.doggo.databinding.FragmentDogScheduleBinding

class DogScheduleFragment : Fragment() {

    private var _binding: FragmentDogScheduleBinding? = null
    private val binding get() = _binding!!

    private var dogProfile: DogProfile? = null

    companion object {
        private const val ARG_DOG_PROFILE = "dog_profile"

        fun newInstance(dogProfile: DogProfile): DogScheduleFragment {
            val fragment = DogScheduleFragment()
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
        _binding = FragmentDogScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupButtons()
        loadSchedule()
    }

    private fun setupRecyclerView() {
        binding.rvSchedule.layoutManager = LinearLayoutManager(requireContext())
        // TODO: Set adapter when schedule model is ready
    }

    private fun setupButtons() {
        binding.btnAddSchedule.setOnClickListener {
            // TODO: Navigate to add schedule screen
            Toast.makeText(requireContext(), "Add Schedule Item (Coming Soon)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadSchedule() {
        // TODO: Load schedule from API/database
        // For now, show empty state
        showEmptyState(true)
    }

    private fun showEmptyState(show: Boolean) {
        if (show) {
            binding.emptyStateSchedule.visibility = View.VISIBLE
            binding.rvSchedule.visibility = View.GONE
        } else {
            binding.emptyStateSchedule.visibility = View.GONE
            binding.rvSchedule.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}