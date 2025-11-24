package com.example.doggo.Home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.doggo.R
import com.example.doggo.databinding.FragmentDogInfoBinding
import com.example.doggo.network.*
import com.example.doggo.notifications.ScheduleNotificationManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
        setupScheduleButtons()
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

    private fun setupScheduleButtons() {
        // Add button untuk setiap schedule type
        binding.cvEatSchedule.setOnLongClickListener {
            showScheduleOptionsDialog("eat")
            true
        }

        binding.cvWalkSchedule.setOnLongClickListener {
            showScheduleOptionsDialog("walk")
            true
        }

        binding.cvSleepSchedule.setOnLongClickListener {
            showScheduleOptionsDialog("sleep")
            true
        }

        binding.cvMedicineSchedule.setOnLongClickListener {
            showScheduleOptionsDialog("medicine")
            true
        }

        binding.cvGroomSchedule.setOnLongClickListener {
            showScheduleOptionsDialog("groom")
            true
        }

        // FAB untuk add schedule baru
        binding.fabAddSchedule.setOnClickListener {
            showAddScheduleDialog()
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
        return scheduleList.joinToString("\n\n") { detail ->
            val time = detail.time.ifEmpty { "Not set" }
            val description = if (detail.description.isNotEmpty()) "\n${detail.description}" else ""
            "⏰ $time$description"
        }
    }

    private fun showAddScheduleDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_schedule, null)
        val tilScheduleType = dialogView.findViewById<TextInputLayout>(R.id.tilScheduleType)
        val actvScheduleType = dialogView.findViewById<AutoCompleteTextView>(R.id.actvScheduleType)
        val etTime = dialogView.findViewById<TextInputEditText>(R.id.etTime)
        val etDescription = dialogView.findViewById<TextInputEditText>(R.id.etDescription)

        // Setup schedule type dropdown
        val scheduleTypes = listOf("🍽️ Eating", "🚶 Walking", "😴 Sleeping", "💊 Medicine", "✂️ Grooming")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, scheduleTypes)
        actvScheduleType.setAdapter(adapter)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        dialogView.findViewById<View>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<View>(R.id.btnSave).setOnClickListener {
            val selectedType = actvScheduleType.text.toString()
            val time = etTime.text.toString().trim()
            val description = etDescription.text.toString().trim()

            if (selectedType.isEmpty()) {
                tilScheduleType.error = "Please select schedule type"
                return@setOnClickListener
            }

            if (time.isEmpty()) {
                dialogView.findViewById<TextInputLayout>(R.id.tilTime).error = "Time is required"
                return@setOnClickListener
            }

            // Convert display name to API type
            val scheduleType = when (selectedType) {
                "🍽️ Eating" -> "eat"
                "🚶 Walking" -> "walk"
                "😴 Sleeping" -> "sleep"
                "💊 Medicine" -> "medicine"
                "✂️ Grooming" -> "groom"
                else -> "eat"
            }

            addScheduleToAPI(scheduleType, time, description)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showScheduleOptionsDialog(scheduleType: String) {
        val scheduleList = when (scheduleType) {
            "eat" -> dogProfile?.schedule?.eat
            "walk" -> dogProfile?.schedule?.walk
            "sleep" -> dogProfile?.schedule?.sleep
            "medicine" -> dogProfile?.schedule?.medicine
            "groom" -> dogProfile?.schedule?.groom
            else -> null
        } ?: return

        val items = scheduleList.map { "${it.time} - ${it.description.ifEmpty { "No description" }}" }.toTypedArray()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Manage ${scheduleType.capitalize()} Schedule")
            .setItems(items) { _, which ->
                val selectedSchedule = scheduleList[which]
                showScheduleActionDialog(scheduleType, selectedSchedule)
            }
            .setPositiveButton("Add New") { _, _ ->
                showAddScheduleDialogForType(scheduleType)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showScheduleActionDialog(scheduleType: String, scheduleDetail: ScheduleDetail) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Schedule Action")
            .setItems(arrayOf("Edit", "Delete")) { _, which ->
                when (which) {
                    0 -> showEditScheduleDialog(scheduleType, scheduleDetail)
                    1 -> deleteSchedule(scheduleType, scheduleDetail.id ?: "")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddScheduleDialogForType(scheduleType: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_schedule, null)
        val tvTitle = dialogView.findViewById<View>(R.id.tvDialogTitle) as? android.widget.TextView
        val tilScheduleType = dialogView.findViewById<TextInputLayout>(R.id.tilScheduleType)
        val etTime = dialogView.findViewById<TextInputEditText>(R.id.etTime)
        val etDescription = dialogView.findViewById<TextInputEditText>(R.id.etDescription)

        tvTitle?.text = "Add ${scheduleType.capitalize()} Schedule"
        tilScheduleType.visibility = View.GONE

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        dialogView.findViewById<View>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<View>(R.id.btnSave).setOnClickListener {
            val time = etTime.text.toString().trim()
            val description = etDescription.text.toString().trim()

            if (time.isEmpty()) {
                dialogView.findViewById<TextInputLayout>(R.id.tilTime).error = "Time is required"
                return@setOnClickListener
            }

            addScheduleToAPI(scheduleType, time, description)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showEditScheduleDialog(scheduleType: String, scheduleDetail: ScheduleDetail) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_schedule, null)
        val tvTitle = dialogView.findViewById<View>(R.id.tvDialogTitle) as? android.widget.TextView
        val tilScheduleType = dialogView.findViewById<TextInputLayout>(R.id.tilScheduleType)
        val etTime = dialogView.findViewById<TextInputEditText>(R.id.etTime)
        val etDescription = dialogView.findViewById<TextInputEditText>(R.id.etDescription)
        val btnSave = dialogView.findViewById<View>(R.id.btnSave) as? com.google.android.material.button.MaterialButton

        tvTitle?.text = "Edit Schedule"
        tilScheduleType.visibility = View.GONE
        btnSave?.text = "Update"

        // Pre-fill data
        etTime.setText(scheduleDetail.time)
        etDescription.setText(scheduleDetail.description)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        dialogView.findViewById<View>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<View>(R.id.btnSave).setOnClickListener {
            val time = etTime.text.toString().trim()
            val description = etDescription.text.toString().trim()

            if (time.isEmpty()) {
                dialogView.findViewById<TextInputLayout>(R.id.tilTime).error = "Time is required"
                return@setOnClickListener
            }

            updateScheduleInAPI(scheduleType, scheduleDetail.id ?: "", time, description)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun addScheduleToAPI(scheduleType: String, time: String, description: String) {
        val dogId = dogProfile?.id ?: return

        Log.d("DogInfoFragment", "📤 Adding schedule: $scheduleType at $time")

        val request = ScheduleRequest(
            scheduleType = scheduleType,
            time = time,
            description = description
        )

        RetrofitClient.instance.addSchedule(dogId, request).enqueue(object : Callback<ScheduleResponse> {
            override fun onResponse(call: Call<ScheduleResponse>, response: Response<ScheduleResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    Log.d("DogInfoFragment", "✅ Schedule added successfully")
                    Toast.makeText(requireContext(), "Schedule added!", Toast.LENGTH_SHORT).show()

                    // Schedule notification
                    val scheduleItem = response.body()?.scheduleItem
                    if (scheduleItem != null) {
                        ScheduleNotificationManager.scheduleNotification(
                            context = requireContext(),
                            dogId = dogId,
                            dogName = dogProfile?.name ?: "Dog",
                            scheduleType = scheduleType,
                            scheduleTime = time,
                            description = description,
                            scheduleId = scheduleItem.id
                        )
                    }

                    reloadDogProfile()
                } else {
                    Log.e("DogInfoFragment", "❌ Failed to add schedule: ${response.body()?.error}")
                    Toast.makeText(requireContext(), "Failed to add schedule", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ScheduleResponse>, t: Throwable) {
                Log.e("DogInfoFragment", "❌ Network error: ${t.message}")
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateScheduleInAPI(scheduleType: String, scheduleItemId: String, time: String, description: String) {
        val dogId = dogProfile?.id ?: return

        Log.d("DogInfoFragment", "📤 Updating schedule: $scheduleType - $scheduleItemId")

        val request = ScheduleUpdateRequest(
            scheduleType = scheduleType,
            scheduleItemId = scheduleItemId,
            time = time,
            description = description
        )

        RetrofitClient.instance.updateSchedule(dogId, request).enqueue(object : Callback<ScheduleResponse> {
            override fun onResponse(call: Call<ScheduleResponse>, response: Response<ScheduleResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    Log.d("DogInfoFragment", "✅ Schedule updated successfully")
                    Toast.makeText(requireContext(), "Schedule updated!", Toast.LENGTH_SHORT).show()

                    // Re-schedule notification dengan waktu baru
                    ScheduleNotificationManager.cancelNotification(
                        context = requireContext(),
                        dogId = dogId,
                        scheduleType = scheduleType,
                        scheduleId = scheduleItemId
                    )

                    ScheduleNotificationManager.scheduleNotification(
                        context = requireContext(),
                        dogId = dogId,
                        dogName = dogProfile?.name ?: "Dog",
                        scheduleType = scheduleType,
                        scheduleTime = time,
                        description = description,
                        scheduleId = scheduleItemId
                    )

                    reloadDogProfile()
                } else {
                    Log.e("DogInfoFragment", "❌ Failed to update schedule: ${response.body()?.error}")
                    Toast.makeText(requireContext(), "Failed to update schedule", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ScheduleResponse>, t: Throwable) {
                Log.e("DogInfoFragment", "❌ Network error: ${t.message}")
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deleteSchedule(scheduleType: String, scheduleItemId: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Schedule")
            .setMessage("Are you sure you want to delete this schedule?")
            .setPositiveButton("Delete") { _, _ ->
                deleteScheduleFromAPI(scheduleType, scheduleItemId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteScheduleFromAPI(scheduleType: String, scheduleItemId: String) {
        val dogId = dogProfile?.id ?: return

        Log.d("DogInfoFragment", "📤 Deleting schedule: $scheduleType - $scheduleItemId")

        val request = ScheduleDeleteRequest(
            scheduleType = scheduleType,
            scheduleItemId = scheduleItemId
        )

        RetrofitClient.instance.deleteSchedule(dogId, request).enqueue(object : Callback<ScheduleResponse> {
            override fun onResponse(call: Call<ScheduleResponse>, response: Response<ScheduleResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    Log.d("DogInfoFragment", "✅ Schedule deleted successfully")
                    Toast.makeText(requireContext(), "Schedule deleted!", Toast.LENGTH_SHORT).show()

                    // Cancel notification
                    ScheduleNotificationManager.cancelNotification(
                        context = requireContext(),
                        dogId = dogId,
                        scheduleType = scheduleType,
                        scheduleId = scheduleItemId
                    )

                    reloadDogProfile()
                } else {
                    Log.e("DogInfoFragment", "❌ Failed to delete schedule: ${response.body()?.error}")
                    Toast.makeText(requireContext(), "Failed to delete schedule", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ScheduleResponse>, t: Throwable) {
                Log.e("DogInfoFragment", "❌ Network error: ${t.message}")
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun reloadDogProfile() {
        val dogId = dogProfile?.id ?: return

        RetrofitClient.instance.getDogById(dogId).enqueue(object : Callback<DogResponse> {
            override fun onResponse(call: Call<DogResponse>, response: Response<DogResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val dogData = response.body()?.dog
                    if (dogData != null) {
                        dogProfile = DogProfile(
                            id = dogData.dogId.toString(),
                            name = dogData.name,
                            breed = dogData.breed,
                            age = dogData.age,
                            weight = dogData.weight ?: 0.0,
                            gender = dogData.gender ?: "",
                            photoUrl = dogData.photo ?: "",
                            additionalInfo = "",
                            schedule = dogData.schedule
                        )
                        displaySchedule()
                    }
                }
            }

            override fun onFailure(call: Call<DogResponse>, t: Throwable) {
                Log.e("DogInfoFragment", "❌ Failed to reload profile: ${t.message}")
            }
        })
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