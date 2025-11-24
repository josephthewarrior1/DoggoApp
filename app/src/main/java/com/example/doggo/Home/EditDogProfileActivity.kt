package com.example.doggo.Home

import android.app.Activity
import android.content.Intent
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
import com.example.doggo.databinding.ActivityEditDogProfileBinding
import com.example.doggo.network.AddDogRequest
import com.example.doggo.network.ApiResponse
import com.example.doggo.network.DogResponse
import com.example.doggo.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.IOException

class EditDogProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditDogProfileBinding
    private var dogId: Int = -1
    private var selectedImageUri: Uri? = null
    private var base64Image: String = ""
    private var currentPhotoUrl: String = ""

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                binding.ivDogPhoto.setImageURI(uri)
                convertImageToBase64(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditDogProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get dog ID from intent
        dogId = intent.getIntExtra("DOG_ID", -1)

        if (dogId == -1) {
            Toast.makeText(this, "Invalid dog ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupUI()
        loadDogData()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.cvProfilePhoto.setOnClickListener {
            openImagePicker()
        }

        binding.tvAddPhoto.setOnClickListener {
            openImagePicker()
        }

        binding.btnSave.setOnClickListener {
            updateDogProfile()
        }
    }

    private fun loadDogData() {
        Log.d("EditDogProfile", "📡 Loading dog data for ID: $dogId")

        RetrofitClient.instance.getDogById(dogId.toString()).enqueue(object : Callback<DogResponse> {
            override fun onResponse(call: Call<DogResponse>, response: Response<DogResponse>) {
                if (response.isSuccessful) {
                    val dogResponse = response.body()

                    if (dogResponse?.success == true && dogResponse.dog != null) {
                        val dog = dogResponse.dog
                        Log.d("EditDogProfile", "✅ Dog loaded: ${dog.name}")

                        // Populate fields
                        binding.etDogName.setText(dog.name)
                        binding.etBreed.setText(dog.breed)
                        binding.etAge.setText(dog.age.toString())
                        binding.etWeight.setText(dog.weight?.toString() ?: "")

                        // Set gender
                        when (dog.gender?.lowercase()) {
                            "male" -> binding.rbMale.isChecked = true
                            "female" -> binding.rbFemale.isChecked = true
                            else -> binding.rbMale.isChecked = true
                        }

                        // Set additional info (jika ada di model lo)
                        // binding.etAdditionalInfo.setText(dog.additionalInfo)

                        // Load photo
                        currentPhotoUrl = dog.photo ?: ""
                        if (currentPhotoUrl.isNotEmpty()) {
                            Glide.with(this@EditDogProfileActivity)
                                .load(currentPhotoUrl)
                                .centerCrop()
                                .placeholder(R.drawable.ic_dog_placeholder)
                                .error(R.drawable.ic_dog_placeholder)
                                .into(binding.ivDogPhoto)
                        }

                    } else {
                        Log.e("EditDogProfile", "❌ Dog not found")
                        Toast.makeText(
                            this@EditDogProfileActivity,
                            "Dog not found",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                } else {
                    Log.e("EditDogProfile", "❌ HTTP error: ${response.code()}")
                    Toast.makeText(
                        this@EditDogProfileActivity,
                        "Failed to load dog data",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }

            override fun onFailure(call: Call<DogResponse>, t: Throwable) {
                Log.e("EditDogProfile", "❌ Network error: ${t.message}")
                Toast.makeText(
                    this@EditDogProfileActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        })
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun convertImageToBase64(uri: Uri) {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)

            // Resize bitmap to reduce size
            val resizedBitmap = resizeBitmap(bitmap, 800, 800)

            val byteArrayOutputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()

            base64Image = "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT)

            Log.d("EditDogProfile", "✅ Image converted to base64")
        } catch (e: IOException) {
            Log.e("EditDogProfile", "❌ Failed to convert image: ${e.message}")
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

    private fun updateDogProfile() {
        val name = binding.etDogName.text.toString().trim()
        val breed = binding.etBreed.text.toString().trim()
        val ageStr = binding.etAge.text.toString().trim()
        val weightStr = binding.etWeight.text.toString().trim()
        val gender = if (binding.rbMale.isChecked) "Male" else "Female"
        val additionalInfo = binding.etAdditionalInfo.text.toString().trim()

        // Validation
        if (name.isEmpty()) {
            binding.tilDogName.error = "Name is required"
            return
        }

        val age = ageStr.toIntOrNull() ?: 0
        val weight = weightStr.toDoubleOrNull() ?: 0.0

        // Use new photo if selected, otherwise keep existing photo URL
        val photoToUpload = if (base64Image.isNotEmpty()) base64Image else currentPhotoUrl

        Log.d("EditDogProfile", "📤 Updating dog profile...")
        Log.d("EditDogProfile", "Name: $name, Breed: $breed, Age: $age, Weight: $weight")
        Log.d("EditDogProfile", "Has new photo: ${base64Image.isNotEmpty()}")

        val updateRequest = AddDogRequest(
            name = name,
            breed = breed,
            age = age,
            weight = weight,
            gender = gender,
            photo = photoToUpload,
            birthDate = "",
            schedule = null // Keep existing schedule
        )

        // Disable button to prevent multiple clicks
        binding.btnSave.isEnabled = false
        binding.btnSave.text = "Updating..."

        RetrofitClient.instance.updateDog(dogId, updateRequest).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                binding.btnSave.isEnabled = true
                binding.btnSave.text = "Update Profile"

                if (response.isSuccessful) {
                    val apiResponse = response.body()

                    if (apiResponse?.success == true) {
                        Log.d("EditDogProfile", "✅ Dog updated successfully!")
                        Toast.makeText(
                            this@EditDogProfileActivity,
                            "Profile updated successfully!",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Return to detail screen
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        Log.e("EditDogProfile", "❌ Update failed: ${apiResponse?.error}")
                        Toast.makeText(
                            this@EditDogProfileActivity,
                            "Update failed: ${apiResponse?.error ?: "Unknown error"}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Log.e("EditDogProfile", "❌ HTTP error: ${response.code()}")
                    Toast.makeText(
                        this@EditDogProfileActivity,
                        "Update failed: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                binding.btnSave.isEnabled = true
                binding.btnSave.text = "Update Profile"

                Log.e("EditDogProfile", "❌ Network error: ${t.message}")
                Toast.makeText(
                    this@EditDogProfileActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}