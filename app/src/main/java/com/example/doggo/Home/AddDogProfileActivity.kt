package com.example.doggo.Home

import android.app.AlertDialog
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
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
import java.io.ByteArrayOutputStream

class AddDogProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddDogProfileBinding
    private val scheduleItems = mutableListOf<ScheduleItem>()
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

        binding.btnAddSchedule.setOnClickListener {
            showScheduleDialog()
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

        val schedule = if (scheduleItems.isNotEmpty()) {
            DogSchedule(
                eat = scheduleItems.filter { it.description.contains("eat", true) || it.description.contains("food", true) || it.description.contains("meal", true) },
                walk = scheduleItems.filter { it.description.contains("walk", true) || it.description.contains("exercise", true) },
                sleep = scheduleItems.filter { it.description.contains("sleep", true) || it.description.contains("rest", true) },
                medicine = scheduleItems.filter { it.description.contains("medicine", true) || it.description.contains("pill", true) },
                groom = scheduleItems.filter { it.description.contains("groom", true) || it.description.contains("bath", true) }
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
            photo = selectedImageBase64 ?: "", // ✅ SEND BASE64 IMAGE
            schedule = schedule
        )

        Log.d("AddDogProfile", "📤 Sending dog profile with photo: ${if (selectedImageBase64 != null) "Yes (${selectedImageBase64!!.length} chars)" else "No"}")

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
                    Log.e("AddDogProfile", "❌ HTTP Error: ${response.code()} - ${response.message()}")
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

                Log.e("AddDogProfile", "❌ Network error: ${t.message}")
                Toast.makeText(
                    this@AddDogProfileActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}