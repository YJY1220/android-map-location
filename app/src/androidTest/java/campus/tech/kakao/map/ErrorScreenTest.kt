package campus.tech.kakao.map

import android.content.Context
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.*
import org.junit.runner.RunWith
import org.junit.Assert.assertEquals

@RunWith(AndroidJUnit4::class)
class ErrorScreenTest {

    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun testShowErrorScreen() {
        scenario.onActivity { activity ->
            activity.showErrorScreen(Exception("Test Error"))

            val errorLayout: RelativeLayout = activity.findViewById(R.id.error_layout)
            val errorDetails: TextView = activity.findViewById(R.id.error_details)

            assertEquals(View.VISIBLE, errorLayout.visibility)
            assertEquals("Test Error", errorDetails.text.toString())
        }
    }
}
