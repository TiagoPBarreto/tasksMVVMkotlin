package com.codinginflow.tasksMVVMkotlin.ui.tasks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codinginflow.tasksMVVMkotlin.data.Task
import com.codinginflow.tasksMVVMkotlin.databinding.ItemTaskBinding

class TaskAdapter(private val listerner: OnItemClickListerner) : ListAdapter<Task,TaskAdapter.TasksViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TasksViewHolder {
       val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return TasksViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TasksViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

   inner class TasksViewHolder(private val binding: ItemTaskBinding): RecyclerView.ViewHolder(binding.root){

        init {
            binding.apply {
                root.setOnClickListener {
                    val position = adapterPosition
                    if(position != RecyclerView.NO_POSITION){
                        val task = getItem(position)
                        listerner.onItemClick(task)

                    }
                }
                checkBoxComplete.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION){
                        val task = getItem(position)
                        listerner.onCcheckBoxClick(task,checkBoxComplete.isChecked)
                    }
                }
            }
        }

        fun bind(task:Task){
            binding.apply {
                checkBoxComplete.isChecked = task.completed
                textName.text = task.name
                textName.paint.isStrikeThruText = task.completed
                labelPriority.isVisible = task.important
            }
        }

    }
    interface OnItemClickListerner{

        fun onItemClick(task: Task)
        fun onCcheckBoxClick(task: Task, isChecked : Boolean)

    }

    class DiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task) =
            oldItem.id == newItem.id


        override fun areContentsTheSame(oldItem: Task, newItem: Task) =
            oldItem == newItem

    }
}