package com.github.kreativshikkk.taskmanagerplugin.toolWindow

import com.intellij.ide.ui.LafManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.swing.*

class MyToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow()
        val content = ContentFactory.getInstance().createContent(myToolWindow.content, null, false)
        toolWindow.contentManager.addContent(content)

        val iconPath = if ( LafManager.getInstance().currentUIThemeLookAndFeel.isDark) {
            "/icons/dark/window_icon.svg"
        } else {
            "/icons/light/window_icon.svg"
        }
        toolWindow.setIcon(IconLoader.getIcon(iconPath, javaClass))
    }

    class MyToolWindow {
        val content: JPanel = JBPanel<JBPanel<*>>().apply {
            layout = BorderLayout()

            val taskListPanel = JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
            }

            val scrollPane = JScrollPane(taskListPanel).apply{
                verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
                horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
                border = BorderFactory.createEmptyBorder()
            }

            val toolBar = JPanel().apply {
                layout = BoxLayout(this, BoxLayout.X_AXIS)
                preferredSize = Dimension(100, 32)
                maximumSize = Dimension(100, 32)
            }

            val addButton = createIconButton("/icons/light/add.svg", "/icons/dark/add.svg", "Add task").apply{
                addActionListener{
                    val newTaskPanel = TaskPanel( "")
                    taskListPanel.add(newTaskPanel, 0)
                    taskListPanel.revalidate()
                    taskListPanel.repaint()
                    scrollPane.viewport.viewPosition = Point(0, 0)
                }
            }

            val sortByDeadlineButton = createIconButton("/icons/light/sort_deadline.svg", "/icons/dark/sort_deadline.svg", "Sort by deadline").apply{
                addActionListener{
                    val now = LocalDateTime.now()
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    val taskPanels = taskListPanel.components.filterIsInstance<TaskPanel>()
                    taskPanels.sortedBy {
                        val deadlineText = it.getDeadlineField().text
                        val deadline = try {
                            LocalDateTime.parse(deadlineText, formatter)
                        } catch (e: DateTimeParseException) {
                            LocalDateTime.MAX
                        }
                        Duration.between(now, deadline).toMillis()
                    }.forEachIndexed { index, taskPanel ->
                        taskListPanel.add(taskPanel, index)
                    }
                    taskListPanel.revalidate()
                    taskListPanel.repaint()
                }
            }

            val sortByPriorityButton = createIconButton("/icons/light/sort_priority.svg", "/icons/dark/sort_priority.svg", "Sort by priority").apply{
                addActionListener{
                    val taskPanels = taskListPanel.components.filterIsInstance<TaskPanel>()
                    taskPanels.sortedBy { it.getPriorityField().text.toIntOrNull() }
                        .forEachIndexed { index, taskPanel ->
                            taskListPanel.add(taskPanel, index)
                        }
                    taskListPanel.revalidate()
                    taskListPanel.repaint()
                }
            }

            toolBar.add(addButton)
            toolBar.add(sortByDeadlineButton)
            toolBar.add(sortByPriorityButton)

            add(toolBar, BorderLayout.NORTH)
            add(scrollPane, BorderLayout.CENTER)
        }

        private fun createIconButton(lightIconPath: String, darkIconPath: String, tooltipText: String): JButton {
            val iconPath = if ( LafManager.getInstance().currentUIThemeLookAndFeel.isDark) {
                darkIconPath
            } else {
                lightIconPath
            }
            val icon = IconLoader.getIcon(iconPath, javaClass)
            return RoundedButton(icon).apply {
                toolTipText = tooltipText

                addMouseListener(object : MouseAdapter() {
                    override fun mouseEntered(e: MouseEvent?) {
                        cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                    }

                    override fun mouseExited(e: MouseEvent?) {
                        cursor = Cursor.getDefaultCursor()
                    }
                })
            }
        }
    }
}

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
