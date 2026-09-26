package app.condo.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.activity.enableEdgeToEdge
import app.condo.CondoApp
import app.condo.createRepository
import app.condo.domain.SystemAppClock
import app.condo.platform.AndroidServices
import app.condo.presentation.AppController

class MainActivity : ComponentActivity() {
    private lateinit var controller: AppController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        controller = lastCustomNonConfigurationInstance as? AppController ?: run {
            val platform = AndroidServices(applicationContext)
            AppController(createRepository(platform.store, SystemAppClock), platform)
        }
        setContent {
            val state by controller.state.collectAsState()
            BackHandler(enabled = state.history.isNotEmpty() && !state.showCode && state.message == null) {
                controller.back()
            }
            CondoApp(controller)
        }
    }
    override fun onRetainCustomNonConfigurationInstance(): Any = controller
    override fun onDestroy() {
        if (!isChangingConfigurations) controller.close()
        super.onDestroy()
    }
}
