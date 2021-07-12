package com.example.tah.ui.habit

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.tah.R
import com.example.tah.databinding.DetailsHabitBinding
import com.example.tah.databinding.DetailsHabitStartedBinding
import com.example.tah.models.Habit
import com.example.tah.utilities.State
import com.example.tah.utilities.ViewHabitTime
import com.example.tah.utilities.ViewInitializable
import com.example.tah.viewModels.HabitViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class HabitDetailsFragment
: Fragment(R.layout.details_habit),
    ViewInitializable,
    ViewHabitTime
{
    private var _binding: DetailsHabitBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HabitViewModel by viewModels()

    private lateinit var mainHandler: Handler

    private var habitId: Long? = -1L

    private val hours = Array(10){it}
    private val minutes = Array(60){it}

    private var sessionLength = 0L

    companion object{
        fun newInstance(id: Long): HabitDetailsFragment {
            val fragment = HabitDetailsFragment()
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
        _binding = DetailsHabitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHabit()
        initSpinnerAdapters()
        initOnClickListeners()
        initViewModelObservables()
    }

    override fun onResume() {
        super.onResume()
        setNotEditableView()
    }

    override fun initOnClickListeners() {
        with(binding) {

            editButton.setOnClickListener {
                setEditableView()
            }

            cancelButton.setOnClickListener {
                setNotEditableView()
            }

            deleteButton.setOnClickListener {
                viewModel.getById(habitId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { habit -> viewModel.delete(habit) }
            }

            saveButton.setOnClickListener {
                val hours = hoursLayout.editText?.text.toString().toIntOrNull() ?: 0
                val minutes = minutesLayout.editText?.text.toString().toIntOrNull() ?: 0
                val sessionLengthInSec = (hours * 60 * 60 + minutes * 60).toLong()

                viewModel.getById(habitId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        {
                            it.name = name.text.toString()
                            it.description = description.text.toString()
                            it.sessionLength = sessionLengthInSec
                            viewModel.update(it)
                        },
                        {
                            Toast.makeText(
                                requireActivity(),
                                "Could not update habit",
                                Toast.LENGTH_SHORT
                            ).show()
                        })
            }

            fabStart.setOnClickListener {
                val fragment = HabitStartedFragment.newInstance(habitId!!)
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.add_fragment_container, fragment, "habitStartedFragment")
                    .addToBackStack("habitStartedFragment")
                    .commit()
            }
        }
    }

    override fun initViewModelObservables() {
        viewModel.state.observe(viewLifecycleOwner){
            if(it.status == State.Status.REMOVED) {
                Toast.makeText(requireActivity(), it.message, Toast.LENGTH_SHORT).show()
                requireActivity().finish()
            }
            else if(it.status == State.Status.UPDATED) {
                setNotEditableView()
                viewModel.getById(habitId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe{ habit -> inflateViews(habit)
                        this.sessionLength = habit.sessionLength}

            }
        }
    }

    private fun getHabit(){
        viewModel.getById(habitId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    inflateViews(it)
                    sessionLength = it.sessionLength
                },
                {
                    Toast.makeText(requireActivity(), "Habit not found", Toast.LENGTH_SHORT).show()
                    requireActivity().finish()
                })
    }

    private fun inflateViews(it: Habit){
        with (binding) {
            name.setText(it.name)
            description.setText(it.description)
            val timeMap = getTimeStrings(it.sessionLength)
            (hoursLayout.editText as AutoCompleteTextView).setText(timeMap["Hours"])
            (minutesLayout.editText as AutoCompleteTextView).setText(timeMap["Minutes"])
        }
    }

    private fun initSpinnerAdapters(){
        createSpinnerAdapter(binding.minutesLayout, minutes)
        createSpinnerAdapter(binding.minutesLayout, hours)
    }

    private fun createSpinnerAdapter(layout: TextInputLayout, array: Array<Int>){
        ArrayAdapter(
            requireActivity(),
            android.R.layout.simple_spinner_item,
            array
        ).also { adapter ->
            (layout.editText as AutoCompleteTextView).setAdapter(adapter)
        }
    }

    private fun setEditableView(){
        with(binding){
            deleteEditLayout.visibility = View.GONE
            cancelSaveLayout.visibility = View.VISIBLE
            enableEditText(name, description, hoursLayout.editText!!, minutesLayout.editText!!)
            initSpinnerAdapters()
        }
    }

    private fun setNotEditableView(){
        with(binding){
            deleteEditLayout.visibility= View.VISIBLE
            cancelSaveLayout.visibility = View.GONE
            disableEditText(name, description, hoursLayout.editText!!, minutesLayout.editText!!)
        }
    }

    private fun disableEditText(vararg et: EditText){
        for(e in et){
            e.isEnabled = false
        }
    }

    private fun enableEditText(vararg et: EditText){
        for(e in et){
            e.isEnabled = true
        }
    }
}