package com.github.kreativshikkk.taskmanagerplugin.state

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import java.awt.Font

data class MyState(
    var description: String = "",
    var priority: String = "",
    var deadline: String = "",
    var checkbox: Boolean = false,
    var descriptionColor: String = "",
    var priorityColor: String = "",
    var deadlineColor: String = "",
    var descriptionFormat: String = Font.PLAIN.toString(),
    var tasks: List<TaskManagerState.Task> = emptyList()
)

@Service(Service.Level.PROJECT)
@State(name = "TaskManagerState", storages = [Storage("taskManagerPlugin.xml")])
class TaskManagerState : PersistentStateComponent<MyState> {
    private var state = MyState()

    override fun getState(): MyState {
        return state
    }

    override fun loadState(state: MyState) {
        this.state = state
    }

    data class Task(
        var description: String = "",
        var priority: String = "",
        var deadline: String = "",
        var checkbox: Boolean = false,
        var descriptionColor: String = "",
        var priorityColor: String = "",
        var deadlineColor: String = "",
        var descriptionFormat: String = Font.PLAIN.toString()
    )
}