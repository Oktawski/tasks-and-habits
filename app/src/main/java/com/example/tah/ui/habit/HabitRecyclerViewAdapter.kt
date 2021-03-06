package com.example.tah.ui.habit

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.tah.databinding.ItemHabitBinding
import com.example.tah.models.Habit
import com.example.tah.ui.main.AddAndDetailsActivity
import com.example.tah.utilities.TimeConverter
import java.util.*

class HabitRecyclerViewAdapter(
    private val context: Context,
) : RecyclerView.Adapter<HabitRecyclerViewAdapter.ViewHolder>(),
    TimeConverter
{
    private val differCallback = object : DiffUtil.ItemCallback<Habit>() {
        override fun areItemsTheSame(oldItem: Habit, newItem: Habit): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Habit, newItem: Habit): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemHabitBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size


    inner class ViewHolder(
        private val binding: ItemHabitBinding
    ) : RecyclerView.ViewHolder(binding.root)
    {
        private lateinit var habit: Habit

        fun bind(habit: Habit){
            this.habit = habit

            val timeMap = getTimeUnitsToValuesAsStrings(habit.sessionLength)
            val timeText = String.format(Locale.ENGLISH,
                    "Time left: %s:%s:%s",
                    timeMap["Hours"],
                    timeMap["Minutes"],
                    timeMap["Seconds"])

            with(binding){
                itemName.text = habit.name
                time.text = timeText

                if (habit.description.isNullOrEmpty()){
                    description.visibility = View.GONE
                } else {
                    description.visibility = View.VISIBLE
                    description.text = habit.description
                }
            }
            setOnClickListeners()
        }

        private fun setOnClickListeners(){
            itemView.setOnClickListener {
                val intent = Intent(context, AddAndDetailsActivity::class.java)
                intent.putExtra("fragmentId", Habit.getDetailsView())
                intent.putExtra("habitId", habit.id)
                context.startActivity(intent)
            }
        }
    }
}