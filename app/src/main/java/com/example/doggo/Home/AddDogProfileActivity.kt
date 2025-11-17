package com.example.doggo.Home

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.doggo.R
import com.example.doggo.databinding.ActivityAddDogProfileBinding
import com.example.doggo.network.RetrofitClient
import com.example.doggo.network.AddDogRequest
import com.example.doggo.network.DogSchedule
import com.example.doggo.network.ScheduleItem
import com.example.doggo.network.ApiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddDogProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddDogProfileBinding
    private val scheduleItems = mutableListOf<ScheduleItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddDogProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.cvProfilePhoto.setOnClickListener {
            Toast.makeText(this, "Image picker coming soon!", Toast.LENGTH_SHORT).show()
        }

        binding.btnSave.setOnClickListener {
            if (validateInputs()) {
                saveDogProfile()
            }
        }

        binding.btnAddSchedule.setOnClickListener {
            showScheduleDialog()
        }
    }

    private fun showScheduleDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_schedule, null)

        // Setup NumberPickers
        val npHour = dialogView.findViewById<NumberPicker>(R.id.npHour)
        val npMinute = dialogView.findViewById<NumberPicker>(R.id.npMinute)

        // Configure hour picker (0-23) - PROGRAMMATICALLY
        npHour.minValue = 0
        npHour.maxValue = 23
        npHour.value = 8 // Default to 8 AM
        npHour.setFormatter { i -> String.format("%02d", i) }
        npHour.wrapSelectorWheel = true

        // Configure minute picker (0-59) - PROGRAMMATICALLY
        npMinute.minValue = 0
        npMinute.maxValue = 59
        npMinute.value = 0 // Default to 00 minutes
        npMinute.setFormatter { i -> String.format("%02d", i) }
        npMinute.wrapSelectorWheel = true

        // Set default description based on selected type
        val etDescription = dialogView.findViewById<EditText>(R.id.etDescription)
        val rgScheduleType = dialogView.findViewById<RadioGroup>(R.id.rgScheduleType)

        rgScheduleType.setOnCheckedChangeListener { _, checkedId ->
            val defaultDescription = when (checkedId) {
                R.id.rbEat -> "Meal time"
                R.id.rbWalk -> "Walk time"
                R.id.rbSleep -> "Sleep time"
                R.id.rbMedicine -> "Medicine time"
                R.id.rbGroom -> "Grooming time"
                else -> "Activity time"
            }
            etDescription.setText(defaultDescription)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add Schedule Item")
            .setView(dialogView)
            .setPositiveButton("Add") { dialog, _ ->
                addScheduleItem(dialogView, npHour.value, npMinute.value)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }

    private fun addScheduleItem(dialogView: View, hour: Int, minute: Int) {
        val scheduleType = when (dialogView.findViewById<RadioGroup>(R.id.rgScheduleType).checkedRadioButtonId) {
            R.id.rbEat -> "eat"
            R.id.rbWalk -> "walk"
            R.id.rbSleep -> "sleep"
            R.id.rbMedicine -> "medicine"
            R.id.rbGroom -> "groom"
            else -> "eat"
        }

        val description = dialogView.findViewById<EditText>(R.id.etDescription).text.toString()
        val time = String.format("%02d:%02d", hour, minute)

        if (description.isNotEmpty()) {
            val scheduleItem = ScheduleItem(
                time = time,
                description = description,
                days = emptyList()
            )

            scheduleItems.add(scheduleItem)
            binding.tvScheduleCount.text = "Schedule items: ${scheduleItems.size}"

            Toast.makeText(this, "$scheduleType schedule added at $time", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Please enter description", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (binding.etDogName.text.isNullOrBlank()) {
            binding.tilDogName.error = "Please enter dog's name"
            isValid = false
        } else {
            binding.tilDogName.error = null
        }

        if (binding.etBreed.text.isNullOrBlank()) {
            binding.tilBreed.error = "Please enter breed"
            isValid = false
        } else {
            binding.tilBreed.error = null
        }

        if (binding.etAge.text.isNullOrBlank()) {
            binding.tilAge.error = "Please enter age"
            isValid = false
        } else {
            binding.tilAge.error = null
        }

        if (binding.etWeight.text.isNullOrBlank()) {
            binding.tilWeight.error = "Please enter weight"
            isValid = false
        } else {
            binding.tilWeight.error = null
        }

        return isValid
    }

    private fun saveDogProfile() {
        val name = binding.etDogName.text.toString()
        val breed = binding.etBreed.text.toString()
        val age = binding.etAge.text.toString().toIntOrNull() ?: 0
        val weight = binding.etWeight.text.toString().toDoubleOrNull() ?: 0.0
        val gender = if (binding.rbMale.isChecked) "Male" else "Female"

        // Create schedule from collected items
        val schedule = if (scheduleItems.isNotEmpty()) {
            DogSchedule(
                eat = scheduleItems.filter { it.description.contains("eat", true) || it.description.contains("food", true) || it.description.contains("meal", true) },
                walk = scheduleItems.filter { it.description.contains("walk", true) || it.description.contains("exercise", true) },
                sleep = scheduleItems.filter { it.description.contains("sleep", true) || it.description.contains("rest", true) },
                medicine = scheduleItems.filter { it.description.contains("medicine", true) || it.description.contains("pill", true) },
                groom = scheduleItems.filter { it.description.contains("groom", true) || it.description.contains("bath", true) }
            )
        } else {
            null // No schedule if user didn't add any
        }

        Toast.makeText(this, "Saving dog profile...", Toast.LENGTH_SHORT).show()

        val addDogRequest = AddDogRequest(
            name = name,
            breed = breed,
            age = age,
            weight = weight,
            gender = gender,
            schedule = schedule
        )

        RetrofitClient.instance.addDog(addDogRequest).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        Toast.makeText(
                            this@AddDogProfileActivity,
                            "Dog profile saved successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@AddDogProfileActivity,
                            apiResponse?.error ?: "Failed to save dog profile",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@AddDogProfileActivity,
                        "Failed to save: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(
                    this@AddDogProfileActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}