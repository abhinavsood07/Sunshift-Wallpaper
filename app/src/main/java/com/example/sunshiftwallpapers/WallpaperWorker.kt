package com.example.sunshift

import android.app.WallpaperManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.Calendar

class WallpaperWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        return try {
            val resId = pickWallpaperResId()
            val bitmap = BitmapFactory.decodeResource(applicationContext.resources, resId)
            val wm = WallpaperManager.getInstance(applicationContext)

            // Set as home screen wallpaper
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                wm.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                // Try to set lock screen as well (allowed on most devices)
                try {
                    wm.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                } catch (_: Exception) { /* Ignore if not permitted */ }
            } else {
                @Suppress("DEPRECATION")
                wm.setBitmap(bitmap)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun pickWallpaperResId(): Int {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..10 -> R.drawable.wallpaper_morning
            in 11..16 -> R.drawable.wallpaper_day
            in 17..19 -> R.drawable.wallpaper_evening
            else -> R.drawable.wallpaper_night
        }
    }

    companion object {
        fun enqueueOneTimeNow(context: Context) {
            val request = OneTimeWorkRequestBuilder<WallpaperWorker>().build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}