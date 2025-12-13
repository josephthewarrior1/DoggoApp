package com.example.doggo.Home.medical

import android.R
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.doggo.databinding.ActivityAddMedicalRecordBinding
import com.example.doggo.network.AddMedicalRecordRequest
import com.example.doggo.network.MedicalRecordResponse
import com.example.doggo.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddMedicalRecordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddMedicalRecordBinding
    private var dogId: Int = -1
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMedicalRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dogId = intent.getIntExtra("DOG_ID", -1)

        if (dogId == -1) {
            Toast.makeText(this, "Invalid dog ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupUI()
        setupTypeDropdown()
        setupDatePickers()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnSave.setOnClickListener {
            saveMedicalRecord()
        }

        // Toggle next due date field based on checkbox
        binding.switchReminder.setOnCheckedChangeListener { _, isChecked ->
            binding.tilNextDueDate.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
    }

    private fun setupTypeDropdown() {
        val types = listOf("Vaccine", "Checkup", "Treatment", "Surgery", "Medication")
        val adapter = ArrayAdapter(this, R.layout.simple_dropdown_item_1line, types)
        binding.actvType.setAdapter(adapter)
    }

    private fun setupDatePickers() {
        val calendar = Calendar.getInstance()

        binding.etDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    binding.etDate.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.etNextDueDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    binding.etNextDueDate.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun saveMedicalRecord() {
        val type = binding.actvType.text.toString().trim().lowercase()
        val name = binding.etName.text.toString().trim()
        val date = binding.etDate.text.toString().trim()
        val veterinarian = binding.etVeterinarian.text.toString().trim()
        val clinic = binding.etClinic.text.toString().trim()
        val notes = binding.etNotes.text.toString().trim()
        val nextDueDate = if (binding.switchReminder.isChecked) {
            binding.etNextDueDate.text.toString().trim()
        } else null

        // Validation
        if (type.isEmpty()) {
            binding.tilType.error = "Type is required"
            return
        }

        if (name.isEmpty()) {
            binding.tilName.error = "Name is required"
            return
        }

        if (date.isEmpty()) {
            binding.tilDate.error = "Date is required"
            return
        }

        if (binding.switchReminder.isChecked && nextDueDate.isNullOrEmpty()) {
            binding.tilNextDueDate.error = "Next due date is required"
            return
        }

        // Show loading
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSave.isEnabled = false

        val request = AddMedicalRecordRequest(
            dogId = dogId,
            type = type,
            name = name,
            date = date,
            nextDueDate = nextDueDate,
            veterinarian = veterinarian.ifEmpty { null },
            clinic = clinic.ifEmpty { null },
            notes = notes.ifEmpty { null },
            reminderEnabled = binding.switchReminder.isChecked
        )

        Log.d("AddMedicalRecord", "📤 Saving medical record: $request")

        RetrofitClient.instance.addMedicalRecord(request)
            .enqueue(object : Callback<MedicalRecordResponse> {
                override fun onResponse(
                    call: Call<MedicalRecordResponse>,
                    response: Response<MedicalRecordResponse>
                ) {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled = true

                    if (response.isSuccessful && response.body()?.success == true) {
                        Log.d("AddMedicalRecord", "✅ Medical record saved")
                        Toast.makeText(
                            this@AddMedicalRecordActivity,
                            "Medical record added!",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Log.e("AddMedicalRecord", "❌ Failed: ${response.body()?.error}")
                        Toast.makeText(
                            this@AddMedicalRecordActivity,
                            "Failed to add medical record",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<MedicalRecordResponse>, t: Throwable) {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled = true

                    Log.e("AddMedicalRecord", "❌ Network error: ${t.message}")
                    Toast.makeText(
                        this@AddMedicalRecordActivity,
                        "Network error",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}