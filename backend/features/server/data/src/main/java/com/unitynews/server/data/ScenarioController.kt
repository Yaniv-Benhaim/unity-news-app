package com.unitynews.server.data

import com.unitynews.server.domain.model.ServerScenario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ScenarioController {
    private val _scenario = MutableStateFlow(ServerScenario.Normal)

    val scenario: StateFlow<ServerScenario> = _scenario.asStateFlow()

    fun setScenario(scenario: ServerScenario) {
        _scenario.value = scenario
    }
}
