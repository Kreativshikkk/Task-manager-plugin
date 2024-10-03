package com.github.kreativshikkk.taskmanagerplugin.toolWindow

import com.github.kreativshikkk.taskmanagerplugin.MyBundle
import com.github.kreativshikkk.taskmanagerplugin.graphicObjects.DeadlineField
import com.github.kreativshikkk.taskmanagerplugin.graphicObjects.DescriptionField
import com.github.kreativshikkk.taskmanagerplugin.graphicObjects.PriorityField
import com.intellij.ide.ui.LafManager
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.components.JBScrollPane
import java.awt.*
import java.awt.event.ActionEvent
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class TaskPanel(
    description: String,
    private val taskListPanel: JPanel,
    private val myToolWindow: MyToolWindowFactory.MyToolWindow
) : JPanel() {
    private val checkBox: JCheckBox
    private val descriptionField: JTextArea
    private val priorityField: JTextField
    private val inactiveColor: Color
    private val activeColor: Color
    private val deleteIcon: Icon
    private val deadlineField: JTextField
    private val deleteButton: JButton

    init {
        if (LafManager.getInstance().currentUIThemeLookAndFeel.isDark) {
            inactiveColor = Color.decode(MyBundle.message("color.dark.inactive"))
            activeColor = Color.decode(MyBundle.message("color.dark.active"))
            deleteIcon = IconLoader.getIcon("/icons/dark/delete.svg", javaClass)
        } else {
            inactiveColor = Color.decode(MyBundle.message("color.light.inactive"))
            activeColor = Color.decode(MyBundle.message("color.light.active"))
            deleteIcon = IconLoader.getIcon("/icons/light/delete.svg", javaClass)
        }

        layout = BorderLayout()
        border = BorderFactory.createEmptyBorder()

        descriptionField = DescriptionField(description).descriptionField
        priorityField = PriorityField().priorityField
        deadlineField = DeadlineField().deadlineField

        descriptionField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) = saveTasks()
            override fun removeUpdate(e: DocumentEvent?) = saveTasks()
            override fun changedUpdate(e: DocumentEvent?) = saveTasks()
        })
        deadlineField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) = saveTasks()
            override fun removeUpdate(e: DocumentEvent?) = saveTasks()
            override fun changedUpdate(e: DocumentEvent?) = saveTasks()
        })

        priorityField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) = saveTasks()
            override fun removeUpdate(e: DocumentEvent?) = saveTasks()
            override fun changedUpdate(e: DocumentEvent?) = saveTasks()
        })

        checkBox = JCheckBox()
        checkBox.addActionListener { _: ActionEvent? -> updateCompletion(); saveTasks() }

        deleteButton = JButton(deleteIcon)
        deleteButton.preferredSize = Dimension(32, 32)
        deleteButton.maximumSize = Dimension(32, 32)
        deleteButton.addActionListener { _: ActionEvent? -> removeTask(); saveTasks() }

        val checkBoxContainer = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply {
            add(checkBox)
        }

        val buttonContainer = JPanel(FlowLayout(FlowLayout.RIGHT, 0, 0)).apply {
            add(deleteButton)
        }

        val priorityPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply {
            add(JLabel(MyBundle.message("priority.field.label")))
            add(priorityField)
        }

        val deadlinePanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply {
            add(JLabel(MyBundle.message("deadline.field.label")))
            add(deadlineField)
        }

        val descriptionScrollPane = JBScrollPane(descriptionField)
        descriptionScrollPane.border = BorderFactory.createEmptyBorder()

        val textContainer = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            add(priorityPanel)
            add(deadlinePanel)
            add(descriptionScrollPane)
        }

        add(textContainer, BorderLayout.CENTER)
        add(buttonContainer, BorderLayout.EAST)
        add(checkBoxContainer, BorderLayout.WEST)
    }

    fun getPriorityField(): JTextField {
        return priorityField
    }

    fun getDeadlineField(): JTextField {
        return deadlineField
    }

    fun getDescriptionField(): JTextArea {
        return descriptionField
    }

    fun getCheckBox(): JCheckBox {
        return checkBox
    }

    private fun saveTasks() {
        myToolWindow.saveTasks(taskListPanel)
    }

    override fun addNotify() {
        super.addNotify()
        preferredSize = Dimension(parent.width, descriptionField.maximumSize.height + 10)
        maximumSize = Dimension(Int.MAX_VALUE, descriptionField.maximumSize.height + 10)
    }

    private fun updateCompletion() {
        if (checkBox.isSelected) {
            descriptionField.font = descriptionField.font.deriveFont(Font.ITALIC)
            descriptionField.foreground = inactiveColor
        } else {
            descriptionField.font = descriptionField.font.deriveFont(Font.PLAIN)
            descriptionField.foreground = activeColor
        }
    }

    private fun removeTask() {
        val parent = parent
        parent.remove(this)
        parent.revalidate()
        parent.repaint()
    }
}
