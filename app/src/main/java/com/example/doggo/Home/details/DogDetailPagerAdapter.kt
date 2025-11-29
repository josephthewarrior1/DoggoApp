package com.example.doggo.Home.details

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.doggo.Home.details.DogInfoFragment
import com.example.doggo.Home.details.DogMedicalFragment
import com.example.doggo.Home.profile.DogProfile

class DogDetailPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val dogProfile: DogProfile
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 2  // ⬅️ UBAH INI dari 3 jadi 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DogInfoFragment.Companion.newInstance(dogProfile)
            1 -> DogMedicalFragment.Companion.newInstance(dogProfile)
            // HAPUS yang position 2
            else -> DogInfoFragment.Companion.newInstance(dogProfile)
        }
    }
}