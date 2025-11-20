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
    private lateinit var scheduleAdapter: ScheduleAdapter

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
        scheduleAdapter = ScheduleAdapter(mutableListOf())
        binding.rvSchedule.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = scheduleAdapter
        }
    }

    private fun setupButtons() {
        binding.btnAddSchedule.setOnClickListener {
            // TODO: Navigate to add schedule screen
            Toast.makeText(requireContext(), "Add Schedule Item (Coming Soon)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadSchedule() {
        val schedule = dogProfile?.schedule

        if (schedule == null) {
            showEmptyState(true)
            return
        }

        // Convert schedule to list of ScheduleItem
        val scheduleItems = mutableListOf<ScheduleItem>()

        // Add eat schedules
        schedule.eat?.forEach { detail ->
            scheduleItems.add(
                ScheduleItem(
                    type = ScheduleType.EAT,
                    time = detail.time,
                    description = detail.description,
                    duration = detail.duration
                )
            )
        }

        // Add walk schedules
        schedule.walk?.forEach { detail ->
            scheduleItems.add(
                ScheduleItem(
                    type = ScheduleType.WALK,
                    time = detail.time,
                    description = detail.description,
                    duration = detail.duration
                )
            )
        }

        // Add sleep schedules
        schedule.sleep?.forEach { detail ->
            scheduleItems.add(
                ScheduleItem(
                    type = ScheduleType.SLEEP,
                    time = detail.time,
                    description = detail.description,
                    duration = detail.duration
                )
            )
        }

        // Add medicine schedules
        schedule.medicine?.forEach { detail ->
            scheduleItems.add(
                ScheduleItem(
                    type = ScheduleType.MEDICINE,
                    time = detail.time,
                    description = detail.description,
                    duration = detail.duration
                )
            )
        }

        // Add groom schedules
        schedule.groom?.forEach { detail ->
            scheduleItems.add(
                ScheduleItem(
                    type = ScheduleType.GROOM,
                    time = detail.time,
                    description = detail.description,
                    duration = detail.duration
                )
            )
        }

        // Sort by time (optional)
        scheduleItems.sortBy { it.time }

        if (scheduleItems.isEmpty()) {
            showEmptyState(true)
        } else {
            showEmptyState(false)
            scheduleAdapter.updateSchedule(scheduleItems)
        }
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