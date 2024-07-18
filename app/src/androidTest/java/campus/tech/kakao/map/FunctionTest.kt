package campus.tech.kakao.map

import android.app.Activity
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
import org.junit.*
import org.junit.runner.RunWith
import org.junit.Assert.assertEquals

@RunWith(AndroidJUnit4::class)
class FunctionTest {

    private lateinit var scenarioMain: ActivityScenario<MainActivity>
    private lateinit var scenarioSearch: ActivityScenario<SearchActivity>
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        scenarioMain = ActivityScenario.launch(MainActivity::class.java)
        scenarioSearch = ActivityScenario.launch(SearchActivity::class.java)
    }

    @After
    fun tearDown() {
        scenarioMain.close()
        scenarioSearch.close()
    }

    @Test
    fun testSearchResultDisplayed() {
        scenarioSearch.onActivity { activity ->
            val searchEditText: EditText = activity.findViewById(R.id.searchEditText)
            searchEditText.setText("Test Place")
            activity.performSearch("Test Place")

            val searchResults = MutableLiveData<List<MapItem>>()
            searchResults.postValue(listOf(MapItem("1", "Test Place", "Test Road", "Test Category", 127.0, 37.0)))
            activity.viewModel.searchResults = searchResults

            val searchResultsRecyclerView: RecyclerView = activity.findViewById(R.id.searchResultsRecyclerView)
            assertEquals(1, searchResultsRecyclerView.adapter?.itemCount)
        }
    }

    @Test
    fun testSelectSearchResultAndShowOnMap() {
        scenarioMain.onActivity { mainActivity ->
            val intent = Intent(mainActivity, SearchActivity::class.java)
            mainActivity.startActivityForResult(intent, MainActivity.SEARCH_REQUEST_CODE)

            scenarioSearch.onActivity { searchActivity ->
                val searchEditText: EditText = searchActivity.findViewById(R.id.searchEditText)
                searchEditText.setText("Test Place")
                searchActivity.performSearch("Test Place")

                val searchResults = MutableLiveData<List<MapItem>>()
                searchResults.postValue(listOf(MapItem("1", "Test Place", "Test Road", "Test Category", 127.0, 37.0)))
                searchActivity.viewModel.searchResults = searchResults

                searchActivity.setResultAndFinish(MapItem("1", "Test Place", "Test Road", "Test Category", 127.0, 37.0))

                val resultIntent = Intent().apply {
                    putExtra("place_name", "Test Place")
                    putExtra("road_address_name", "Test Road")
                    putExtra("x", 127.0)
                    putExtra("y", 37.0)
                }

                mainActivity.onActivityResult(MainActivity.SEARCH_REQUEST_CODE, Activity.RESULT_OK, resultIntent)
                val bottomSheetTitle: TextView = mainActivity.findViewById(R.id.bottomSheetTitle)
                assertEquals("Test Place", bottomSheetTitle.text.toString())
            }
        }
    }

    @Test
    fun testSaveAndLoadLastMarkerPosition() {
        scenarioMain.onActivity { activity ->
            val sharedPreferences = activity.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putFloat(MainActivity.PREF_LATITUDE, 37.5665f)
                putFloat(MainActivity.PREF_LONGITUDE, 126.9780f)
                putString(MainActivity.PREF_PLACE_NAME, "Seoul")
                putString(MainActivity.PREF_ROAD_ADDRESS_NAME, "Seoul Road")
                apply()
            }

            activity.loadLastMarkerPosition()

            val bottomSheetTitle: TextView = activity.findViewById(R.id.bottomSheetTitle)
            val bottomSheetAddress: TextView = activity.findViewById(R.id.bottomSheetAddress)

            assertEquals("Seoul", bottomSheetTitle.text.toString())
            assertEquals("Seoul Road", bottomSheetAddress.text.toString())
            assertEquals(View.VISIBLE, activity.findViewById<FrameLayout>(R.id.bottomSheetLayout).visibility)
        }
    }
}
