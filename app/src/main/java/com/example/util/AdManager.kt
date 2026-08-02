package com.example.util

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdManager {
    private const val TAG = "AdManager"
    private const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-4391223105178139/4231762738"

    private var mInterstitialAd: InterstitialAd? = null
    private var isLoading = false
    private var isInitialized = false

    var analysisCount = 0
        private set

    /**
     * Initializes the Mobile Ads SDK and preloads the first ad.
     * Uses application context to prevent memory leaks.
     */
    fun initialize(context: Context) {
        if (isInitialized) return
        val appContext = context.applicationContext
        MobileAds.initialize(appContext) {
            isInitialized = true
            // Load the first ad as soon as the app starts
            loadAd(appContext)
        }
    }

    /**
     * Preloads an interstitial ad if not already loading or loaded.
     * Uses application context to avoid memory leaks.
     */
    fun loadAd(context: Context) {
        val appContext = context.applicationContext
        if (isLoading || mInterstitialAd != null) {
            Log.d(TAG, "Ad is already loaded or loading is in progress.")
            return
        }
        isLoading = true

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            appContext,
            INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                    isLoading = false
                    Log.d(TAG, "Interstitial ad loaded successfully.")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    mInterstitialAd = null
                    isLoading = false
                    Log.e(TAG, "Failed to load interstitial ad: ${loadAdError.message}")
                }
            }
        )
    }

    /**
     * Increments the count of completed laboratory analyses.
     */
    fun incrementAnalysisCount() {
        analysisCount++
        Log.d(TAG, "Analysis completed. Current count: $analysisCount")
    }

    /**
     * Checks if we should show the ad based on the completed analysis count.
     * Show ad ONLY after every second completed laboratory analysis (2, 4, 6, 8, ...).
     */
    fun shouldShowAd(): Boolean {
        val result = analysisCount > 0 && analysisCount % 2 == 0
        Log.d(TAG, "shouldShowAd: $result (count: $analysisCount)")
        return result
    }

    /**
     * Shows the preloaded interstitial ad.
     * Always calls [onAdClosed] eventually, even if the ad is not loaded or fails.
     */
    fun showAdIfReady(activity: Activity, onAdClosed: () -> Unit) {
        val ad = mInterstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Ad was dismissed.")
                    mInterstitialAd = null
                    onAdClosed()
                    // Preload the next ad immediately after dismissal
                    loadAd(activity)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "Ad failed to show: ${adError.message}")
                    mInterstitialAd = null
                    onAdClosed()
                    // Attempt to preload next ad
                    loadAd(activity)
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Ad showed full screen content.")
                }
            }
            ad.show(activity)
        } else {
            Log.d(TAG, "Ad is not ready yet. Skipping silently.")
            onAdClosed()
            // Attempt to load an ad
            loadAd(activity)
        }
    }
}
