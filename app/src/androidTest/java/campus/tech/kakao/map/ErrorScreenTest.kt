package campus.tech.kakao.map

import android.content.Context
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import campus.tech.kakao.map.ui.MainActivity
import org.junit.*
import org.junit.runner.RunWith
import org.junit.Assert.assertTrue

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
        if (::scenario.isInitialized) {
            scenario.close()
        }
    }

    @Test
    fun testShowErrorScreen() {
        scenario.onActivity { activity ->
            // 에러 화면이 표시될 때까지 대기
            Thread.sleep(2000)

            val errorLayout: RelativeLayout = activity.findViewById(R.id.error_layout)
            val errorDetails: TextView = activity.findViewById(R.id.error_details)

            assertTrue(errorLayout.visibility == View.VISIBLE)
            assertTrue(errorDetails.text.toString().contains("Unauthorized"))
        }
    }
}