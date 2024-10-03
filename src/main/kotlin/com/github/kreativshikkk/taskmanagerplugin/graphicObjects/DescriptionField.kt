package com.github.kreativshikkk.taskmanagerplugin.graphicObjects

import com.github.kreativshikkk.taskmanagerplugin.MyBundle
import com.intellij.ide.ui.LafManager
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import javax.swing.BorderFactory
import javax.swing.JTextArea

class DescriptionField(description: String) {
    internal val descriptionField: JTextArea = JTextArea(description, 5, 1)
    private val inactiveColor: Color
    private val activeColor: Color

    init {
        if (LafManager.getInstance().currentUIThemeLookAndFeel.isDark) {
            inactiveColor = Color.decode(MyBundle.message("color.dark.inactive"))
            activeColor = Color.decode(MyBundle.message("color.dark.active"))
        } else {
            inactiveColor = Color.decode(MyBundle.message("color.light.inactive"))
            activeColor = Color.decode(MyBundle.message("color.light.active"))
            descriptionField.background = Color.decode(MyBundle.message("color.light.background"))
        }
        descriptionField.maximumSize = Dimension(Int.MAX_VALUE, 110)
        descriptionField.lineWrap = true
        descriptionField.wrapStyleWord = true
        descriptionField.border = BorderFactory.createEmptyBorder()
        descriptionField.font = Font("Arial", Font.PLAIN, 14)
        if (description.isEmpty()) {
            descriptionField.text = MyBundle.message("description.field.default.text")
            descriptionField.foreground = inactiveColor
        }
        descriptionField.addFocusListener(object : FocusAdapter() {
            override fun focusGained(e: FocusEvent) {
                if (descriptionField.text == MyBundle.message("description.field.default.text")) {
                    descriptionField.text = ""
                    descriptionField.foreground = activeColor
                }
            }

            override fun focusLost(e: FocusEvent) {
                if (descriptionField.text.isEmpty()) {
                    descriptionField.text = MyBundle.message("description.field.default.text")
                    descriptionField.foreground = inactiveColor
                }
            }
        })
    }
}