package com.github.kreativshikkk.taskmanagerplugin.toolWindow

import com.intellij.ide.ui.LafManager
import com.intellij.openapi.util.IconLoader
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.time.LocalDateTime
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import java.util.regex.Pattern
import javax.swing.*
import javax.swing.text.AttributeSet
import javax.swing.text.PlainDocument

class NumberOnlyDocument : PlainDocument() {
    override fun insertString(offs: Int, str: String?, a: AttributeSet?) {
        if (str == null) return
        if (str.all { it.isDigit() } || str == "Set priority...") {
            super.insertString(offs, str, a)
        }
    }
}

class TaskPanel(description: String) : JPanel() {
    private val checkBox: JCheckBox
    private val descriptionField: JTextArea
    private val priorityField: JTextField
    private val inactiveColor: Color
    private val activeColor: Color
    private val deleteIcon: Icon
    private val deadlineField: JTextField
    private val deleteButton: JButton

    fun getPriorityField(): JTextField {
        return priorityField
    }

    fun getDeadlineField(): JTextField {
        return deadlineField
    }

    init {

        if  (LafManager.getInstance().currentUIThemeLookAndFeel.isDark){
            inactiveColor = Color(80, 80, 80)
            activeColor = Color(255, 255, 255)
            deleteIcon = IconLoader.getIcon("/icons/dark/delete.svg", javaClass)
        } else {
            inactiveColor = Color(60, 60, 60)
            activeColor = Color(0, 0, 0)
            deleteIcon = IconLoader.getIcon("/icons/light/delete.svg", javaClass)
        }

        layout = BorderLayout()
        border = BorderFactory.createEmptyBorder() // Удаление рамки

        checkBox = JCheckBox()
        checkBox.addActionListener { e: ActionEvent? -> updateCompletion() }
        val checkBoxContainer = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply {
            add(checkBox)
        }
        add(checkBoxContainer, BorderLayout.WEST)

        descriptionField = JTextArea(description, 5, 1)
        descriptionField.maximumSize = Dimension(Int.MAX_VALUE, 110)
        descriptionField.lineWrap = true
        descriptionField.wrapStyleWord = true
        descriptionField.border = BorderFactory.createEmptyBorder() // Удаление рамки у поля описания
        descriptionField.font = Font("Arial", Font.PLAIN, 14)
        if (description.isEmpty()) {
            descriptionField.text = "Write task description..."
            descriptionField.foreground = inactiveColor
        }
        descriptionField.addFocusListener(object : FocusAdapter() {
            override fun focusGained(e: FocusEvent) {
                if (descriptionField.text == "Write task description...") {
                    descriptionField.text = ""
                    descriptionField.foreground = activeColor
                }
            }

            override fun focusLost(e: FocusEvent) {
                if (descriptionField.text.isEmpty()) {
                    descriptionField.text = "Write task description..."
                    descriptionField.foreground = inactiveColor
                }
            }
        })

        priorityField = JTextField(10).apply {
            border = BorderFactory.createEmptyBorder()
            document = NumberOnlyDocument()
            document.insertString(0, "Set priority...", null)
            foreground = inactiveColor
        }

        priorityField.addFocusListener(object : FocusAdapter() {
            override fun focusGained(e: FocusEvent) {
                if (priorityField.text == "Set priority...") {
                    priorityField.text = ""
                    priorityField.foreground = activeColor
                }
            }

            override fun focusLost(e: FocusEvent) {
                if (priorityField.text.isEmpty()) {
                    priorityField.text = "Set priority..."
                    priorityField.foreground = inactiveColor
                }
            }
        })

        val priorityPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply {
            add(JLabel("Priority:"))
            add(priorityField)
        }

        deadlineField = JTextField(20).apply {
            border = BorderFactory.createEmptyBorder()
            text = "YYYY-MM-DD hh:mm:ss"
            foreground = inactiveColor

            addFocusListener(object : FocusAdapter() {
                override fun focusGained(e: FocusEvent) {
                    if (text == "YYYY-MM-DD hh:mm:ss") {
                        text = ""
                        foreground = activeColor
                    }
                }

                override fun focusLost(e: FocusEvent) {
                    if (text.isEmpty()) {
                        text = "YYYY-MM-DD hh:mm:ss"
                        foreground = inactiveColor
                    } else {
                        validateDateTime()
                    }
                }
            })
        }

        val descriptionScrollPane = JScrollPane(descriptionField)
        descriptionScrollPane.border = BorderFactory.createEmptyBorder()

        val deadlinePanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply{
            add(JLabel("Deadline:"))
            add(deadlineField)
        }

        val textContainer = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            add(priorityPanel)
            add(deadlinePanel)
            add(descriptionScrollPane)
        }

        add(textContainer, BorderLayout.CENTER)

        deleteButton = JButton(deleteIcon)
        deleteButton.preferredSize = Dimension(32, 32)
        deleteButton.maximumSize = Dimension(32, 32)
        deleteButton.addActionListener { e: ActionEvent? -> removeTask() }

        val buttonContainer = JPanel(FlowLayout(FlowLayout.RIGHT, 0, 0)).apply {
            add(deleteButton)
        }
        add(buttonContainer, BorderLayout.EAST)
    }


    private fun validateDateTime() {
        val datePattern = "uuuu-MM-dd HH:mm:ss"
        val formatter = DateTimeFormatterBuilder()
            .appendPattern(datePattern)
            .toFormatter()
            .withResolverStyle(ResolverStyle.STRICT)  // Установка строгого режима проверки

        val matcher = Pattern.compile("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$").matcher(deadlineField.text)

        if (!matcher.matches()) {
            showError("Invalid format. Please follow: YYYY-MM-DD hh:mm:ss")
            return
        }

        try {
            val dateTime = LocalDateTime.parse(deadlineField.text, formatter)

            if (dateTime.isBefore(LocalDateTime.now())) {
                showError("Date and time must be in the future.")
                return
            }
            deadlineField.foreground = activeColor
        } catch (e: DateTimeParseException) {
            showError("Failed to parse date and time. Please enter a valid date and time.")
        }
    }

    private fun showError(message: String) {
        JOptionPane.showMessageDialog(null, message, "Format Error", JOptionPane.ERROR_MESSAGE)
        deadlineField.foreground = Color.RED
    }

    override fun addNotify() {
        super.addNotify()
        preferredSize = Dimension(parent.width, descriptionField.preferredSize.height + 10)
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