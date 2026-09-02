package com.example.ui.tools

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.util.Locale

// EIA-96 Standard Table
val Eia96Table = mapOf(
  "01" to 100, "02" to 102, "03" to 105, "04" to 107, "05" to 110, "06" to 113, "07" to 115, "08" to 118,
  "09" to 121, "10" to 124, "11" to 127, "12" to 130, "13" to 133, "14" to 137, "15" to 140, "16" to 143,
  "17" to 147, "18" to 150, "19" to 154, "20" to 158, "21" to 162, "22" to 165, "23" to 169, "24" to 174,
  "25" to 178, "26" to 182, "27" to 187, "28" to 191, "29" to 196, "30" to 200, "31" to 205, "32" to 210,
  "33" to 215, "34" to 221, "35" to 226, "36" to 232, "37" to 237, "38" to 243, "39" to 249, "40" to 255,
  "41" to 261, "42" to 267, "43" to 274, "44" to 280, "45" to 287, "46" to 294, "47" to 301, "48" to 309,
  "49" to 316, "50" to 324, "51" to 332, "52" to 340, "53" to 348, "54" to 357, "55" to 365, "56" to 374,
  "57" to 383, "58" to 392, "59" to 402, "60" to 412, "61" to 422, "62" to 432, "63" to 442, "64" to 453,
  "65" to 464, "66" to 475, "67" to 487, "68" to 499, "69" to 511, "70" to 523, "71" to 536, "72" to 549,
  "73" to 562, "74" to 576, "75" to 590, "76" to 604, "77" to 619, "78" to 634, "79" to 649, "80" to 665,
  "81" to 681, "82" to 698, "83" to 715, "84" to 732, "85" to 750, "86" to 768, "87" to 787, "88" to 806,
  "89" to 825, "90" to 845, "91" to 866, "92" to 887, "93" to 909, "94" to 931, "95" to 953, "96" to 976
)

val Eia96Multipliers = mapOf(
  'Z' to 0.001, 'Y' to 0.01, 'R' to 0.01, 'X' to 0.1, 'S' to 0.1,
  'A' to 1.0, 'B' to 10.0, 'H' to 10.0, 'C' to 100.0, 'D' to 1000.0,
  'E' to 10000.0, 'F' to 100000.0
)

@Composable
fun ChipComponentGraphic(
  code: String,
  bodyColor: Color = Color(0xFF1E293B),
  textColor: Color = Color.White,
  tagLabel: String = "SMD"
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .height(130.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    shape = RoundedCornerShape(16.dp)
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height
      val chipW = 160.dp.toPx()
      val chipH = 70.dp.toPx()
      val cx = w / 2f
      val cy = h / 2f
      val left = cx - chipW / 2f
      val top = cy - chipH / 2f

      // PCB Green board background traces
      drawLine(Color(0xFF334155), Offset(left - 30.dp.toPx(), cy), Offset(left, cy), strokeWidth = 8.dp.toPx())
      drawLine(Color(0xFF334155), Offset(left + chipW, cy), Offset(left + chipW + 30.dp.toPx(), cy), strokeWidth = 8.dp.toPx())

      // Solder Terminals (Metallic Silver pads on both sides)
      val termW = 24.dp.toPx()
      val termColor = Color(0xFFCBD5E1)

      // Chip Body
      drawRoundRect(
        color = bodyColor,
        topLeft = Offset(left, top),
        size = Size(chipW, chipH),
        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
      )

      // Left Metal Terminal
      drawRoundRect(
        color = termColor,
        topLeft = Offset(left, top),
        size = Size(termW, chipH),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
      )

      // Right Metal Terminal
      drawRoundRect(
        color = termColor,
        topLeft = Offset(left + chipW - termW, top),
        size = Size(termW, chipH),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
      )

      // Bevel / Highlight on top
      drawLine(
        Color.White.copy(alpha = 0.25f),
        Offset(left + termW + 4.dp.toPx(), top + 4.dp.toPx()),
        Offset(left + chipW - termW - 4.dp.toPx(), top + 4.dp.toPx()),
        strokeWidth = 2.dp.toPx()
      )
    }

    // Display printed code text centered
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text(
        text = if (code.isBlank()) "---" else code.uppercase(),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.ExtraBold,
        fontFamily = FontFamily.Monospace,
        color = textColor,
        letterSpacing = 2.sp
      )
    }
  }
}

