package com.example.doggo.Home.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.doggo.Home.medical.AddMedicalRecordActivity
import com.example.doggo.Home.medical.MedicalRecordAdapter
import com.example.doggo.R
import com.example.doggo.databinding.ActivityDogProfileDetailTabsBinding
import com.example.doggo.network.DogResponse
import com.example.doggo.network.MedicalRecord
import com.example.doggo.network.MedicalRecordsResponse
import com.example.doggo.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

class DogProfileDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDogProfileDetailTabsBinding
    private var dogProfile: DogProfile? = null
    private lateinit var medicalAdapter: MedicalRecordAdapter
    private val medicalRecords = mutableListOf<MedicalRecord>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDogProfileDetailTabsBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                        // Don't auto-load medical, only when user clicks button

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
            // Don't auto-load medical, only when user clicks button
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

            // Load photo using Glide
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

        Log.d("DogProfileDetail", "📱 Displaying: ${profile.name} - ${profile.breed}")
    }

    private fun displayDogDetails(profile: DogProfile) {
        binding.apply {
            // Display basic info
            tvBreedValue.text = profile.breed
            tvGenderValue.text = profile.gender.ifEmpty { "Unknown" }
            tvAgeValue.text = "${profile.age} years"

            // Format birth date if available
            // You might need to calculate this from age or get it from API
            tvBirthDateValue.text = "N/A" // Update this with actual birth date

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

        // Setup Schedule button
        binding.btnSchedule.setOnClickListener {
            showScheduleSection()
        }

        // Setup Medical Records button
        binding.btnMedicalRecords.setOnClickListener {
            showMedicalSection()
        }

        // Setup Add Medical button
        binding.btnAddMedical.setOnClickListener {
            val intent = Intent(this, AddMedicalRecordActivity::class.java)
            intent.putExtra("DOG_ID", dogProfile?.id?.toIntOrNull() ?: -1)
            startActivity(intent)
        }
    }

    private fun showScheduleSection() {
        // Show schedule, hide medical
        binding.layoutScheduleSection.visibility = View.VISIBLE
        binding.layoutMedicalSection.visibility = View.GONE

        // Update button styles
        binding.layoutScheduleButton.setBackgroundColor(getColor(android.R.color.transparent))
        binding.layoutScheduleButton.setBackgroundResource(R.color.blue_50)
        binding.layoutMedicalButton.setBackgroundColor(getColor(R.color.gray_50))
    }

    private fun showMedicalSection() {
        // Hide schedule, show medical
        binding.layoutScheduleSection.visibility = View.GONE
        binding.layoutMedicalSection.visibility = View.VISIBLE

        // Update button styles
        binding.layoutScheduleButton.setBackgroundColor(getColor(R.color.gray_50))
        binding.layoutMedicalButton.setBackgroundColor(getColor(android.R.color.transparent))
        binding.layoutMedicalButton.setBackgroundResource(R.color.blue_50)

        // Load medical records
        loadMedicalRecords(dogProfile?.id?.toIntOrNull() ?: -1)
    }

    private fun setupMedicalRecyclerView() {
        medicalAdapter = MedicalRecordAdapter(
            onItemClick = { record ->
                // TODO: Show medical record details
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

        Log.d("DogProfileDetail", "📡 Loading medical records for dog ID: $dogId")

        RetrofitClient.instance.getMedicalRecordsByDog(dogId)
            .enqueue(object : Callback<MedicalRecordsResponse> {
                override fun onResponse(
                    call: Call<MedicalRecordsResponse>,
                    response: Response<MedicalRecordsResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val records = response.body()?.medicalRecords?.values?.toList() ?: emptyList()

                        Log.d("DogProfileDetail", "✅ Found ${records.size} medical records")

                        if (records.isEmpty()) {
                            binding.cvEmptyMedical.visibility = View.VISIBLE
                            binding.rvMedicalRecords.visibility = View.GONE
                        } else {
                            binding.cvEmptyMedical.visibility = View.GONE
                            binding.rvMedicalRecords.visibility = View.VISIBLE
                            medicalAdapter.updateRecords(records)
                        }
                    } else {
                        Log.e("DogProfileDetail", "❌ Failed to load medical records")
                        binding.cvEmptyMedical.visibility = View.VISIBLE
                        binding.rvMedicalRecords.visibility = View.GONE
                    }
                }

                override fun onFailure(call: Call<MedicalRecordsResponse>, t: Throwable) {
                    Log.e("DogProfileDetail", "❌ Network error: ${t.message}")
                    binding.cvEmptyMedical.visibility = View.VISIBLE
                    binding.rvMedicalRecords.visibility = View.GONE
                }
            })
    }

    private fun showDeleteMedicalDialog(record: MedicalRecord) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Medical Record")
        builder.setMessage("Are you sure you want to delete this medical record: ${record.name}?")

        builder.setPositiveButton("Delete") { dialog, _ ->
            deleteMedicalRecord(record.medicalId)
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            ?.setTextColor(getColor(android.R.color.holo_red_dark))
    }

    private fun deleteMedicalRecord(medicalId: Int) {
        RetrofitClient.instance.deleteMedicalRecord(medicalId)
            .enqueue(object : Callback<com.example.doggo.network.MedicalRecordResponse> {
                override fun onResponse(
                    call: Call<com.example.doggo.network.MedicalRecordResponse>,
                    response: Response<com.example.doggo.network.MedicalRecordResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(this@DogProfileDetailActivity, "Medical record deleted", Toast.LENGTH_SHORT).show()
                        loadMedicalRecords(dogProfile?.id?.toIntOrNull() ?: -1)
                    } else {
                        Toast.makeText(this@DogProfileDetailActivity, "Failed to delete", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<com.example.doggo.network.MedicalRecordResponse>, t: Throwable) {
                    Toast.makeText(this@DogProfileDetailActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showDeleteConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Profile")
        builder.setMessage("Are you sure you want to delete ${dogProfile?.name}'s profile? This action cannot be undone.")

        builder.setPositiveButton("Delete") { dialog, _ ->
            deleteProfile()
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            ?.setTextColor(getColor(android.R.color.holo_red_dark))
    }

    private fun deleteProfile() {
        dogProfile?.let { profile ->
            // TODO: Implement API delete
            ProfileManager.removeProfile(profile.id)

            Toast.makeText(
                this,
                "${profile.name}'s profile has been deleted",
                Toast.LENGTH_SHORT
            ).show()

            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload profile when returning from edit
        dogProfile?.id?.let {
            loadDogFromAPI(it)
            // Reload medical records if medical section is visible
            if (binding.layoutMedicalSection.visibility == View.VISIBLE) {
                loadMedicalRecords(it.toIntOrNull() ?: -1)
            }
        }
    }
}