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
        // Verify analysis count and shouldShowAd logic
        val current = AdManager.analysisCount
        var count = current
        for (i in 1..10) {
            AdManager.incrementAnalysisCount()
            count++
            assertEquals(count, AdManager.analysisCount)
            if (count > 0 && count % 2 == 0) {
                assertTrue("Count $count should trigger ad show", AdManager.shouldShowAd())
            } else {
                assertFalse("Count $count should NOT trigger ad show", AdManager.shouldShowAd())
            }
        }
    }

    @Test
    fun testTestAdUnitIdUsedAlways() {
        // Use reflection to fetch the private property 'adUnitId' from AdManager object to test its behavior.
        val privateProp = AdManager::class.java.getDeclaredMethod("getAdUnitId")
        privateProp.isAccessible = true
        val value = privateProp.invoke(AdManager) as String
        // Must always return the official Google test interstitial ad unit ID
        assertEquals("ca-app-pub-3940256099942544/1033173712", value)
    }
}
