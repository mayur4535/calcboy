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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.log10

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoltageDividerScreen(
  onBack: () -> Unit,
  onSaveHistory: (String, String, String) -> Unit
) {
  var isSolveForR2 by remember { mutableStateOf(false) }

  var vinStr by remember { mutableStateOf("12") }
  var r1Str by remember { mutableStateOf("10") }
  var r1Unit by remember { mutableStateOf("kΩ") }

  var r2Str by remember { mutableStateOf("10") }
  var r2Unit by remember { mutableStateOf("kΩ") }

  var targetVoutStr by remember { mutableStateOf("5") }

  val clipboardManager: ClipboardManager = LocalClipboardManager.current
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  fun unitFactor(u: String) = when (u) { "kΩ" -> 1000.0; "MΩ" -> 1000000.0; else -> 1.0 }

  val vin = vinStr.toDoubleOrNull() ?: 0.0
  val r1Ohms = (r1Str.toDoubleOrNull() ?: 0.0) * unitFactor(r1Unit)

  var r2Ohms = (r2Str.toDoubleOrNull() ?: 0.0) * unitFactor(r2Unit)
  var vout = 0.0

  if (isSolveForR2) {
    val targetVout = targetVoutStr.toDoubleOrNull() ?: 0.0
    if (vin > targetVout && targetVout > 0 && r1Ohms > 0) {
      r2Ohms = (targetVout * r1Ohms) / (vin - targetVout)
      vout = targetVout
    }
  } else {
    if (vin > 0 && (r1Ohms + r2Ohms) > 0) {
      vout = vin * (r2Ohms / (r1Ohms + r2Ohms))
    }
  }

  val vr1 = if (vin >= vout) vin - vout else 0.0
  val totalR = r1Ohms + r2Ohms
  val currentMa = if (totalR > 0) (vin / totalR) * 1000.0 else 0.0
  val currentA = currentMa / 1000.0
  val powerR1Mw = (currentA * currentA * r1Ohms) * 1000.0
  val powerR2Mw = (currentA * currentA * r2Ohms) * 1000.0
  val attenuationRatio = if (vin > 0) vout / vin else 0.0
  val attenuationDb = if (attenuationRatio > 0) 20 * log10(attenuationRatio) else 0.0

  fun formatR(r: Double): String {
    return when {
      r >= 1000000 -> String.format(Locale.US, "%.2f MΩ", r / 1000000.0)
      r >= 1000 -> String.format(Locale.US, "%.2f kΩ", r / 1000.0)
      else -> String.format(Locale.US, "%.2f Ω", r)
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Voltage Divider", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("divider_back_button")
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(
            onClick = {
              vinStr = "12"
              r1Str = "10"
              r1Unit = "kΩ"
              r2Str = "10"
              r2Unit = "kΩ"
              targetVoutStr = "5"
            },
            modifier = Modifier.testTag("divider_reset_button")
          ) {
            Icon(Icons.Default.Refresh, contentDescription = "Reset")
          }
          IconButton(
            onClick = {
              val text = "Voltage Divider: Vin=${vin}V, R1=${formatR(r1Ohms)}, R2=${formatR(r2Ohms)} -> Vout=${String.format(Locale.US, "%.3f V", vout)} (Ratio: ${String.format(Locale.US, "%.3f", attenuationRatio)})"
              clipboardManager.setText(AnnotatedString(text))
              scope.launch { snackbarHostState.showSnackbar("Copied divider calculation!") }
              onSaveHistory("Voltage Divider", "Vin=${vin}V, R1=${formatR(r1Ohms)}, R2=${formatR(r2Ohms)}", "Vout=${String.format(Locale.US, "%.3f V", vout)}")
            },
            modifier = Modifier.testTag("divider_copy_button")
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
      // Mode selector
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
      ) {
        FilterChip(
          selected = !isSolveForR2,
          onClick = { isSolveForR2 = false },
          label = { Text("Calculate Vout") },
          modifier = Modifier
            .padding(end = 8.dp)
            .testTag("chip_calc_vout"),
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
          )
        )
        FilterChip(
          selected = isSolveForR2,
          onClick = { isSolveForR2 = true },
          label = { Text("Solve R2 for Target Vout") },
          modifier = Modifier.testTag("chip_solve_r2"),
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
          )
        )
      }

      // Circuit Schematic Canvas
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .height(150.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val w = size.width
          val h = size.height
          val cx = w / 2f
          val wireColor = Color(0xFF64748B)

          // Vin node at top
          drawLine(wireColor, Offset(cx, 16.dp.toPx()), Offset(cx, 32.dp.toPx()), strokeWidth = 3.dp.toPx())

          // R1 resistor symbol
          val r1Top = 32.dp.toPx()
          val r1Bottom = 68.dp.toPx()
          drawRect(
            color = Color(0xFF0284C7),
            topLeft = Offset(cx - 10.dp.toPx(), r1Top),
            size = androidx.compose.ui.geometry.Size(20.dp.toPx(), r1Bottom - r1Top)
          )

          // Intermediate node to Vout
          drawLine(wireColor, Offset(cx, r1Bottom), Offset(cx, r1Bottom + 20.dp.toPx()), strokeWidth = 3.dp.toPx())
          // Vout branch
          val voutY = r1Bottom + 10.dp.toPx()
          drawLine(wireColor, Offset(cx, voutY), Offset(cx + 60.dp.toPx(), voutY), strokeWidth = 3.dp.toPx())
          drawCircle(Color(0xFFF59E0B), radius = 4.dp.toPx(), center = Offset(cx + 60.dp.toPx(), voutY))

          // R2 resistor symbol
          val r2Top = r1Bottom + 20.dp.toPx()
          val r2Bottom = r2Top + 36.dp.toPx()
          drawRect(
            color = Color(0xFF0284C7),
            topLeft = Offset(cx - 10.dp.toPx(), r2Top),
            size = androidx.compose.ui.geometry.Size(20.dp.toPx(), r2Bottom - r2Top)
          )

          // GND lead and symbol
          drawLine(wireColor, Offset(cx, r2Bottom), Offset(cx, r2Bottom + 12.dp.toPx()), strokeWidth = 3.dp.toPx())
          val gndY = r2Bottom + 12.dp.toPx()
          drawLine(wireColor, Offset(cx - 14.dp.toPx(), gndY), Offset(cx + 14.dp.toPx(), gndY), strokeWidth = 3.dp.toPx())
          drawLine(wireColor, Offset(cx - 9.dp.toPx(), gndY + 4.dp.toPx()), Offset(cx + 9.dp.toPx(), gndY + 4.dp.toPx()), strokeWidth = 2.dp.toPx())
          drawLine(wireColor, Offset(cx - 4.dp.toPx(), gndY + 8.dp.toPx()), Offset(cx + 4.dp.toPx(), gndY + 8.dp.toPx()), strokeWidth = 2.dp.toPx())
        }
      }

      // Presets
      Text("Common Engineering Presets:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        SuggestionChip(
          onClick = {
            isSolveForR2 = false
            vinStr = "5"
            r1Str = "1.7"
            r1Unit = "kΩ"
            r2Str = "3.3"
            r2Unit = "kΩ"
          },
          label = { Text("5V → 3.3V Logic") },
          colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
          )
        )
        SuggestionChip(
          onClick = {
            isSolveForR2 = false
            vinStr = "12"
            r1Str = "14"
            r1Unit = "kΩ"
            r2Str = "10"
            r2Unit = "kΩ"
          },
          label = { Text("12V → 5V Step") },
          colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
          )
        )
      }

      // Input: Vin
      OutlinedTextField(
        value = vinStr,
        onValueChange = { vinStr = it },
        label = { Text("Input Voltage (Vin)") },
        trailingIcon = { Text("Volts (V)", modifier = Modifier.padding(end = 12.dp)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_vin"),
        singleLine = true
      )

      // Input: R1
      ParameterInputField(
        label = "Resistor R1 (Top)",
        value = r1Str,
        onValueChange = { r1Str = it },
        currentUnit = r1Unit,
        units = listOf("Ω", "kΩ", "MΩ"),
        onUnitChange = { r1Unit = it },
        isCalculated = false,
        calculatedValue = null,
        tag = "input_r1"
      )

      if (isSolveForR2) {
        // Target Vout
        OutlinedTextField(
          value = targetVoutStr,
          onValueChange = { targetVoutStr = it },
          label = { Text("Desired Output Voltage (Vout)") },
          trailingIcon = { Text("Volts (V)", modifier = Modifier.padding(end = 12.dp)) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_target_vout"),
          singleLine = true
        )
      } else {
        // Input: R2
        ParameterInputField(
          label = "Resistor R2 (Bottom)",
          value = r2Str,
          onValueChange = { r2Str = it },
          currentUnit = r2Unit,
          units = listOf("Ω", "kΩ", "MΩ"),
          onUnitChange = { r2Unit = it },
          isCalculated = false,
          calculatedValue = null,
          tag = "input_r2"
        )
      }

      // Results Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text(
            text = "OUTPUT & ATTENUATION METRICS",
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
              Text("Output Voltage (Vout):", style = MaterialTheme.typography.bodySmall)
              Text(
                text = String.format(Locale.US, "%.3f V", vout),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
            }
            if (isSolveForR2) {
              Column(horizontalAlignment = Alignment.End) {
                Text("Required R2:", style = MaterialTheme.typography.bodySmall)
                Text(
                  text = formatR(r2Ohms),
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.secondary
                )
              }
            } else {
              Column(horizontalAlignment = Alignment.End) {
                Text("Drop on R1:", style = MaterialTheme.typography.bodySmall)
                Text(
                  text = String.format(Locale.US, "%.3f V", vr1),
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(2.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text("Ratio (Vout/Vin):", style = MaterialTheme.typography.bodySmall)
              Text(
                text = String.format(Locale.US, "%.4f (%.2f dB)", attenuationRatio, attenuationDb),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            }
            Column(horizontalAlignment = Alignment.End) {
              Text("Total Current:", style = MaterialTheme.typography.bodySmall)
              Text(
                text = String.format(Locale.US, "%.3f mA", currentMa),
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
              Text("R1 Power Dissipation:", style = MaterialTheme.typography.bodySmall)
              Text(
                text = String.format(Locale.US, "%.2f mW", powerR1Mw),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
              )
            }
            Column(horizontalAlignment = Alignment.End) {
              Text("R2 Power Dissipation:", style = MaterialTheme.typography.bodySmall)
              Text(
                text = String.format(Locale.US, "%.2f mW", powerR2Mw),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}
