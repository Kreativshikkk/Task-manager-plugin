package com.github.kreativshikkk.taskmanagerplugin.graphicObjects

import com.github.kreativshikkk.taskmanagerplugin.MyBundle
import com.intellij.ide.ui.LafManager
import com.intellij.ui.JBColor
import java.awt.Color
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.time.LocalDateTime
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import java.util.regex.Pattern
import javax.swing.BorderFactory
import javax.swing.JOptionPane
import javax.swing.JTextField

class DeadlineField {
    private val inactiveColor: Color
    private val activeColor: Color

    init {
        if (LafManager.getInstance().currentUIThemeLookAndFeel.isDark) {
            inactiveColor = Color.decode(MyBundle.message("color.dark.inactive"))
            activeColor = Color.decode(MyBundle.message("color.dark.active"))
        } else {
            inactiveColor = Color.decode(MyBundle.message("color.light.inactive"))
            activeColor = Color.decode(MyBundle.message("color.light.active"))
        }
    }
    internal val deadlineField = JTextField(20).apply {
        border = BorderFactory.createEmptyBorder()
        text = MyBundle.message("deadline.field.default.text")
        foreground = inactiveColor
        background = Color.decode(MyBundle.message("color.light.background"))

        addFocusListener(object : FocusAdapter() {
            override fun focusGained(e: FocusEvent) {
                if (text == MyBundle.message("deadline.field.default.text")) {
                    text = ""
                    foreground = activeColor
                }
            }

            override fun focusLost(e: FocusEvent) {
                if (text.isEmpty()) {
                    text = MyBundle.message("deadline.field.default.text")
                    foreground = inactiveColor
                } else {
                    validateDateTime()
                }
            }
        })
    }

    private fun validateDateTime() {
        val datePattern = "uuuu-MM-dd HH:mm:ss"
        val formatter = DateTimeFormatterBuilder()
            .appendPattern(datePattern)
            .toFormatter()
            .withResolverStyle(ResolverStyle.STRICT)

        val matcher = Pattern.compile("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$").matcher(deadlineField.text)

        if (!matcher.matches()) {
            showError(MyBundle.message("deadline.field.format.error.text"))
            return
        }

        try {
            val dateTime = LocalDateTime.parse(deadlineField.text, formatter)

            if (dateTime.isBefore(LocalDateTime.now())) {
                showError(MyBundle.message("deadline.field.past.error.text"))
                return
            }
            deadlineField.foreground = activeColor
        } catch (e: DateTimeParseException) {
            showError(MyBundle.message("deadline.field.failed.parse.text"))
        }
    }

    private fun showError(message: String) {
        JOptionPane.showMessageDialog(null, message, "Format Error", JOptionPane.ERROR_MESSAGE)
        deadlineField.foreground = JBColor.RED
    }
}