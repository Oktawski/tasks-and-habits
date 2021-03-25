package com.example.tah.models

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.tah.R

@Entity(tableName = "tasks")
class Task(
        @PrimaryKey(autoGenerate = true) val id: Int?,

        @ColumnInfo(name = "name")
        @NonNull
        val name: String,

        @ColumnInfo(name = "description") val description: String?,

        @ColumnInfo(name = "is_complete") val isComplete: Boolean = false
): ViewType{
        constructor(name: String, description: String?, isComplete: Boolean)
        :this(null, name, description, isComplete)


        override fun getBasicView(): Int {
                return R.layout.fragment_tasks
        }

        override fun getAddView(): Int {
                return R.layout.add_task_fragment
        }

        override fun getItemView(): Int {
                return R.layout.item_task
        }
}
