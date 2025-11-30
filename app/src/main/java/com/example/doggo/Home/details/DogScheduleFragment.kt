package com.example.doggo.Home.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.doggo.Home.schedule.ScheduleAdapter
import com.example.doggo.Home.schedule.ScheduleItem
import com.example.doggo.Home.schedule.ScheduleType
import com.example.doggo.Home.profile.DogProfile
import com.example.doggo.databinding.FragmentDogScheduleBinding
import com.example.doggo.network.RetrofitClient
import com.example.doggo.network.ScheduleDeleteRequest
import com.example.doggo.network.ScheduleResponse
import com.example.doggo.network.ScheduleUpdateRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
        scheduleAdapter = ScheduleAdapter(
            scheduleItems = mutableListOf(),
            onDeleteClick = { scheduleItem, position ->
                showDeleteScheduleDialog(scheduleItem, position)
            },
            onItemClick = { scheduleItem, position ->
                showEditScheduleDialog(scheduleItem, position)
            }
        )
        binding.rvSchedule.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = scheduleAdapter
        }
    }

    private fun setupButtons() {
        binding.btnAddSchedule.setOnClickListener {
            showAddScheduleDialog()
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
                    id = detail.id,  // ← PENTING: Simpan ID dari backend
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
                    id = detail.id,  // ← PENTING
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
                    id = detail.id,  // ← PENTING
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
                    id = detail.id,  // ← PENTING
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
                    id = detail.id,  // ← PENTING
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

    private fun showAddScheduleDialog() {
        // TODO: Implement add schedule dialog or navigate to add screen
        Toast.makeText(requireContext(), "Add Schedule (Coming Soon)", Toast.LENGTH_SHORT).show()
    }

    private fun showEditScheduleDialog(scheduleItem: ScheduleItem, position: Int) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(android.R.layout.simple_list_item_2, null)

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Edit ${scheduleItem.type.displayName} Schedule")

        // Create edit text fields (simplified version)
        val timeInput = android.widget.EditText(requireContext())
        timeInput.setText(scheduleItem.time)
        timeInput.hint = "Time (HH:MM)"

        val descInput = android.widget.EditText(requireContext())
        descInput.setText(scheduleItem.description)
        descInput.hint = "Description"

        val layout = android.widget.LinearLayout(requireContext())
        layout.orientation = android.widget.LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)
        layout.addView(timeInput)
        layout.addView(descInput)

        builder.setView(layout)

        builder.setPositiveButton("Update") { dialog, _ ->
            val newTime = timeInput.text.toString().trim()
            val newDesc = descInput.text.toString().trim()

            if (newTime.isNotEmpty() && newDesc.isNotEmpty()) {
                updateScheduleItem(scheduleItem, newTime, newDesc)
            } else {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()
    }

    private fun updateScheduleItem(scheduleItem: ScheduleItem, newTime: String, newDescription: String) {
        val dogId = dogProfile?.id ?: return
        val scheduleItemId = scheduleItem.id

        if (scheduleItemId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Invalid schedule ID", Toast.LENGTH_SHORT).show()
            return
        }

        val updateRequest = ScheduleUpdateRequest(
            scheduleType = scheduleItem.type.apiName,  // ← Gunakan apiName
            scheduleItemId = scheduleItemId,
            time = newTime,
            description = newDescription
        )

        RetrofitClient.instance.updateSchedule(dogId, updateRequest)
            .enqueue(object : Callback<ScheduleResponse> {
                override fun onResponse(call: Call<ScheduleResponse>, response: Response<ScheduleResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(requireContext(), "Schedule updated successfully", Toast.LENGTH_SHORT).show()

                        // Refresh data dari server
                        refreshDogProfile()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Failed to update: ${response.body()?.error ?: response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ScheduleResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showDeleteScheduleDialog(scheduleItem: ScheduleItem, position: Int) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Delete Schedule")
        builder.setMessage("Are you sure you want to delete this ${scheduleItem.type.displayName} schedule at ${scheduleItem.time}?")

        builder.setPositiveButton("Delete") { dialog, _ ->
            deleteScheduleItem(scheduleItem, position)
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = builder.create()
        alertDialog.show()

        // Make delete button red
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            ?.setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
    }

    private fun deleteScheduleItem(scheduleItem: ScheduleItem, position: Int) {
        val dogId = dogProfile?.id ?: return
        val scheduleItemId = scheduleItem.id

        if (scheduleItemId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Invalid schedule ID", Toast.LENGTH_SHORT).show()
            return
        }

        val deleteRequest = ScheduleDeleteRequest(
            scheduleType = scheduleItem.type.apiName,  // ← Gunakan apiName
            scheduleItemId = scheduleItemId
        )

        RetrofitClient.instance.deleteSchedule(dogId, deleteRequest)
            .enqueue(object : Callback<ScheduleResponse> {
                override fun onResponse(call: Call<ScheduleResponse>, response: Response<ScheduleResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        scheduleAdapter.removeItem(position)
                        Toast.makeText(requireContext(), "Schedule deleted successfully", Toast.LENGTH_SHORT).show()

                        // Check if list is now empty
                        if (scheduleAdapter.itemCount == 0) {
                            showEmptyState(true)
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Failed to delete: ${response.body()?.error ?: response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ScheduleResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun refreshDogProfile() {
        val dogId = dogProfile?.id ?: return

        RetrofitClient.instance.getDogById(dogId).enqueue(object : Callback<com.example.doggo.network.DogResponse> {
            override fun onResponse(
                call: Call<com.example.doggo.network.DogResponse>,
                response: Response<com.example.doggo.network.DogResponse>
            ) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val dogData = response.body()?.dog
                    dogData?.let {
                        dogProfile = DogProfile(
                            id = it.dogId.toString(),
                            name = it.name,
                            breed = it.breed,
                            age = it.age,
                            weight = it.weight ?: 0.0,
                            gender = it.gender ?: "",
                            photoUrl = it.photo ?: "",
                            additionalInfo = "",
                            schedule = it.schedule
                        )
                        loadSchedule()
                    }
                }
            }

            override fun onFailure(call: Call<com.example.doggo.network.DogResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Failed to refresh data", Toast.LENGTH_SHORT).show()
            }
        })
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