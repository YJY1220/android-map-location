// MainActivity.kt

package campus.tech.kakao.map

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.kakao.vectormap.MapView
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.KakaoMap

class MainActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var errorLayout: RelativeLayout
    private lateinit var errorMessage: TextView
    private lateinit var errorDetails: TextView
    private lateinit var retryButton: ImageButton

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
            override fun onMapReady(kakaoMap: KakaoMap) {
                // 인증 후 API가 정상적으로 실행될 때 호출됨
            }
        })

        // 검색창 클릭 시 검색 페이지로 이동
        val searchEditText = findViewById<EditText>(R.id.search_edit_text)
        searchEditText.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

        // 에러 레이아웃 초기화
        errorLayout = findViewById(R.id.error_layout)
        errorMessage = findViewById(R.id.error_message)
        errorDetails = findViewById(R.id.error_details)
        retryButton = findViewById(R.id.retry_button)
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
                // 인증 후 API가 정상적으로 실행될 때 호출됨
            }
        })  // 지도 다시 시작
    }
}
