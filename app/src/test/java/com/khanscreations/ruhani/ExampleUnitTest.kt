package com.khanscreations.ruhani

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun adhanQibla_dhaka_isAccurate() {
    // Dhaka, Bangladesh: Lat 23.8103, Lng 90.4125
    val bearing = com.khanscreations.ruhani.domain.AstronomicalEngine.calculateQiblaBearing(23.8103, 90.4125)
    // Qibla from Dhaka should be around 277.5°
    assertTrue("Qibla bearing should be around 277.5°, got $bearing", bearing in 275.0..280.0)
  }

  @Test
  fun adhanPrayerTimes_schedule_isCalculated() {
    val district = com.khanscreations.ruhani.domain.BdDistrict(
      id = "dhaka",
      nameEn = "Dhaka",
      nameBn = "ঢাকা",
      divisionBn = "ঢাকা",
      latitude = 23.8103,
      longitude = 90.4125
    )
    val schedule = com.khanscreations.ruhani.domain.AstronomicalEngine.calculateSchedule(
      district = district,
      method = com.khanscreations.ruhani.domain.CalculationMethod.ISLAMIC_FOUNDATION_BD,
      madhhab = com.khanscreations.ruhani.domain.Madhhab.HANAFI
    )
    assertNotNull(schedule)
    assertEquals(5, schedule.fivePrayers.size)
    assertTrue("Qibla bearing in schedule should be valid", schedule.qiblaBearingDegrees in 275.0..280.0)
    assertTrue("Distance to Kaaba should be valid", schedule.distanceToKaabaKm in 5000..5500)
  }
}
