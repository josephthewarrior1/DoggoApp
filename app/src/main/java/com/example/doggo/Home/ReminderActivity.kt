package com.example.doggo.Home  // UBAH INI dari Reminders ke Home

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.doggo.R
import com.example.doggo.databinding.ActivityRemindersBinding
import com.example.doggo.network.*
import com.google.android.material.tabs.TabLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class ReminderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRemindersBinding
    private lateinit var reminderAdapter: ReminderAdapter
    private val allReminders = mutableListOf<ReminderItem>()
    private var currentFilter = ReminderFilter.ALL

    enum class ReminderFilter {
        ALL, OVERDUE, UPCOMING, TODAY
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRemindersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupRecyclerView()
        setupTabs()
        loadReminders()
    }

    private fun setupUI() {
        // Setup toolbar back button
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.swipeRefresh.setOnRefreshListener {
            loadReminders()
        }
    }

    private fun setupRecyclerView() {
        reminderAdapter = ReminderAdapter { reminder ->
            handleReminderClick(reminder)
        }

        binding.rvReminders.apply {
            layoutManager = LinearLayoutManager(this@ReminderActivity)
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
        Log.d("ReminderActivity", "📥 Loading all reminders...")
        binding.swipeRefresh.isRefreshing = true
        allReminders.clear()

        // Load all data first, then filter client-side
        loadAllMedicalRecords()
    }

    private fun loadAllMedicalRecords() {
        // Get all dogs first to know which medical records to load
        RetrofitClient.instance.getMyDogs().enqueue(object : Callback<DogsResponse> {
            override fun onResponse(call: Call<DogsResponse>, response: Response<DogsResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val dogs = response.body()?.dogs?.values?.toList() ?: emptyList()
                    Log.d("ReminderActivity", "🐕 Found ${dogs.size} dogs")

                    // For each dog, load medical records
                    var loadedCount = 0

                    if (dogs.isEmpty()) {
                        // No dogs, skip to schedules
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

                                    // When all dogs processed, load schedules
                                    if (loadedCount == dogs.size) {
                                        loadScheduleReminders()
                                    }
                                }

                                override fun onFailure(call: Call<MedicalRecordsResponse>, t: Throwable) {
                                    loadedCount++
                                    Log.e("ReminderActivity", "❌ Failed to load medical for dog ${dog.dogId}: ${t.message}")

                                    if (loadedCount == dogs.size) {
                                        loadScheduleReminders()
                                    }
                                }
                            })
                    }
                } else {
                    Log.e("ReminderActivity", "❌ Failed to load dogs")
                    loadScheduleReminders()
                }
            }

            override fun onFailure(call: Call<DogsResponse>, t: Throwable) {
                Log.e("ReminderActivity", "❌ Network error: ${t.message}")
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

            if (nextDueDate.isNullOrEmpty() || !record.reminderEnabled) {
                return@forEach
            }

            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val dueDate = dateFormat.parse(nextDueDate) ?: return@forEach
                val dueDateCalendar = Calendar.getInstance().apply { time = dueDate }

                // Check if overdue
                if (dueDateCalendar.before(today)) {
                    allReminders.add(
                        ReminderItem(
                            id = "medical_${record.medicalId}",
                            dogId = record.dogId,
                            dogName = dogName,
                            title = "${record.name} - $dogName",
                            description = record.type,
                            dueDate = nextDueDate,
                            type = ReminderType.MEDICAL,
                            status = ReminderStatus.OVERDUE,
                            medicalRecord = record
                        )
                    )
                    Log.d("ReminderActivity", "⚠️ Overdue: ${record.name} for $dogName")
                }
                // Check if upcoming (within 30 days)
                else if (dueDateCalendar.after(today) && dueDateCalendar.before(thirtyDaysFromNow)) {
                    allReminders.add(
                        ReminderItem(
                            id = "medical_${record.medicalId}",
                            dogId = record.dogId,
                            dogName = dogName,
                            title = "${record.name} - $dogName",
                            description = record.type,
                            dueDate = nextDueDate,
                            type = ReminderType.MEDICAL,
                            status = ReminderStatus.UPCOMING,
                            medicalRecord = record
                        )
                    )
                    Log.d("ReminderActivity", "📅 Upcoming: ${record.name} for $dogName")
                }
            } catch (e: Exception) {
                Log.e("ReminderActivity", "❌ Error parsing date: ${e.message}")
            }
        }
    }

    private fun loadScheduleReminders() {
        // Get all dogs with schedules
        RetrofitClient.instance.getMyDogs().enqueue(object : Callback<DogsResponse> {
            override fun onResponse(call: Call<DogsResponse>, response: Response<DogsResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val dogs = response.body()?.dogs?.values?.toList() ?: emptyList()
                    Log.d("ReminderActivity", "🐕 Processing schedules for ${dogs.size} dogs")

                    dogs.forEach { dog ->
                        processScheduleForDog(dog)
                    }
                }

                // Done loading everything
                updateUI()
            }

            override fun onFailure(call: Call<DogsResponse>, t: Throwable) {
                Log.e("ReminderActivity", "❌ Failed to load dogs for schedules: ${t.message}")
                updateUI()
            }
        })
    }

    private fun processScheduleForDog(dog: DogData) {
        val schedule = dog.schedule ?: return
        val now = Calendar.getInstance()

        // Process all schedule types
        schedule.eat?.forEach { detail ->
            addScheduleReminder(dog, "Eating", detail, now)
        }

        schedule.walk?.forEach { detail ->
            addScheduleReminder(dog, "Walking", detail, now)
        }

        schedule.sleep?.forEach { detail ->
            addScheduleReminder(dog, "Sleeping", detail, now)
        }

        schedule.medicine?.forEach { detail ->
            addScheduleReminder(dog, "Medicine", detail, now)
        }

        schedule.groom?.forEach { detail ->
            addScheduleReminder(dog, "Grooming", detail, now)
        }
    }

    private fun addScheduleReminder(
        dog: DogData,
        activityType: String,
        detail: ScheduleDetail,
        now: Calendar
    ) {
        val scheduleTime = parseScheduleTime(detail.time) ?: return
        val diffMinutes = getTimeDifferenceInMinutes(now, scheduleTime)

        // Only show if within next 2 hours or missed (within last 30 mins)
        if (diffMinutes in -30..120) {
            val status = when {
                diffMinutes < 0 -> ReminderStatus.OVERDUE
                diffMinutes <= 30 -> ReminderStatus.UPCOMING
                else -> ReminderStatus.UPCOMING
            }

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
                val parsedTime = Calendar.getInstance().apply {
                    this.time = time
                }
                set(Calendar.HOUR_OF_DAY, parsedTime.get(Calendar.HOUR_OF_DAY))
                set(Calendar.MINUTE, parsedTime.get(Calendar.MINUTE))
                set(Calendar.SECOND, 0)
            }
        } catch (e: Exception) {
            Log.e("ReminderActivity", "Failed to parse time: $timeStr", e)
            null
        }
    }

    private fun getTimeDifferenceInMinutes(now: Calendar, target: Calendar): Int {
        val diffMillis = target.timeInMillis - now.timeInMillis
        return (diffMillis / (1000 * 60)).toInt()
    }

    private fun updateUI() {
        binding.swipeRefresh.isRefreshing = false

        Log.d("ReminderActivity", "📊 Total reminders: ${allReminders.size}")

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
            ReminderFilter.OVERDUE -> allReminders.filter {
                it.status == ReminderStatus.OVERDUE
            }
            ReminderFilter.UPCOMING -> allReminders.filter {
                it.status == ReminderStatus.UPCOMING && !isToday(it.dueDate)
            }
            ReminderFilter.TODAY -> allReminders.filter {
                isToday(it.dueDate)
            }
        }

        Log.d("ReminderActivity", "🔍 Filtered (${currentFilter.name}): ${filtered.size}")
        reminderAdapter.updateReminders(filtered)

        if (filtered.isEmpty()) {
            showEmptyState()
        } else {
            showReminders()
        }
    }

    private fun isToday(dateStr: String): Boolean {
        return try {
            // For schedule times (HH:mm format)
            if (dateStr.contains(":") && dateStr.length <= 5) {
                return true // Schedule times are always "today"
            }

            // For dates (yyyy-MM-dd format)
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
        val upcomingCount = allReminders.count {
            it.status == ReminderStatus.UPCOMING && !isToday(it.dueDate)
        }
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
        when (reminder.type) {
            ReminderType.MEDICAL -> {
                // Navigate to medical detail
                Toast.makeText(this, "Medical Record: ${reminder.title}", Toast.LENGTH_SHORT).show()
            }
            ReminderType.SCHEDULE -> {
                // Navigate to dog profile
                Toast.makeText(this, "Schedule: ${reminder.title}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh reminders when returning
        loadReminders()
    }
}