package com.codinginflow.tasksMVVMkotlin.ui.tasks

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codinginflow.tasksMVVMkotlin.R
import com.codinginflow.tasksMVVMkotlin.data.SortOrder
import com.codinginflow.tasksMVVMkotlin.data.Task
import com.codinginflow.tasksMVVMkotlin.databinding.FragmentTaskBinding
import com.codinginflow.tasksMVVMkotlin.util.exhaustive
import com.codinginflow.tasksMVVMkotlin.util.onQueryTextChanged
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TasksFragments : Fragment(R.layout.fragment_task),TaskAdapter.OnItemClickListerner{
    private val viewModel : TasksViewModel by viewModels()
    private lateinit var searchView: SearchView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentTaskBinding.bind(view)
        val taskAdapter = TaskAdapter(this)
        binding.apply {
            recyclerViewTask.apply {
                adapter = taskAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val task = taskAdapter.currentList[viewHolder.adapterPosition]
                    viewModel.onTaskSwiped(task)
                }

            }).attachToRecyclerView(recyclerViewTask)
            fabAddTask.setOnClickListener {
                viewModel.onAddNewTaskClick()
            }
        }

        setFragmentResultListener("add_edit_request") {_,bundle ->

            val result = bundle.getInt("add_edit_result")
            viewModel.onAddEditResult(result)

        }

        viewModel.tasks.observe(viewLifecycleOwner){
            taskAdapter.submitList(it)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.taskEvent.collect {event ->
                when(event){
                    is TasksViewModel.TaskEvents.ShowUndoDeleteTaskMessage ->{
                        Snackbar.make(requireView(),"Task deleted",Snackbar.LENGTH_LONG)
                            .setAction("Undo"){
                                viewModel.onUndoDeleteClick(event.task)
                            }.show()
                    }
                    is TasksViewModel.TaskEvents.NavigateToEditTaskScreen -> {
                        val action = TasksFragmentsDirections.actionTasksFragments2ToAddEditTaskFragment(event.task,"edit task")
                        findNavController().navigate(action)
                    }
                    is TasksViewModel.TaskEvents.NavigatedToAddTaskScreen -> {
                        val action = TasksFragmentsDirections.actionTasksFragments2ToAddEditTaskFragment(null,"New Task")
                        findNavController().navigate(action)
                    }
                    is TasksViewModel.TaskEvents.showTaskSavedConfirmationMessage -> {
                        Snackbar.make(requireView(),event.msg,Snackbar.LENGTH_LONG).show()

                    }
                    TasksViewModel.TaskEvents.NavigateToDeleteAllCompletedScreen -> {

                        val action = TasksFragmentsDirections.actionGlobalDeleteAllCompleteDialogFragment()
                        findNavController().navigate(action)
                    }
                }.exhaustive
            }
        }

        setHasOptionsMenu(true)
    }



    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_frag_task,menu)
        val searchItem = menu.findItem(R.id.action_search)
         searchView = searchItem.actionView as SearchView

        val pendingQuery = viewModel.searchQuery.value
        if( pendingQuery != null && pendingQuery.isNotEmpty() ){

            searchItem.expandActionView()
            searchView.setQuery(pendingQuery,false)
        }
        searchView.onQueryTextChanged{
            //update search query
            viewModel.searchQuery.value = it
        }

        viewLifecycleOwner.lifecycleScope.launch {
            menu.findItem(R.id.action_hide_completed_tasks).isChecked =
                viewModel.preferencesFlow.first().hideCompleted
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
       return when(item.itemId){
            R.id.action_sort_by_name ->{
                viewModel.onSortOrderSelected(SortOrder.BY_NAME)
                true
            }
            R.id.action_sort_by_date_created->{
                viewModel.onSortOrderSelected(SortOrder.BY_DATE)
                true
            }
            R.id.action_hide_completed_tasks ->{
                item.isChecked = !item.isChecked
                viewModel.onHideCompletedClick(item.isChecked)
                true
            }
            R.id.action_delet_all_completed_tasks->{
                viewModel.onDeleteAllCompletedClick()
                true
            }
           else-> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchView.setOnQueryTextListener(null)
    }


    override fun onItemClick(task: Task) {
        viewModel.onTaskSelected(task)
    }

    override fun onCcheckBoxClick(task: Task, isChecked: Boolean) {
       viewModel.onTaskCheckedChanged(task,isChecked)
    }
}


