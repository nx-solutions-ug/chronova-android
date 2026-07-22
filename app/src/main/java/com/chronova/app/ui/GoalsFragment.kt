package com.chronova.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chronova.app.data.ChronovaRepository
import com.chronova.app.databinding.FragmentGoalsBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class GoalsFragment : Fragment() {

    private var _binding: FragmentGoalsBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: ChronovaRepository
    private lateinit var goalAdapter: GoalAdapter
    private var loadJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGoalsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = ChronovaRepository(requireContext())

        setupRecyclerView()
        loadGoals()

        binding.fabAddGoal.setOnClickListener {
            CreateGoalDialogFragment { loadGoals() }
                .show(parentFragmentManager, "CreateGoalDialog")
        }
    }

    private fun setupRecyclerView() {
        goalAdapter = GoalAdapter(mutableListOf()) { goal ->
            deleteGoal(goal)
        }
        binding.goalsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.goalsRecyclerView.adapter = goalAdapter

        val touchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                rv: RecyclerView,
                vh: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val goal = goalAdapter.goals[position]
                    deleteGoal(goal)
                }
            }
        })
        touchHelper.attachToRecyclerView(binding.goalsRecyclerView)
    }

    private fun loadGoals() {
        binding.progressBar.visibility = View.VISIBLE
        binding.emptyStateText.visibility = View.GONE

        loadJob?.cancel()
        loadJob = viewLifecycleOwner.lifecycleScope.launch {
            repository.getGoals()
                .onSuccess { goals ->
                    if (!isAdded || _binding == null) return@launch
                    binding.progressBar.visibility = View.GONE
                    if (goals.isEmpty()) {
                        binding.emptyStateText.visibility = View.VISIBLE
                    } else {
                        binding.emptyStateText.visibility = View.GONE
                    }
                    goalAdapter.updateGoals(goals)
                }
                .onFailure { e ->
                    if (!isAdded || _binding == null) return@launch
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun deleteGoal(goal: com.chronova.app.data.Goal) {
        viewLifecycleOwner.lifecycleScope.launch {
            repository.deleteGoal(goal.id)
                .onSuccess {
                    if (!isAdded || _binding == null) return@launch
                    Toast.makeText(context, "Goal deleted", Toast.LENGTH_SHORT).show()
                    loadGoals()
                }
                .onFailure { e ->
                    if (!isAdded || _binding == null) return@launch
                    Toast.makeText(context, "Delete failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    loadGoals()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadJob?.cancel()
        _binding = null
    }
}