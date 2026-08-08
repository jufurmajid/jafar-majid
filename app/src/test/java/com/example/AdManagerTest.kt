package com.example

import com.example.util.AdManager
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AdManagerTest {

    @Test
    fun testInitialState() {
        val activity = org.robolectric.Robolectric.buildActivity(android.app.Activity::class.java).setup().get()
        val current = AdManager.analysisCount
        var count = current
        for (i in 1..5) {
            AdManager.incrementAnalysisCount()
            count++
            assertEquals(count, AdManager.analysisCount)
            // It should want to show an ad now
            assertTrue("Count $count should trigger ad show", AdManager.shouldShowAd())
            // Simulate showing the ad
            AdManager.showAdIfReady(activity) { }
            // Now shouldShowAd should be false for the same count
            assertFalse("Count $count should NOT trigger ad show again", AdManager.shouldShowAd())
        }
    }

    @Test
    fun testTestAdUnitIdUsedAlways() {
        // Use reflection to fetch the private property 'adUnitId' from AdManager object to test its behavior.
        val privateProp = AdManager::class.java.getDeclaredMethod("getAdUnitId")
        privateProp.isAccessible = true
        val value = privateProp.invoke(AdManager) as String
        // Must always return the specified production interstitial ad unit ID
        assertEquals("ca-app-pub-4391223105178139/4231762738", value)
    }
}
