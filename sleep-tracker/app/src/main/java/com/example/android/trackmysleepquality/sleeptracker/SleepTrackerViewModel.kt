/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.launch

/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(
        val database: SleepDatabaseDao,
        application: Application) : AndroidViewModel(application) {

    private val tonight = MutableLiveData<SleepNight?>()

    val nights = database.getAllNights()
    val nightsString = Transformations.map(nights) { nights->
        formatNights(nights, application.resources)
    }

    val startButtonVisible: LiveData<Boolean?> = Transformations
        .map(tonight) { night->
        night == null
    }

    val stopButtonVisible: LiveData<Boolean?> = Transformations
        .map(tonight) { night->
        night != null
    }

    val clearButtonVisible: LiveData<Boolean?> = Transformations
        .map(nights) { nights->
        nights.isNotEmpty()
    }

    private val _eventClearSnackbar = MutableLiveData<Boolean>()
    val eventClearSnackbar: LiveData<Boolean>
        get() = _eventClearSnackbar

    fun doneShowingSnackbar() {
        _eventClearSnackbar.value = false
    }

    private val _navigateToSleepQuality = MutableLiveData<SleepNight?>()
    val navigateToSleepQuality: LiveData<SleepNight?>
        get() = _navigateToSleepQuality

    fun doneNavigating() {
        _navigateToSleepQuality.value = null
    }

    init {
        initializeTonight()
    }

    private fun initializeTonight() {
        viewModelScope.launch {
            tonight.value = getTonightFromDatabase()
        }
    }

    private suspend fun getTonightFromDatabase() : SleepNight? {
        var night = database.getTonight()
        if (night?.startTimeMilli != night?.endTimeMilli) {
            night = null
        }
        return night
    }

    fun onStartTracker() {
        viewModelScope.launch {
            val newNight = SleepNight()
            insert(newNight)

            tonight.value = getTonightFromDatabase()
        }
    }

    private suspend fun insert(night: SleepNight) {
        database.insert(night)
    }

    fun onStopTracker() {
        viewModelScope.launch {
            val oldNight = tonight.value ?: return@launch
            oldNight.endTimeMilli = System.currentTimeMillis()

            update(oldNight)
            _navigateToSleepQuality.value = oldNight
        }
    }

    private suspend fun update(night: SleepNight) {
        database.update(night)
    }

    fun onClear() {
        viewModelScope.launch {
            clear()
        }
        _eventClearSnackbar.value = true
    }

    private suspend fun clear() {
        database.clear()
    }

    private val _navigatieToSleepDataQuality = MutableLiveData<Long?>()
    val navigateToSleepDataQuality: LiveData<Long?>
        get() = _navigatieToSleepDataQuality

    fun onSleepNightClicked(nightId: Long) {
        _navigatieToSleepDataQuality.value = nightId
    }

    fun onSleepDataQualityNavigated() {
        _navigatieToSleepDataQuality.value = null
    }
}

