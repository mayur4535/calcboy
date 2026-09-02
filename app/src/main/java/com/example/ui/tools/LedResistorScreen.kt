package com.example.ui.tools

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.util.Locale

data class LedPreset(
  val name: String,
  val vf: Double,
  val ifMa: Double,
  val color: Color
)

val LedPresets = listOf(
  LedPreset("Red", 2.0, 20.0, Color(0xFFEF4444)),
  LedPreset("Green", 2.2, 20.0, Color(0xFF10B981)),
  LedPreset("Yellow", 2.1, 20.0, Color(0xFFF59E0B)),
  LedPreset("Blue", 3.2, 20.0, Color(0xFF3B82F6)),
  LedPreset("White", 3.2, 20.0, Color(0xFFF8FAFC)),
  LedPreset("IR (Infrared)", 1.5, 20.0, Color(0xFF8B5CF6))
)

// Standard E24 decade series multipliers
val E24Multipliers = doubleArrayOf(
  1.0, 1.1, 1.2, 1.3, 1.5, 1.6, 1.8, 2.0, 2.2, 2.4, 2.7, 3.0,
  3.3, 3.6, 3.9, 4.3, 4.7, 5.1, 5.6, 6.2, 6.8, 7.5, 8.2, 9.1
)

fun findNearestStandardResistor(target: Double): Double {
  if (target <= 0) return 0.0
  var bestVal = target
  var minDiff = Double.MAX_VALUE

  var decade = 1.0
  while (decade <= 1000000.0) {
    for (m in E24Multipliers) {
      val valOhms = m * decade
      val diff = Math.abs(valOhms - target)
      if (diff < minDiff) {
        minDiff = diff
        bestVal = valOhms
      }
    }
    decade *= 10.0
  }
  return bestVal
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedResistorScreen(
  onBack: () -> Unit,
  onSaveHistory: (String, String, String) -> Unit
) {
  var supplyVoltageStr by remember { mutableStateOf("5") }
  var forwardVoltageStr by remember { mutableStateOf("2.0") }
  var forwardCurrentStr by remember { mutableStateOf("20") }
  var selectedLedColor by remember { mutableStateOf(Color(0xFFEF4444)) }

  val clipboardManager: ClipboardManager = LocalClipboardManager.current
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  val vs = supplyVoltageStr.toDoubleOrNull() ?: 0.0
  val vf = forwardVoltageStr.toDoubleOrNull() ?: 0.0
  val iMa = forwardCurrentStr.toDoubleOrNull() ?: 0.0
  val iAmps = iMa / 1000.0

  val isVoltageTooLow = (vs > 0 && vf > 0 && vs <= vf)
  val exactR = if (vs > vf && iAmps > 0) (vs - vf) / iAmps else 0.0
  val standardR = if (exactR > 0) findNearestStandardResistor(exactR) else 0.0
  val powerMw = if (exactR > 0) (iAmps * iAmps * exactR) * 1000.0 else 0.0
  val powerW = powerMw / 1000.0

  val recommendedRating = when {
    powerW <= 0.125 -> "1/8 Watt (0.125 W)"
    powerW <= 0.25 -> "1/4 Watt (0.25 W)"
    powerW <= 0.5 -> "1/2 Watt (0.50 W)"
    powerW <= 1.0 -> "1 Watt (1.00 W)"
    else -> "2 Watt or higher (Heavy Duty)"
  }

  fun formatR(r: Double): String {
    return when {
      r >= 1000000 -> String.format(Locale.US, "%.2f MΩ", r / 1000000.0)
      r >= 1000 -> String.format(Locale.US, "%.1f kΩ", r / 1000.0)
      else -> String.format(Locale.US, "%.1f Ω", r)
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("LED Series Resistor", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("led_back_button")
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(
            onClick = {
              if (exactR > 0) {
                val text = "LED Resistor for Vs=${vs}V, Vf=${vf}V @ ${iMa}mA -> ${formatR(exactR)} (Standard: ${formatR(standardR)}, Rating: $recommendedRating)"
                clipboardManager.setText(AnnotatedString(text))
                scope.launch { snackbarHostState.showSnackbar("Copied LED calculation!") }
                onSaveHistory("LED Series Resistor", "Vs=${vs}V, Vf=${vf}V, If=${iMa}mA", "${formatR(standardR)} ($recommendedRating)")
              }
            },
            modifier = Modifier.testTag("led_copy_button")
          ) {
            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    snackbarHost = { SnackbarHost(snackbarHostState) }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Circuit Visual Graphic
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .height(140.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val w = size.width
          val h = size.height
          val cy = h / 2f

          val wireColor = Color(0xFF64748B)

          // Power source lead
          drawLine(wireColor, Offset(24.dp.toPx(), cy), Offset(70.dp.toPx(), cy), strokeWidth = 3.dp.toPx())

          // Draw Resistor zigzag
          val rPath = Path().apply {
            val rStart = 70.dp.toPx()
            val rEnd = 160.dp.toPx()
            val step = (rEnd - rStart) / 6f
            moveTo(rStart, cy)
            lineTo(rStart + step * 0.5f, cy - 14.dp.toPx())
            lineTo(rStart + step * 1.5f, cy + 14.dp.toPx())
            lineTo(rStart + step * 2.5f, cy - 14.dp.toPx())
            lineTo(rStart + step * 3.5f, cy + 14.dp.toPx())
            lineTo(rStart + step * 4.5f, cy - 14.dp.toPx())
            lineTo(rStart + step * 5.5f, cy + 14.dp.toPx())
            lineTo(rEnd, cy)
          }
          drawPath(rPath, color = Color(0xFF0284C7), style = Stroke(width = 3.dp.toPx()))

          // Connect resistor to LED
          val rEnd = 160.dp.toPx()
          val ledStart = w - 100.dp.toPx()
          drawLine(wireColor, Offset(rEnd, cy), Offset(ledStart, cy), strokeWidth = 3.dp.toPx())

          // Draw LED diode symbol
          val diodePath = Path().apply {
            moveTo(ledStart, cy - 18.dp.toPx())
            lineTo(ledStart, cy + 18.dp.toPx())
            lineTo(ledStart + 24.dp.toPx(), cy)
            close()
          }
          drawPath(diodePath, color = selectedLedColor)

          // Cathode bar
          drawLine(
            color = wireColor,
            start = Offset(ledStart + 24.dp.toPx(), cy - 18.dp.toPx()),
            end = Offset(ledStart + 24.dp.toPx(), cy + 18.dp.toPx()),
            strokeWidth = 4.dp.toPx()
          )

          // LED light emission arrows
          val arrowColor = selectedLedColor.copy(alpha = 0.9f)
          drawLine(arrowColor, Offset(ledStart + 16.dp.toPx(), cy - 20.dp.toPx()), Offset(ledStart + 26.dp.toPx(), cy - 32.dp.toPx()), strokeWidth = 2.dp.toPx())
          drawLine(arrowColor, Offset(ledStart + 24.dp.toPx(), cy - 16.dp.toPx()), Offset(ledStart + 34.dp.toPx(), cy - 28.dp.toPx()), strokeWidth = 2.dp.toPx())

          // Connect to ground/negative
          drawLine(wireColor, Offset(ledStart + 24.dp.toPx(), cy), Offset(w - 24.dp.toPx(), cy), strokeWidth = 3.dp.toPx())
        }
      }

      // Quick LED Color Presets
      Text("LED Color Presets:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        LedPresets.forEach { preset ->
          FilterChip(
            selected = (forwardVoltageStr == preset.vf.toString()),
            onClick = {
              forwardVoltageStr = preset.vf.toString()
              forwardCurrentStr = preset.ifMa.toInt().toString()
              selectedLedColor = preset.color
            },
            label = { Text(preset.name) },
            leadingIcon = {
              Box(
                modifier = Modifier
                  .size(14.dp)
                  .clip(CircleShape)
                  .background(preset.color)
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
            )
          )
        }
      }

      // Input: Source Voltage (Vs)
      OutlinedTextField(
        value = supplyVoltageStr,
        onValueChange = { supplyVoltageStr = it },
        label = { Text("Power Supply Voltage (Vs)") },
        trailingIcon = { Text("Volts (V)", modifier = Modifier.padding(end = 12.dp)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_led_vs"),
        singleLine = true
      )

      // Input: LED Forward Voltage (Vf)
      OutlinedTextField(
        value = forwardVoltageStr,
        onValueChange = { forwardVoltageStr = it },
        label = { Text("LED Forward Voltage Drop (Vf)") },
        trailingIcon = { Text("Volts (V)", modifier = Modifier.padding(end = 12.dp)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_led_vf"),
        singleLine = true
      )

      // Input: LED Forward Current (If)
      OutlinedTextField(
        value = forwardCurrentStr,
        onValueChange = { forwardCurrentStr = it },
        label = { Text("Desired LED Current (If)") },
        trailingIcon = { Text("mA", modifier = Modifier.padding(end = 12.dp)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_led_if"),
        singleLine = true
      )

      // Warning Card if Supply <= Forward
      if (isVoltageTooLow) {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
          shape = RoundedCornerShape(12.dp)
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            Text(
              text = "Supply voltage ($vs V) must be strictly greater than LED forward voltage ($vf V) for the LED to conduct current.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onErrorContainer
            )
          }
        }
      }

      // Results Card
      if (exactR > 0 && !isVoltageTooLow) {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
          ),
          shape = RoundedCornerShape(16.dp)
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Text(
              text = "RECOMMENDED CURRENT-LIMITING RESISTOR",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text("Standard E24 Resistor:", style = MaterialTheme.typography.bodySmall)
                Text(
                  text = formatR(standardR),
                  style = MaterialTheme.typography.headlineMedium,
                  fontWeight = FontWeight.ExtraBold,
                  color = MaterialTheme.colorScheme.onPrimaryContainer
                )
              }
              Column(horizontalAlignment = Alignment.End) {
                Text("Calculated Exact:", style = MaterialTheme.typography.bodySmall)
                Text(
                  text = formatR(exactR),
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace,
                  color = MaterialTheme.colorScheme.primary
                )
              }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text("Resistor Power Dissipation:", style = MaterialTheme.typography.bodySmall)
                Text(
                  text = String.format(Locale.US, "%.1f mW (%.3f W)", powerMw, powerW),
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold
                )
              }
              Column(horizontalAlignment = Alignment.End) {
                Text("Minimum Power Rating:", style = MaterialTheme.typography.bodySmall)
                Text(
                  text = recommendedRating,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.secondary
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}
