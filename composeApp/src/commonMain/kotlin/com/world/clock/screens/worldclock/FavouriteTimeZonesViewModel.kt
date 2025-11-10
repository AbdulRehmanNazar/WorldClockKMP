package com.world.clock.screens.worldclock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.world.clock.data.dao.FavouriteTimeZoneDao
import com.world.clock.data.entity.FavouriteTimeZone
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FavouriteTimeZonesViewModel(private val dao: FavouriteTimeZoneDao) : ViewModel() {

    private val _timeZones = MutableStateFlow<List<FavouriteTimeZone>>(emptyList())
    val timeZone: StateFlow<List<FavouriteTimeZone>> = _timeZones

    init {
        loadTimeZones()
    }


    fun deleteTimeZone(timeZoneDbId: Long, timeZoneId: String, timeZoneName: String) {
        viewModelScope.launch {
            dao.delete(FavouriteTimeZone(dbId = timeZoneDbId, id = timeZoneId, name = timeZoneName))
            loadTimeZones()
        }
    }

    private fun loadTimeZones() {
        viewModelScope.launch {
            val zonesFromDb = dao.getAllTimeZones()
            // val currentZone = getCurrentTimeZone()
            val allZones = mutableListOf<FavouriteTimeZone>()

            // Add current zone on top
            //   allZones.add(FavouriteTimeZone( zonesFromDb))


            _timeZones.value = zonesFromDb
        }
    }

    fun updateTimeZone(id:Long, newName: String){
        viewModelScope.launch {
            dao.updateName(id, newName)
            loadTimeZones()
        }
    }


}