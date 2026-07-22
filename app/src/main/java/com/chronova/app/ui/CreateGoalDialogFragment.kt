package com.chronova.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.chronova.app.data.ChronovaRepository
import com.chronova.app.data.GoalCreateRequest
import com.chronova.app.data.GoalSuggestion
import com.chronova.app.databinding.DialogCreateGoalBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class CreateGoalDialogFragment(
    private val onGoalCreated: () -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: DialogCreateGoalBinding? = null
    private val binding get() = _binding!!
    private val repository by lazy { ChronovaRepository(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCreateGoalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDropdowns()
        loadSuggestions()

        binding.btnCreateGoal.setOnClickListener {
            createGoal()
        }
    }

    private fun setupDropdowns() {
        val deltaOptions = arrayOf("day", "week")
        binding.etGoalDelta.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, deltaOptions)
        )
        binding.etGoalDelta.setText("day", false)

        val categoryOptions = arrayOf("coding_time", "language", "project", "consistency", "growth")
        binding.etGoalCategory.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, categoryOptions)
        )
    }

    private fun loadSuggestions() {
        viewLifecycleOwner.lifecycleScope.launch {
            repository.getGoalSuggestions()
                .onSuccess { suggestions ->
                    if (suggestions.isNotEmpty()) showSuggestions(suggestions)
                }
        }
    }

    private fun showSuggestions(suggestions: List<GoalSuggestion>) {
        binding.tvSuggestionsLabel.visibility = View.VISIBLE
        binding.suggestionsContainer.visibility = View.VISIBLE

        suggestions.take(5).forEach { suggestion ->
            val chip = Chip(requireContext()).apply {
                text = suggestion.title
                isClickable = true
                setOnClickListener {
                    binding.etGoalTitle.setText(suggestion.title)
                    binding.etGoalSeconds.setText((suggestion.seconds / 3600.0).toString())
                    binding.etGoalDelta.setText(suggestion.delta, false)
                    suggestion.category?.let { binding.etGoalCategory.setText(it, false) }
                }
            }
            binding.suggestionsChipGroup.addView(chip)
        }
    }

    private fun createGoal() {
        val title = binding.etGoalTitle.text?.toString()?.trim()
        val hoursStr = binding.etGoalSeconds.text?.toString()?.trim()
        val delta = binding.etGoalDelta.text?.toString()?.trim() ?: "day"
        val category = binding.etGoalCategory.text?.toString()?.trim()
            ?.takeIf { it.isNotEmpty() && it != "coding_time" }

        if (title.isNullOrEmpty()) {
            binding.etGoalTitle.error = "Title is required"
            return
        }
        val hours = hoursStr?.toDoubleOrNull()
        if (hours == null || hours <= 0) {
            binding.etGoalSeconds.error = "Enter a valid number of hours"
            return
        }

        val seconds = hours * 3600.0
        val request = GoalCreateRequest(
            title = title,
            type = "coding_time",
            seconds = seconds,
            delta = delta,
            category = category
        )

        viewLifecycleOwner.lifecycleScope.launch {
            repository.createGoal(request)
                .onSuccess {
                    Toast.makeText(context, "Goal created", Toast.LENGTH_SHORT).show()
                    onGoalCreated()
                    dismiss()
                }
                .onFailure { e ->
                    Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}