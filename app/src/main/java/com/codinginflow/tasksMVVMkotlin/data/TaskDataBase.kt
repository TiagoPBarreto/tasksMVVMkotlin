package com.codinginflow.tasksMVVMkotlin.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.codinginflow.tasksMVVMkotlin.di.AppModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Task::class], version = 1)
abstract class TaskDataBase : RoomDatabase() {

abstract fun taskDao() : TaskDao
class Callback @Inject constructor(
   private val database :Provider <TaskDataBase>,
  @AppModule.ApplicationScope private val applicationScope : CoroutineScope
) : RoomDatabase.Callback(){

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)

       val dao =  database.get().taskDao()


        applicationScope.launch {

            dao.insert(Task("tiago",important = true))
            dao.insert(Task("Sarah", completed = true))
            dao.insert(Task("Leandro"))
            dao.insert(Task("Vinicius"))
        }

    }
}


}