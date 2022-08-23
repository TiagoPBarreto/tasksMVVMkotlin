package com.codinginflow.tasksMVVMkotlin.ui.tasks

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.codinginflow.tasksMVVMkotlin.data.PreferencesMenager
import com.codinginflow.tasksMVVMkotlin.data.SortOrder
import com.codinginflow.tasksMVVMkotlin.data.Task
import com.codinginflow.tasksMVVMkotlin.data.TaskDao
import com.codinginflow.tasksMVVMkotlin.ui.ADD_TASK_RESULT_OK
import com.codinginflow.tasksMVVMkotlin.ui.EDIT_TASK_RESULT_OK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class TasksViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    private val preferencesMenager: PreferencesMenager,
    @Assisted private val state : SavedStateHandle
) : ViewModel(){
    val  searchQuery = state.getLiveData("searchQuery","")

    val preferencesFlow = preferencesMenager.preferecesFlow
    private val taskEventsChannel = Channel<TaskEvents>()
    val taskEvent = taskEventsChannel.receiveAsFlow()


    private val taskFlow = combine(

        searchQuery.asFlow(),
        preferencesFlow

    ){  query,filterPreferences->
        Pair(query,filterPreferences)

    }.flatMapLatest {(query,filterPreferences)->

        taskDao.getTasks(query, filterPreferences.sortOrder,filterPreferences.hideCompleted)
    }
    val tasks = taskFlow.asLiveData()
    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesMenager.updateSortOrder(sortOrder)
    }
    fun onHideCompletedClick(hideCompleted:Boolean) = viewModelScope.launch {
        preferencesMenager.upfdateHideCompleted(hideCompleted)
    }

   fun onTaskSelected(task: Task) = viewModelScope.launch {
       taskEventsChannel.send(TaskEvents.NavigateToEditTaskScreen(task))
   }


    fun onTaskCheckedChanged(task: Task,isChecked:Boolean){
        viewModelScope.launch {
            taskDao.update(task.copy(completed = isChecked))
        }

    }
    fun onTaskSwiped(task: Task){
        viewModelScope.launch {
            taskDao.delete(task)
            taskEventsChannel.send(TaskEvents.ShowUndoDeleteTaskMessage(task))
        }
    }
    fun onUndoDeleteClick(task: Task) =viewModelScope.launch {
        taskDao.insert(task)
    }
    fun onAddNewTaskClick() = viewModelScope.launch {
        taskEventsChannel.send(TaskEvents.NavigatedToAddTaskScreen)
    }
    fun onAddEditResult(result:Int){

        when(result){
            ADD_TASK_RESULT_OK -> showTaskSavedConfirmationMessage("Task added")
            EDIT_TASK_RESULT_OK -> showTaskSavedConfirmationMessage("Task update")
        }
    }

    fun showTaskSavedConfirmationMessage(text:String) = viewModelScope.launch {
        taskEventsChannel.send(TaskEvents.showTaskSavedConfirmationMessage(text))

    }

    fun onDeleteAllCompletedClick() = viewModelScope.launch {

        taskEventsChannel.send(TaskEvents.NavigateToDeleteAllCompletedScreen)
    }





    sealed class TaskEvents{
        object NavigatedToAddTaskScreen : TaskEvents()
        data class NavigateToEditTaskScreen(val task: Task) :TaskEvents()
        data class ShowUndoDeleteTaskMessage(val task: Task) : TaskEvents()
        data class showTaskSavedConfirmationMessage(val msg:String) : TaskEvents()
        object NavigateToDeleteAllCompletedScreen : TaskEvents()

    }

}