// -------------------------------------------------------------
// 1. SMD RESISTOR SCREEN
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmdResistorScreen(
  onBack: () -> Unit,
  onSaveHistory: (String, String, String) -> Unit
) {
  var inputCode by remember { mutableStateOf("472") }
  val clipboardManager: ClipboardManager = LocalClipboardManager.current
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  val code = inputCode.trim().uppercase()

  var resultOhms: Double? = null
  var systemType = "Standard 3-Digit (5%)"
  var tolerance = "±5%"

  when {
    code == "0" || code == "00" || code == "000" || code == "0000" || code == "0R0" -> {
      resultOhms = 0.0
      systemType = "Zero-Ohm Jumper (0Ω)"
      tolerance = "0Ω Shunt"
    }
    code.contains("R") -> {
      // Decimal point, e.g., 4R7 -> 4.7 ohms, R22 -> 0.22 ohms
      val parts = code.split("R")
      val left = parts.getOrNull(0)?.ifEmpty { "0" } ?: "0"
      val right = parts.getOrNull(1)?.ifEmpty { "0" } ?: "0"
      val full = "$left.$right"
      resultOhms = full.toDoubleOrNull()
      systemType = "R-Decimal Notation"
      tolerance = "±1% / ±5%"
    }
    code.length == 3 && code.all { it.isDigit() } -> {
      // 3-digit: first 2 digits * 10^3rd
      val base = code.substring(0, 2).toIntOrNull() ?: 0
      val exp = code[2].digitToIntOrNull() ?: 0
      resultOhms = base * Math.pow(10.0, exp.toDouble())
      systemType = "3-Digit EIA (E24 Series)"
      tolerance = "±5%"
    }
    code.length == 4 && code.all { it.isDigit() } -> {
      // 4-digit: first 3 digits * 10^4th
      val base = code.substring(0, 3).toIntOrNull() ?: 0
      val exp = code[3].digitToIntOrNull() ?: 0
      resultOhms = base * Math.pow(10.0, exp.toDouble())
      systemType = "4-Digit Precision (E96 Series)"
      tolerance = "±1%"
    }
    code.length == 3 && code.substring(0, 2).all { it.isDigit() } && code[2].isLetter() -> {
      // EIA-96 system, e.g. 01A -> 100 * 1 = 100 ohms
      val digits = code.substring(0, 2)
      val multLetter = code[2]
      val base = Eia96Table[digits]
      val mult = Eia96Multipliers[multLetter]
      if (base != null && mult != null) {
        resultOhms = base * mult
        systemType = "EIA-96 1% Precision Code"
        tolerance = "±1%"
      }
    }
  }

  fun formatR(r: Double): String {
    return when {
      r >= 1_000_000 -> String.format(Locale.US, "%.3f MΩ", r / 1_000_000)
      r >= 1_000 -> String.format(Locale.US, "%.2f kΩ", r / 1_000)
      else -> String.format(Locale.US, "%.2f Ω", r)
    }
  }

  val displayValue = if (resultOhms != null) formatR(resultOhms) else "Invalid Code"

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("SMD Resistor Codes", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("smd_r_back_button")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(
            onClick = {
              if (resultOhms != null) {
                val text = "SMD Resistor $code -> $displayValue ($tolerance, $systemType)"
                clipboardManager.setText(AnnotatedString(text))
                scope.launch { snackbarHostState.showSnackbar("Copied: $text") }
                onSaveHistory("SMD Resistor Code", "Code: $code ($systemType)", "$displayValue $tolerance")
              }
            },
            modifier = Modifier.testTag("smd_r_copy_button")
          ) {
            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
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
      ChipComponentGraphic(code = code, bodyColor = Color(0xFF0F172A))

      // Presets
      Text("Common SMD Codes:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf("472", "103", "1002", "4R7", "01A", "01C", "000").forEach { sample ->
          SuggestionChip(
            onClick = { inputCode = sample },
            label = { Text(sample) }
          )
        }
      }

      OutlinedTextField(
        value = inputCode,
        onValueChange = { inputCode = it.take(5) },
        label = { Text("Enter SMD Resistor Code (e.g. 472, 1002, 4R7, 01A)") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_smd_resistor")
      )

      if (resultOhms != null) {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
          shape = RoundedCornerShape(16.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
              "RESISTANCE RESULT",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            Row(
              verticalAlignment = Alignment.Bottom,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Text(
                displayValue,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
              Text(
                tolerance,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 4.dp)
              )
            }
            Text("System: $systemType", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
          }
        }
      }

      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("SMD Resistor Reading Rules:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
          }
          Text("• 3-Digit: 1st two digits are significant figures, 3rd digit is 10ⁿ multiplier (e.g. 472 = 47 × 10² = 4,700Ω = 4.7kΩ).", style = MaterialTheme.typography.bodySmall)
          Text("• 4-Digit: 1st three digits are significant, 4th is multiplier (e.g. 1002 = 100 × 10² = 10,000Ω = 10kΩ 1%).", style = MaterialTheme.typography.bodySmall)
          Text("• R as Decimal: 4R7 = 4.7Ω, 0R22 = 0.22Ω, 000 = 0Ω Jumper.", style = MaterialTheme.typography.bodySmall)
          Text("• EIA-96 (1%): 2-digit lookup code + multiplier letter (e.g. 01 = 100, A = ×1 → 100Ω; 01C = 100 × 100 = 10kΩ).", style = MaterialTheme.typography.bodySmall)
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

// -------------------------------------------------------------
// 2. SMD INDUCTOR SCREEN
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmdInductorScreen(
  onBack: () -> Unit,
  onSaveHistory: (String, String, String) -> Unit
) {
  var inputCode by remember { mutableStateOf("100") }
  val clipboardManager: ClipboardManager = LocalClipboardManager.current
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  val code = inputCode.trim().uppercase()
  var inductanceMicroH: Double? = null
  var note = "Standard 3-Digit μH/nH Code"

  when {
    code.contains("R") -> {
      val parts = code.split("R")
      val left = parts.getOrNull(0)?.ifEmpty { "0" } ?: "0"
      val right = parts.getOrNull(1)?.ifEmpty { "0" } ?: "0"
      inductanceMicroH = "$left.$right".toDoubleOrNull()
      note = "Decimal μH Code"
    }
    code.contains("N") -> {
      val parts = code.split("N")
      val left = parts.getOrNull(0)?.ifEmpty { "0" } ?: "0"
      val right = parts.getOrNull(1)?.ifEmpty { "0" } ?: "0"
      val nH = "$left.$right".toDoubleOrNull()
      if (nH != null) {
        inductanceMicroH = nH / 1000.0
        note = "Decimal nanoHenry (nH) Code"
      }
    }
    code.length == 3 && code.all { it.isDigit() } -> {
      val base = code.substring(0, 2).toIntOrNull() ?: 0
      val exp = code[2].digitToIntOrNull() ?: 0
      inductanceMicroH = base * Math.pow(10.0, exp.toDouble())
      note = "Standard 3-digit Code (in μH)"
    }
  }

  fun formatL(uh: Double): String {
    return when {
      uh >= 1000 -> String.format(Locale.US, "%.2f mH", uh / 1000.0)
      uh < 1.0 -> String.format(Locale.US, "%.1f nH (%.3f μH)", uh * 1000.0, uh)
      else -> String.format(Locale.US, "%.2f μH", uh)
    }
  }

  val displayValue = if (inductanceMicroH != null) formatL(inductanceMicroH) else "Invalid Code"

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("SMD Inductor Code", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("smd_l_back_button")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(
            onClick = {
              if (inductanceMicroH != null) {
                val text = "SMD Inductor $code -> $displayValue"
                clipboardManager.setText(AnnotatedString(text))
                scope.launch { snackbarHostState.showSnackbar("Copied: $text") }
                onSaveHistory("SMD Inductor Code", "Code: $code", displayValue)
              }
            },
            modifier = Modifier.testTag("smd_l_copy_button")
          ) {
            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
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
      ChipComponentGraphic(code = code, bodyColor = Color(0xFF0D9488))

      Text("Common SMD Inductor Presets:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf("100", "220", "101", "4R7", "R22", "4N7").forEach { sample ->
          SuggestionChip(onClick = { inputCode = sample }, label = { Text(sample) })
        }
      }

      OutlinedTextField(
        value = inputCode,
        onValueChange = { inputCode = it.take(5) },
        label = { Text("Enter SMD Inductor Code (e.g. 100, 4R7, 101, 4N7)") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
        modifier = Modifier.fillMaxWidth().testTag("input_smd_inductor")
      )

      if (inductanceMicroH != null) {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
          shape = RoundedCornerShape(16.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("CALCULATED INDUCTANCE", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
            Text(displayValue, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text("Rule: $note", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

// -------------------------------------------------------------
// 3. SMD CAPACITOR SCREEN
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmdCapacitorScreen(
  onBack: () -> Unit,
  onSaveHistory: (String, String, String) -> Unit
) {
  var inputCode by remember { mutableStateOf("104") }
  val clipboardManager: ClipboardManager = LocalClipboardManager.current
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  val code = inputCode.trim().uppercase()
  var capacitancePf: Double? = null
  var tolText = "±10% (Std)"

  when {
    code.length == 3 && code.all { it.isDigit() } -> {
      val base = code.substring(0, 2).toIntOrNull() ?: 0
      val exp = code[2].digitToIntOrNull() ?: 0
      capacitancePf = base * Math.pow(10.0, exp.toDouble())
    }
    code.length == 4 && code.substring(0, 3).all { it.isDigit() } && code[3].isLetter() -> {
      val base = code.substring(0, 2).toIntOrNull() ?: 0
      val exp = code[2].digitToIntOrNull() ?: 0
      capacitancePf = base * Math.pow(10.0, exp.toDouble())
      tolText = when (code[3]) {
        'J' -> "±5%"
        'K' -> "±10%"
        'M' -> "±20%"
        'Z' -> "+80% / -20%"
        else -> "Letter ${code[3]}"
      }
    }
  }

  fun formatC(pf: Double): String {
    return when {
      pf >= 1_000_000 -> String.format(Locale.US, "%.3f μF (%.0f nF)", pf / 1_000_000, pf / 1000)
      pf >= 1_000 -> String.format(Locale.US, "%.2f nF (%.0f pF)", pf / 1000, pf)
      else -> String.format(Locale.US, "%.1f pF", pf)
    }
  }

  val displayValue = if (capacitancePf != null) formatC(capacitancePf) else "Invalid Code"

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("SMD Capacitor Code", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("smd_c_back_button")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(
            onClick = {
              if (capacitancePf != null) {
                val text = "SMD Capacitor $code -> $displayValue ($tolText)"
                clipboardManager.setText(AnnotatedString(text))
                scope.launch { snackbarHostState.showSnackbar("Copied: $text") }
                onSaveHistory("SMD Capacitor Code", "Code: $code", "$displayValue $tolText")
              }
            },
            modifier = Modifier.testTag("smd_c_copy_button")
          ) {
            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
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
      // Ceramic MLCC is typically tan / golden brown
      ChipComponentGraphic(code = code, bodyColor = Color(0xFFB45309), textColor = Color(0xFFFEF3C7))

      Text("Common SMD Capacitor Presets:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf("104", "105", "224", "103K", "472J", "106").forEach { sample ->
          SuggestionChip(onClick = { inputCode = sample }, label = { Text(sample) })
        }
      }

      OutlinedTextField(
        value = inputCode,
        onValueChange = { inputCode = it.take(5) },
        label = { Text("Enter SMD Capacitor Code (e.g. 104, 105, 103K)") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
        modifier = Modifier.fillMaxWidth().testTag("input_smd_capacitor")
      )

      if (capacitancePf != null) {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
          shape = RoundedCornerShape(16.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("CAPACITANCE RESULT", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
            Text(displayValue, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text("Tolerance: $tolText | Base: in pF (Picofarads)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}
