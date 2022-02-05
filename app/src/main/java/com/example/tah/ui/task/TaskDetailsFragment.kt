package com.example.tah.ui.task

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.tah.R
import com.example.tah.databinding.DetailsTaskBinding
import com.example.tah.models.Task
import com.example.tah.models.TaskType
import com.example.tah.ui.todo.TodosFragment
import com.example.tah.utilities.State
import com.example.tah.utilities.ViewInitializable
import com.example.tah.viewModels.TaskDetailsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TaskDetailsFragment: Fragment(R.layout.details_task), ViewInitializable {

    private var _binding: DetailsTaskBinding? = null
    private val binding get() = _binding!!

    private val taskDetailsViewModel: TaskDetailsViewModel by viewModels()

    companion object {
        fun newInstance(taskId: Long): TaskDetailsFragment{
            val fragment = TaskDetailsFragment()
            val args = Bundle()
            args.putLong("taskId", taskId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = DetailsTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        taskDetailsViewModel.init(arguments?.getLong("taskId")!!)
        initOnClickListeners()
        initViewModelObservables()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun initOnClickListeners() {
        binding.saveButton.setOnClickListener {
            taskDetailsViewModel.update(
                binding.name.text.toString(),
                binding.description.text.toString()
            )
        }

        binding.deleteButton.setOnClickListener {
            taskDetailsViewModel.deleteTaskWithTodos()
        }
    }

    override fun initViewModelObservables() {
        taskDetailsViewModel.task.observe(viewLifecycleOwner) {
            with (binding) {
                name.setText(it.name)
                description.setText(it.description)
                if (it.type == TaskType.SHOPPING) {
                    descriptionLayout.visibility = View.GONE
                    parentFragmentManager
                        .beginTransaction()
                        .replace(R.id.fragment_container,
                            TodosFragment.newInstance(it.taskId!!))
                        .commit()
                }
            }
        }

        taskDetailsViewModel.state.observe(viewLifecycleOwner) {
            when(it.status){
                State.Status.REMOVED -> {
                    requireActivity().finish()
                }
                State.Status.UPDATED -> {
                    requireActivity().finish()
                }
                else -> toast("Else? idk lol")
            }
            toast(it.message)
        }
    }

    private fun toast(message: String?){
        if(!message.isNullOrEmpty()){
            Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
        }
    }
}