package com.zioanacleto.feedtracker.features.settings.tracking

import com.zioanacleto.feedtracker.data.repositories.InMemoryTrackingPreferencesRepository
import com.zioanacleto.feedtracker.domain.preferences.DateDisplayFormat
import com.zioanacleto.feedtracker.domain.preferences.DurationDisplayFormat
import com.zioanacleto.feedtracker.domain.preferences.PersonPrefillMode
import com.zioanacleto.feedtracker.domain.preferences.TrackingPreferences
import com.zioanacleto.feedtracker.testutil.runViewModelTest
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class TrackingPreferencesViewModelTest {

    @Test
    fun savePersistsCanonicalBirthDateAndSelectedFormats() = runViewModelTest {
        val repository = InMemoryTrackingPreferencesRepository()
        val viewModel = TrackingPreferencesViewModel(repository)

        viewModel.onDateFormatChange(DateDisplayFormat.MONTH_DAY_YEAR)
        viewModel.onDayStartHourChange(6)
        viewModel.onDayStartMinuteChange(15)
        viewModel.onDurationFormatChange(DurationDisplayFormat.HOURS_MINUTES)
        viewModel.onPersonPrefillModeChange(PersonPrefillMode.CUSTOM)
        viewModel.onDefaultPersonNameChange("Luigi")
        viewModel.onDefaultPersonSurnameChange("Bianchi")
        viewModel.onDefaultPersonBirthDateChange("02/02/1991")
        viewModel.save()

        val saved = repository.preferences.value
        saved shouldBe TrackingPreferences(
            dateFormat = DateDisplayFormat.MONTH_DAY_YEAR,
            dayStartHour = 6,
            dayStartMinute = 15,
            durationFormat = DurationDisplayFormat.HOURS_MINUTES,
            personPrefillMode = PersonPrefillMode.CUSTOM,
            defaultPersonName = "Luigi",
            defaultPersonSurname = "Bianchi",
            defaultPersonBirthDate = "02/02/1991",
        )
        viewModel.uiState.value.saveSucceeded shouldBe true
    }
}
