package com.github.kreativshikkk.taskmanagerplugin.graphicObjects

import com.github.kreativshikkk.taskmanagerplugin.MyBundle
import com.intellij.ide.ui.LafManager
import java.awt.Color
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import javax.swing.BorderFactory
import javax.swing.JTextField

class PriorityField {
    private val inactiveColor: Color
    private val activeColor: Color
    internal val priorityField = JTextField(10)

    init {
        if (LafManager.getInstance().currentUIThemeLookAndFeel.isDark) {
            inactiveColor = Color.decode(MyBundle.message("color.dark.inactive"))
            activeColor = Color.decode(MyBundle.message("color.dark.active"))
        } else {
            inactiveColor = Color.decode(MyBundle.message("color.light.inactive"))
            activeColor = Color.decode(MyBundle.message("color.light.active"))
            priorityField.background = Color.decode(MyBundle.message("color.light.background"))
        }
        priorityField.addFocusListener(object : FocusAdapter() {
            override fun focusGained(e: FocusEvent) {
                if (priorityField.text == MyBundle.message("priority.field.default.text")) {
                    priorityField.text = ""
                    priorityField.foreground = activeColor
                }
            }

            override fun focusLost(e: FocusEvent) {
                if (priorityField.text.isEmpty()) {
                    priorityField.text = MyBundle.message("priority.field.default.text")
                    priorityField.foreground = inactiveColor
                }
            }
        })
        priorityField.apply {
            border = BorderFactory.createEmptyBorder()
            document = NumberOnlyDocument()
            document.insertString(0, MyBundle.message("priority.field.default.text"), null)
            foreground = inactiveColor
        }
    }
}