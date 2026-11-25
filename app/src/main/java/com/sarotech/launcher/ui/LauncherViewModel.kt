package com.sarotech.launcher.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sarotech.launcher.data.AppEntry
import com.sarotech.launcher.data.LauncherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LauncherUiState(
    val apps: List<AppEntry> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class LauncherViewModel(
    private val repository: LauncherRepository
) : ViewModel() {
    private val _state = MutableStateFlow(LauncherUiState())
    val state: StateFlow<LauncherUiState> = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            runCatching { repository.loadLaunchableApps() }
                .onSuccess { _state.value = LauncherUiState(apps = it, isLoading = false) }
                .onFailure {
                    _state.value = LauncherUiState(
                        apps = emptyList(),
                        isLoading = false,
                        error = it.message ?: "Unable to load apps"
                    )
                }
        }
    }
}
