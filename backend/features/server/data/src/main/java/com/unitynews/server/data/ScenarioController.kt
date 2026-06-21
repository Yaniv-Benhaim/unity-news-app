package com.unitynews.server.data

import com.unitynews.server.domain.model.ServerScenario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Holds the currently selected backend demo scenario.
 *
 * Both the visible console and the AIDL service observe/use this same state.
 */
class ScenarioController {
    private val _scenario = MutableStateFlow(ServerScenario.Normal)

    val scenario: StateFlow<ServerScenario> = _scenario.asStateFlow()

    /** Switch the scenario used for future backend requests. */
    fun setScenario(scenario: ServerScenario) {
        _scenario.value = scenario
    }
}
