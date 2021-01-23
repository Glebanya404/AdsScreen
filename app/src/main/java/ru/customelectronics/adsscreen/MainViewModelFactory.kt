package ru.customelectronics.adsscreen

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.customelectronics.adsscreen.repository.ServerRepository
import ru.customelectronics.adsscreen.repository.SqlRepository

class MainViewModelFactory(
    private val serverRepository: ServerRepository,
    private val sqlRepository: SqlRepository,
    private val filesDir: String
): ViewModelProvider.Factory{

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MainViewModel(serverRepository, sqlRepository, filesDir) as T
    }
}