package com.example.doggo.Home.profile

import android.app.AlertDialog
import android.app.DatePickerDialog
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
import android.widget.LinearLayout
import android.widget.NumberPicker
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
import com.google.android.material.chip.Chip
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
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
    private var selectedBirthDate: String? = null
    private var isMaleSelected = true

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
        setupScheduleButtons()
        setupGenderSelection()
        setupBirthDatePicker()
        binding.tvCancel.setOnClickListener {
            finish()
        }
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Image picker click
        binding.cvProfilePhoto.setOnClickListener {
            showImagePickerDialog()
        }

        binding.btnSave.setOnClickListener {
            if (validateInputs()) {
                saveDogProfile()
            }
        }
    }

    private fun setupGenderSelection() {
        // Using MaterialButtonToggleGroup listener
        binding.toggleGender.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnMale -> updateGenderSelection(true)
                    R.id.btnFemale -> updateGenderSelection(false)
                }
            }
        }

        // Set default to male
        binding.btnMale.isChecked = true
        updateGenderSelection(true)
    }

    private fun updateGenderSelection(isMale: Boolean) {
        isMaleSelected = isMale

        if (isMale) {
            binding.btnMale.setBackgroundColor(resources.getColor(R.color.primary_500, theme))
            binding.btnMale.setTextColor(resources.getColor(android.R.color.white, theme))
            binding.btnFemale.setBackgroundColor(resources.getColor(android.R.color.transparent, theme))
            binding.btnFemale.setTextColor(resources.getColor(R.color.text_primary, theme))
        } else {
            binding.btnFemale.setBackgroundColor(resources.getColor(R.color.primary_500, theme))
            binding.btnFemale.setTextColor(resources.getColor(android.R.color.white, theme))
            binding.btnMale.setBackgroundColor(resources.getColor(android.R.color.transparent, theme))
            binding.btnMale.setTextColor(resources.getColor(R.color.text_primary, theme))
        }
    }

    private fun setupScheduleButtons() {
        // Setup listener untuk tiap jenis schedule
        binding.btnAddEatSchedule.setOnClickListener {
            showTimePickerDialog("eat")
        }

        binding.btnAddWalkSchedule.setOnClickListener {
            showTimePickerDialog("walk")
        }

        binding.btnAddSleepSchedule.setOnClickListener {
            showTimePickerDialog("sleep")
        }

        binding.btnAddMedicineSchedule.setOnClickListener {
            showTimePickerDialog("medicine")
        }

        binding.btnAddGroomSchedule.setOnClickListener {
            showTimePickerDialog("groom")
        }
    }

    private fun setupBirthDatePicker() {
        binding.llBirthDate.setOnClickListener {
            showDatePickerDialog()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                // Format tanggal: "1 Dec 2025"
                val date = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }

                val formattedDate = SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(date.time)
                binding.tvBirthDate.text = formattedDate
                selectedBirthDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date.time)
            },
            year,
            month,
            day
        )

        datePickerDialog.show()
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
        Glide.with(this)
            .load(uri)
            .centerCrop()
            .placeholder(R.drawable.ic_dog_placeholder)
            .into(binding.ivDogPhoto)

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

    private fun showTimePickerDialog(scheduleType: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_time_picker, null)

        val npHour = dialogView.findViewById<NumberPicker>(R.id.npHour)
        val npMinute = dialogView.findViewById<NumberPicker>(R.id.npMinute)
        val etDescription = dialogView.findViewById<EditText>(R.id.etDescription)

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

        // Set default description berdasarkan schedule type
        val defaultDescription = when (scheduleType) {
            "eat" -> "Meal time"
            "walk" -> "Walk time"
            "sleep" -> "Sleep time"
            "medicine" -> "Medicine time"
            "groom" -> "Grooming time"
            else -> "Activity time"
        }
        etDescription.setText(defaultDescription)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add ${scheduleType.capitalize()} Schedule")
            .setView(dialogView)
            .setPositiveButton("Add") { dialog, _ ->
                val time = String.format("%02d:%02d", npHour.value, npMinute.value)
                val description = etDescription.text.toString()

                addScheduleItem(scheduleType, time, description)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }

    private fun addScheduleItem(scheduleType: String, time: String, description: String) {
        // ✅ Generate unique ID
        val uniqueId = "${scheduleType}_${System.currentTimeMillis()}_${(Math.random() * 1000).toInt()}"

        // ✅ Create schedule detail with ID
        val scheduleDetail = ScheduleDetail(
            id = uniqueId,
            time = time,
            description = description,
            duration = null,
            createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(Date()),
            updatedAt = null
        )

        Log.d("AddDogProfile", "✅ Created schedule item: $scheduleType at $time")

        // Add to respective list based on schedule type
        when (scheduleType) {
            "eat" -> {
                eatSchedule.add(scheduleDetail)
                updateScheduleUI("eat")
            }
            "walk" -> {
                walkSchedule.add(scheduleDetail)
                updateScheduleUI("walk")
            }
            "sleep" -> {
                sleepSchedule.add(scheduleDetail)
                updateScheduleUI("sleep")
            }
            "medicine" -> {
                medicineSchedule.add(scheduleDetail)
                updateScheduleUI("medicine")
            }
            "groom" -> {
                groomSchedule.add(scheduleDetail)
                updateScheduleUI("groom")
            }
        }
    }

    private fun updateScheduleUI(scheduleType: String) {
        when (scheduleType) {
            "eat" -> {
                if (eatSchedule.isNotEmpty()) {
                    binding.tvEatScheduleStatus.text = "${eatSchedule.size} schedule(s) added"
                    showScheduleChips(binding.llEatSchedule, eatSchedule)
                }
            }
            "walk" -> {
                if (walkSchedule.isNotEmpty()) {
                    binding.tvWalkScheduleStatus.text = "${walkSchedule.size} schedule(s) added"
                    showScheduleChips(binding.llWalkSchedule, walkSchedule)
                }
            }
            "sleep" -> {
                if (sleepSchedule.isNotEmpty()) {
                    binding.tvSleepScheduleStatus.text = "${sleepSchedule.size} schedule(s) added"
                    showScheduleChips(binding.llSleepSchedule, sleepSchedule)
                }
            }
            "medicine" -> {
                if (medicineSchedule.isNotEmpty()) {
                    binding.tvMedicineScheduleStatus.text = "${medicineSchedule.size} schedule(s) added"
                    showScheduleChips(binding.llMedicineSchedule, medicineSchedule)
                }
            }
            "groom" -> {
                if (groomSchedule.isNotEmpty()) {
                    binding.tvGroomScheduleStatus.text = "${groomSchedule.size} schedule(s) added"
                    showScheduleChips(binding.llGroomSchedule, groomSchedule)
                }
            }
        }
    }

    private fun showScheduleChips(container: View, scheduleList: List<ScheduleDetail>) {
        // Pastikan container adalah ViewGroup (LinearLayout)
        if (container !is ViewGroup) return

        // Cari parent dari container (ini adalah LinearLayout yang jadi parent)
        val parentContainer = container.parent as? ViewGroup
        parentContainer?.let { parent ->
            // Cari chip container lama berdasarkan tag
            val oldChipContainer = parent.findViewWithTag<View>("chipContainer_${container.id}")
            oldChipContainer?.let { old ->
                parent.removeView(old)
            }

            // Buat container baru untuk chips
            val chipContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(40.dpToPx(), 4.dpToPx(), 0, 0)
                }
                tag = "chipContainer_${container.id}"
            }

            // Tambah chips ke container
            scheduleList.forEach { schedule ->
                val chip = Chip(this).apply {
                    text = "${schedule.time} - ${schedule.description}"
                    isCloseIconVisible = true
                    chipBackgroundColor = getColorStateList(R.color.blue_50)

                    setOnCloseIconClickListener {
                        // Remove from list
                        when (container.id) {
                            R.id.llEatSchedule -> eatSchedule.remove(schedule)
                            R.id.llWalkSchedule -> walkSchedule.remove(schedule)
                            R.id.llSleepSchedule -> sleepSchedule.remove(schedule)
                            R.id.llMedicineSchedule -> medicineSchedule.remove(schedule)
                            R.id.llGroomSchedule -> groomSchedule.remove(schedule)
                        }
                        // Update status text
                        updateStatusText(container.id)
                        // Refresh chips display
                        showScheduleChips(container, getScheduleList(container.id))
                    }

                    // Tambah margin bottom
                    val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        bottomMargin = 8.dpToPx()
                    }
                    this.layoutParams = layoutParams
                }
                chipContainer.addView(chip)
            }

            // Tambah chip container ke parent setelah container asli
            val index = parent.indexOfChild(container) + 1
            parent.addView(chipContainer, index)
        }
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    private fun getScheduleList(containerId: Int): List<ScheduleDetail> {
        return when (containerId) {
            R.id.llEatSchedule -> eatSchedule
            R.id.llWalkSchedule -> walkSchedule
            R.id.llSleepSchedule -> sleepSchedule
            R.id.llMedicineSchedule -> medicineSchedule
            R.id.llGroomSchedule -> groomSchedule
            else -> emptyList()
        }
    }

    private fun updateStatusText(containerId: Int) {
        when (containerId) {
            R.id.llEatSchedule -> {
                binding.tvEatScheduleStatus.text =
                    if (eatSchedule.isEmpty()) "No schedule added"
                    else "${eatSchedule.size} schedule(s) added"
            }
            R.id.llWalkSchedule -> {
                binding.tvWalkScheduleStatus.text =
                    if (walkSchedule.isEmpty()) "No schedule added"
                    else "${walkSchedule.size} schedule(s) added"
            }
            R.id.llSleepSchedule -> {
                binding.tvSleepScheduleStatus.text =
                    if (sleepSchedule.isEmpty()) "No schedule added"
                    else "${sleepSchedule.size} schedule(s) added"
            }
            R.id.llMedicineSchedule -> {
                binding.tvMedicineScheduleStatus.text =
                    if (medicineSchedule.isEmpty()) "No schedule added"
                    else "${medicineSchedule.size} schedule(s) added"
            }
            R.id.llGroomSchedule -> {
                binding.tvGroomScheduleStatus.text =
                    if (groomSchedule.isEmpty()) "No schedule added"
                    else "${groomSchedule.size} schedule(s) added"
            }
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

        if (selectedBirthDate.isNullOrBlank()) {
            Toast.makeText(this, "Please select birth date", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (binding.etWeight.text.isNullOrBlank()) {
            binding.tilWeight.error = "Please enter weight"
            isValid = false
        } else {
            binding.tilWeight.error = null
        }

        return isValid
    }

    private fun calculateAgeFromBirthDate(birthDate: String): Int {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val birthDateObj = sdf.parse(birthDate)

            if (birthDateObj != null) {
                val birthCalendar = Calendar.getInstance().apply { time = birthDateObj }
                val today = Calendar.getInstance()

                var age = today.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)

                if (today.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
                    age--
                }

                age.coerceAtLeast(0)
            } else {
                0
            }
        } catch (e: Exception) {
            Log.e("AddDogProfile", "Error calculating age: ${e.message}")
            0
        }
    }

    private fun saveDogProfile() {
        val name = binding.etDogName.text.toString()
        val breed = binding.etBreed.text.toString()
        val birthDate = selectedBirthDate ?: ""
        val age = calculateAgeFromBirthDate(birthDate)
        val weight = binding.etWeight.text.toString().toDoubleOrNull() ?: 0.0
        val gender = if (isMaleSelected) "Male" else "Female"

        val schedule = if (eatSchedule.isNotEmpty() || walkSchedule.isNotEmpty() ||
            sleepSchedule.isNotEmpty() || medicineSchedule.isNotEmpty() || groomSchedule.isNotEmpty()) {
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
            birthDate = birthDate,
            photo = selectedImageBase64 ?: "",
            schedule = schedule
        )

        Log.d("AddDogProfile", "📤 Sending dog profile")
        Log.d("AddDogProfile", "📅 Schedule included: ${schedule != null}")

        RetrofitClient.instance.addDog(addDogRequest).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                binding.btnSave.isEnabled = true
                binding.btnSave.text = "Save Profile"

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        Log.d("AddDogProfile", "✅ Dog saved successfully with ID: ${apiResponse.dogId}")

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
}