package dev.gaphunter.jenkinsfilecompanion.settings

import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.JBCheckBox
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel

class JenkinsfileCompanionConfigurable : Configurable {

    private val checkboxes = JenkinsfileRule.entries.associateWith { JBCheckBox(it.displayName) }
    private var panel: JPanel? = null

    override fun getDisplayName(): String = "Jenkinsfile Companion"

    override fun createComponent(): JComponent {
        val settings = JenkinsfileCompanionSettings.getInstance()
        val newPanel = JPanel().apply { layout = BoxLayout(this, BoxLayout.Y_AXIS) }
        for (rule in JenkinsfileRule.entries) {
            val checkbox = checkboxes.getValue(rule)
            checkbox.isSelected = settings.isEnabled(rule)
            newPanel.add(checkbox)
        }
        panel = newPanel
        return newPanel
    }

    override fun isModified(): Boolean {
        val settings = JenkinsfileCompanionSettings.getInstance()
        return JenkinsfileRule.entries.any { checkboxes.getValue(it).isSelected != settings.isEnabled(it) }
    }

    override fun apply() {
        val settings = JenkinsfileCompanionSettings.getInstance()
        for (rule in JenkinsfileRule.entries) {
            settings.setEnabled(rule, checkboxes.getValue(rule).isSelected)
        }
    }

    override fun reset() {
        val settings = JenkinsfileCompanionSettings.getInstance()
        for (rule in JenkinsfileRule.entries) {
            checkboxes.getValue(rule).isSelected = settings.isEnabled(rule)
        }
    }
}
