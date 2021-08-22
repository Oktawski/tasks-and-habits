package com.example.tah.ui.habit

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.tah.R
import com.example.tah.databinding.DetailsHabitStartedBinding
import com.example.tah.ui.main.AddAndDetailsActivity
import com.example.tah.utilities.ViewHabitTime
import com.example.tah.utilities.ViewInitializable
import com.example.tah.viewModels.HabitViewModel
import com.google.android.material.appbar.AppBarLayout
import dagger.hilt.android.AndroidEntryPoint
import hilt_aggregated_deps._com_example_tah_viewModels_HabitViewModel_HiltModules_BindsModule
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


@AndroidEntryPoint
class HabitStartedFragment
    : Fragment(R.layout.details_habit_started),
    ViewHabitTime,
    ViewInitializable
{
    private var _binding: DetailsHabitStartedBinding? = null
    private val binding get() = _binding!!

    private var habitId: Long? = null
    private var sessionLength = 0L
    private lateinit var mainHandler: Handler
    private var job: Job? = null

    private val viewModel: HabitViewModel by viewModels()

    companion object {
        fun newInstance(id: Long): HabitStartedFragment {
            val fragment = HabitStartedFragment()
            val args = Bundle()
            args.putLong("habitId", id)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        habitId = arguments?.getLong("habitId")
        mainHandler = Handler(Looper.getMainLooper())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DetailsHabitStartedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (habitId != null) {
            activity?.findViewById<AppBarLayout>(R.id.app_bar)?.visibility = View.GONE
            getHabit(habitId!!)
            (activity as AddAndDetailsActivity).setTitle("")
            initViewModelObservables()
            initOnClickListeners()
            viewModel.startStop()
        }
        else {
            Toast.makeText(requireActivity(), "Habit not found", Toast.LENGTH_SHORT).show()
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mainHandler.removeCallbacks(decreaseTime)
        job?.cancel()
    }

    override fun initViewModelObservables() {
        viewModel.isStarted.observe(viewLifecycleOwner) { bool ->
            if(bool) {
                val timeMap = getTimeStrings(sessionLength)
                val timeText = "${timeMap["Hours"]}:${timeMap["Minutes"]}:${timeMap["Seconds"]}"
                binding.timeText.text = timeText
                mainHandler.postDelayed(decreaseTime, 0)
            } else {
                mainHandler.removeCallbacks(decreaseTime)

                job = CoroutineScope(Dispatchers.Main).launch {
                    val habit = viewModel.getByIdSus(habitId)
                    habit.sessionLength = this@HabitStartedFragment.sessionLength
                    viewModel.update(habit)
                }
            }
        }
    }

    override fun initOnClickListeners() {
        binding.fabStarted.setOnClickListener {
            viewModel.startStop()
        }
    }

    @SuppressLint("CheckResult")
    private fun getHabit(id: Long) {
        job = CoroutineScope(Dispatchers.Main).launch {
            val habit = viewModel.getByIdSus(id)
            val timeMap = getTimeStrings(habit.sessionLength)
            val timeText = "${timeMap["Hours"]}:${timeMap["Minutes"]}:${timeMap["Seconds"]}"
            this@HabitStartedFragment.sessionLength = habit.sessionLength
            binding.timeText.text = timeText
            binding.habitName.text = habit.name }
    }


    private fun updateHabit() {
        job = CoroutineScope(Dispatchers.Main).launch {
            val habit = viewModel.getByIdSus(habitId)
            habit.sessionLength = this@HabitStartedFragment.sessionLength
            viewModel.update(habit)
        }
    }

    private val decreaseTime = object : Runnable {
        override fun run() {
            val timeMap = getTimeStrings(sessionLength)
            val timeText = "${timeMap["Hours"]}:${timeMap["Minutes"]}:${timeMap["Seconds"]}"
            binding.timeText.text = timeText
            if (sessionLength > 0) {
                sessionLength--
            } else {
                viewModel.startStop()
                Toast.makeText(activity, "Time's up", Toast.LENGTH_SHORT).show()
            }
            updateHabit()
            mainHandler.postDelayed(this, 1000)
        }
    }
}