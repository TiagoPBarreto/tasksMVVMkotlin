package com.codinginflow.tasksMVVMkotlin.data

import androidx.room.*

@Dao
interface TaskDao {

    fun getTasks(query: String,sortOrder:SortOrder, hideCompleted: Boolean):kotlinx.coroutines.flow.Flow<List<Task>> =
        when(sortOrder){
            SortOrder.BY_DATE -> getTaskSortedByDateCreated(query,hideCompleted)
            SortOrder.BY_NAME -> getTaskSortedByName(query,hideCompleted)
        }

    @Query("SELECT * FROM task_table WHERE(completed !=:hideCompleted OR completed = 0) AND name LIKE '%' || :searchQuery || '%' ORDER BY important DESC,name")
    fun getTaskSortedByName(searchQuery:String, hideCompleted:Boolean): kotlinx.coroutines.flow.Flow<List<Task>>

    @Query("SELECT * FROM task_table WHERE(completed !=:hideCompleted OR completed = 0) AND name LIKE '%' || :searchQuery || '%' ORDER BY important DESC,created")
    fun getTaskSortedByDateCreated(searchQuery:String, hideCompleted:Boolean): kotlinx.coroutines.flow.Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)
    @Delete
    suspend fun delete(task: Task)

    @Query("DELETE FROM task_table WHERE completed = 1")
    suspend fun deletedCompletedTasks()


}