package com.example.doggo.Home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.doggo.databinding.FragmentDogInfoBinding

class DogInfoFragment : Fragment() {

    private var _binding: FragmentDogInfoBinding? = null
    private val binding get() = _binding!!

    private var dogProfile: DogProfile? = null

    companion object {
        private const val ARG_DOG_PROFILE = "dog_profile"

        fun newInstance(dogProfile: DogProfile): DogInfoFragment {
            val fragment = DogInfoFragment()
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
        _binding = FragmentDogInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayDogInfo()
    }

    private fun displayDogInfo() {
        dogProfile?.let { profile ->
            binding.apply {
                tvAgeValue.text = profile.age.toString()
                tvGenderValue.text = if (profile.gender.isNotEmpty()) profile.gender else "Not specified"
                tvWeightValue.text = if (profile.weight > 0) String.format("%.1f", profile.weight) else "N/A"

                if (profile.additionalInfo.isNotEmpty()) {
                    tvAdditionalInfo.text = profile.additionalInfo
                } else {
                    tvAdditionalInfo.text = "No additional information provided"
                }
            }
        }
    }

    fun updateDogProfile(profile: DogProfile) {
        dogProfile = profile
        if (_binding != null) {
            displayDogInfo()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}