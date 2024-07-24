import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import campus.tech.kakao.map.MainActivity
import campus.tech.kakao.map.MapItem
import campus.tech.kakao.map.MapRepository
import campus.tech.kakao.map.MapViewModel
import campus.tech.kakao.map.MapViewModelFactory
import campus.tech.kakao.map.R
import campus.tech.kakao.map.SearchActivity
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import java.util.concurrent.Executors

@RunWith(AndroidJUnit4::class)
class FunctionTest {

    private lateinit var scenarioMain: ActivityScenario<MainActivity>
    private lateinit var scenarioSearch: ActivityScenario<SearchActivity>
    private lateinit var context: Context
    private lateinit var repository: MapRepository

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        repository = mock(MapRepository::class.java)

        val application = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
        val viewModelFactory = MapViewModelFactory(application, repository)

        scenarioMain = ActivityScenario.launch(MainActivity::class.java)
        scenarioMain.onActivity { activity ->
            activity.viewModelFactory = viewModelFactory
        }
    }

    @After
    fun tearDown() {
        scenarioMain.close()
        if (::scenarioSearch.isInitialized) {
            scenarioSearch.close()
        }
    }

    @Test
    fun testCompleteFlow() {
        scenarioMain.onActivity { mainActivity ->

            val searchEditText: EditText = mainActivity.findViewById(R.id.search_edit_text)
            searchEditText.performClick()

            Executors.newSingleThreadExecutor().execute {
                scenarioSearch = ActivityScenario.launch(SearchActivity::class.java)
                scenarioSearch.onActivity { searchActivity ->

                    // Repository Mocking 설정
                    runBlocking {
                        `when`(repository.searchItems("바다 정원")).thenReturn(
                            listOf(
                                MapItem(
                                    "1",
                                    "바다 정원",
                                    "강원도 고성군",
                                    "카페",
                                    127.0,
                                    37.0
                                )
                            )
                        )
                    }

                    // ViewModel에 Mock Repository 주입
                    searchActivity.viewModel = MapViewModel(searchActivity.application, repository)

                    //1 검색 기능 테스트
                    val searchEditText: EditText = searchActivity.findViewById(R.id.searchEditText)
                    searchEditText.setText("바다 정원")
                    searchActivity.performSearch("바다 정원")

                    //2 recyclerview로 검색결과 보이는지 테스트
                    val searchResultsRecyclerView: RecyclerView =
                        searchActivity.findViewById(R.id.searchResultsRecyclerView)
                    searchResultsRecyclerView.adapter?.notifyDataSetChanged()
                    assertEquals(1, searchResultsRecyclerView.adapter?.itemCount)

                    //3. 해당 검색결과 중 하나 눌러서 지도 마커표시, bottomsheet 정보 표시
                    searchResultsRecyclerView.findViewHolderForAdapterPosition(0)?.itemView?.performClick()

                    searchActivity.setResultAndFinish(
                        MapItem(
                            "0",
                            "바다 정원",
                            "강원도 고성군",
                            "카페",
                            127.0,
                            37.0
                        )
                    )
                    val resultIntent = Intent().apply {
                        putExtra("place_name", "바다 정원")
                        putExtra("road_address_name", "강원도 고성군")
                        putExtra("x", 127.0)
                        putExtra("y", 37.0)
                    }

                    mainActivity.onActivityResult(
                        MainActivity.SEARCH_REQUEST_CODE,
                        Activity.RESULT_OK,
                        resultIntent
                    )
                    val bottomSheetTitle: TextView =
                        mainActivity.findViewById(R.id.bottomSheetTitle)
                    val bottomSheetAddress: TextView =
                        mainActivity.findViewById(R.id.bottomSheetAddress)
                    assertEquals("바다 정원", bottomSheetTitle.text.toString())
                    assertEquals("강원도 고성군", bottomSheetAddress.text.toString())

                    // 4. 마지막 위치 저장해서 다시 앱 실행시 해당 위치로 지도 뜨도록 하기
                    mainActivity.saveLastMarkerPosition(37.0, 127.0, "바다 정원", "강원도 고성군")
                }

                scenarioMain.recreate()
                scenarioMain.onActivity { activity ->
                    activity.loadLastMarkerPosition()

                    val bottomSheetTitle: TextView = activity.findViewById(R.id.bottomSheetTitle)
                    val bottomSheetAddress: TextView =
                        activity.findViewById(R.id.bottomSheetAddress)
                    assertEquals("바다 정원", bottomSheetTitle.text.toString())
                    assertEquals("강원도 고성군", bottomSheetAddress.text.toString())
                    assertEquals(
                        View.VISIBLE,
                        activity.findViewById<FrameLayout>(R.id.bottomSheetLayout).visibility
                    )

                    // 5. searchactivity에서 저장된 검색어 남아있는지 확인하고 해당 검색어 누르면 해당 검색어로 재검색되는지 확인하기
                    val searchEditText: EditText = activity.findViewById(R.id.search_edit_text)
                    searchEditText.performClick()

                    Executors.newSingleThreadExecutor().execute {
                        scenarioSearch = ActivityScenario.launch(SearchActivity::class.java)
                        scenarioSearch.onActivity { searchActivity ->

                            val selectedItemsRecyclerView: RecyclerView =
                                searchActivity.findViewById(R.id.selectedItemsRecyclerView)
                            assertEquals(1, selectedItemsRecyclerView.adapter?.itemCount)
                            val selectedViewHolder =
                                selectedItemsRecyclerView.findViewHolderForAdapterPosition(0)
                            assertEquals(
                                "바다 정원",
                                selectedViewHolder?.itemView?.findViewById<TextView>(R.id.selectedItemName)?.text.toString()
                            )

                            selectedViewHolder?.itemView?.performClick()
                            searchActivity.performSearch("바다 정원")

                            val searchResultsRecyclerView: RecyclerView =
                                searchActivity.findViewById(R.id.searchResultsRecyclerView)
                            assertEquals(1, searchResultsRecyclerView.adapter?.itemCount)
                        }
                    }
                }
            }
        }
    }
}
