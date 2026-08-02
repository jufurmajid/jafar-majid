package com.example.util

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdManager {
    private const val TAG = "AdManager"

    // Real AdMob Interstitial Ad Unit ID for production releases
    private const val PROD_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-4391223105178139/4231762738"
    // Google's official Interstitial TEST Ad Unit ID for debugging/testing
    private const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    private var mInterstitialAd: InterstitialAd? = null
    private var isLoading = false
    private var isInitialized = false

    var analysisCount = 0
        private set

    /**
     * Gets the appropriate Ad Unit ID based on build type.
     */
    private val adUnitId: String
        get() = if (BuildConfig.DEBUG) {
            Log.d(TAG, "Build is in DEBUG mode. Using Google official test ad unit ID: $TEST_INTERSTITIAL_AD_UNIT_ID")
            TEST_INTERSTITIAL_AD_UNIT_ID
        } else {
            Log.d(TAG, "Build is in RELEASE mode. Using production ad unit ID: $PROD_INTERSTITIAL_AD_UNIT_ID")
            PROD_INTERSTITIAL_AD_UNIT_ID
        }

    /**
     * Initializes the Mobile Ads SDK and preloads the first ad.
     * Uses application context to prevent memory leaks.
     */
    fun initialize(context: Context) {
        if (isInitialized) {
            Log.d(TAG, "Mobile Ads SDK is already initialized.")
            return
        }
        val appContext = context.applicationContext
        Log.i(TAG, "Initializing Mobile Ads SDK...")
        MobileAds.initialize(appContext) { initializationStatus ->
            isInitialized = true
            val statusMap = initializationStatus.adapterStatusMap
            for ((adapterClass, status) in statusMap) {
                Log.d(TAG, "Adapter Name: $adapterClass, State: ${status.initializationState}, Description: ${status.description}")
            }
            Log.i(TAG, "Mobile Ads SDK initialization complete. Preloading the first ad...")
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
        if (isLoading) {
            Log.d(TAG, "Ad load is already in progress. Skipping duplicate load request.")
            return
        }
        if (mInterstitialAd != null) {
            Log.d(TAG, "An ad is already loaded and ready. Skipping load request.")
            return
        }
        isLoading = true

        val targetedAdUnitId = adUnitId
        Log.i(TAG, "Requesting interstitial ad load for Unit ID: $targetedAdUnitId")

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            appContext,
            targetedAdUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                    isLoading = false
                    Log.i(TAG, "Interstitial ad loaded successfully.")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    mInterstitialAd = null
                    isLoading = false
                    Log.e(
                        TAG,
                        "Failed to load interstitial ad: Code=${loadAdError.code}, Message=${loadAdError.message}, Domain=${loadAdError.domain}"
                    )
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
            Log.i(TAG, "Interstitial ad is ready. Attempting to show...")
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.i(TAG, "Interstitial ad was dismissed.")
                    mInterstitialAd = null
                    onAdClosed()
                    // Preload the next ad immediately after dismissal
                    Log.d(TAG, "Ad dismissed. Preloading next ad...")
                    loadAd(activity)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(
                        TAG,
                        "Interstitial ad failed to show: Code=${adError.code}, Message=${adError.message}, Domain=${adError.domain}"
                    )
                    mInterstitialAd = null
                    onAdClosed()
                    // Attempt to preload next ad
                    Log.d(TAG, "Ad failed to show. Preloading next ad...")
                    loadAd(activity)
                }

                override fun onAdShowedFullScreenContent() {
                    Log.i(TAG, "Interstitial ad showed full screen content successfully.")
                }
            }
            ad.show(activity)
        } else {
            Log.w(TAG, "Interstitial ad is not ready yet. Skipping show request and calling onAdClosed callback.")
            onAdClosed()
            // Attempt to load an ad
            Log.d(TAG, "Ad not ready on show request. Initiating ad load...")
            loadAd(activity)
        }
    }
}
