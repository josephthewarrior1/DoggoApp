package com.example.doggo.Home

import android.app.AlertDialog
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.doggo.R
import com.example.doggo.databinding.ActivityAddDogProfileBinding
import com.example.doggo.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

class EditDogProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddDogProfileBinding
    private var dogId: Int = -1

    private var selectedImageBase64: String? = null
    private var existingPhotoUrl: String? = null

    private var schedule: DogSchedule? = null
    private val scheduleItems = mutableListOf<ScheduleItem>()

    // Gallery launcher
    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                displaySelectedImage(it)
                convertImageToBase64(it)
            }
        }

    // Camera launcher
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
            bitmap?.let {
                displaySelectedBitmap(it)
                convertBitmapToBase64(it)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddDogProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dogId = intent.getIntExtra("DOG_ID", -1)
        if (dogId == -1) {
            Toast.makeText(this, "Invalid Dog ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadDogData()
        setupUI()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }

        binding.cvProfilePhoto.setOnClickListener {
            showImagePickerDialog()
        }

        binding.btnSave.text = "Update Profile"
        binding.btnSave.setOnClickListener {
            if (validateInputs()) updateDogProfile()
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")

        AlertDialog.Builder(this)
            .setTitle("Select New Photo")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> galleryLauncher.launch("image/*")
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun openCamera() {
        try {
            cameraLauncher.launch(null)
        } catch (e: Exception) {
            Toast.makeText(this, "Camera error", Toast.LENGTH_SHORT).show()
        }
    }

    // --------------------------------------------------
    // LOAD EXISTING DOG DATA
    // --------------------------------------------------

    private fun loadDogData() {
        RetrofitClient.instance.getDogById(dogId.toString())
            .enqueue(object : Callback<DogResponse> {
                override fun onResponse(
                    call: Call<DogResponse>,
                    response: Response<DogResponse>
                ) {
                    val dog = response.body()?.dog

                    if (dog != null) {
                        fillFieldsWithDogData(dog)
                    } else {
                        Toast.makeText(this@EditDogProfileActivity, "Failed to load dog data", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }

                override fun onFailure(call: Call<DogResponse>, t: Throwable) {
                    Toast.makeText(this@EditDogProfileActivity, "Network error", Toast.LENGTH_SHORT).show()
                    finish()
                }
            })
    }

    private fun fillFieldsWithDogData(d: DogData) {
        binding.etDogName.setText(d.name)
        binding.etBreed.setText(d.breed)
        binding.etAge.setText(d.age.toString())
        binding.etWeight.setText(d.weight?.toString() ?: "0")
        if (d.gender == "Male") binding.rbMale.isChecked = true
        else binding.rbFemale.isChecked = true

        existingPhotoUrl = d.photo ?: ""

        Glide.with(this)
            .load(existingPhotoUrl)
            .centerCrop()
            .placeholder(R.drawable.ic_dog_placeholder)
            .into(binding.ivDogPhoto)

        // Load schedule if needed
        schedule = d.schedule
    }

    // --------------------------------------------------
    // IMAGE HANDLING
    // --------------------------------------------------

    private fun displaySelectedImage(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .into(binding.ivDogPhoto)
        binding.tvAddPhoto.text = "New Photo Selected ✓"
    }

    private fun displaySelectedBitmap(bitmap: Bitmap) {
        binding.ivDogPhoto.setImageBitmap(bitmap)
        binding.tvAddPhoto.text = "New Photo Selected ✓"
    }

    private fun convertImageToBase64(uri: Uri) {
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
        convertBitmapToBase64(bitmap)
    }

    private fun convertBitmapToBase64(bitmap: Bitmap) {
        val resized = resizeBitmap(bitmap, 800, 800)
        val output = ByteArrayOutputStream()
        resized.compress(Bitmap.CompressFormat.JPEG, 80, output)
        val bytes = output.toByteArray()

        selectedImageBase64 = "data:image/jpeg;base64," +
                Base64.encodeToString(bytes, Base64.NO_WRAP)

        Log.d("EditDog", "New image converted to Base64")
    }

    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val ratio = bitmap.width.toFloat() / bitmap.height
        val newWidth = if (ratio > 1) maxWidth else (maxHeight * ratio).toInt()
        val newHeight = if (ratio > 1) (maxWidth / ratio).toInt() else maxHeight
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    // --------------------------------------------------
    // UPDATE DOG PROFILE
    // --------------------------------------------------

    private fun validateInputs(): Boolean {
        if (binding.etDogName.text.isNullOrBlank()) return false
        if (binding.etBreed.text.isNullOrBlank()) return false
        if (binding.etAge.text.isNullOrBlank()) return false
        if (binding.etWeight.text.isNullOrBlank()) return false
        return true
    }

    private fun updateDogProfile() {

        val request = AddDogRequest(
            name = binding.etDogName.text.toString(),
            breed = binding.etBreed.text.toString(),
            age = binding.etAge.text.toString().toInt(),
            weight = binding.etWeight.text.toString().toDouble(),
            gender = if (binding.rbMale.isChecked) "Male" else "Female",
            photo = selectedImageBase64 ?: existingPhotoUrl ?: "",
            schedule = schedule
        )

        binding.btnSave.isEnabled = false
        binding.btnSave.text = "Updating..."

        RetrofitClient.instance.updateDog(dogId, request)
            .enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    binding.btnSave.isEnabled = true
                    binding.btnSave.text = "Update Profile"

                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(this@EditDogProfileActivity, "Profile updated!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@EditDogProfileActivity, "Update failed", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    binding.btnSave.isEnabled = true
                    binding.btnSave.text = "Update Profile"
                    Toast.makeText(this@EditDogProfileActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
