package com.example.doggo.Home.details

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.doggo.Home.profile.DogProfile

class DogDetailPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val dogProfile: DogProfile
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 3  // ✅ KEMBALIKAN ke 3 tabs

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DogInfoFragment.newInstance(dogProfile)
            1 -> DogScheduleFragment.newInstance(dogProfile)  // ✅ TAMBAHKAN Schedule tab
            2 -> DogMedicalFragment.newInstance(dogProfile)
            else -> DogInfoFragment.newInstance(dogProfile)
        }
    }
}