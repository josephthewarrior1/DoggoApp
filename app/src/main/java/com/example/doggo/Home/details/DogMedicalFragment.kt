package com.example.doggo.Home.details

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.doggo.Home.medical.AddMedicalRecordActivity
import com.example.doggo.Home.medical.MedicalRecordAdapter
import com.example.doggo.Home.profile.DogProfile
import com.example.doggo.databinding.FragmentDogMedicalBinding
import com.example.doggo.network.MedicalRecord
import com.example.doggo.network.MedicalRecordResponse
import com.example.doggo.network.MedicalRecordsResponse
import com.example.doggo.network.RetrofitClient
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DogMedicalFragment : Fragment() {

    private var _binding: FragmentDogMedicalBinding? = null
    private val binding get() = _binding!!

    private var dogProfile: DogProfile? = null
    private lateinit var medicalAdapter: MedicalRecordAdapter

    companion object {
        private const val ARG_DOG_PROFILE = "dog_profile"

        fun newInstance(dogProfile: DogProfile): DogMedicalFragment {
            val fragment = DogMedicalFragment()
            val args = Bundle()
            args.putSerializable(ARG_DOG_PROFILE, dogProfile)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            dogProfile = it.getSerializable(ARG_DOG_PROFILE) as? DogProfile
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDogMedicalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupButtons()
        loadMedicalRecords()
    }

    private fun setupRecyclerView() {
        medicalAdapter = MedicalRecordAdapter(
            onItemClick = { record ->
                showMedicalRecordDetails(record)
            },
            onItemLongClick = { record ->
                showMedicalRecordOptions(record)
            }
        )

        binding.rvMedicalRecords.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = medicalAdapter
        }
    }

    private fun setupButtons() {
        binding.btnAddMedical.setOnClickListener {
            val intent = Intent(requireContext(), AddMedicalRecordActivity::class.java)
            intent.putExtra("DOG_ID", dogProfile?.id?.toIntOrNull() ?: -1)
            startActivity(intent)
        }
    }

    private fun loadMedicalRecords() {
        val dogId = dogProfile?.id?.toIntOrNull()

        if (dogId == null) {
            Log.e("DogMedicalFragment", "❌ Invalid dog ID: ${dogProfile?.id}")
            showEmptyState(true)
            return
        }

        Log.d("DogMedicalFragment", "📋 Loading medical records for dog: $dogId")

        RetrofitClient.instance.getMedicalRecordsByDog(dogId)
            .enqueue(object : Callback<MedicalRecordsResponse> {
                override fun onResponse(
                    call: Call<MedicalRecordsResponse>,
                    response: Response<MedicalRecordsResponse>
                ) {
                    Log.d("DogMedicalFragment", "📡 Response received: ${response.code()}")
                    Log.d("DogMedicalFragment", "📊 Response body: ${response.body()}")

                    if (response.isSuccessful && response.body()?.success == true) {
                        val recordsMap = response.body()?.medicalRecords

                        Log.d("DogMedicalFragment", "📦 Records map: $recordsMap")
                        Log.d("DogMedicalFragment", "📊 Records map size: ${recordsMap?.size}")

                        if (recordsMap.isNullOrEmpty()) {
                            Log.d("DogMedicalFragment", "🔭 No medical records found")
                            showEmptyState(true)
                        } else {
                            Log.d("DogMedicalFragment", "✅ Found ${recordsMap.size} medical records")
                            val medicalRecords = recordsMap.values.sortedByDescending { it.date }

                            Log.d("DogMedicalFragment", "📝 Medical records list: $medicalRecords")
                            Log.d("DogMedicalFragment", "📝 Adapter item count before: ${medicalAdapter.itemCount}")

                            medicalAdapter.updateRecords(medicalRecords)

                            Log.d("DogMedicalFragment", "📝 Adapter item count after: ${medicalAdapter.itemCount}")

                            showEmptyState(false)
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("DogMedicalFragment", "❌ Failed to load: ${response.body()?.error}")
                        Log.e("DogMedicalFragment", "❌ Error body: $errorBody")
                        showEmptyState(true)
                    }
                }

                override fun onFailure(call: Call<MedicalRecordsResponse>, t: Throwable) {
                    Log.e("DogMedicalFragment", "❌ Network error: ${t.message}")
                    Log.e("DogMedicalFragment", "❌ Stack trace: ", t)
                    Toast.makeText(requireContext(), "Failed to load medical records", Toast.LENGTH_SHORT).show()
                    showEmptyState(true)
                }
            })
    }

    private fun showMedicalRecordDetails(record: MedicalRecord) {
        val details = buildString {
            append("Type: ${record.type}\n")
            append("Name: ${record.name}\n")
            append("Date: ${record.date}\n")

            record.veterinarian?.let {
                if (it.isNotEmpty()) append("Veterinarian: $it\n")
            }

            record.clinic?.let {
                if (it.isNotEmpty()) append("Clinic: $it\n")
            }

            record.nextDueDate?.let {
                if (it.isNotEmpty()) append("Next Due: $it\n")
            }

            record.notes?.let {
                if (it.isNotEmpty()) append("\nNotes:\n$it")
            }
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Medical Record Details")
            .setMessage(details)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showMedicalRecordOptions(record: MedicalRecord) {
        val options = arrayOf("View Details", "Edit", "Delete")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Medical Record")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showMedicalRecordDetails(record)
                    1 -> editMedicalRecord(record)
                    2 -> deleteMedicalRecord(record)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun editMedicalRecord(record: MedicalRecord) {
        // TODO: Navigate to edit medical record activity
        Toast.makeText(requireContext(), "Edit Medical Record (Coming Soon)", Toast.LENGTH_SHORT).show()
    }

    private fun deleteMedicalRecord(record: MedicalRecord) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Medical Record")
            .setMessage("Are you sure you want to delete this medical record?")
            .setPositiveButton("Delete") { _, _ ->
                performDelete(record)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performDelete(record: MedicalRecord) {
        Log.d("DogMedicalFragment", "🗑️ Deleting medical record: ${record.medicalId}")

        RetrofitClient.instance.deleteMedicalRecord(record.medicalId)
            .enqueue(object : Callback<MedicalRecordResponse> {
                override fun onResponse(
                    call: Call<MedicalRecordResponse>,
                    response: Response<MedicalRecordResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Log.d("DogMedicalFragment", "✅ Medical record deleted")
                        Toast.makeText(requireContext(), "Medical record deleted", Toast.LENGTH_SHORT).show()
                        loadMedicalRecords() // Reload list
                    } else {
                        Log.e("DogMedicalFragment", "❌ Failed to delete: ${response.body()?.error}")
                        Toast.makeText(requireContext(), "Failed to delete record", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<MedicalRecordResponse>, t: Throwable) {
                    Log.e("DogMedicalFragment", "❌ Network error: ${t.message}")
                    Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showEmptyState(show: Boolean) {
        if (show) {
            binding.emptyStateMedical.visibility = View.VISIBLE
            binding.rvMedicalRecords.visibility = View.GONE
        } else {
            binding.emptyStateMedical.visibility = View.GONE
            binding.rvMedicalRecords.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload when returning from add/edit screen
        loadMedicalRecords()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}