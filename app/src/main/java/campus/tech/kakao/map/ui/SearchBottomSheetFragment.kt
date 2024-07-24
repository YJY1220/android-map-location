package campus.tech.kakao.map.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import campus.tech.kakao.map.viewmodel.MapViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import campus.tech.kakao.map.databinding.FragmentSearchBottomSheetBinding

class SearchBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentSearchBottomSheetBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MapViewModel by activityViewModels()
    private lateinit var searchAdapter: SearchAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        searchAdapter = SearchAdapter { item ->
            viewModel.selectItem(item)
            dismiss()
        }

        binding.searchResultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = searchAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.searchResults.observe(viewLifecycleOwner, { results ->
            searchAdapter.submitList(results)
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}