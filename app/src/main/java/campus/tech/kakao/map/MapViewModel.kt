package campus.tech.kakao.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MapAccess(application)

    // 검색한 쿼리 저장 LiveData
    val searchQuery = MutableLiveData<String>()

    private val _searchResults = MutableLiveData<List<MapItem>>(emptyList())
    var searchResults: LiveData<List<MapItem>> = _searchResults

    // 선택된 항목 저장 MutableLiveData
    private val _selectedItems = MutableLiveData<List<MapItem>>(emptyList())
    val selectedItems: LiveData<List<MapItem>> get() = _selectedItems

    // 검색 쿼리 변화 계속
    init {
        searchQuery.observeForever { query ->
            // 검색 쿼리 비어있으면 - 검색 결과 빈 리스트
            if (query.isNullOrEmpty()) {
                _searchResults.postValue(emptyList())
            } else {
                // 검색 쿼리에 뭐가 있으면 검색 수행
                searchItems(query)
            }
        }
    }

    // 검색쿼리 기반 항목 검색
    private fun searchItems(query: String) {
        viewModelScope.launch {
            // repository에서 결과 가져옴
            val results = repository.searchItems(query)
            // 검색 결과 - _searchResults
            _searchResults.postValue(results)
        }
    }

    // 항목 선택
    fun selectItem(item: MapItem) {
        _selectedItems.value = _selectedItems.value?.plus(item)
    }

    // 선택 항목 제거
    fun removeSelectedItem(item: MapItem) {
        _selectedItems.value = _selectedItems.value?.minus(item)
    }

    // 선택된 항목 설정
    fun setSelectedItems(items: List<MapItem>) {
        _selectedItems.value = items
    }

    // 검색 결과 설정 (테스트를 위한 메서드)
    fun setSearchResults(results: List<MapItem>) {
        _searchResults.value = results
    }
}
