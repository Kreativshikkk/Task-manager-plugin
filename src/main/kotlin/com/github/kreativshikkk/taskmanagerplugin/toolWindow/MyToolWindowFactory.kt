package com.github.kreativshikkk.taskmanagerplugin.toolWindow

import com.github.kreativshikkk.taskmanagerplugin.MyBundle
import com.github.kreativshikkk.taskmanagerplugin.graphicObjects.RoundedButton
import com.github.kreativshikkk.taskmanagerplugin.state.TaskManagerState
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
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import javax.swing.*

class MyToolWindowFactory : ToolWindowFactory {
    private lateinit var globalToolWindow: ToolWindow
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        this.globalToolWindow = toolWindow
        val myToolWindow = MyToolWindow(project)
        val content = ContentFactory.getInstance().createContent(myToolWindow.content, null, false)
        this.globalToolWindow.contentManager.addContent(content)

        val iconPath = if (LafManager.getInstance().currentUIThemeLookAndFeel.isDark) {
            "/icons/dark/window_icon.svg"
        } else {
            "/icons/light/window_icon.svg"
        }
        this.globalToolWindow.setIcon(IconLoader.getIcon(iconPath, javaClass))
    }

    class MyToolWindow(private val project: Project) {
        private val inactiveColor: String
        private val activeColor: String
        private val addButton: JButton
        private val sortByDeadlineButton: JButton
        private val sortByPriorityButton: JButton

        init {
            if (LafManager.getInstance().currentUIThemeLookAndFeel.isDark) {
                inactiveColor = MyBundle.message("color.dark.inactive")
                activeColor = MyBundle.message("color.dark.active")
            } else {
                inactiveColor = MyBundle.message("color.light.inactive")
                activeColor = MyBundle.message("color.light.active")
            }
        }

        val content: JPanel = JBPanel<JBPanel<*>>().apply {
            layout = BorderLayout()

            val taskListPanel = JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
            }

            val scrollPane = JScrollPane(taskListPanel).apply {
                verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
                horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
                border = BorderFactory.createEmptyBorder()
            }

            val toolBar = JPanel().apply {
                layout = BoxLayout(this, BoxLayout.X_AXIS)
                preferredSize = Dimension(100, 32)
                maximumSize = Dimension(100, 32)
            }

            addButton = createIconButton("/icons/light/add.svg", "/icons/dark/add.svg", "Add task").apply {
                addActionListener {
                    val newTaskPanel = TaskPanel("", taskListPanel, this@MyToolWindow)
                    taskListPanel.add(newTaskPanel, 0)
                    taskListPanel.revalidate()
                    taskListPanel.repaint()
                    scrollPane.viewport.viewPosition = Point(0, 0)
                    saveTasks(taskListPanel)
                }
            }

            sortByDeadlineButton = createIconButton(
                "/icons/light/sort_deadline.svg",
                "/icons/dark/sort_deadline.svg",
                "Sort by deadline"
            ).apply {
                addActionListener {
                    val now = LocalDateTime.now()
                    val datePattern = "uuuu-MM-dd HH:mm:ss"
                    val formatter = DateTimeFormatterBuilder()
                        .appendPattern(datePattern)
                        .toFormatter()
                        .withResolverStyle(ResolverStyle.STRICT)
                    val taskPanels = taskListPanel.components.filterIsInstance<TaskPanel>()
                    taskPanels.sortedBy {
                        val deadlineText = it.getDeadlineField().text
                        val deadline = try {
                            LocalDateTime.parse(deadlineText, formatter)
                        } catch (e: DateTimeParseException) {
                            throw RuntimeException("Failed to parse date and time. Please enter a valid date and time.")
                        }
                        Duration.between(now, deadline).toMillis()
                    }.forEachIndexed { index, taskPanel ->
                        taskListPanel.add(taskPanel, index)
                    }
                    taskListPanel.revalidate()
                    taskListPanel.repaint()
                    saveTasks(taskListPanel)
                }
            }

            sortByPriorityButton = createIconButton(
                "/icons/light/sort_priority.svg",
                "/icons/dark/sort_priority.svg",
                "Sort by priority"
            ).apply {
                addActionListener {
                    val taskPanels = taskListPanel.components.filterIsInstance<TaskPanel>()
                    taskPanels.sortedBy { it.getPriorityField().text.toIntOrNull() }
                        .forEachIndexed { index, taskPanel ->
                            taskListPanel.add(taskPanel, index)
                        }
                    taskListPanel.revalidate()
                    taskListPanel.repaint()
                    saveTasks(taskListPanel)
                }
            }

            toolBar.add(addButton)
            toolBar.add(sortByDeadlineButton)
            toolBar.add(sortByPriorityButton)

            add(toolBar, BorderLayout.NORTH)
            add(scrollPane, BorderLayout.CENTER)

            loadTasks(taskListPanel)
        }

        private fun loadTasks(taskListPanel: JPanel) {
            val state = project.getService(TaskManagerState::class.java).state
            state.tasks.forEach { task ->
                val taskPanel = TaskPanel(task.description, taskListPanel, this@MyToolWindow).apply {
                    getDeadlineField().text = task.deadline
                    getPriorityField().text = task.priority
                    getCheckBox().isSelected = task.checkbox

                    getDescriptionField().foreground =
                        Color.decode(task.descriptionColor) ?: Color.decode(inactiveColor)
                    getPriorityField().foreground = Color.decode(task.priorityColor) ?: Color.decode(inactiveColor)
                    getDeadlineField().foreground = Color.decode(task.deadlineColor) ?: Color.decode(inactiveColor)

                    getDescriptionField().font = Font("Arial", task.descriptionFormat.toInt(), 14)
                }
                taskListPanel.add(taskPanel)
            }
            taskListPanel.revalidate()
            taskListPanel.repaint()
        }

        fun saveTasks(taskListPanel: JPanel) {
            val state = project.getService(TaskManagerState::class.java).state
            state.tasks = taskListPanel.components.filterIsInstance<TaskPanel>().map { taskPanel ->
                TaskManagerState.Task(
                    description = taskPanel.getDescriptionField().text ?: "Write task description...",
                    priority = taskPanel.getPriorityField().text ?: "Set priority...",
                    deadline = taskPanel.getDeadlineField().text ?: "YYYY-MM-DD hh:mm:ss",
                    checkbox = taskPanel.getCheckBox().isSelected,

                    descriptionColor = "#${
                        Integer.toHexString(taskPanel.getDescriptionField().foreground.rgb).substring(2)
                    }".ifEmpty { inactiveColor },
                    priorityColor = "#${
                        Integer.toHexString(taskPanel.getPriorityField().foreground.rgb).substring(2)
                    }".ifEmpty { inactiveColor },
                    deadlineColor = "#${
                        Integer.toHexString(taskPanel.getDeadlineField().foreground.rgb).substring(2)
                    }".ifEmpty { inactiveColor },

                    descriptionFormat = taskPanel.getDescriptionField().font.style.toString()
                        .ifEmpty { Font.PLAIN.toString() }
                )
            }
            project.getService(TaskManagerState::class.java).loadState(state)
        }

        private fun createIconButton(lightIconPath: String, darkIconPath: String, tooltipText: String): JButton {
            val iconPath = if (LafManager.getInstance().currentUIThemeLookAndFeel.isDark) {
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