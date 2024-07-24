package campus.tech.kakao.map.model

//이름 보다 알기 쉽게 변경 - api맞춰서
data class MapItem(
    val id: String,
    val place_name: String,
    val road_address_name: String,
    val category_group_name: String,
    val x: Double,
    val y: Double

)