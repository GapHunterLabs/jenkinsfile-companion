package dev.gaphunter.jenkinsfilecompanion.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service

/** Every rule on by default, every rule independently disableable -- same discipline as gitlab-ci-companion's settings. */
@State(name = "JenkinsfileCompanionSettings", storages = [Storage("jenkinsfileCompanion.xml")])
class JenkinsfileCompanionSettings : PersistentStateComponent<JenkinsfileCompanionSettings.State> {

    class State {
        var disabledRuleIds: MutableSet<String> = mutableSetOf()
    }

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    fun isEnabled(rule: JenkinsfileRule): Boolean = rule.id !in state.disabledRuleIds

    fun setEnabled(rule: JenkinsfileRule, enabled: Boolean) {
        if (enabled) state.disabledRuleIds.remove(rule.id) else state.disabledRuleIds.add(rule.id)
    }

    companion object {
        fun getInstance(): JenkinsfileCompanionSettings = service()
    }
}
