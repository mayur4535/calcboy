package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.model.CalcHistoryItem
import com.example.model.ToolId
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.tools.CeramicCapTempCoeffScreen
import com.example.ui.tools.CeramicCapVoltageScreen
import com.example.ui.tools.CeramicCapacitorCodeScreen
import com.example.ui.tools.DiodeColorCodeScreen
import com.example.ui.tools.FilmCapacitorCodeScreen
import com.example.ui.tools.FuseColorCodeScreen
import com.example.ui.tools.IcColorCodeScreen
import com.example.ui.tools.InductorColorCodeScreen
import com.example.ui.tools.LedResistorScreen
import com.example.ui.tools.MicaCapacitorColorScreen
import com.example.ui.tools.OhmsLawScreen
import com.example.ui.tools.PolyesterCapColorScreen
import com.example.ui.tools.ResistorColorCodeScreen
import com.example.ui.tools.SmdCapacitorScreen
import com.example.ui.tools.SmdInductorScreen
import com.example.ui.tools.SmdResistorScreen
import com.example.ui.tools.TantalumCapColorScreen
import com.example.ui.tools.Timer555Screen
import com.example.ui.tools.TransistorColorCodeScreen
import com.example.ui.tools.VoltageDividerScreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        CalcBoyApp()
      }
    }
  }
}

@Composable
fun CalcBoyApp() {
  var currentScreen by remember { mutableStateOf<ToolId?>(null) }
  val historyList = remember {
    mutableStateListOf<CalcHistoryItem>(
      CalcHistoryItem(
        toolTitle = "Resistor Color Code",
        summary = "4-Band: Yellow, Violet, Red, Gold",
        resultText = "4.70 kΩ ±5.0%",
        timestamp = "Sample"
      ),
      CalcHistoryItem(
        toolTitle = "Ohm's Law",
        summary = "V=12.000 V, R=100.000 Ω",
        resultText = "I=120.000 mA, P=1.440 W",
        timestamp = "Sample"
      )
    )
  }

  fun saveHistory(toolTitle: String, summary: String, resultText: String) {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val time = formatter.format(Date())
    historyList.add(0, CalcHistoryItem(
      toolTitle = toolTitle,
      summary = summary,
      resultText = resultText,
      timestamp = time
    ))
  }

  BackHandler(enabled = currentScreen != null) {
    currentScreen = null
  }

  Surface(modifier = Modifier.fillMaxSize()) {
    AnimatedContent(
      targetState = currentScreen,
      transitionSpec = {
        if (targetState != null) {
          slideInHorizontally { width -> width } togetherWith slideOutHorizontally { width -> -width / 2 }
        } else {
          slideInHorizontally { width -> -width / 2 } togetherWith slideOutHorizontally { width -> width }
        }
      },
      label = "screenTransition"
    ) { screen ->
      when (screen) {
        null -> HomeScreen(
          onToolSelected = { selectedTool -> currentScreen = selectedTool },
          historyList = historyList,
          onClearHistory = { historyList.clear() }
        )
        ToolId.RESISTOR_COLOR_CODE -> ResistorColorCodeScreen(
          onBack = { currentScreen = null },
          onSaveHistory = ::saveHistory
        )
        ToolId.INDUCTOR_COLOR_CODE -> InductorColorCodeScreen(
          onBack = { currentScreen = null },
          onSaveHistory = ::saveHistory
        )
        ToolId.SMD_RESISTOR_CODE -> SmdResistorScreen(
          onBack = { currentScreen = null },
          onSaveHistory = ::saveHistory
        )
        ToolId.SMD_INDUCTOR_CODE -> SmdInductorScreen(
          onBack = { currentScreen = null },
          onSaveHistory = ::saveHistory
        )
        ToolId.SMD_CAPACITOR_CODE -> SmdCapacitorScreen(
          onBack = { currentScreen = null },
          onSaveHistory = ::saveHistory
        )
        ToolId.DIODE_COLOR_CODE -> DiodeColorCodeScreen(
          onBack = { currentScreen = null },
          onSaveHistory = ::saveHistory
        )
        ToolId.CERAMIC_CAPACITOR_CODE -> CeramicCapacitorCodeScreen(
          onBack = { currentScreen = null },
          onSaveHistory = ::saveHistory
        )
        ToolId.CERAMIC_CAP_VOLTAGE -> CeramicCapVoltageScreen(
          onBack = { currentScreen = null },
          onSaveHistory = ::saveHistory
        )
        ToolId.CERAMIC_CAP_TEMP_COEFF -> CeramicCapTempCoeffScreen(
          onBack = { currentScreen = null },
          onSaveHistory = ::saveHistory
        )
        ToolId.POLYESTER_CAP_COLOR_CODE -> PolyesterCapColorScreen(
          onBack = { currentScreen = null },
          onSaveHistory = ::saveHistory
        )
        ToolId.FILM_CAPACITOR_CODE -> FilmCapacitorCodeScreen(
          onBack = { currentScreen = null },
          onSaveHistory = ::saveHistory
        )
        ToolId.TRANSISTOR_COLOR_CODE -> TransistorColorCodeScreen(
          onBack = { currentScreen = null },
          onSaveHistory = ::saveHistory
        )
        ToolId.TANTALUM_CAP_COLOR_CODE -> TantalumCapColorScreen(
          onBack = { currentScreen = null },
          onSaveHistory = ::saveHistory
        )
        ToolId.MICA_CAPACITOR_COLOR_CODE -> MicaCapacitorColorScreen(
          onBack = { currentScreen = null },
          onSaveHistory = ::saveHistory
        )
        ToolId.FUSE_COLOR_CODE -> FuseColorCodeScreen(
          onBack = { currentScreen = null },
          onSaveHistory = ::saveHistory
        )
        ToolId.IC_COLOR_CODE -> IcColorCodeScreen(
          onBack = { currentScreen = null },
          onSaveHistory = ::saveHistory
        )
        ToolId.OHMS_LAW -> OhmsLawScreen(
          onBack = { currentScreen = null },
          onSaveHistory = ::saveHistory
        )
        ToolId.LED_RESISTOR -> LedResistorScreen(
          onBack = { currentScreen = null },
          onSaveHistory = ::saveHistory
        )
        ToolId.VOLTAGE_DIVIDER -> VoltageDividerScreen(
          onBack = { currentScreen = null },
          onSaveHistory = ::saveHistory
        )
        ToolId.TIMER_555 -> Timer555Screen(
          onBack = { currentScreen = null },
          onSaveHistory = ::saveHistory
        )
      }
    }
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  MyApplicationTheme { Greeting("CalcBoy") }
}

