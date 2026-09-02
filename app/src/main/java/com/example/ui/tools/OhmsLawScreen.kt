package com.example.ui.tools

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import kotlin.math.pow
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OhmsLawScreen(
  onBack: () -> Unit,
  onSaveHistory: (String, String, String) -> Unit
) {
  var voltageInput by remember { mutableStateOf("12") }
  var currentInput by remember { mutableStateOf("") }
  var resistanceInput by remember { mutableStateOf("100") }
  var powerInput by remember { mutableStateOf("") }

  var voltageUnit by remember { mutableStateOf("V") } // "mV", "V", "kV"
  var currentUnit by remember { mutableStateOf("mA") } // "μA", "mA", "A"
  var resistanceUnit by remember { mutableStateOf("Ω") } // "Ω", "kΩ", "MΩ"
  var powerUnit by remember { mutableStateOf("W") } // "mW", "W", "kW"

  val clipboardManager: ClipboardManager = LocalClipboardManager.current
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  // Base unit conversion factors
  fun vFactor(u: String) = when (u) { "mV" -> 0.001; "kV" -> 1000.0; else -> 1.0 }
  fun iFactor(u: String) = when (u) { "μA" -> 0.000001; "mA" -> 0.001; else -> 1.0 }
  fun rFactor(u: String) = when (u) { "kΩ" -> 1000.0; "MΩ" -> 1000000.0; else -> 1.0 }
  fun pFactor(u: String) = when (u) { "mW" -> 0.001; "kW" -> 1000.0; else -> 1.0 }

  val vVal = voltageInput.toDoubleOrNull()?.let { it * vFactor(voltageUnit) }
  val iVal = currentInput.toDoubleOrNull()?.let { it * iFactor(currentUnit) }
  val rVal = resistanceInput.toDoubleOrNull()?.let { it * rFactor(resistanceUnit) }
  val pVal = powerInput.toDoubleOrNull()?.let { it * pFactor(powerUnit) }

  // Count known inputs
  var calculatedV: Double? = null
  var calculatedI: Double? = null
  var calculatedR: Double? = null
  var calculatedP: Double? = null
  var formulaUsed = ""

  if (vVal != null && iVal != null && vVal > 0 && iVal > 0) {
    calculatedV = vVal
    calculatedI = iVal
    calculatedR = vVal / iVal
    calculatedP = vVal * iVal
    formulaUsed = "R = V / I  |  P = V × I"
  } else if (vVal != null && rVal != null && vVal > 0 && rVal > 0) {
    calculatedV = vVal
    calculatedR = rVal
    calculatedI = vVal / rVal
    calculatedP = (vVal * vVal) / rVal
    formulaUsed = "I = V / R  |  P = V² / R"
  } else if (vVal != null && pVal != null && vVal > 0 && pVal > 0) {
    calculatedV = vVal
    calculatedP = pVal
    calculatedI = pVal / vVal
    calculatedR = (vVal * vVal) / pVal
    formulaUsed = "I = P / V  |  R = V² / P"
  } else if (iVal != null && rVal != null && iVal > 0 && rVal > 0) {
    calculatedI = iVal
    calculatedR = rVal
    calculatedV = iVal * rVal
    calculatedP = (iVal * iVal) * rVal
    formulaUsed = "V = I × R  |  P = I² × R"
  } else if (iVal != null && pVal != null && iVal > 0 && pVal > 0) {
    calculatedI = iVal
    calculatedP = pVal
    calculatedV = pVal / iVal
    calculatedR = pVal / (iVal * iVal)
    formulaUsed = "V = P / I  |  R = P / I²"
  } else if (rVal != null && pVal != null && rVal > 0 && pVal > 0) {
    calculatedR = rVal
    calculatedP = pVal
    calculatedV = sqrt(pVal * rVal)
    calculatedI = sqrt(pVal / rVal)
    formulaUsed = "V = √(P × R)  |  I = √(P / R)"
  }

  fun formatUnit(value: Double, unit: String, factor: Double): String {
    val converted = value / factor
    return when {
      converted >= 1000 -> String.format(Locale.US, "%,.2f %s", converted, unit)
      converted >= 1 -> String.format(Locale.US, "%.3f %s", converted, unit)
      else -> String.format(Locale.US, "%.4f %s", converted, unit)
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Ohm's Law & Power", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("ohms_law_back_button")
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(
            onClick = {
              voltageInput = ""
              currentInput = ""
              resistanceInput = ""
              powerInput = ""
            },
            modifier = Modifier.testTag("ohms_law_clear_button")
          ) {
            Icon(Icons.Default.Clear, contentDescription = "Clear All")
          }
          IconButton(
            onClick = {
              if (calculatedV != null && calculatedI != null && calculatedR != null && calculatedP != null) {
                val text = "Ohm's Law: V=${formatUnit(calculatedV, "V", 1.0)}, I=${formatUnit(calculatedI, "A", 1.0)}, R=${formatUnit(calculatedR, "Ω", 1.0)}, P=${formatUnit(calculatedP, "W", 1.0)}"
                clipboardManager.setText(AnnotatedString(text))
                scope.launch { snackbarHostState.showSnackbar("Results copied to clipboard!") }
                onSaveHistory("Ohm's Law", "V=${formatUnit(calculatedV, "V", 1.0)}, R=${formatUnit(calculatedR, "Ω", 1.0)}", "I=${formatUnit(calculatedI, "A", 1.0)}, P=${formatUnit(calculatedP, "W", 1.0)}")
              }
            },
            modifier = Modifier.testTag("ohms_law_copy_button")
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
      // Info Prompt
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Icon(
            Icons.Default.Lightbulb,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary
          )
          Text(
            text = "Enter any 2 parameters. The calculator will automatically solve for the other 2 in real-time.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      // Presets
      Text("Quick Presets:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        SuggestionChip(
          onClick = {
            voltageInput = "5"
            voltageUnit = "V"
            currentInput = "2"
            currentUnit = "A"
            resistanceInput = ""
            powerInput = ""
          },
          label = { Text("5V USB (2A)") },
          colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
          )
        )
        SuggestionChip(
          onClick = {
            voltageInput = "12"
            voltageUnit = "V"
            resistanceInput = "220"
            resistanceUnit = "Ω"
            currentInput = ""
            powerInput = ""
          },
          label = { Text("12V Relay (220Ω)") },
          colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
          )
        )
      }

      // Input 1: Voltage (V)
      ParameterInputField(
        label = "Voltage (V)",
        value = voltageInput,
        onValueChange = { voltageInput = it },
        currentUnit = voltageUnit,
        units = listOf("mV", "V", "kV"),
        onUnitChange = { voltageUnit = it },
        isCalculated = (voltageInput.isEmpty() && calculatedV != null),
        calculatedValue = calculatedV?.let { formatUnit(it, voltageUnit, vFactor(voltageUnit)) },
        tag = "input_voltage"
      )

      // Input 2: Current (I)
      ParameterInputField(
        label = "Current (I)",
        value = currentInput,
        onValueChange = { currentInput = it },
        currentUnit = currentUnit,
        units = listOf("μA", "mA", "A"),
        onUnitChange = { currentUnit = it },
        isCalculated = (currentInput.isEmpty() && calculatedI != null),
        calculatedValue = calculatedI?.let { formatUnit(it, currentUnit, iFactor(currentUnit)) },
        tag = "input_current"
      )

      // Input 3: Resistance (R)
      ParameterInputField(
        label = "Resistance (R)",
        value = resistanceInput,
        onValueChange = { resistanceInput = it },
        currentUnit = resistanceUnit,
        units = listOf("Ω", "kΩ", "MΩ"),
        onUnitChange = { resistanceUnit = it },
        isCalculated = (resistanceInput.isEmpty() && calculatedR != null),
        calculatedValue = calculatedR?.let { formatUnit(it, resistanceUnit, rFactor(resistanceUnit)) },
        tag = "input_resistance"
      )

      // Input 4: Power (P)
      ParameterInputField(
        label = "Power (P)",
        value = powerInput,
        onValueChange = { powerInput = it },
        currentUnit = powerUnit,
        units = listOf("mW", "W", "kW"),
        onUnitChange = { powerUnit = it },
        isCalculated = (powerInput.isEmpty() && calculatedP != null),
        calculatedValue = calculatedP?.let { formatUnit(it, powerUnit, pFactor(powerUnit)) },
        tag = "input_power"
      )

      // Results Overview Card
      if (calculatedV != null && calculatedI != null && calculatedR != null && calculatedP != null) {
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
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "SOLVED PARAMETERS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
              )
              Text(
                text = formulaUsed,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.primary
              )
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text("Voltage (V):", style = MaterialTheme.typography.bodySmall)
                Text(
                  formatUnit(calculatedV, "V", 1.0),
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
              }
              Column(horizontalAlignment = Alignment.End) {
                Text("Current (I):", style = MaterialTheme.typography.bodySmall)
                Text(
                  formatUnit(calculatedI, "A", 1.0),
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
              }
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text("Resistance (R):", style = MaterialTheme.typography.bodySmall)
                Text(
                  formatUnit(calculatedR, "Ω", 1.0),
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
              }
              Column(horizontalAlignment = Alignment.End) {
                Text("Power (P):", style = MaterialTheme.typography.bodySmall)
                Text(
                  formatUnit(calculatedP, "W", 1.0),
                  style = MaterialTheme.typography.titleMedium,
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

@Composable
fun ParameterInputField(
  label: String,
  value: String,
  onValueChange: (String) -> Unit,
  currentUnit: String,
  units: List<String>,
  onUnitChange: (String) -> Unit,
  isCalculated: Boolean,
  calculatedValue: String?,
  tag: String
) {
  var menuExpanded by remember { mutableStateOf(false) }

  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
      if (isCalculated) {
        Text(
          "Auto-Calculated",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.primary,
          fontWeight = FontWeight.Bold
        )
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      OutlinedTextField(
        value = if (isCalculated) (calculatedValue ?: "") else value,
        onValueChange = onValueChange,
        placeholder = { Text(if (isCalculated) calculatedValue ?: "" else "Enter value") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier
          .weight(1f)
          .testTag(tag),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = if (isCalculated) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface,
          unfocusedContainerColor = if (isCalculated) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
        )
      )

      Box {
        Card(
          modifier = Modifier
            .clickable { menuExpanded = true }
            .height(56.dp)
            .width(80.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
          ),
          shape = RoundedCornerShape(8.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxSize()
              .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(currentUnit, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Unit selector")
          }
        }

        DropdownMenu(
          expanded = menuExpanded,
          onDismissRequest = { menuExpanded = false }
        ) {
          units.forEach { unit ->
            DropdownMenuItem(
              text = { Text(unit) },
              onClick = {
                onUnitChange(unit)
                menuExpanded = false
              }
            )
          }
        }
      }
    }
  }
}
