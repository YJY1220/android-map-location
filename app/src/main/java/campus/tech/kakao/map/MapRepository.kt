package campus.tech.kakao.map

import android.app.Application

interface MapRepository {
    suspend fun searchItems(query: String): List<MapItem>
}

class MapRepositoryImpl(private val application: Application) : MapRepository {
    override suspend fun searchItems(query: String): List<MapItem> {
        return listOf() //실제 검색
    }
}
