package campus.tech.kakao.map

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import campus.tech.kakao.map.databinding.ActivitySearchBinding

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

        // 선택된 항목 복원
        val selectedItemsSize = intent.getIntExtra("selectedItemsSize", 0)
        val selectedItems = mutableListOf<MapItem>()
        for (i in 0 until selectedItemsSize) {
            val id = intent.getStringExtra("id_$i") ?: ""
            val place_name = intent.getStringExtra("place_name_$i") ?: ""
            val road_address_name = intent.getStringExtra("road_address_name_$i") ?: ""
            val category_group_name = intent.getStringExtra("category_group_name_$i") ?: ""
            val x = intent.getDoubleExtra("x_$i", 0.0)
            val y = intent.getDoubleExtra("y_$i", 0.0)
            selectedItems.add(MapItem(id, place_name, road_address_name, category_group_name, x, y))
        }
        viewModel.setSelectedItems(selectedItems)
    }

    private fun setupRecyclerViews() {
        searchAdapter = SearchAdapter { item ->
            if (viewModel.selectedItems.value?.contains(item) == true) {
                Toast.makeText(this, getString(R.string.item_already_selected), Toast.LENGTH_SHORT).show()
            } else {
                viewModel.selectItem(item)
                setResultAndFinish(item)
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

    private fun setResultAndFinish(selectedItem: MapItem) {
        val intent = Intent().apply {
            putExtra("place_name", selectedItem.place_name)
            putExtra("road_address_name", selectedItem.road_address_name)
            putExtra("x", selectedItem.x)
            putExtra("y", selectedItem.y)
            putExtra("selectedItemsSize", viewModel.selectedItems.value?.size ?: 0)
            viewModel.selectedItems.value?.forEachIndexed { index, item ->
                putExtra("id_$index", item.id)
                putExtra("place_name_$index", item.place_name)
                putExtra("road_address_name_$index", item.road_address_name)
                putExtra("category_group_name_$index", item.category_group_name)
                putExtra("x_$index", item.x)
                putExtra("y_$index", item.y)
            }
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}
