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

    // Official Google Test Interstitial Ad Unit ID
    private const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    // Production Interstitial Ad Unit ID
    private const val PRODUCTION_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-4391223105178139/4231762738"

    private var mInterstitialAd: InterstitialAd? = null
    private var isLoading = false
    private var isInitialized = false

    private const val INITIAL_RETRY_DELAY_MS = 2000L
    private const val MAX_RETRY_DELAY_MS = 64000L
    private var retryDelayMs = INITIAL_RETRY_DELAY_MS

    private var lastAttemptedAnalysisCount = -1

    var analysisCount = 0
        private set

    /**
     * Gets the appropriate Ad Unit ID.
     * Uses the production ad unit ID.
     */
    private val adUnitId: String
        get() = PRODUCTION_INTERSTITIAL_AD_UNIT_ID

    /**
     * Initializes the Mobile Ads SDK and preloads the first ad.
     * Uses application context to prevent memory leaks.
     */
    @Synchronized
    fun initialize(context: Context) {
        if (isInitialized) {
            Log.d(TAG, "Mobile Ads SDK is already initialized.")
            return
        }
        val appContext = context.applicationContext
        Log.i(TAG, "Initializing Mobile Ads SDK...")
        // Mark as initialized immediately to avoid duplicate concurrent initialization calls
        isInitialized = true
        MobileAds.initialize(appContext) { initializationStatus ->
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
                    retryDelayMs = INITIAL_RETRY_DELAY_MS // Reset retry delay on successful load
                    Log.i(
                        TAG,
                        "Interstitial ad loaded successfully.\n" +
                        " - Response Info: ${interstitialAd.responseInfo}"
                    )
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    mInterstitialAd = null
                    isLoading = false
                    Log.e(
                        TAG,
                        "Failed to load interstitial ad:\n" +
                        " - Error Code: ${loadAdError.code}\n" +
                        " - Error Message: ${loadAdError.message}\n" +
                        " - Response Info: ${loadAdError.responseInfo}"
                    )
                    // Automatically retry using Google's recommended approach: exponential backoff
                    val currentDelay = retryDelayMs
                    retryDelayMs = (retryDelayMs * 2).coerceAtMost(MAX_RETRY_DELAY_MS)
                    Log.i(TAG, "Retrying ad load in ${currentDelay}ms (exponential backoff)...")
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        loadAd(appContext)
                    }, currentDelay)
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
     * Show ad after EVERY completed laboratory analysis.
     */
    fun shouldShowAd(): Boolean {
        val result = analysisCount > 0 && analysisCount > lastAttemptedAnalysisCount
        Log.d(TAG, "shouldShowAd: $result (count: $analysisCount, lastAttempted: $lastAttemptedAnalysisCount)")
        return result
    }

    /**
     * Shows the preloaded interstitial ad.
     * Always calls [onAdClosed] eventually, even if the ad is not loaded or fails.
     */
    fun showAdIfReady(activity: Activity, onAdClosed: () -> Unit) {
        val ad = mInterstitialAd
        // Mark that we are showing/attempting to show the ad for this analysis count, so we don't trigger again on recomposition
        lastAttemptedAnalysisCount = analysisCount

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
                        "Interstitial ad failed to show:\n" +
                        " - Error Code: ${adError.code}\n" +
                        " - Error Message: ${adError.message}"
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
