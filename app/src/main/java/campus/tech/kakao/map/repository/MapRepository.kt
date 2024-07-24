package campus.tech.kakao.map.repository

import android.app.Application
import campus.tech.kakao.map.model.MapItem

interface MapRepository {
    suspend fun searchItems(query: String): List<MapItem>
}

class MapRepositoryImpl(private val application: Application) : MapRepository {

    private val mapAccess = MapAccess(application)

    override suspend fun searchItems(query: String): List<MapItem> {
        return mapAccess.searchItems(query)
    }
}
