package campus.tech.kakao.map

import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kakao.vectormap.MapView
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
class ErrorScreenTest {

    private lateinit var scenarioMain: ActivityScenario<MainActivity>

    @Before
    fun setUp() {
        scenarioMain = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun tearDown() {
        scenarioMain.close()
    }

    @Test
    fun testShowErrorScreen() {
        scenarioMain.onActivity { activity ->
            val errorLayout: RelativeLayout = activity.findViewById(R.id.error_layout)
            val errorMessage: TextView = activity.findViewById(R.id.error_message)
            val errorDetails: TextView = activity.findViewById(R.id.error_details)
            val mapView: MapView = activity.findViewById(R.id.map_view)

            val mockException = Mockito.mock(Exception::class.java)
            Mockito.`when`(mockException.message).thenReturn("Test Error")

            activity.showErrorScreen(mockException)

            assertEquals(View.VISIBLE, errorLayout.visibility)
            assertEquals(View.GONE, mapView.visibility)
            assertEquals("Test Error", errorDetails.text.toString())
        }
    }
}
