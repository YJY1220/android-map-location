package campus.tech.kakao.map.model

import campus.tech.kakao.map.model.MapItem

//MapItem 넘겨받기 -> 매핑
data class KakaoSearchResponse(
    val documents: List<MapItem>
)