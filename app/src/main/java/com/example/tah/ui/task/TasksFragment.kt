package com.example.tah.ui.task

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tah.R
import com.example.tah.ui.main.MainActivity
import com.example.tah.utilities.State
import com.example.tah.utilities.ViewInitializable
import com.example.tah.viewModels.TaskViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TasksFragment
    : Fragment(R.layout.fragment_tasks),
    ViewInitializable
{
    private lateinit var viewModel: TaskViewModel

    @Inject
    lateinit var adapter: TaskRecyclerViewAdapter

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_tasks, container, false)

        viewModel = ViewModelProvider(requireActivity()).get(TaskViewModel::class.java)

        if (view is RecyclerView) {
            val context = view.context
            view.layoutManager = LinearLayoutManager(context)
            view.adapter = adapter
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModelObservables()
    }

    override fun onPause() {
        super.onPause()
        viewModel.clearCheckedItems()
        (activity as MainActivity).setDeleteIconVisibility(View.GONE)
    }

    override fun initViewModelObservables() {
        viewModel.checkedItemsLD.observe(viewLifecycleOwner) {
            if(it.isNotEmpty()) (activity as MainActivity).setDeleteIconVisibility(View.VISIBLE)
            else (activity as MainActivity).setDeleteIconVisibility(View.GONE)
        }

        viewModel.itemsLD!!.observe(viewLifecycleOwner) {
            adapter.differ.submitList(it)
        }

        viewModel.getCheckBoxVisibility().observe(viewLifecycleOwner) {
            adapter.notifyDataSetChanged()
        }

        viewModel.state.observe(viewLifecycleOwner) {
            if(it.status == State.Status.REMOVED){
                Toast.makeText(requireActivity(), it.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun initOnClickListeners() {
        activity?.findViewById<ImageView>(R.id.delete_icon)?.setOnClickListener {
            viewModel.deleteSelected()
        }
    }

}
