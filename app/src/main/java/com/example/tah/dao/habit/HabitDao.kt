package com.example.tah.dao.habit

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.example.tah.dao.BaseDao
import com.example.tah.models.Habit
import io.reactivex.Single


@Dao
interface HabitDao : BaseDao<Habit> {

    @Query("SELECT * FROM Habits ORDER BY id ASC")
    fun getAll(): LiveData<List<Habit>>

    @Query("SELECT * FROM Habits WHERE id=:habitId")
    suspend fun getById(habitId: Long?): Habit

    @Query("DELETE FROM habits WHERE id in (:selectedHabitsIds)")
    fun deleteSelected(selectedHabitsIds: List<Int>): Single<Int>

    @Query( "DELETE FROM habits")
    fun deleteAll(): Int

}