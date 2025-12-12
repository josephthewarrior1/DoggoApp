package com.example.doggo.Home.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.doggo.Home.medical.AddMedicalRecordActivity
import com.example.doggo.Home.medical.MedicalRecordAdapter
import com.example.doggo.Home.schedule.ScheduleAdapter
import com.example.doggo.R
import com.example.doggo.databinding.ActivityDogProfileDetailTabsBinding
import com.example.doggo.network.*
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DogProfileDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDogProfileDetailTabsBinding
    private var dogProfile: DogProfile? = null
    private lateinit var medicalAdapter: MedicalRecordAdapter

    // Schedule adapters (cuma 3 yang ada di layout)
    private lateinit var eatScheduleAdapter: ScheduleAdapter
    private lateinit var walkScheduleAdapter: ScheduleAdapter
    private lateinit var sleepScheduleAdapter: ScheduleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDogProfileDetailTabsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupScheduleRecyclerViews()
        setupMedicalRecyclerView()
        loadDogProfile()
        setupUI()
    }

    private fun loadDogProfile() {
        val profileId = intent.getStringExtra("DOG_PROFILE_ID")

        if (profileId != null) {
            Log.d("DogProfileDetail", "🔍 Loading dog with ID: $profileId")
            loadDogFromAPI(profileId)
        } else {
            Toast.makeText(this, "Invalid profile ID", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadDogFromAPI(profileId: String) {
        Log.d("DogProfileDetail", "📡 Loading dog from API...")

        RetrofitClient.instance.getDogById(profileId).enqueue(object : Callback<DogResponse> {
            override fun onResponse(call: Call<DogResponse>, response: Response<DogResponse>) {
                if (response.isSuccessful) {
                    val dogResponse = response.body()

                    if (dogResponse?.success == true && dogResponse.dog != null) {
                        val dogData = dogResponse.dog
                        Log.d("DogProfileDetail", "✅ Dog found in API: ${dogData.name}")
                        Log.d("DogProfileDetail", "📅 Schedule data: ${dogData.schedule}")

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

                        displayDogProfile(dogProfile!!)
                        displayDogDetails(dogProfile!!)
                        loadScheduleData(dogProfile!!)

                    } else {
                        Log.e("DogProfileDetail", "❌ Dog not found in API, trying local...")
                        loadDogFromLocal(profileId)
                    }
                } else {
                    Log.e("DogProfileDetail", "❌ HTTP error: ${response.code()}")
                    loadDogFromLocal(profileId)
                }
            }

            override fun onFailure(call: Call<DogResponse>, t: Throwable) {
                Log.e("DogProfileDetail", "❌ Network error: ${t.message}")
                loadDogFromLocal(profileId)
            }
        })
    }

    private fun loadDogFromLocal(profileId: String) {
        dogProfile = ProfileManager.getAllProfiles().find { it.id == profileId }

        if (dogProfile != null) {
            Log.d("DogProfileDetail", "✅ Dog found in local: ${dogProfile!!.name}")
            displayDogProfile(dogProfile!!)
            displayDogDetails(dogProfile!!)
            loadScheduleData(dogProfile!!)
        } else {
            Log.e("DogProfileDetail", "❌ Dog not found anywhere")
            Toast.makeText(this, "Profile not found", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun displayDogProfile(profile: DogProfile) {
        binding.apply {
            tvDogName.text = profile.name
            tvBreed.text = profile.breed

            if (profile.photoUrl.isNotEmpty()) {
                Log.d("DogProfileDetail", "🖼️ Loading photo from: ${profile.photoUrl}")
                Glide.with(this@DogProfileDetailActivity)
                    .load(profile.photoUrl)
                    .centerCrop()
                    .placeholder(R.drawable.ic_dog_placeholder)
                    .error(R.drawable.ic_dog_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(ivDogPhoto)
            } else {
                Log.d("DogProfileDetail", "📷 No photo URL, using placeholder")
                ivDogPhoto.setImageResource(R.drawable.ic_dog_placeholder)
            }
        }
    }

    private fun displayDogDetails(profile: DogProfile) {
        binding.apply {
            tvBreedValue.text = profile.breed
            tvGenderValue.text = profile.gender.ifEmpty { "Unknown" }
            tvAgeValue.text = "${profile.age} years"
            tvBirthDateValue.text = "N/A"
            tvWeightValue.text = "${profile.weight} kg"
        }
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnEdit.setOnClickListener {
            val intent = Intent(this, EditDogProfileActivity::class.java)
            intent.putExtra("DOG_ID", dogProfile?.id?.toIntOrNull() ?: -1)
            startActivity(intent)
        }

        binding.btnDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        binding.btnSchedule.setOnClickListener {
            showScheduleSection()
        }

        binding.btnMedicalRecords.setOnClickListener {
            showMedicalSection()
        }

        binding.btnAddMedical.setOnClickListener {
            val intent = Intent(this, AddMedicalRecordActivity::class.java)
            intent.putExtra("DOG_ID", dogProfile?.id?.toIntOrNull() ?: -1)
            startActivity(intent)
        }

        // ✅ FIX: Add Schedule button
        binding.btnAddSchedule.setOnClickListener {
            showAddScheduleDialog()
        }
    }

    // ✅ NEW: Show Add Schedule Dialog
    private fun showAddScheduleDialog() {
        try {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_schedule, null)
            val tilScheduleType = dialogView.findViewById<TextInputLayout>(R.id.tilScheduleType)
            val actvScheduleType = dialogView.findViewById<AutoCompleteTextView>(R.id.actvScheduleType)
            val tilTime = dialogView.findViewById<TextInputLayout>(R.id.tilTime)
            val etTime = dialogView.findViewById<TextInputEditText>(R.id.etTime)
            val etDescription = dialogView.findViewById<TextInputEditText>(R.id.etDescription)

            // Setup dropdown
            val scheduleTypes = listOf("🍽️ Eating", "🚶 Walking", "😴 Sleeping", "💊 Medicine", "✂️ Grooming")
            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, scheduleTypes)
            actvScheduleType.setAdapter(adapter)

            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create()

            dialogView.findViewById<View>(R.id.btnCancel)?.setOnClickListener {
                dialog.dismiss()
            }

            dialogView.findViewById<View>(R.id.btnSave)?.setOnClickListener {
                val selectedType = actvScheduleType.text.toString()
                val time = etTime.text?.toString()?.trim() ?: ""
                val description = etDescription.text?.toString()?.trim() ?: ""

                // Reset errors
                tilScheduleType.error = null
                tilTime.error = null

                // Validation
                if (selectedType.isEmpty()) {
                    tilScheduleType.error = "Please select schedule type"
                    return@setOnClickListener
                }

                if (time.isEmpty()) {
                    tilTime.error = "Time is required"
                    return@setOnClickListener
                }

                if (!time.matches(Regex("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$"))) {
                    tilTime.error = "Invalid time format (use HH:MM)"
                    return@setOnClickListener
                }

                // Convert display name to API type
                val scheduleType = when (selectedType) {
                    "🍽️ Eating" -> "eat"
                    "🚶 Walking" -> "walk"
                    "😴 Sleeping" -> "sleep"
                    "💊 Medicine" -> "medicine"
                    "✂️ Grooming" -> "groom"
                    else -> {
                        Toast.makeText(this, "Invalid schedule type", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                }

                addScheduleToAPI(scheduleType, time, description)
                dialog.dismiss()
            }

            dialog.show()
        } catch (e: Exception) {
            Log.e("DogProfileDetail", "❌ Error showing dialog: ${e.message}", e)
            Toast.makeText(this, "Error opening dialog: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // ✅ NEW: Add schedule to API
    private fun addScheduleToAPI(scheduleType: String, time: String, description: String) {
        val dogId = dogProfile?.id

        if (dogId.isNullOrEmpty()) {
            Toast.makeText(this, "Invalid dog ID", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("DogProfileDetail", "📤 Adding schedule: $scheduleType at $time")

        val request = ScheduleRequest(
            scheduleType = scheduleType,
            time = time,
            description = description
        )

        RetrofitClient.instance.addSchedule(dogId, request).enqueue(object : Callback<ScheduleResponse> {
            override fun onResponse(call: Call<ScheduleResponse>, response: Response<ScheduleResponse>) {
                Log.d("DogProfileDetail", "📡 Response: ${response.code()} - ${response.body()}")

                if (response.isSuccessful && response.body()?.success == true) {
                    Log.d("DogProfileDetail", "✅ Schedule added successfully")
                    Toast.makeText(this@DogProfileDetailActivity, "Schedule added!", Toast.LENGTH_SHORT).show()
                    loadDogFromAPI(dogId) // Reload data
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("DogProfileDetail", "❌ Failed: ${response.body()?.error}")
                    Log.e("DogProfileDetail", "❌ Error body: $errorBody")
                    Toast.makeText(this@DogProfileDetailActivity, "Failed to add schedule", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ScheduleResponse>, t: Throwable) {
                Log.e("DogProfileDetail", "❌ Network error: ${t.message}", t)
                Toast.makeText(this@DogProfileDetailActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupScheduleRecyclerViews() {
        // Eat Schedule
        eatScheduleAdapter = ScheduleAdapter(
            onItemClick = { schedule ->
                showEditScheduleDialog("eat", schedule)
            },
            onDeleteClick = { schedule ->
                showDeleteScheduleDialog(schedule, "eat")
            }
        )
        binding.rvEatSchedule.apply {
            layoutManager = LinearLayoutManager(this@DogProfileDetailActivity)
            adapter = eatScheduleAdapter
        }

        // Walk Schedule
        walkScheduleAdapter = ScheduleAdapter(
            onItemClick = { schedule ->
                showEditScheduleDialog("walk", schedule)
            },
            onDeleteClick = { schedule ->
                showDeleteScheduleDialog(schedule, "walk")
            }
        )
        binding.rvWalkSchedule.apply {
            layoutManager = LinearLayoutManager(this@DogProfileDetailActivity)
            adapter = walkScheduleAdapter
        }

        // Sleep Schedule
        sleepScheduleAdapter = ScheduleAdapter(
            onItemClick = { schedule ->
                showEditScheduleDialog("sleep", schedule)
            },
            onDeleteClick = { schedule ->
                showDeleteScheduleDialog(schedule, "sleep")
            }
        )
        binding.rvSleepSchedule.apply {
            layoutManager = LinearLayoutManager(this@DogProfileDetailActivity)
            adapter = sleepScheduleAdapter
        }
    }

    // ✅ NEW: Edit Schedule Dialog
    private fun showEditScheduleDialog(scheduleType: String, schedule: ScheduleDetail) {
        try {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_schedule, null)
            val tilScheduleType = dialogView.findViewById<TextInputLayout>(R.id.tilScheduleType)
            val tilTime = dialogView.findViewById<TextInputLayout>(R.id.tilTime)
            val etTime = dialogView.findViewById<TextInputEditText>(R.id.etTime)
            val etDescription = dialogView.findViewById<TextInputEditText>(R.id.etDescription)

            tilScheduleType.visibility = View.GONE
            etTime.setText(schedule.time)
            etDescription.setText(schedule.description)

            val dialog = AlertDialog.Builder(this)
                .setTitle("Edit Schedule")
                .setView(dialogView)
                .setCancelable(true)
                .create()

            dialogView.findViewById<View>(R.id.btnCancel)?.setOnClickListener {
                dialog.dismiss()
            }

            dialogView.findViewById<View>(R.id.btnSave)?.setOnClickListener {
                val time = etTime.text?.toString()?.trim() ?: ""
                val description = etDescription.text?.toString()?.trim() ?: ""

                tilTime.error = null

                if (time.isEmpty()) {
                    tilTime.error = "Time is required"
                    return@setOnClickListener
                }

                if (!time.matches(Regex("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$"))) {
                    tilTime.error = "Invalid time format (use HH:MM)"
                    return@setOnClickListener
                }

                updateScheduleInAPI(scheduleType, schedule.id ?: "", time, description)
                dialog.dismiss()
            }

            dialog.show()
        } catch (e: Exception) {
            Log.e("DogProfileDetail", "❌ Error: ${e.message}", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // ✅ NEW: Update schedule
    private fun updateScheduleInAPI(scheduleType: String, scheduleItemId: String, time: String, description: String) {
        val dogId = dogProfile?.id ?: return

        if (scheduleItemId.isEmpty()) {
            Toast.makeText(this, "Invalid schedule ID", Toast.LENGTH_SHORT).show()
            return
        }

        val request = ScheduleUpdateRequest(
            scheduleType = scheduleType,
            scheduleItemId = scheduleItemId,
            time = time,
            description = description
        )

        RetrofitClient.instance.updateSchedule(dogId, request).enqueue(object : Callback<ScheduleResponse> {
            override fun onResponse(call: Call<ScheduleResponse>, response: Response<ScheduleResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@DogProfileDetailActivity, "Schedule updated!", Toast.LENGTH_SHORT).show()
                    loadDogFromAPI(dogId)
                } else {
                    Toast.makeText(this@DogProfileDetailActivity, "Failed to update", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ScheduleResponse>, t: Throwable) {
                Toast.makeText(this@DogProfileDetailActivity, "Network error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadScheduleData(profile: DogProfile) {
        val schedule = profile.schedule

        if (schedule == null) {
            binding.cvEmptySchedule.visibility = View.VISIBLE
            return
        }

        binding.cvEmptySchedule.visibility = View.GONE

        // Load cuma 3 schedule yang ada di layout
        schedule.eat?.let { if (it.isNotEmpty()) eatScheduleAdapter.updateSchedules(it) }
        schedule.walk?.let { if (it.isNotEmpty()) walkScheduleAdapter.updateSchedules(it) }
        schedule.sleep?.let { if (it.isNotEmpty()) sleepScheduleAdapter.updateSchedules(it) }

        // Medicine & Groom ga ada RecyclerView nya di layout, jadi skip
    }

    private fun showDeleteScheduleDialog(schedule: ScheduleDetail, scheduleType: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete Schedule")
            .setMessage("Are you sure you want to delete: ${schedule.description}?")
            .setPositiveButton("Delete") { _, _ ->
                deleteSchedule(schedule, scheduleType)
            }
            .setNegativeButton("Cancel", null)
            .create()
            .apply {
                show()
                getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(getColor(android.R.color.holo_red_dark))
            }
    }

    private fun deleteSchedule(schedule: ScheduleDetail, scheduleType: String) {
        val dogId = dogProfile?.id ?: return
        val scheduleItemId = schedule.id

        if (scheduleItemId.isNullOrEmpty()) {
            Toast.makeText(this, "Invalid schedule ID", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("DogProfileDetail", "🗑️ Deleting: $scheduleType - $scheduleItemId")

        val request = ScheduleDeleteRequest(
            scheduleType = scheduleType,
            scheduleItemId = scheduleItemId
        )

        RetrofitClient.instance.deleteSchedule(dogId, request).enqueue(object : Callback<ScheduleResponse> {
            override fun onResponse(call: Call<ScheduleResponse>, response: Response<ScheduleResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    Log.d("DogProfileDetail", "✅ Delete success, refreshing...")
                    Toast.makeText(this@DogProfileDetailActivity, "Schedule deleted!", Toast.LENGTH_SHORT).show()

                    // ✅ Manual update: Remove item dari adapter
                    when (scheduleType) {
                        "eat" -> {
                            dogProfile?.schedule?.eat?.let { list ->
                                val newList = list.filter { it.id != scheduleItemId }
                                if (newList.isNotEmpty()) {
                                    eatScheduleAdapter.updateSchedules(newList)
                                } else {
                                    // Kalau list kosong, reload dari API
                                    loadDogFromAPI(dogId)
                                }
                            }
                        }
                        "walk" -> {
                            dogProfile?.schedule?.walk?.let { list ->
                                val newList = list.filter { it.id != scheduleItemId }
                                if (newList.isNotEmpty()) {
                                    walkScheduleAdapter.updateSchedules(newList)
                                } else {
                                    loadDogFromAPI(dogId)
                                }
                            }
                        }
                        "sleep" -> {
                            dogProfile?.schedule?.sleep?.let { list ->
                                val newList = list.filter { it.id != scheduleItemId }
                                if (newList.isNotEmpty()) {
                                    sleepScheduleAdapter.updateSchedules(newList)
                                } else {
                                    loadDogFromAPI(dogId)
                                }
                            }
                        }
                    }

                    // ✅ Reload full data untuk sync
                    loadDogFromAPI(dogId)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("DogProfileDetail", "❌ Delete failed: ${response.body()?.error}")
                    Log.e("DogProfileDetail", "❌ Error body: $errorBody")
                    Toast.makeText(this@DogProfileDetailActivity, "Failed to delete", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ScheduleResponse>, t: Throwable) {
                Log.e("DogProfileDetail", "❌ Network error: ${t.message}", t)
                Toast.makeText(this@DogProfileDetailActivity, "Network error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showScheduleSection() {
        binding.layoutScheduleSection.visibility = View.VISIBLE
        binding.layoutMedicalSection.visibility = View.GONE
        binding.layoutScheduleButton.setBackgroundColor(0xFFE3F2FD.toInt())
        binding.layoutMedicalButton.setBackgroundColor(0xFFF5F5F5.toInt())
    }

    private fun showMedicalSection() {
        binding.layoutScheduleSection.visibility = View.GONE
        binding.layoutMedicalSection.visibility = View.VISIBLE
        binding.layoutScheduleButton.setBackgroundColor(0xFFF5F5F5.toInt())
        binding.layoutMedicalButton.setBackgroundColor(0xFFE3F2FD.toInt())
        loadMedicalRecords(dogProfile?.id?.toIntOrNull() ?: -1)
    }

    private fun setupMedicalRecyclerView() {
        medicalAdapter = MedicalRecordAdapter(
            onItemClick = { record ->
                Toast.makeText(this, "Medical record: ${record.name}", Toast.LENGTH_SHORT).show()
            },
            onItemLongClick = { record ->
                showDeleteMedicalDialog(record)
            }
        )
        binding.rvMedicalRecords.apply {
            layoutManager = LinearLayoutManager(this@DogProfileDetailActivity)
            adapter = medicalAdapter
        }
    }

    private fun loadMedicalRecords(dogId: Int) {
        if (dogId == -1) return

        RetrofitClient.instance.getMedicalRecordsByDog(dogId).enqueue(object : Callback<MedicalRecordsResponse> {
            override fun onResponse(call: Call<MedicalRecordsResponse>, response: Response<MedicalRecordsResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val records = response.body()?.medicalRecords?.values?.toList() ?: emptyList()
                    if (records.isEmpty()) {
                        binding.cvEmptyMedical.visibility = View.VISIBLE
                        binding.rvMedicalRecords.visibility = View.GONE
                    } else {
                        binding.cvEmptyMedical.visibility = View.GONE
                        binding.rvMedicalRecords.visibility = View.VISIBLE
                        medicalAdapter.updateRecords(records)
                    }
                } else {
                    binding.cvEmptyMedical.visibility = View.VISIBLE
                    binding.rvMedicalRecords.visibility = View.GONE
                }
            }

            override fun onFailure(call: Call<MedicalRecordsResponse>, t: Throwable) {
                binding.cvEmptyMedical.visibility = View.VISIBLE
                binding.rvMedicalRecords.visibility = View.GONE
            }
        })
    }

    private fun showDeleteMedicalDialog(record: MedicalRecord) {
        AlertDialog.Builder(this)
            .setTitle("Delete Medical Record")
            .setMessage("Delete ${record.name}?")
            .setPositiveButton("Delete") { _, _ ->
                deleteMedicalRecord(record.medicalId)
            }
            .setNegativeButton("Cancel", null)
            .create()
            .apply {
                show()
                getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(getColor(android.R.color.holo_red_dark))
            }
    }

    private fun deleteMedicalRecord(medicalId: Int) {
        RetrofitClient.instance.deleteMedicalRecord(medicalId).enqueue(object : Callback<MedicalRecordResponse> {
            override fun onResponse(call: Call<MedicalRecordResponse>, response: Response<MedicalRecordResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@DogProfileDetailActivity, "Medical record deleted", Toast.LENGTH_SHORT).show()
                    loadMedicalRecords(dogProfile?.id?.toIntOrNull() ?: -1)
                } else {
                    Toast.makeText(this@DogProfileDetailActivity, "Failed to delete", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MedicalRecordResponse>, t: Throwable) {
                Toast.makeText(this@DogProfileDetailActivity, "Network error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Profile")
            .setMessage("Delete ${dogProfile?.name}'s profile? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteProfile()
            }
            .setNegativeButton("Cancel", null)
            .create()
            .apply {
                show()
                getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(getColor(android.R.color.holo_red_dark))
            }
    }

    private fun deleteProfile() {
        dogProfile?.let { profile ->
            ProfileManager.removeProfile(profile.id)
            Toast.makeText(this, "${profile.name}'s profile deleted", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        dogProfile?.id?.let {
            loadDogFromAPI(it)
            if (binding.layoutMedicalSection.visibility == View.VISIBLE) {
                loadMedicalRecords(it.toIntOrNull() ?: -1)
            }
        }
    }
}