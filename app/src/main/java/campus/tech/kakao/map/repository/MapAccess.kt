package campus.tech.kakao.map.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import campus.tech.kakao.map.model.MapItem
import campus.tech.kakao.map.network.RetrofitInstance
import campus.tech.kakao.map.util.Constants

class MapAccess(context: Context) {

    // 검색어 기반 항목 검색 suspend 함수
    suspend fun searchItems(query: String, page: Int = 1, size: Int = 15): List<MapItem> {

        return withContext(Dispatchers.IO) {

            val apiKey = Constants.KAKAO_API_KEY

            val response = RetrofitInstance.api.searchPlaces(apiKey, query, page, size)

            //응답여부 체크
            if (response.isSuccessful) {
                Log.d("MapAccess", "Response: ${response.body()?.documents}")
                response.body()?.documents ?: emptyList()
            } else {
                Log.e("MapAccess", "Error: ${response.errorBody()?.string()}")
                emptyList()
            }
        }
    }
}
