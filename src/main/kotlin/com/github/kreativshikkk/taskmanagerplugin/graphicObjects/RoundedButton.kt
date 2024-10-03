package com.github.kreativshikkk.taskmanagerplugin.graphicObjects

import com.intellij.ide.ui.LafManager
import java.awt.*
import javax.swing.Icon
import javax.swing.JButton

class RoundedButton(icon: Icon) : JButton(icon) {
    init {
        isFocusPainted = false
        isBorderPainted = false
        isContentAreaFilled = false
        isOpaque = false
        preferredSize = Dimension(32, 32)
        minimumSize = Dimension(32, 32)
        maximumSize = Dimension(32, 32)
    }

    override fun paintComponent(g: Graphics) {
        val g2 = g as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        val isDarkTheme = LafManager.getInstance().currentUIThemeLookAndFeel.isDark
        if (model.isRollover) {
            g2.color = if (isDarkTheme) Color(58, 61, 64) else Color(230, 230, 232)
        } else {
            g2.color = background
        }

        g2.fillRoundRect(0, 0, width, height, 8, 8)

        super.paintComponent(g)
    }
}