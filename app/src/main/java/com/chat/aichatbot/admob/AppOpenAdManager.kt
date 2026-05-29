package com.chat.aichatbot.admob

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import java.util.Date

class AppOpenAdManager {

    companion object {
        private const val LOG_TAG = "AppOpenAdManager"
        // Google official App Open test ad unit ID
        private const val AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"
    }

    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    var isShowingAd = false
        private set

    private var loadTime: Long = 0

    /**
     * Request an ad.
     */
    fun loadAd(context: Context) {
        // Do not load ad if there is an unused ad or if we are currently loading.
        if (isLoadingAd || isAdAvailable()) {
            return
        }

        isLoadingAd = true
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            AD_UNIT_ID,
            request,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isLoadingAd = false
                    loadTime = Date().time
                    Log.d(LOG_TAG, "AppOpenAd Loaded successfully.")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoadingAd = false
                    Log.e(LOG_TAG, "AppOpenAd Failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    /**
     * Check if ad exists and has not expired.
     */
    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Long {
        val dateDifference = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference / numMilliSecondsPerHour
    }

    private fun isAdAvailable(): Boolean {
        // App Open Ad is considered expired after 4 hours as per official policy.
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4) < 4
    }

    /**
     * Shows the ad if one is available.
     */
    fun showAdIfAvailable(activity: Activity) {
        showAdIfAvailable(activity, object : OnShowAdCompleteListener {
            override fun onShowAdComplete() {
                // Default no-op
            }
        })
    }

    /**
     * Shows the ad if one is available.
     */
    fun showAdIfAvailable(activity: Activity, onShowAdCompleteListener: OnShowAdCompleteListener) {
        // If the app open ad is already showing, do not show it again.
        if (isShowingAd) {
            Log.d(LOG_TAG, "AppOpenAd is already showing.")
            return
        }

        // If the app open ad is not available, load one and complete.
        if (!isAdAvailable()) {
            Log.d(LOG_TAG, "AppOpenAd is not available. Loading one...")
            onShowAdCompleteListener.onShowAdComplete()
            loadAd(activity)
            return
        }

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                // Set the ad reference to null so that another ad can be loaded.
                appOpenAd = null
                isShowingAd = false
                Log.d(LOG_TAG, "AppOpenAd Dismissed.")
                onShowAdCompleteListener.onShowAdComplete()
                loadAd(activity)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                appOpenAd = null
                isShowingAd = false
                Log.e(LOG_TAG, "AppOpenAd Failed to show: ${adError.message}")
                onShowAdCompleteListener.onShowAdComplete()
                loadAd(activity)
            }

            override fun onAdShowedFullScreenContent() {
                isShowingAd = true
                Log.d(LOG_TAG, "AppOpenAd Showed full screen.")
            }
        }

        isShowingAd = true
        appOpenAd?.show(activity)
    }

    interface OnShowAdCompleteListener {
        fun onShowAdComplete()
    }
}
