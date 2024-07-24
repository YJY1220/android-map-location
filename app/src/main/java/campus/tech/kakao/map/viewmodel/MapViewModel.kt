package campus.tech.kakao.map.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import campus.tech.kakao.map.model.MapItem
import campus.tech.kakao.map.repository.MapRepository
import kotlinx.coroutines.launch

class MapViewModel(application: Application, private val repository: MapRepository) : AndroidViewModel(application) {

    val searchQuery = MutableLiveData<String>()

    private val _searchResults = MutableLiveData<List<MapItem>>(emptyList())
    val searchResults: LiveData<List<MapItem>> = _searchResults

    private val _selectedItems = MutableLiveData<List<MapItem>>(emptyList())
    val selectedItems: LiveData<List<MapItem>> get() = _selectedItems

    init {
        searchQuery.observeForever { query ->
            if (query.isNullOrEmpty()) {
                _searchResults.postValue(emptyList())
            } else {
                searchItems(query)
            }
        }
    }

    private fun searchItems(query: String) {
        viewModelScope.launch {
            val results = repository.searchItems(query)
            _searchResults.postValue(results)
        }
    }

    fun selectItem(item: MapItem) {
        _selectedItems.value = _selectedItems.value?.plus(item)
    }

    fun removeSelectedItem(item: MapItem) {
        _selectedItems.value = _selectedItems.value?.minus(item)
    }

    fun setSelectedItems(items: List<MapItem>) {
        _selectedItems.value = items
    }

    fun setSearchResults(results: List<MapItem>) {
        _searchResults.value = results
    }
}
