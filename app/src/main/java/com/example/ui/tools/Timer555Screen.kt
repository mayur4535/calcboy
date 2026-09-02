package com.example.ui.tools

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Timer555Screen(
  onBack: () -> Unit,
  onSaveHistory: (String, String, String) -> Unit
) {
  var r1Str by remember { mutableStateOf("1") }
  var r1Unit by remember { mutableStateOf("kΩ") }

  var r2Str by remember { mutableStateOf("10") }
  var r2Unit by remember { mutableStateOf("kΩ") }

  var cStr by remember { mutableStateOf("100") }
  var cUnit by remember { mutableStateOf("nF") }

  val clipboardManager: ClipboardManager = LocalClipboardManager.current
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  fun rFactor(u: String) = when (u) { "kΩ" -> 1000.0; "MΩ" -> 1000000.0; else -> 1.0 }
  fun cFactor(u: String) = when (u) { "pF" -> 1e-12; "nF" -> 1e-9; "μF" -> 1e-6; else -> 1e-6 }

  val r1 = (r1Str.toDoubleOrNull() ?: 0.0) * rFactor(r1Unit)
  val r2 = (r2Str.toDoubleOrNull() ?: 0.0) * rFactor(r2Unit)
  val c = (cStr.toDoubleOrNull() ?: 0.0) * cFactor(cUnit)

  val isValid = (r1 > 0 && r2 > 0 && c > 0)

  val tHigh = if (isValid) 0.693 * (r1 + r2) * c else 0.0
  val tLow = if (isValid) 0.693 * r2 * c else 0.0
  val tTotal = tHigh + tLow
  val freqHz = if (isValid && tTotal > 0) 1.0 / tTotal else 0.0
  val dutyCyclePercent = if (isValid && tTotal > 0) (tHigh / tTotal) * 100.0 else 0.0

  fun formatFreq(f: Double): String {
    return when {
      f >= 1_000_000 -> String.format(Locale.US, "%.3f MHz", f / 1_000_000.0)
      f >= 1_000 -> String.format(Locale.US, "%.2f kHz", f / 1_000.0)
      else -> String.format(Locale.US, "%.2f Hz", f)
    }
  }

  fun formatTime(sec: Double): String {
    return when {
      sec >= 1.0 -> String.format(Locale.US, "%.3f s", sec)
      sec >= 1e-3 -> String.format(Locale.US, "%.3f ms", sec * 1e3)
      sec >= 1e-6 -> String.format(Locale.US, "%.2f µs", sec * 1e6)
      else -> String.format(Locale.US, "%.2f ns", sec * 1e9)
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("555 Timer Astable", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("timer_back_button")
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(
            onClick = {
              r1Str = "1"
              r1Unit = "kΩ"
              r2Str = "10"
              r2Unit = "kΩ"
              cStr = "100"
              cUnit = "nF"
            },
            modifier = Modifier.testTag("timer_reset_button")
          ) {
            Icon(Icons.Default.Refresh, contentDescription = "Reset")
          }
          IconButton(
            onClick = {
              if (isValid) {
                val text = "555 Astable: f=${formatFreq(freqHz)}, Duty Cycle=${String.format(Locale.US, "%.1f%%", dutyCyclePercent)}, T=${formatTime(tTotal)} (Thigh=${formatTime(tHigh)}, Tlow=${formatTime(tLow)})"
                clipboardManager.setText(AnnotatedString(text))
                scope.launch { snackbarHostState.showSnackbar("Copied 555 Timer specs!") }
                onSaveHistory("555 Timer Astable", "R1=${r1Str}${r1Unit}, R2=${r2Str}${r2Unit}, C=${cStr}${cUnit}", "${formatFreq(freqHz)} (${String.format(Locale.US, "%.1f%%", dutyCyclePercent)} duty)")
              }
            },
            modifier = Modifier.testTag("timer_copy_button")
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
      // Oscilloscope Waveform Canvas
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .height(130.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(16.dp)
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val w = size.width
          val h = size.height
          val gridColor = Color(0xFF1E293B)

          // Draw Oscilloscope Grid
          for (x in 0 until w.toInt() step 40) {
            drawLine(gridColor, Offset(x.toFloat(), 0f), Offset(x.toFloat(), h), strokeWidth = 1f)
          }
          for (y in 0 until h.toInt() step 30) {
            drawLine(gridColor, Offset(0f, y.toFloat()), Offset(w, y.toFloat()), strokeWidth = 1f)
          }

          val highY = 24.dp.toPx()
          val lowY = h - 24.dp.toPx()

          // Draw digital square wave based on actual duty cycle
          val cycleFraction = if (tTotal > 0) (tHigh / tTotal).coerceIn(0.1, 0.9).toFloat() else 0.5f
          val wavePeriodPx = 100.dp.toPx()
          val highLengthPx = wavePeriodPx * cycleFraction
          val lowLengthPx = wavePeriodPx * (1f - cycleFraction)

          val wavePath = Path()
          var curX = 16.dp.toPx()
          wavePath.moveTo(curX, lowY)

          while (curX < w) {
            wavePath.lineTo(curX, highY)
            curX += highLengthPx
            wavePath.lineTo(curX, highY)
            wavePath.lineTo(curX, lowY)
            curX += lowLengthPx
            wavePath.lineTo(curX, lowY)
          }

          drawPath(
            wavePath,
            color = Color(0xFF38BDF8),
            style = Stroke(width = 3.dp.toPx())
          )
        }
      }

      // Presets
      Text("Standard 555 Presets:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        SuggestionChip(
          onClick = {
            r1Str = "1"
            r1Unit = "kΩ"
            r2Str = "10"
            r2Unit = "kΩ"
            cStr = "68"
            cUnit = "nF"
          },
          label = { Text("1 kHz Tone") },
          colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
          )
        )
        SuggestionChip(
          onClick = {
            r1Str = "10"
            r1Unit = "kΩ"
            r2Str = "100"
            r2Unit = "kΩ"
            cStr = "4.7"
            cUnit = "μF"
          },
          label = { Text("1 Hz Flasher") },
          colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
          )
        )
      }

      // Input: R1
      ParameterInputField(
        label = "Resistor R1 (Vcc to Pin 7)",
        value = r1Str,
        onValueChange = { r1Str = it },
        currentUnit = r1Unit,
        units = listOf("Ω", "kΩ", "MΩ"),
        onUnitChange = { r1Unit = it },
        isCalculated = false,
        calculatedValue = null,
        tag = "input_555_r1"
      )

      // Input: R2
      ParameterInputField(
        label = "Resistor R2 (Pin 7 to Pin 6/2)",
        value = r2Str,
        onValueChange = { r2Str = it },
        currentUnit = r2Unit,
        units = listOf("Ω", "kΩ", "MΩ"),
        onUnitChange = { r2Unit = it },
        isCalculated = false,
        calculatedValue = null,
        tag = "input_555_r2"
      )

      // Input: Capacitor C
      ParameterInputField(
        label = "Timing Capacitor C",
        value = cStr,
        onValueChange = { cStr = it },
        currentUnit = cUnit,
        units = listOf("pF", "nF", "μF"),
        onUnitChange = { cUnit = it },
        isCalculated = false,
        calculatedValue = null,
        tag = "input_555_c"
      )

      // Output Card
      if (isValid) {
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
              text = "OSCILLATION SPECIFICATIONS",
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
                Text("Output Frequency (f):", style = MaterialTheme.typography.bodySmall)
                Text(
                  text = formatFreq(freqHz),
                  style = MaterialTheme.typography.headlineMedium,
                  fontWeight = FontWeight.ExtraBold,
                  color = MaterialTheme.colorScheme.onPrimaryContainer
                )
              }
              Column(horizontalAlignment = Alignment.End) {
                Text("Duty Cycle:", style = MaterialTheme.typography.bodySmall)
                Text(
                  text = String.format(Locale.US, "%.1f%%", dutyCyclePercent),
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.secondary
                )
              }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text("Time Period (T):", style = MaterialTheme.typography.bodySmall)
                Text(
                  text = formatTime(tTotal),
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
              }
              Column(horizontalAlignment = Alignment.End) {
                Text("High Time (Thigh):", style = MaterialTheme.typography.bodySmall)
                Text(
                  text = formatTime(tHigh),
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
              }
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text("Low Time (Tlow):", style = MaterialTheme.typography.bodySmall)
                Text(
                  text = formatTime(tLow),
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
              }
              Column(horizontalAlignment = Alignment.End) {
                Text("Formula:", style = MaterialTheme.typography.bodySmall)
                Text(
                  text = "f = 1.44 / ((R1 + 2·R2)·C)",
                  style = MaterialTheme.typography.bodySmall,
                  fontFamily = FontFamily.Monospace,
                  color = MaterialTheme.colorScheme.primary
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
