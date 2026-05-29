package com.chat.aichatbot

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.chat.aichatbot.admob.AppOpenAdManager
import com.google.android.gms.ads.MobileAds

class ChatBotApp : Application(), Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    private lateinit var appOpenAdManager: AppOpenAdManager
    private var currentActivity: Activity? = null

    companion object {
        private const val LOG_TAG = "ChatBotApp"
    }

    override fun onCreate() {
        super<Application>.onCreate()
        
        // Register Activity Lifecycle Callbacks to track the current active Activity.
        registerActivityLifecycleCallbacks(this)
        
        // Setup AppOpenAdManager
        appOpenAdManager = AppOpenAdManager()
        
        // Register for process lifecycle notifications (DefaultLifecycleObserver) to observe foreground changes.
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        // Initialize Google Mobile Ads SDK asynchronously
        Log.d(LOG_TAG, "Initializing MobileAds SDK...")
        MobileAds.initialize(this) { status ->
            Log.d(LOG_TAG, "MobileAds SDK Initialized: $status")
            // Prefetch the first App Open Ad
            appOpenAdManager.loadAd(this)
        }
    }

    /**
     * DefaultLifecycleObserver method. Fired when the app is brought back into the foreground.
     */
    override fun onStart(owner: LifecycleOwner) {
        // No super call needed for DefaultLifecycleObserver interface default empty method
        Log.d(LOG_TAG, "App brought to foreground. Requesting App Open Ad...")
        currentActivity?.let {
            appOpenAdManager.showAdIfAvailable(it)
        }
    }

    // --- ActivityLifecycleCallbacks Implementation ---

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        // Update the current active activity reference as it starts.
        if (!appOpenAdManager.isShowingAd) {
            currentActivity = activity
        }
    }

    override fun onActivityResumed(activity: Activity) {
        // Also update currentActivity in resume for safety.
        if (!appOpenAdManager.isShowingAd) {
            currentActivity = activity
        }
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity == activity) {
            currentActivity = null
        }
    }
}
