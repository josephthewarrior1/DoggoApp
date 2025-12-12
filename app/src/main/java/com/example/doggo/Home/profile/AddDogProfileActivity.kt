package com.example.doggo.Home.profile

import android.app.AlertDialog
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.doggo.R
import com.example.doggo.databinding.ActivityAddDogProfileBinding
import com.example.doggo.network.AddDogRequest
import com.example.doggo.network.ApiResponse
import com.example.doggo.network.DogResponse
import com.example.doggo.network.DogSchedule
import com.example.doggo.network.RetrofitClient
import com.example.doggo.network.ScheduleDetail
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddDogProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddDogProfileBinding
    private val eatSchedule = mutableListOf<ScheduleDetail>()
    private val walkSchedule = mutableListOf<ScheduleDetail>()
    private val sleepSchedule = mutableListOf<ScheduleDetail>()
    private val medicineSchedule = mutableListOf<ScheduleDetail>()
    private val groomSchedule = mutableListOf<ScheduleDetail>()
    private var selectedImageBase64: String? = null
    private var selectedImageUri: Uri? = null

    // Image Picker Launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            displaySelectedImage(it)
            convertImageToBase64(it)
        }
    }

    // Camera Launcher
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            displaySelectedBitmap(it)
            convertBitmapToBase64(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddDogProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupGenderSelection()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Image picker click
        binding.cvProfilePhoto.setOnClickListener {
            showImagePickerDialog()
        }

        // ✅ FIX: Setup Add Schedule Button
        binding.btnAddSchedule.setOnClickListener {
            showScheduleDialog()
        }

        binding.btnSave.setOnClickListener {
            if (validateInputs()) {
                saveDogProfile()
            }
        }
    }

    private fun setupGenderSelection() {
        // Setup gender button selection
        val maleButton = binding.rbMale
        val femaleButton = binding.rbFemale

        maleButton.setOnClickListener {
            updateGenderUI(true)
        }

        femaleButton.setOnClickListener {
            updateGenderUI(false)
        }

        // Set default to male
        updateGenderUI(true)
    }

    private fun updateGenderUI(isMale: Boolean) {
        if (isMale) {
            binding.rbMale.setBackgroundColor(getColor(R.color.primary_500))
            binding.rbMale.setTextColor(getColor(android.R.color.white))
            binding.rbFemale.setBackgroundColor(getColor(android.R.color.transparent))
            binding.rbFemale.setTextColor(getColor(R.color.text_primary))
        } else {
            binding.rbFemale.setBackgroundColor(getColor(R.color.primary_500))
            binding.rbFemale.setTextColor(getColor(android.R.color.white))
            binding.rbMale.setBackgroundColor(getColor(android.R.color.transparent))
            binding.rbMale.setTextColor(getColor(R.color.text_primary))
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")

        AlertDialog.Builder(this)
            .setTitle("Select Photo")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun openCamera() {
        try {
            cameraLauncher.launch(null)
        } catch (e: Exception) {
            Log.e("AddDogProfile", "Camera error: ${e.message}")
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        try {
            imagePickerLauncher.launch("image/*")
        } catch (e: Exception) {
            Log.e("AddDogProfile", "Gallery error: ${e.message}")
            Toast.makeText(this, "Gallery not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displaySelectedImage(uri: Uri) {
        // Load image using Glide
        Glide.with(this)
            .load(uri)
            .centerCrop()
            .placeholder(R.drawable.ic_dog_placeholder)
            .into(binding.ivDogPhoto)

        // Update hint text
        binding.tvAddPhoto.text = "Photo selected ✓"

        Log.d("AddDogProfile", "✅ Image selected from gallery")
    }

    private fun displaySelectedBitmap(bitmap: Bitmap) {
        binding.ivDogPhoto.setImageBitmap(bitmap)
        binding.tvAddPhoto.text = "Photo captured ✓"
        Log.d("AddDogProfile", "✅ Image captured from camera")
    }

    private fun convertImageToBase64(uri: Uri) {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            convertBitmapToBase64(bitmap)
        } catch (e: Exception) {
            Log.e("AddDogProfile", "❌ Image conversion error: ${e.message}")
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun convertBitmapToBase64(bitmap: Bitmap) {
        try {
            // Resize bitmap to reduce size (max 800x800)
            val resizedBitmap = resizeBitmap(bitmap, 800, 800)

            val byteArrayOutputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()

            selectedImageBase64 = "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP)

            Log.d("AddDogProfile", "✅ Image converted to Base64 (${byteArray.size / 1024} KB)")
        } catch (e: Exception) {
            Log.e("AddDogProfile", "❌ Base64 conversion error: ${e.message}")
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val ratioBitmap = width.toFloat() / height.toFloat()
        val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()

        var finalWidth = maxWidth
        var finalHeight = maxHeight

        if (ratioMax > ratioBitmap) {
            finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
        } else {
            finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true)
    }

    private fun showScheduleDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_schedule, null)

        // Setup Dropdown untuk schedule type
        val actvScheduleType = dialogView.findViewById<AutoCompleteTextView>(R.id.actvScheduleType)
        val scheduleTypes = arrayOf("Eat", "Walk", "Sleep", "Medicine", "Groom")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, scheduleTypes)
        actvScheduleType.setAdapter(adapter)
        actvScheduleType.setText("Eat", false) // Set default value

        val npHour = dialogView.findViewById<NumberPicker>(R.id.npHour)
        val npMinute = dialogView.findViewById<NumberPicker>(R.id.npMinute)

        npHour.minValue = 0
        npHour.maxValue = 23
        npHour.value = 8
        npHour.setFormatter { i -> String.format("%02d", i) }
        npHour.wrapSelectorWheel = true

        npMinute.minValue = 0
        npMinute.maxValue = 59
        npMinute.value = 0
        npMinute.setFormatter { i -> String.format("%02d", i) }
        npMinute.wrapSelectorWheel = true

        val etDescription = dialogView.findViewById<EditText>(R.id.etDescription)

        // Update description when schedule type changes
        actvScheduleType.setOnItemClickListener { _, _, position, _ ->
            val defaultDescription = when (scheduleTypes[position]) {
                "Eat" -> "Meal time"
                "Walk" -> "Walk time"
                "Sleep" -> "Sleep time"
                "Medicine" -> "Medicine time"
                "Groom" -> "Grooming time"
                else -> "Activity time"
            }
            etDescription.setText(defaultDescription)
        }

        // Set initial description
        etDescription.setText("Meal time")

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add Schedule Item")
            .setView(dialogView)
            .setPositiveButton("Add") { dialog, _ ->
                val selectedType = actvScheduleType.text.toString()
                addScheduleItem(selectedType, npHour.value, npMinute.value, etDescription.text.toString())
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }

    private fun addScheduleItem(scheduleType: String, hour: Int, minute: Int, description: String) {
        val time = String.format("%02d:%02d", hour, minute)

        if (description.isEmpty()) {
            Toast.makeText(this, "Please enter description", Toast.LENGTH_SHORT).show()
            return
        }

        // ✅ Generate schedule type string (lowercase)
        val scheduleTypeStr = scheduleType.lowercase()

        // ✅ Generate unique ID
        val uniqueId = "${scheduleTypeStr}_${System.currentTimeMillis()}_${(Math.random() * 1000).toInt()}"

        // ✅ Create schedule detail with ID
        val scheduleDetail = ScheduleDetail(
            id = uniqueId,
            time = time,
            description = description,
            duration = null,
            createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(Date()),
            updatedAt = null
        )

        Log.d("AddDogProfile", "✅ Created schedule item with ID: $uniqueId")
        Log.d("AddDogProfile", "Time: $time, Description: $description")

        // Add to respective list based on schedule type
        when (scheduleType) {
            "Eat" -> eatSchedule.add(scheduleDetail)
            "Walk" -> walkSchedule.add(scheduleDetail)
            "Sleep" -> sleepSchedule.add(scheduleDetail)
            "Medicine" -> medicineSchedule.add(scheduleDetail)
            "Groom" -> groomSchedule.add(scheduleDetail)
        }

        // Update UI dengan preview
        updateSchedulePreview()
    }

    private fun updateSchedulePreview() {
        val totalItems = eatSchedule.size + walkSchedule.size + sleepSchedule.size +
                medicineSchedule.size + groomSchedule.size

        // Update summary text
        if (totalItems > 0) {
            binding.tvScheduleSummary.text = "$totalItems schedule item(s) added"

            // Show schedule items container
            binding.llScheduleItems.isVisible = true
            binding.llScheduleItems.removeAllViews()

            // Add all schedule items as chips with sections
            addScheduleChipsSection(eatSchedule, "🍽️ Eating Schedule", R.color.schedule_eat_bg)
            addScheduleChipsSection(walkSchedule, "🚶 Walking Schedule", R.color.schedule_walk_bg)
            addScheduleChipsSection(sleepSchedule, "😴 Sleeping Schedule", R.color.schedule_sleep_bg)
            addScheduleChipsSection(medicineSchedule, "💊 Medicine Schedule", R.color.schedule_medicine_bg)
            addScheduleChipsSection(groomSchedule, "✂️ Grooming Schedule", R.color.schedule_groom_bg)
        } else {
            binding.tvScheduleSummary.text = "No schedules added yet"
            binding.llScheduleItems.isVisible = false
        }

        // Untuk debug
        binding.tvScheduleCount.text = "Schedule items: $totalItems"
    }

    private fun addScheduleChipsSection(
        scheduleList: List<ScheduleDetail>,
        sectionTitle: String,
        chipBgColor: Int
    ) {
        if (scheduleList.isNotEmpty()) {
            // Add section header
            val sectionHeader = TextView(this).apply {
                text = sectionTitle
                setTextColor(getColor(R.color.text_primary))
                textSize = 16f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setPadding(0, 24.dpToPx(), 0, 8.dpToPx())
            }
            binding.llScheduleItems.addView(sectionHeader)

            // Add chips for each schedule item in this section
            scheduleList.forEach { schedule ->
                val chip = Chip(this).apply {
                    text = "${schedule.time} - ${schedule.description}"
                    isCloseIconVisible = true
                    chipBackgroundColor = getColorStateList(chipBgColor)
                    setTextColor(getColor(R.color.text_primary))

                    setOnCloseIconClickListener {
                        // Remove from the correct list based on section title
                        when (sectionTitle) {
                            "🍽️ Eating Schedule" -> eatSchedule.removeAll { it.id == schedule.id }
                            "🚶 Walking Schedule" -> walkSchedule.removeAll { it.id == schedule.id }
                            "😴 Sleeping Schedule" -> sleepSchedule.removeAll { it.id == schedule.id }
                            "💊 Medicine Schedule" -> medicineSchedule.removeAll { it.id == schedule.id }
                            "✂️ Grooming Schedule" -> groomSchedule.removeAll { it.id == schedule.id }
                        }
                        updateSchedulePreview()
                    }

                    // Add margin between chips
                    val layoutParams = ViewGroup.MarginLayoutParams(
                        ViewGroup.MarginLayoutParams.WRAP_CONTENT,
                        ViewGroup.MarginLayoutParams.WRAP_CONTENT
                    )
                    layoutParams.setMargins(0, 0, 0, 8.dpToPx())
                    this.layoutParams = layoutParams
                }
                binding.llScheduleItems.addView(chip)
            }
        }
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

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
        val gender = if (binding.rbMale.isPressed) "Male" else "Female"

        val totalItems = eatSchedule.size + walkSchedule.size + sleepSchedule.size +
                medicineSchedule.size + groomSchedule.size

        // ✅ Log schedule items dengan IDs
        Log.d("AddDogProfile", "📋 Preparing to save dog with schedules:")
        Log.d("AddDogProfile", "Eat schedules: ${eatSchedule.size}")
        eatSchedule.forEachIndexed { index, item ->
            Log.d("AddDogProfile", "  [$index] ID: ${item.id}, Time: ${item.time}, Desc: ${item.description}")
        }
        Log.d("AddDogProfile", "Walk schedules: ${walkSchedule.size}")
        walkSchedule.forEachIndexed { index, item ->
            Log.d("AddDogProfile", "  [$index] ID: ${item.id}, Time: ${item.time}, Desc: ${item.description}")
        }

        val schedule = if (totalItems > 0) {
            DogSchedule(
                eat = eatSchedule.takeIf { it.isNotEmpty() },
                walk = walkSchedule.takeIf { it.isNotEmpty() },
                sleep = sleepSchedule.takeIf { it.isNotEmpty() },
                medicine = medicineSchedule.takeIf { it.isNotEmpty() },
                groom = groomSchedule.takeIf { it.isNotEmpty() }
            )
        } else {
            null
        }

        // Show loading
        binding.btnSave.isEnabled = false
        binding.btnSave.text = "Uploading..."

        val addDogRequest = AddDogRequest(
            name = name,
            breed = breed,
            age = age,
            weight = weight,
            gender = gender,
            photo = selectedImageBase64 ?: "",
            schedule = schedule
        )

        Log.d("AddDogProfile", "📤 Sending dog profile with photo: ${if (selectedImageBase64 != null) "Yes (${selectedImageBase64!!.length} chars)" else "No"}")
        Log.d("AddDogProfile", "📅 Schedule included: ${schedule != null}")

        if (schedule != null) {
            Log.d("AddDogProfile", "📊 Schedule breakdown:")
            Log.d("AddDogProfile", "  - Eat: ${schedule.eat?.size ?: 0} items")
            Log.d("AddDogProfile", "  - Walk: ${schedule.walk?.size ?: 0} items")
            Log.d("AddDogProfile", "  - Sleep: ${schedule.sleep?.size ?: 0} items")
            Log.d("AddDogProfile", "  - Medicine: ${schedule.medicine?.size ?: 0} items")
            Log.d("AddDogProfile", "  - Groom: ${schedule.groom?.size ?: 0} items")
        }

        RetrofitClient.instance.addDog(addDogRequest).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                binding.btnSave.isEnabled = true
                binding.btnSave.text = "Save Profile"

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        Log.d("AddDogProfile", "✅ Dog saved successfully with ID: ${apiResponse.dogId}")

                        // ✅ Verify schedule was saved
                        apiResponse.dogId?.let { dogId ->
                            verifyScheduleSaved(dogId.toString())
                        }

                        Toast.makeText(
                            this@AddDogProfileActivity,
                            "Dog profile saved successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Log.e("AddDogProfile", "❌ API Error: ${apiResponse?.error}")
                        Toast.makeText(
                            this@AddDogProfileActivity,
                            apiResponse?.error ?: "Failed to save dog profile",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("AddDogProfile", "❌ HTTP Error: ${response.code()} - ${response.message()}")
                    Log.e("AddDogProfile", "Error body: $errorBody")
                    Toast.makeText(
                        this@AddDogProfileActivity,
                        "Failed to save: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                binding.btnSave.isEnabled = true
                binding.btnSave.text = "Save Profile"

                Log.e("AddDogProfile", "❌ Network error: ${t.message}", t)
                Toast.makeText(
                    this@AddDogProfileActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    // ✅ Method untuk verify schedule tersimpan dengan benar
    private fun verifyScheduleSaved(dogId: String) {
        RetrofitClient.instance.getDogById(dogId).enqueue(object : Callback<DogResponse> {
            override fun onResponse(call: Call<DogResponse>, response: Response<DogResponse>) {
                if (response.isSuccessful) {
                    val dog = response.body()?.dog
                    val schedule = dog?.schedule

                    Log.d("AddDogProfile", "✅ Schedule verification:")
                    Log.d("AddDogProfile", "  Eat items: ${schedule?.eat?.size ?: 0}")
                    schedule?.eat?.forEach { item ->
                        Log.d("AddDogProfile", "    - ID: ${item.id}, Time: ${item.time}")
                    }
                    Log.d("AddDogProfile", "  Walk items: ${schedule?.walk?.size ?: 0}")
                    schedule?.walk?.forEach { item ->
                        Log.d("AddDogProfile", "    - ID: ${item.id}, Time: ${item.time}")
                    }

                    // ✅ Check for items without ID
                    var hasItemsWithoutId = false
                    schedule?.eat?.forEach { if (it.id.isNullOrEmpty()) hasItemsWithoutId = true }
                    schedule?.walk?.forEach { if (it.id.isNullOrEmpty()) hasItemsWithoutId = true }
                    schedule?.sleep?.forEach { if (it.id.isNullOrEmpty()) hasItemsWithoutId = true }
                    schedule?.medicine?.forEach { if (it.id.isNullOrEmpty()) hasItemsWithoutId = true }
                    schedule?.groom?.forEach { if (it.id.isNullOrEmpty()) hasItemsWithoutId = true }

                    if (hasItemsWithoutId) {
                        Log.e("AddDogProfile", "⚠️ WARNING: Some schedule items don't have IDs!")
                    } else {
                        Log.d("AddDogProfile", "✅ All schedule items have valid IDs")
                    }
                }
            }

            override fun onFailure(call: Call<DogResponse>, t: Throwable) {
                Log.e("AddDogProfile", "❌ Failed to verify schedule: ${t.message}")
            }
        })
    }
}