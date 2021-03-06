package com.example.tah.ui.task

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.tah.R
import com.example.tah.databinding.AddTaskFragmentBinding
import com.example.tah.models.Task
import com.example.tah.models.TaskType
import com.example.tah.models.TaskWithTodos
import com.example.tah.ui.main.AddAndDetailsActivity
import com.example.tah.utilities.Converters
import com.example.tah.utilities.State
import com.example.tah.utilities.ViewHelper
import com.example.tah.utilities.ViewInitializable
import com.example.tah.viewModels.TaskViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class TaskAddFragment
    : Fragment(R.layout.add_task_fragment),
    ViewInitializable,
    ViewHelper
{
    private var _binding: AddTaskFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: TaskViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireActivity()).get(TaskViewModel::class.java)

        val adapter = ArrayAdapter(
            requireActivity(), android.R.layout.simple_spinner_dropdown_item, TaskType.values()
        )

        _binding = AddTaskFragmentBinding.inflate(inflater, container, false)
        (binding.typeLayout.editText as? AutoCompleteTextView)?.apply {
            setAdapter(adapter)
            onItemClickListener = spinnerAdapter
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AddAndDetailsActivity).setTitle("New Task")
        initOnClickListeners()
        initViewModelObservables()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun initOnClickListeners() {
        binding.add.setOnClickListener{
            val name = binding.name.text.toString()
            val description = binding.description.text.toString()
            val type = binding.typeLayout.editText?.text.toString()

            if (name.isEmpty() && type.isEmpty()) {
                showErrorMessages(binding.name, binding.typeLayout)
            }

            if (Converters.toType(type) == TaskType.SHOPPING) {
                startTaskWithTodosDetailsFragment()
            } else {
                viewModel.add(
                    Task(name, description, Converters.toType(type), false)
                )
            }
        }
    }

    override fun initViewModelObservables() {
        viewModel.state.observe(viewLifecycleOwner){
            when(it.status){
                State.Status.LOADING -> viewsLoading()
                State.Status.ADDED -> requireActivity().finish()
                else -> viewsNotLoading()
            }
            toast(it.message)
        }
    }

    private fun startTaskWithTodosDetailsFragment() {
        CoroutineScope(Dispatchers.Main).launch {
            val id = withContext(Dispatchers.Default) {
                viewModel.addTaskWithTodos(
                    TaskWithTodos(
                        Task(
                            binding.name.text.toString(),
                            binding.description.text.toString(),
                            TaskType.SHOPPING,
                            false
                        ),
                        mutableListOf()
                    )
                )
            }
            requireActivity().finish()
            val intent = Intent(requireActivity(), AddAndDetailsActivity::class.java)
            intent.putExtra("fragmentId", Task.getDetailsView())
            intent.putExtra("taskId", id)
            requireActivity().startActivity(intent)
        }
    }

    private fun viewsLoading(){
        binding.add.hide()
    }

    private fun viewsNotLoading(){
        binding.add.show()
    }

    private fun toast(message: String?){
        if(!message.isNullOrEmpty()){
            Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private val spinnerAdapter: AdapterView.OnItemClickListener
    = AdapterView.OnItemClickListener { _, _, position, _ ->
        if (position == TaskType.SHOPPING.ordinal) {
            binding.descriptionLayout.visibility = View.GONE
        } else {
            initViewModelObservables()
            binding.descriptionLayout.visibility = View.VISIBLE
        }
    }
}