package com.example.doggo.Home.medical

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.doggo.R
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
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var selectedType = "vaccine"
    private var selectedStatus = "completed"
    private var selectedDate: Calendar = Calendar.getInstance()
    private var selectedNextDueDate: Calendar? = null

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
        setupTypeButtons()
        setupDatePickers()
        updateDateDisplay()
    }

    private fun setupUI() {
        binding.btnCancel.setOnClickListener {
            finish()
        }

        binding.btnSave.setOnClickListener {
            saveMedicalRecord()
        }

        // Toggle next due date visibility
        binding.switchNextDueDate.setOnCheckedChangeListener { _, isChecked ->
            // You can add logic here if needed
        }

        // Status dropdown
        binding.tvStatus.setOnClickListener {
            showStatusDialog()
        }
    }

    private fun setupTypeButtons() {
        // Set initial selected type (Vaccine)
        selectType("vaccine", binding.btnTypeVaccine)

        binding.btnTypeVaccine.setOnClickListener {
            selectType("vaccine", binding.btnTypeVaccine)
        }

        binding.btnTypeSurgery.setOnClickListener {
            selectType("surgery", binding.btnTypeSurgery)
        }

        binding.btnTypeMedication.setOnClickListener {
            selectType("medication", binding.btnTypeMedication)
        }
    }

    private fun selectType(type: String, selectedCard: androidx.cardview.widget.CardView) {
        selectedType = type

        // Reset all cards to unselected state
        listOf(binding.btnTypeVaccine, binding.btnTypeSurgery, binding.btnTypeMedication).forEach { card ->
            card.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.white))
        }

        // Highlight selected card with light blue background
        selectedCard.setCardBackgroundColor(android.graphics.Color.parseColor("#E3F2FD"))
    }

    private fun setupDatePickers() {
        binding.tvDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    selectedDate.set(year, month, day)
                    updateDateDisplay()
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun updateDateDisplay() {
        binding.tvDate.text = dateFormat.format(selectedDate.time)
    }

    private fun showStatusDialog() {
        val statuses = arrayOf("Completed", "Pending", "Scheduled")
        val currentIndex = when (selectedStatus) {
            "completed" -> 0
            "pending" -> 1
            "scheduled" -> 2
            else -> 0
        }

        AlertDialog.Builder(this)
            .setTitle("Select Status")
            .setSingleChoiceItems(statuses, currentIndex) { dialog, which ->
                selectedStatus = when (which) {
                    0 -> "completed"
                    1 -> "pending"
                    2 -> "scheduled"
                    else -> "completed"
                }
                binding.tvStatus.text = "${statuses[which]} ▼"
                dialog.dismiss()
            }
            .show()
    }

    private fun saveMedicalRecord() {
        val name = binding.etName.text.toString().trim()
        val veterinarian = binding.etVeterinarian.text.toString().trim()
        val clinic = binding.etClinic.text.toString().trim()
        val notes = binding.etNotes.text.toString().trim()
        val date = apiDateFormat.format(selectedDate.time)
        val hasNextDueDate = binding.switchNextDueDate.isChecked

        // Validation
        if (name.isEmpty()) {
            binding.tilName.error = "Name is required"
            return
        }

        // Show loading
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSave.isEnabled = false

        val request = AddMedicalRecordRequest(
            dogId = dogId,
            type = selectedType,
            name = name,
            date = date,
            nextDueDate = if (hasNextDueDate && selectedNextDueDate != null) {
                apiDateFormat.format(selectedNextDueDate!!.time)
            } else null,
            veterinarian = veterinarian.ifEmpty { null },
            clinic = clinic.ifEmpty { null },
            notes = notes.ifEmpty { null },
            reminderEnabled = hasNextDueDate
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