// SearchActivity.kt

package campus.tech.kakao.map

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import campus.tech.kakao.map.databinding.ActivitySearchBinding
import androidx.lifecycle.ViewModelProvider
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var sqLiteHelper: SQLiteHelper
    private lateinit var viewModel: MapViewModel
    private lateinit var searchAdapter: SearchAdapter
    private lateinit var selectedAdapter: SelectedAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sqLiteHelper = SQLiteHelper(this)
        sqLiteHelper.writableDatabase

        val viewModelInit = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        viewModel = ViewModelProvider(this, viewModelInit).get(MapViewModel::class.java)

        setupRecyclerViews()
        setupSearchEditText()
        setupClearTextButton()
        observeViewModel()
    }

    private fun setupRecyclerViews() {
        searchAdapter = SearchAdapter { item ->
            if (viewModel.selectedItems.value?.contains(item) == true) {
                Toast.makeText(this, getString(R.string.item_already_selected), Toast.LENGTH_SHORT).show()
            } else {
                viewModel.selectItem(item)
            }
        }

        binding.searchResultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = searchAdapter
        }

        selectedAdapter = SelectedAdapter(
            onItemRemoved = { item -> viewModel.removeSelectedItem(item) },
            onItemClicked = { item -> performSearch(item.place_name) }
        )

        binding.selectedItemsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@SearchActivity, RecyclerView.HORIZONTAL, false)
            adapter = selectedAdapter
        }
    }

    private fun setupSearchEditText() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()) {
                    binding.clearTextButton.visibility = View.VISIBLE
                } else {
                    binding.clearTextButton.visibility = View.GONE
                }
                viewModel.searchQuery.value = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupClearTextButton() {
        binding.clearTextButton.setOnClickListener {
            binding.searchEditText.text.clear()
        }
    }

    private fun observeViewModel() {
        viewModel.searchResults.observe(this, Observer { results ->
            searchAdapter.submitList(results)

            if (results.isEmpty()) {
                if (viewModel.searchQuery.value.isNullOrEmpty()) {
                    binding.noResultsTextView.visibility = View.VISIBLE
                } else {
                    binding.noResultsTextView.visibility = View.GONE
                }
            } else {
                binding.noResultsTextView.visibility = View.GONE
            }

            if (results.isEmpty()) {
                if (viewModel.searchQuery.value.isNullOrEmpty()) {
                    binding.searchResultsRecyclerView.visibility = View.GONE
                } else {
                    binding.searchResultsRecyclerView.visibility = View.VISIBLE
                }
            } else {
                binding.searchResultsRecyclerView.visibility = View.VISIBLE
            }
        })

        viewModel.selectedItems.observe(this, Observer { selectedItems ->
            selectedAdapter.submitList(selectedItems)
        })
    }

    private fun performSearch(query: String) {
        binding.searchEditText.setText(query)
        viewModel.searchQuery.value = query
    }
}
