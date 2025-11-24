package com.example.doggo.Home

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class DogDetailPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val dogProfile: DogProfile
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 2  // ⬅️ UBAH INI dari 3 jadi 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DogInfoFragment.newInstance(dogProfile)
            1 -> DogMedicalFragment.newInstance(dogProfile)
            // HAPUS yang position 2
            else -> DogInfoFragment.newInstance(dogProfile)
        }
    }
}