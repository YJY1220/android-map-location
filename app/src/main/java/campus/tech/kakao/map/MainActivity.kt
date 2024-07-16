package campus.tech.kakao.map

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.kakao.vectormap.MapView
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.KakaoMap
import android.view.View
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import android.app.Activity
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.label.LabelLayer
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakao.vectormap.label.LabelTextStyle
import com.kakao.vectormap.camera.CameraUpdateFactory


private val Any.longitude: Double
    get() {
        TODO("Not yet implemented")
    }
private val Any.latitude: Double
    get() {
        TODO("Not yet implemented")
    }
private val Any.mapPointGeoCoord: Any
    get() {
        TODO("Not yet implemented")
    }
private val Any.mapCenterPoint: Any
    get() {
        TODO("Not yet implemented")
    }

class MainActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var errorLayout: RelativeLayout
    private lateinit var errorMessage: TextView
    private lateinit var errorDetails: TextView
    private lateinit var retryButton: ImageButton
    private lateinit var kakaoMap: KakaoMap
    private lateinit var labelLayer: LabelLayer
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>
    private lateinit var bottomSheetTitle: TextView
    private lateinit var bottomSheetAddress: TextView
    private lateinit var bottomSheetLayout: FrameLayout
    private var selectedItems = mutableListOf<MapItem>()

    private var savedLatitude: Double = 37.5642
    private var savedLongitude: Double = 127.0
    private var savedZoomLevel: Float = 10.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 카카오 지도 초기화
        mapView = findViewById(R.id.map_view)
        loadData() // 저장된 위치 불러오기

        mapView.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
                // 지도 API가 정상적으로 종료될 때 호출됨
            }

            override fun onMapError(error: Exception) {
                showErrorScreen(error)
            }
        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(map: KakaoMap) {
                kakaoMap = map
                labelLayer = kakaoMap.labelManager?.layer!!
                restoreCameraPosition()
                processIntentData()
            }
        })

        // 검색창 클릭 시 검색 페이지로 이동
        val searchEditText = findViewById<EditText>(R.id.search_edit_text)
        searchEditText.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            intent.putExtra("selectedItemsSize", selectedItems.size)
            selectedItems.forEachIndexed { index, mapItem ->
                intent.putExtra("id_$index", mapItem.id)
                intent.putExtra("place_name_$index", mapItem.place_name)
                intent.putExtra("road_address_name_$index", mapItem.road_address_name)
                intent.putExtra("category_group_name_$index", mapItem.category_group_name)
            }
            startActivityForResult(intent, SEARCH_REQUEST_CODE)
        }

        // 에러 화면 초기화
        errorLayout = findViewById(R.id.error_layout)
        errorMessage = findViewById(R.id.error_message)
        errorDetails = findViewById(R.id.error_details)
        retryButton = findViewById(R.id.retry_button)

        // Bottomsheet 초기화
        bottomSheetLayout = findViewById(R.id.bottomSheetLayout)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout)
        bottomSheetTitle = findViewById(R.id.bottomSheetTitle)
        bottomSheetAddress = findViewById(R.id.bottomSheetAddress)

        // 처음에는 bottomsheet 숨기기
        bottomSheetLayout.visibility = View.GONE
    }

    // 지도 -> 검색페이지 돌아갈 때 저장된 검색어 목록 그대로 저장
    private fun processIntentData() {
        val placeName = intent.getStringExtra("place_name")
        val roadAddressName = intent.getStringExtra("road_address_name")
        val x = intent.getDoubleExtra("x", 0.0)
        val y = intent.getDoubleExtra("y", 0.0)
        if (placeName != null && roadAddressName != null) {
            addLabel(placeName, roadAddressName, x, y)
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.resume()  // MapView의 resume 호출
    }

    override fun onPause() {
        saveCameraPosition()
        super.onPause()
        mapView.pause()  // MapView의 pause 호출
    }

    private fun showErrorScreen(error: Exception) {
        errorLayout.visibility = View.VISIBLE
        errorDetails.text = error.message
        mapView.visibility = View.GONE
    }

    fun onRetryButtonClick(view: View) {
        errorLayout.visibility = View.GONE
        mapView.visibility = View.VISIBLE
        // 지도 다시 시작
        mapView.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
            }

            override fun onMapError(error: Exception) {
                showErrorScreen(error)
            }
        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(kakaoMap: KakaoMap) {
                this@MainActivity.kakaoMap = kakaoMap
                labelLayer = kakaoMap.labelManager?.layer!!
                restoreCameraPosition()
            }
        })
    }

    // 결과 반환
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SEARCH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.let {
                val placeName = it.getStringExtra("place_name")
                val roadAddressName = it.getStringExtra("road_address_name")
                val x = it.getDoubleExtra("x", 0.0)
                val y = it.getDoubleExtra("y", 0.0)
                // 다시 돌아갈 때 저장된 검색어 확인
                selectedItems.clear()
                val selectedItemsSize = it.getIntExtra("selectedItemsSize", 0)
                for (i in 0 until selectedItemsSize) {
                    val id = it.getStringExtra("id_$i") ?: ""
                    val place_name = it.getStringExtra("place_name_$i") ?: ""
                    val road_address_name = it.getStringExtra("road_address_name_$i") ?: ""
                    val category_group_name = it.getStringExtra("category_group_name_$i") ?: ""
                    val x = it.getDoubleExtra("x_$i", 0.0)
                    val y = it.getDoubleExtra("y_$i", 0.0)
                    selectedItems.add(MapItem(id, place_name, road_address_name, category_group_name, x, y))
                }
                addLabel(placeName, roadAddressName, x, y)
            }
        }
    }

    private fun addLabel(placeName: String?, roadAddressName: String?, x: Double, y: Double) {
        if (placeName != null && roadAddressName != null) {
            val position = LatLng.from(y, x)
            val styles = kakaoMap.labelManager?.addLabelStyles(
                LabelStyles.from(
                    LabelStyle.from(R.drawable.new_marker).setZoomLevel(1),
                    LabelStyle.from(R.drawable.new_marker)
                        .setTextStyles(LabelTextStyle.from(this, R.style.labelTextStyle_1))
                        .setZoomLevel(1)
                )
            )

            labelLayer.addLabel(
                LabelOptions.from(placeName, position).setStyles(styles).setTexts(placeName)
            )

            kakaoMap.setOnLabelClickListener { map, layer, label ->
                bottomSheetTitle.text = placeName
                bottomSheetAddress.text = roadAddressName
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                bottomSheetLayout.visibility = View.VISIBLE
                true
            }

            // 카메라 이동
            moveCamera(position)

            // bottom sheet 업데이트
            bottomSheetTitle.text = placeName
            bottomSheetAddress.text = roadAddressName
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            bottomSheetLayout.visibility = View.VISIBLE
        }
    }

    private fun moveCamera(position: LatLng) {
        val cameraUpdate = CameraUpdateFactory.newCenterPosition(position)
        kakaoMap.moveCamera(cameraUpdate)
    }

    private fun restoreCameraPosition() {
        val savedPosition = loadData()
        val target = LatLng.from(savedPosition.first, savedPosition.second)
        val cameraUpdate = CameraUpdateFactory.newCenterPosition(target).apply {
            zoomLevel(savedZoomLevel)
        }
        kakaoMap.moveCamera(cameraUpdate)
    }

    private fun zoomLevel(savedZoomLevel: Float) {
        TODO("Not yet implemented")
    }

    private fun saveCameraPosition() {
        val currentPosition = kakaoMap.mapCenterPoint.mapPointGeoCoord
        val zoom = kakaoMap.cameraPosition?.zoomLevel
        saveData(currentPosition.latitude, currentPosition.longitude, zoom)
    }

    private fun saveData(latitude: Double, longitude: Double, zoom: Int?) {
        val pref = getSharedPreferences("pref", 0)
        val edit = pref.edit()
        edit.putString("latitude", latitude.toString())
        edit.putString("longitude", longitude.toString())
        edit.putFloat("zoom", zoom)
        edit.apply()
    }

    private fun loadData(): Triple<Double, Double, Float> {
        val pref = getSharedPreferences("pref", 0)
        val latitude = pref.getString("latitude", "37.5642")?.toDouble() ?: 37.5642
        val longitude = pref.getString("longitude", "127.0")?.toDouble() ?: 127.0
        val zoom = pref.getFloat("zoom", 10.0f)
        return Triple(latitude, longitude, zoom)
    }

    companion object {
        private const val SEARCH_REQUEST_CODE = 1
    }
}

private fun Any.putFloat(s: String, zoom: Int?) {

}
