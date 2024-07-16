package campus.tech.kakao.map

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.kakao.vectormap.MapView
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.label.LabelLayer
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakao.vectormap.label.LabelTextStyle
import com.kakao.vectormap.camera.CameraAnimation
import com.kakao.vectormap.camera.CameraUpdateFactory

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
    private var selectedItems = mutableListOf<MapItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 카카오 지도 초기화
        mapView = findViewById(R.id.map_view)
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

        // 에러 레이아웃 초기화
        errorLayout = findViewById(R.id.error_layout)
        errorMessage = findViewById(R.id.error_message)
        errorDetails = findViewById(R.id.error_details)
        retryButton = findViewById(R.id.retry_button)

        // BottomSheet 초기화
        val bottomSheet = findViewById<FrameLayout>(R.id.bottomSheetLayout)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetTitle = findViewById(R.id.bottomSheetTitle)
        bottomSheetAddress = findViewById(R.id.bottomSheetAddress)
    }

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
        mapView.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
                // 지도 API가 정상적으로 종료될 때 호출됨
            }

            override fun onMapError(error: Exception) {
                showErrorScreen(error)
            }
        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(kakaoMap: KakaoMap) {
                this@MainActivity.kakaoMap = kakaoMap
                labelLayer = kakaoMap.labelManager?.layer!!
            }
        })  // 지도 다시 시작
    }

    //지도 돌아갔다가 다시 검색페이지 돌아올 때 해당 저장된 검색어 남아있도록 하기 위함
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SEARCH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.let {
                val placeName = it.getStringExtra("place_name")
                val roadAddressName = it.getStringExtra("road_address_name")
                val x = it.getDoubleExtra("x", 0.0)
                val y = it.getDoubleExtra("y", 0.0)
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

    //bottomsheet에 정보추가하도록 바꿈
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

            // 라벨 생성
            labelLayer.addLabel(
                LabelOptions.from(placeName, position).setStyles(styles).setTexts(placeName)
            )

            // 라벨 클릭 이벤트 처리
            kakaoMap.setOnLabelClickListener { map, layer, label ->
                bottomSheetTitle.text = placeName
                bottomSheetAddress.text = roadAddressName
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                true
            }

            // 카메라 이동
            moveCamera(position)

            // BottomSheet에 정보 표시
            bottomSheetTitle.text = placeName
            bottomSheetAddress.text = roadAddressName
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun moveCamera(position: LatLng) {
        kakaoMap.moveCamera(
            CameraUpdateFactory.newCenterPosition(position),
            CameraAnimation.from(10, false, false)
        )
    }

    companion object {
        private const val SEARCH_REQUEST_CODE = 1
    }
}