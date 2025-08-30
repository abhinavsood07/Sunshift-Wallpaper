package com.example.sunshift

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.sunshift.databinding.ActivityMainBinding
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object {
        private const val UNIQUE_WORK_NAME = "SunshiftWallpaperPeriodicWork"
        private const val PREFS = "sunshift_prefs"
        private const val KEY_ENABLED = "enabled"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        val enabled = prefs.getBoolean(KEY_ENABLED, false)
        binding.enableSwitch.isChecked = enabled

        binding.enableSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_ENABLED, isChecked).apply()
            if (isChecked) {
                schedulePeriodicWork()
                // Also run once immediately so the user sees it work right away
                WallpaperWorker.enqueueOneTimeNow(applicationContext)
            } else {
                WorkManager.getInstance(applicationContext)
                    .cancelUniqueWork(UNIQUE_WORK_NAME)
            }
        }

        if (enabled) {
            schedulePeriodicWork()
        }
    }

    private fun schedulePeriodicWork() {
        val workRequest = PeriodicWorkRequestBuilder<WallpaperWorker>(1, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
}