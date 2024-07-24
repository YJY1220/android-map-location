package campus.tech.kakao.map.ui

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
import android.app.Activity
import android.content.Intent
import campus.tech.kakao.map.viewmodel.MapViewModel
import campus.tech.kakao.map.viewmodel.MapViewModelFactory
import campus.tech.kakao.map.R
import campus.tech.kakao.map.util.SQLiteHelper
import campus.tech.kakao.map.model.MapItem
import campus.tech.kakao.map.repository.MapRepositoryImpl

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var sqLiteHelper: SQLiteHelper
    lateinit var viewModel: MapViewModel
    private lateinit var searchAdapter: SearchAdapter
    private lateinit var selectedAdapter: SelectedAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sqLiteHelper = SQLiteHelper(this)
        sqLiteHelper.writableDatabase

        val repository = MapRepositoryImpl(application)
        val viewModelFactory = MapViewModelFactory(application, repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MapViewModel::class.java)

        setupRecyclerViews()
        setupSearchEditText()
        setupClearTextButton()
        observeViewModel()

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

            binding.noResultsTextView.visibility = if (results.isEmpty() && !viewModel.searchQuery.value.isNullOrEmpty()) View.VISIBLE else View.GONE
            binding.searchResultsRecyclerView.visibility = if (results.isEmpty()) View.GONE else View.VISIBLE
        })

        viewModel.selectedItems.observe(this, Observer { selectedItems ->
            selectedAdapter.submitList(selectedItems)
        })
    }

    fun performSearch(query: String) {
        binding.searchEditText.setText(query)
        viewModel.searchQuery.value = query
    }

    fun setResultAndFinish(selectedItem: MapItem) {
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
