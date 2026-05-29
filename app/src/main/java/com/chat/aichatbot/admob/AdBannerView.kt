package com.chat.aichatbot.admob

import android.content.Context
import android.util.DisplayMetrics
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun AdBannerView(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val testAdUnitId = "ca-app-pub-3940256099942544/6300978111" // Official Google Test Banner ID

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx ->
            // Create a parent layout to contain the AdView. This is highly recommended for stable rendering inside Compose.
            val frameLayout = FrameLayout(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            val adView = AdView(ctx).apply {
                adUnitId = testAdUnitId
                setAdSize(getAdaptiveAdSize(ctx))
                loadAd(AdRequest.Builder().build())
            }

            frameLayout.addView(adView)
            frameLayout
        },
        update = { _ ->
            // No update operations needed; the ad handles its own lifecycle.
        },
        onRelease = { frameLayout ->
            // Find the AdView inside the FrameLayout and destroy it to release all memory resources.
            for (i in 0 until frameLayout.childCount) {
                val child = frameLayout.getChildAt(i)
                if (child is AdView) {
                    child.destroy()
                }
            }
        }
    )
}

/**
 * Returns the adaptive banner size based on screen width.
 */
private fun getAdaptiveAdSize(context: Context): AdSize {
    val displayMetrics: DisplayMetrics = context.resources.displayMetrics
    val adWidthPixels = displayMetrics.widthPixels.toFloat()
    val density = displayMetrics.density
    val adWidth = (adWidthPixels / density).toInt()
    return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
}
