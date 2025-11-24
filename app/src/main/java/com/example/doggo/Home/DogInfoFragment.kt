package com.example.doggo.Home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.doggo.databinding.FragmentDogInfoBinding
import com.example.doggo.network.DogSchedule
import com.example.doggo.network.ScheduleDetail

class DogInfoFragment : Fragment() {

    private var _binding: FragmentDogInfoBinding? = null
    private val binding get() = _binding!!

    private var dogProfile: DogProfile? = null

    companion object {
        private const val ARG_DOG_PROFILE = "dog_profile"

        fun newInstance(dogProfile: DogProfile): DogInfoFragment {
            val fragment = DogInfoFragment()
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
        _binding = FragmentDogInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayDogInfo()
        displaySchedule()
    }

    private fun displayDogInfo() {
        dogProfile?.let { profile ->
            binding.apply {
                tvAgeValue.text = profile.age.toString()
                tvGenderValue.text = if (profile.gender.isNotEmpty()) profile.gender else "Not specified"
                tvWeightValue.text = if (profile.weight > 0) String.format("%.1f", profile.weight) else "N/A"

                if (profile.additionalInfo.isNotEmpty()) {
                    tvAdditionalInfo.text = profile.additionalInfo
                } else {
                    tvAdditionalInfo.text = "No additional information provided"
                }
            }
        }
    }

    private fun displaySchedule() {
        val schedule = dogProfile?.schedule

        Log.d("DogInfoFragment", "📅 Displaying schedule: $schedule")

        if (schedule == null) {
            binding.tvEmptySchedule.isVisible = true
            binding.llScheduleContainer.isVisible = false
            return
        }

        var hasAnySchedule = false

        // Display Eat Schedule
        if (!schedule.eat.isNullOrEmpty()) {
            hasAnySchedule = true
            binding.cvEatSchedule.isVisible = true
            binding.tvEatSchedule.text = formatScheduleList(schedule.eat)
        } else {
            binding.cvEatSchedule.isVisible = false
        }

        // Display Walk Schedule
        if (!schedule.walk.isNullOrEmpty()) {
            hasAnySchedule = true
            binding.cvWalkSchedule.isVisible = true
            binding.tvWalkSchedule.text = formatScheduleList(schedule.walk)
        } else {
            binding.cvWalkSchedule.isVisible = false
        }

        // Display Sleep Schedule
        if (!schedule.sleep.isNullOrEmpty()) {
            hasAnySchedule = true
            binding.cvSleepSchedule.isVisible = true
            binding.tvSleepSchedule.text = formatScheduleList(schedule.sleep)
        } else {
            binding.cvSleepSchedule.isVisible = false
        }

        // Display Medicine Schedule
        if (!schedule.medicine.isNullOrEmpty()) {
            hasAnySchedule = true
            binding.cvMedicineSchedule.isVisible = true
            binding.tvMedicineSchedule.text = formatScheduleList(schedule.medicine)
        } else {
            binding.cvMedicineSchedule.isVisible = false
        }

        // Display Groom Schedule
        if (!schedule.groom.isNullOrEmpty()) {
            hasAnySchedule = true
            binding.cvGroomSchedule.isVisible = true
            binding.tvGroomSchedule.text = formatScheduleList(schedule.groom)
        } else {
            binding.cvGroomSchedule.isVisible = false
        }

        // Show empty message if no schedule
        binding.tvEmptySchedule.isVisible = !hasAnySchedule
        binding.llScheduleContainer.isVisible = hasAnySchedule
    }

    private fun formatScheduleList(scheduleList: List<ScheduleDetail>): String {
        return scheduleList.joinToString("\n") { detail ->
            val time = detail.time.ifEmpty { "Not set" }
            val description = if (detail.description.isNotEmpty()) " - ${detail.description}" else ""
            val duration = if (!detail.duration.isNullOrEmpty()) " (${detail.duration})" else ""
            "⏰ $time$description$duration"
        }
    }

    fun updateDogProfile(profile: DogProfile) {
        dogProfile = profile
        if (_binding != null) {
            displayDogInfo()
            displaySchedule()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}