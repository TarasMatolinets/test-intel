package com.android.aura.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.android.aura.R
import com.android.aura.models.BootLoadData
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

const val SHARED_PREF_NAME = "boot-load-name"
class MainViewModel( application: Application) : AndroidViewModel(application) {
   private val _uiState  = MutableStateFlow<UIState>(UIState.Empty)
    val uiState: StateFlow<UIState> = _uiState.asStateFlow()
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    init {

        coroutineScope.launch {
            val dataToLoad: BootLoadData = getBootLoadData(application)

            when (dataToLoad.count)
            {
                0 -> _uiState.value = UIState.NoBoots(dataToLoad)
                1 -> _uiState.value = UIState.SingleBoots(dataToLoad)
                else -> _uiState.value = UIState.MultipleBoots(dataToLoad)
            }
        }
    }

    private fun getBootLoadData(application: Application): BootLoadData {
        val sharedPref = application.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        val result = sharedPref.getString(application.getString(R.string.pref_key), application.getString(R.string.no_boots_detected))

        return if(result == application.getString(R.string.no_boots_detected)) {
            BootLoadData(0,0)
        }else {
            val gson = Gson()
            gson.fromJson(result, BootLoadData::class.java)
        }
    }

    sealed class UIState {
        object Empty : UIState()
        data class NoBoots (val data: BootLoadData): UIState()
        data class SingleBoots (val data: BootLoadData): UIState()
        data class MultipleBoots (val data: BootLoadData) : UIState()
    }
}
