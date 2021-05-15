package com.example.tah.dao.task

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.tah.models.Task
import com.example.tah.utilities.State
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class TaskRepository(application: Application) {

    private var taskDao: TaskDao
    private var tasksLD: LiveData<List<Task>>
    private var state: MutableLiveData<State> = MutableLiveData()

    private val disposable = CompositeDisposable()

    init{
        val database: TaskDatabase = TaskDatabase.getDatabase(application)
        taskDao = database.taskDao()
        tasksLD = taskDao.getAll()
    }

    fun getState(): MutableLiveData<State> = state

    fun getTasksLD(): LiveData<List<Task>>{
        return tasksLD
    }

    fun add(task: Task){
        state.value = State.loading()

        disposable.add(taskDao.insert(task)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({state.value = State.added()},
                        {state.value = State.error("Error")}))
    }

    fun getById(id: Int?): Single<Task> {
        return taskDao.getById(id)
    }

    fun delete(task: Task){
        state.value = State.loading()

        disposable.add(taskDao.delete(task)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({state.value = State.removed("Task removed")},
                        {state.value = State.error("Error")}))
    }

    fun deleteSelected(idList: List<Int>){
        disposable.add(taskDao.deleteSelected(idList)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({state.value = State.removed("Tasks removed")},
                        {state.value = State.error("Error")}))
    }

    fun deleteAll(){
        //TaskDatabase.databaseWriteExecutor.execute { taskDao.deleteAll() }
        disposable.add(taskDao.deleteAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe())
    }

    fun update(task: Task){
        disposable.add(taskDao.update(task)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({state.value = State.success("Task updated")},
                        {state.value = State.error("Task not updated")}))
    }
}