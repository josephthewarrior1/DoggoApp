package com.example.doggo.Home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.doggo.databinding.FragmentRemindersBinding
import com.example.doggo.network.*
import com.google.android.material.tabs.TabLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class ReminderFragment : Fragment() {

    private var _binding: FragmentRemindersBinding? = null
    private val binding get() = _binding!!

    private lateinit var reminderAdapter: ReminderAdapter
    private val allReminders = mutableListOf<ReminderItem>()
    private var currentFilter = ReminderFilter.ALL

    enum class ReminderFilter {
        ALL, OVERDUE, UPCOMING, TODAY
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRemindersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupTabs()
        setupSwipeRefresh()
        loadReminders()
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            loadReminders()
        }
    }

    private fun setupRecyclerView() {
        reminderAdapter = ReminderAdapter { reminder ->
            handleReminderClick(reminder)
        }

        binding.rvReminders.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reminderAdapter
        }
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentFilter = when (tab?.position) {
                    0 -> ReminderFilter.ALL
                    1 -> ReminderFilter.OVERDUE
                    2 -> ReminderFilter.UPCOMING
                    3 -> ReminderFilter.TODAY
                    else -> ReminderFilter.ALL
                }
                filterReminders()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun loadReminders() {
        Log.d("ReminderFragment", "📥 Loading all reminders...")
        binding.swipeRefresh.isRefreshing = true
        allReminders.clear()
        loadAllMedicalRecords()
    }

    private fun loadAllMedicalRecords() {
        RetrofitClient.instance.getMyDogs().enqueue(object : Callback<DogsResponse> {
            override fun onResponse(call: Call<DogsResponse>, response: Response<DogsResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val dogs = response.body()?.dogs?.values?.toList() ?: emptyList()
                    var loadedCount = 0

                    if (dogs.isEmpty()) {
                        loadScheduleReminders()
                        return
                    }

                    dogs.forEach { dog ->
                        RetrofitClient.instance.getMedicalRecordsByDog(dog.dogId)
                            .enqueue(object : Callback<MedicalRecordsResponse> {
                                override fun onResponse(
                                    call: Call<MedicalRecordsResponse>,
                                    response: Response<MedicalRecordsResponse>
                                ) {
                                    loadedCount++
                                    if (response.isSuccessful && response.body()?.success == true) {
                                        val records = response.body()?.medicalRecords?.values?.toList() ?: emptyList()
                                        processMedicalRecords(records, dog.name)
                                    }
                                    if (loadedCount == dogs.size) {
                                        loadScheduleReminders()
                                    }
                                }

                                override fun onFailure(call: Call<MedicalRecordsResponse>, t: Throwable) {
                                    loadedCount++
                                    if (loadedCount == dogs.size) {
                                        loadScheduleReminders()
                                    }
                                }
                            })
                    }
                } else {
                    loadScheduleReminders()
                }
            }

            override fun onFailure(call: Call<DogsResponse>, t: Throwable) {
                loadScheduleReminders()
            }
        })
    }

    private fun processMedicalRecords(records: List<MedicalRecord>, dogName: String) {
        val today = Calendar.getInstance()
        val thirtyDaysFromNow = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 30)
        }

        records.forEach { record ->
            val nextDueDate = record.nextDueDate
            if (nextDueDate.isNullOrEmpty() || !record.reminderEnabled) return@forEach

            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val dueDate = dateFormat.parse(nextDueDate) ?: return@forEach
                val dueDateCalendar = Calendar.getInstance().apply { time = dueDate }

                val status = when {
                    dueDateCalendar.before(today) -> ReminderStatus.OVERDUE
                    dueDateCalendar.before(thirtyDaysFromNow) -> ReminderStatus.UPCOMING
                    else -> return@forEach
                }

                allReminders.add(
                    ReminderItem(
                        id = "medical_${record.medicalId}",
                        dogId = record.dogId,
                        dogName = dogName,
                        title = "${record.name} - $dogName",
                        description = record.type,
                        dueDate = nextDueDate,
                        type = ReminderType.MEDICAL,
                        status = status,
                        medicalRecord = record
                    )
                )
            } catch (e: Exception) {
                Log.e("ReminderFragment", "Error parsing date: ${e.message}")
            }
        }
    }

    private fun loadScheduleReminders() {
        RetrofitClient.instance.getMyDogs().enqueue(object : Callback<DogsResponse> {
            override fun onResponse(call: Call<DogsResponse>, response: Response<DogsResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val dogs = response.body()?.dogs?.values?.toList() ?: emptyList()
                    dogs.forEach { dog -> processScheduleForDog(dog) }
                }
                updateUI()
            }

            override fun onFailure(call: Call<DogsResponse>, t: Throwable) {
                updateUI()
            }
        })
    }

    private fun processScheduleForDog(dog: DogData) {
        val schedule = dog.schedule ?: return
        val now = Calendar.getInstance()

        schedule.eat?.forEach { addScheduleReminder(dog, "Eating", it, now) }
        schedule.walk?.forEach { addScheduleReminder(dog, "Walking", it, now) }
        schedule.sleep?.forEach { addScheduleReminder(dog, "Sleeping", it, now) }
        schedule.medicine?.forEach { addScheduleReminder(dog, "Medicine", it, now) }
        schedule.groom?.forEach { addScheduleReminder(dog, "Grooming", it, now) }
    }

    private fun addScheduleReminder(dog: DogData, activityType: String, detail: ScheduleDetail, now: Calendar) {
        val scheduleTime = parseScheduleTime(detail.time) ?: return
        val diffMinutes = getTimeDifferenceInMinutes(now, scheduleTime)

        if (diffMinutes in -30..120) {
            val status = if (diffMinutes < 0) ReminderStatus.OVERDUE else ReminderStatus.UPCOMING
            allReminders.add(
                ReminderItem(
                    id = "schedule_${dog.dogId}_${detail.id}",
                    dogId = dog.dogId,
                    dogName = dog.name,
                    title = "${dog.name} - $activityType",
                    description = detail.description.ifEmpty { "Scheduled activity" },
                    dueDate = detail.time,
                    type = ReminderType.SCHEDULE,
                    status = status,
                    scheduleDetail = detail,
                    minutesUntil = diffMinutes
                )
            )
        }
    }

    private fun parseScheduleTime(timeStr: String): Calendar? {
        return try {
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())
            val time = format.parse(timeStr) ?: return null
            Calendar.getInstance().apply {
                val parsedTime = Calendar.getInstance().apply { this.time = time }
                set(Calendar.HOUR_OF_DAY, parsedTime.get(Calendar.HOUR_OF_DAY))
                set(Calendar.MINUTE, parsedTime.get(Calendar.MINUTE))
                set(Calendar.SECOND, 0)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun getTimeDifferenceInMinutes(now: Calendar, target: Calendar): Int {
        return ((target.timeInMillis - now.timeInMillis) / (1000 * 60)).toInt()
    }

    private fun updateUI() {
        binding.swipeRefresh.isRefreshing = false
        if (allReminders.isEmpty()) {
            showEmptyState()
        } else {
            showReminders()
            filterReminders()
        }
        updateTabBadges()
    }

    private fun filterReminders() {
        val filtered = when (currentFilter) {
            ReminderFilter.ALL -> allReminders
            ReminderFilter.OVERDUE -> allReminders.filter { it.status == ReminderStatus.OVERDUE }
            ReminderFilter.UPCOMING -> allReminders.filter { it.status == ReminderStatus.UPCOMING && !isToday(it.dueDate) }
            ReminderFilter.TODAY -> allReminders.filter { isToday(it.dueDate) }
        }
        reminderAdapter.updateReminders(filtered)
        if (filtered.isEmpty()) showEmptyState() else showReminders()
    }

    private fun isToday(dateStr: String): Boolean {
        return try {
            if (dateStr.contains(":") && dateStr.length <= 5) return true
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = format.parse(dateStr) ?: return false
            val today = Calendar.getInstance()
            val dateCalendar = Calendar.getInstance().apply { time = date }
            today.get(Calendar.YEAR) == dateCalendar.get(Calendar.YEAR) &&
                    today.get(Calendar.DAY_OF_YEAR) == dateCalendar.get(Calendar.DAY_OF_YEAR)
        } catch (e: Exception) {
            false
        }
    }

    private fun updateTabBadges() {
        val overdueCount = allReminders.count { it.status == ReminderStatus.OVERDUE }
        val upcomingCount = allReminders.count { it.status == ReminderStatus.UPCOMING && !isToday(it.dueDate) }
        val todayCount = allReminders.count { isToday(it.dueDate) }

        binding.tabLayout.getTabAt(0)?.text = "All (${allReminders.size})"
        binding.tabLayout.getTabAt(1)?.text = "Overdue ($overdueCount)"
        binding.tabLayout.getTabAt(2)?.text = "Upcoming ($upcomingCount)"
        binding.tabLayout.getTabAt(3)?.text = "Today ($todayCount)"
    }

    private fun showEmptyState() {
        binding.emptyStateReminders.visibility = View.VISIBLE
        binding.rvReminders.visibility = View.GONE
    }

    private fun showReminders() {
        binding.emptyStateReminders.visibility = View.GONE
        binding.rvReminders.visibility = View.VISIBLE
    }

    private fun handleReminderClick(reminder: ReminderItem) {
        Toast.makeText(requireContext(), reminder.title, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}