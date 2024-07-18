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
import java.util.concurrent.Executors

@RunWith(AndroidJUnit4::class)
class FunctionTest {

    private lateinit var scenarioMain: ActivityScenario<MainActivity>
    private lateinit var scenarioSearch: ActivityScenario<SearchActivity>
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        scenarioMain = ActivityScenario.launch(MainActivity::class.java)
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
            // Click on the search bar to go to SearchActivity
            val searchEditText: EditText = mainActivity.findViewById(R.id.search_edit_text)
            searchEditText.performClick()

            // Launch SearchActivity in a separate thread to avoid calling it on the main thread
            Executors.newSingleThreadExecutor().execute {
                scenarioSearch = ActivityScenario.launch(SearchActivity::class.java)
                scenarioSearch.onActivity { searchActivity ->
                    // Search for "바다 정원"
                    val searchEditText: EditText = searchActivity.findViewById(R.id.searchEditText)
                    searchEditText.setText("바다 정원")
                    searchActivity.performSearch("바다 정원")

                    // Set search results
                    val searchResults = MutableLiveData<List<MapItem>>()
                    searchResults.postValue(listOf(MapItem("1", "바다 정원", "강원도 고성군", "카페", 127.0, 37.0)))
                    searchActivity.viewModel.setSearchResults(searchResults.value ?: emptyList())

                    // Check if the search results are displayed
                    val searchResultsRecyclerView: RecyclerView = searchActivity.findViewById(R.id.searchResultsRecyclerView)
                    searchResultsRecyclerView.adapter?.notifyDataSetChanged()
                    assertEquals(1, searchResultsRecyclerView.adapter?.itemCount)

                    // Select a search result
                    searchResultsRecyclerView.findViewHolderForAdapterPosition(0)?.itemView?.performClick()

                    // Check if the selected item is saved and returned to MainActivity
                    searchActivity.setResultAndFinish(MapItem("0", "바다 정원", "강원도 고성군", "카페", 127.0, 37.0))
                    val resultIntent = Intent().apply {
                        putExtra("place_name", "바다 정원")
                        putExtra("road_address_name", "강원도 고성군")
                        putExtra("x", 127.0)
                        putExtra("y", 37.0)
                    }

                    mainActivity.onActivityResult(MainActivity.SEARCH_REQUEST_CODE, Activity.RESULT_OK, resultIntent)
                    val bottomSheetTitle: TextView = mainActivity.findViewById(R.id.bottomSheetTitle)
                    val bottomSheetAddress: TextView = mainActivity.findViewById(R.id.bottomSheetAddress)
                    assertEquals("바다 정원", bottomSheetTitle.text.toString())
                    assertEquals("강원도 고성군", bottomSheetAddress.text.toString())

                    // Save last position as "바다 정원"
                    mainActivity.saveLastMarkerPosition(37.0, 127.0, "바다 정원", "강원도 고성군")
                }

                // Relaunch MainActivity and check if the last marker position is loaded
                scenarioMain.recreate()

                scenarioMain.onActivity { activity ->
                    activity.loadLastMarkerPosition()

                    val bottomSheetTitle: TextView = activity.findViewById(R.id.bottomSheetTitle)
                    val bottomSheetAddress: TextView = activity.findViewById(R.id.bottomSheetAddress)
                    assertEquals("바다 정원", bottomSheetTitle.text.toString())
                    assertEquals("강원도 고성군", bottomSheetAddress.text.toString())
                    assertEquals(View.VISIBLE, activity.findViewById<FrameLayout>(R.id.bottomSheetLayout).visibility)

                    // Click on the search bar to go to SearchActivity again
                    val searchEditText: EditText = activity.findViewById(R.id.search_edit_text)
                    searchEditText.performClick()

                    // Launch SearchActivity in a separate thread to avoid calling it on the main thread
                    Executors.newSingleThreadExecutor().execute {
                        scenarioSearch = ActivityScenario.launch(SearchActivity::class.java)
                        scenarioSearch.onActivity { searchActivity ->
                            // Check if the previously selected item "바다 정원" is still saved
                            val selectedItemsRecyclerView: RecyclerView = searchActivity.findViewById(R.id.selectedItemsRecyclerView)
                            assertEquals(1, selectedItemsRecyclerView.adapter?.itemCount)
                            val selectedViewHolder = selectedItemsRecyclerView.findViewHolderForAdapterPosition(0)
                            assertEquals("바다 정원", selectedViewHolder?.itemView?.findViewById<TextView>(R.id.selectedItemName)?.text.toString())

                            // Click on the saved search item
                            selectedViewHolder?.itemView?.performClick()

                            // Perform search again for the same item
                            searchActivity.performSearch("바다 정원")

                            // Check if the search results are displayed again
                            val searchResultsRecyclerView: RecyclerView = searchActivity.findViewById(R.id.searchResultsRecyclerView)
                            assertEquals(1, searchResultsRecyclerView.adapter?.itemCount)
                        }
                    }
                }
            }
        }
    }
}