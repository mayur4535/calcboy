package com.example.ui.tools

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
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

@Composable
fun DiscCapacitorGraphic(
  stampText: String,
  topDotColor: Color? = null,
  voltageStripeColor: Color? = null,
  discColor: Color = Color(0xFFD97706) // Ochre / Tan ceramic disc
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .height(140.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    shape = RoundedCornerShape(16.dp)
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height
      val cx = w / 2f
      val cy = h / 2f - 10.dp.toPx()
      val radius = 42.dp.toPx()

      // Wire leads coming down
      val leadColor = Color(0xFF94A3B8)
      val leadSpacing = 24.dp.toPx()
      drawLine(leadColor, Offset(cx - leadSpacing / 2f, cy + radius - 6.dp.toPx()), Offset(cx - leadSpacing / 2f, h - 10.dp.toPx()), strokeWidth = 3.dp.toPx())
      drawLine(leadColor, Offset(cx + leadSpacing / 2f, cy + radius - 6.dp.toPx()), Offset(cx + leadSpacing / 2f, h - 10.dp.toPx()), strokeWidth = 3.dp.toPx())

      // Disc Ceramic Body
      drawCircle(color = discColor, radius = radius, center = Offset(cx, cy))

      // Top color dot (for Temp Coeff or 1st identifier)
      if (topDotColor != null) {
        drawCircle(color = topDotColor, radius = 9.dp.toPx(), center = Offset(cx, cy - radius + 10.dp.toPx()))
      }

      // Voltage stripe or dot (if provided)
      if (voltageStripeColor != null) {
        drawLine(
          color = voltageStripeColor,
          start = Offset(cx - radius * 0.7f, cy + radius * 0.4f),
          end = Offset(cx + radius * 0.7f, cy + radius * 0.4f),
          strokeWidth = 6.dp.toPx()
        )
      }

      // Disc highlight edge
      drawCircle(color = Color.White.copy(alpha = 0.2f), radius = radius - 3.dp.toPx(), center = Offset(cx - 2.dp.toPx(), cy - 2.dp.toPx()))
    }

    // Text stamped on disc
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text(
        text = stampText,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Black,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFF1E1B4B),
        modifier = Modifier.padding(bottom = 20.dp)
      )
    }
  }
}

// -------------------------------------------------------------
// 7. CERAMIC CAPACITOR CODE (3-Digit Stamp)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CeramicCapacitorCodeScreen(
  onBack: () -> Unit,
  onSaveHistory: (String, String, String) -> Unit
) {
  var codeInput by remember { mutableStateOf("104") }
  var selectedTol by remember { mutableStateOf("K (±10%)") }

  val clipboardManager: ClipboardManager = LocalClipboardManager.current
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  val code = codeInput.trim()
  var pfVal: Double? = null

  if (code.length == 3 && code.all { it.isDigit() }) {
    val b = code.substring(0, 2).toIntOrNull() ?: 0
    val exp = code[2].digitToIntOrNull() ?: 0
    pfVal = b * Math.pow(10.0, exp.toDouble())
  } else if (code.length in 1..2 && code.all { it.isDigit() }) {
    pfVal = code.toDoubleOrNull()
  }

  fun formatC(pf: Double): String {
    return when {
      pf >= 1_000_000 -> String.format(Locale.US, "%.3f μF (%.0f nF)", pf / 1_000_000, pf / 1000)
      pf >= 1_000 -> String.format(Locale.US, "%.2f nF (%.0f pF)", pf / 1000, pf)
      else -> String.format(Locale.US, "%.1f pF", pf)
    }
  }

  val displayVal = if (pfVal != null) formatC(pfVal) else "Invalid Code"

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Ceramic Capacitor Code", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("ceramic_back_button")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(
            onClick = {
              if (pfVal != null) {
                val text = "Ceramic Cap: $code -> $displayVal $selectedTol"
                clipboardManager.setText(AnnotatedString(text))
                scope.launch { snackbarHostState.showSnackbar("Copied: $text") }
                onSaveHistory("Ceramic Capacitor Code", "Stamp: $code", "$displayVal $selectedTol")
              }
            },
            modifier = Modifier.testTag("ceramic_copy_button")
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
      DiscCapacitorGraphic(stampText = "$code\n${selectedTol.take(1)}")

      Text("Common Values:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
      Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf("104", "103", "223", "472", "102", "101", "22").forEach { sample ->
          SuggestionChip(onClick = { codeInput = sample }, label = { Text(sample) })
        }
      }

      OutlinedTextField(
        value = codeInput,
        onValueChange = { codeInput = it.take(3) },
        label = { Text("3-Digit Code (e.g. 104 = 0.1μF)") },
        modifier = Modifier.fillMaxWidth().testTag("input_ceramic_code"),
        singleLine = true
      )

      Text("Tolerance Code Letter:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
      Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf("J (±5%)", "K (±10%)", "M (±20%)", "Z (+80/-20%)").forEach { tol ->
          FilterChip(
            selected = selectedTol == tol,
            onClick = { selectedTol = tol },
            label = { Text(tol) },
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer)
          )
        }
      }

      if (pfVal != null) {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
          shape = RoundedCornerShape(16.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("CAPACITANCE VALUE", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
            Text(displayVal, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text("Tolerance: $selectedTol | Raw: ${String.format(Locale.US, "%.0f", pfVal)} pF", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

// -------------------------------------------------------------
// 8. CERAMIC CAPACITOR COLOR CODE WITH VOLTAGE
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CeramicCapVoltageScreen(
  onBack: () -> Unit,
  onSaveHistory: (String, String, String) -> Unit
) {
  var d1Index by remember { mutableIntStateOf(1) } // Brown = 1
  var d2Index by remember { mutableIntStateOf(0) } // Black = 0
  var multIndex by remember { mutableIntStateOf(3) } // Orange = 1000 -> 10,000 pF (10nF)
  var tolIndex by remember { mutableIntStateOf(1) } // Brown = ±10%
  var voltIndex by remember { mutableIntStateOf(2) } // Red = 250V / Yellow = 400V

  val clipboardManager: ClipboardManager = LocalClipboardManager.current
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  val voltageOptions = listOf(
    "Brown" to (Color(0xFF8B4513) to "100 V DC"),
    "Red" to (Color(0xFFDC2626) to "250 V DC"),
    "Yellow" to (Color(0xFFEAB308) to "400 V DC"),
    "Blue" to (Color(0xFF2563EB) to "630 V DC"),
    "Violet" to (Color(0xFF9333EA) to "1000 V (1 kV) DC")
  )

  val digits = listOf(
    "Black" to (Color(0xFF1E1E1E) to 0),
    "Brown" to (Color(0xFF8B4513) to 1),
    "Red" to (Color(0xFFDC2626) to 2),
    "Orange" to (Color(0xFFF97316) to 3),
    "Yellow" to (Color(0xFFEAB308) to 4),
    "Green" to (Color(0xFF16A34A) to 5),
    "Blue" to (Color(0xFF2563EB) to 6),
    "Violet" to (Color(0xFF9333EA) to 7),
    "Gray" to (Color(0xFF6B7280) to 8),
    "White" to (Color(0xFFF3F4F6) to 9)
  )

  val d1 = digits[d1Index].second.second
  val d2 = digits[d2Index].second.second
  val mult = Math.pow(10.0, multIndex.toDouble())
  val pfVal = (d1 * 10 + d2) * mult
  val voltString = voltageOptions[voltIndex].second.second
  val voltColor = voltageOptions[voltIndex].second.first

  fun formatC(pf: Double): String {
    return when {
      pf >= 1_000_000 -> String.format(Locale.US, "%.3f μF", pf / 1_000_000)
      pf >= 1_000 -> String.format(Locale.US, "%.2f nF", pf / 1000)
      else -> String.format(Locale.US, "%.1f pF", pf)
    }
  }

  val displayVal = "${formatC(pfVal)} @ $voltString"

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Ceramic Cap with Voltage", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("ceramic_volt_back_button")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(
            onClick = {
              val text = "Ceramic Cap: $displayVal"
              clipboardManager.setText(AnnotatedString(text))
              scope.launch { snackbarHostState.showSnackbar("Copied: $text") }
              onSaveHistory("Ceramic Cap with Voltage", "D1/D2/Mult/Volt", displayVal)
            },
            modifier = Modifier.testTag("ceramic_volt_copy_button")
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
      DiscCapacitorGraphic(
        stampText = "${d1}${d2}×10^$multIndex",
        voltageStripeColor = voltColor
      )

      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(16.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text("CAPACITANCE & VOLTAGE RATING", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
          Text(formatC(pfVal), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
          Text("Rated Working Voltage: $voltString", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
        }
      }

      Text("Select Voltage Band / Stripe Color:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
      Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        voltageOptions.forEachIndexed { i, opt ->
          FilterChip(
            selected = voltIndex == i,
            onClick = { voltIndex = i },
            label = { Text("${opt.first} (${opt.second.second})") },
            leadingIcon = {
              Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(opt.second.first))
            },
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer)
          )
        }
      }

      Text("1st & 2nd Significant Digit:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
      Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf(10 to "10", 22 to "22", 33 to "33", 47 to "47", 68 to "68", 15 to "15").forEach { (valPair, label) ->
          SuggestionChip(
            onClick = {
              d1Index = valPair / 10
              d2Index = valPair % 10
            },
            label = { Text(label) }
          )
        }
      }

      Text("Multiplier (Power of 10):", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
      Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf(0 to "×1 (pF)", 1 to "×10", 2 to "×100", 3 to "×1k (nF)", 4 to "×10k", 5 to "×100k").forEach { (m, lbl) ->
          FilterChip(
            selected = multIndex == m,
            onClick = { multIndex = m },
            label = { Text(lbl) },
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer)
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

// -------------------------------------------------------------
// 9. CERAMIC CAPACITOR COLOR CODE WITH TEMPERATURE COEFFICIENT
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CeramicCapTempCoeffScreen(
  onBack: () -> Unit,
  onSaveHistory: (String, String, String) -> Unit
) {
  var selectedIndex by remember { mutableIntStateOf(0) }
  val clipboardManager: ClipboardManager = LocalClipboardManager.current
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  val tempCoeffs = listOf(
    TempCoeff("Black", Color(0xFF1E1E1E), "C0G / NP0", "0 ± 30 ppm/°C", "Ultra-stable zero drift. Ideal for RF filters & oscillators."),
    TempCoeff("Brown", Color(0xFF8B4513), "N080", "-80 ± 30 ppm/°C", "Compensates slight positive temperature drift in coils."),
    TempCoeff("Red", Color(0xFFDC2626), "N150", "-150 ± 30 ppm/°C", "Negative drift for tuning LC circuits."),
    TempCoeff("Orange", Color(0xFFF97316), "N220", "-220 ± 60 ppm/°C", "Medium negative drift."),
    TempCoeff("Yellow", Color(0xFFEAB308), "N330", "-330 ± 60 ppm/°C", "Standard temperature compensation."),
    TempCoeff("Green", Color(0xFF16A34A), "N470", "-470 ± 60 ppm/°C", "High negative temperature coefficient."),
    TempCoeff("Violet", Color(0xFF9333EA), "N750", "-750 ± 120 ppm/°C", "Maximum negative drift compensation."),
    TempCoeff("Gray", Color(0xFF6B7280), "P100", "+100 ± 30 ppm/°C", "Positive drift characteristic."),
    TempCoeff("White", Color(0xFFF3F4F6), "SL / GP", "+350 to -1000 ppm/°C", "General purpose ceramic bypass.")
  )

  val item = tempCoeffs[selectedIndex]

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Ceramic Cap Temp Coeff", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("ceramic_tc_back_button")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(
            onClick = {
              val text = "Ceramic Temp Coeff (${item.colorName}): ${item.eiaCode} -> ${item.ppmDrift}"
              clipboardManager.setText(AnnotatedString(text))
              scope.launch { snackbarHostState.showSnackbar("Copied: $text") }
              onSaveHistory("Ceramic Cap Temp Coeff", item.colorName, "${item.eiaCode}: ${item.ppmDrift}")
            },
            modifier = Modifier.testTag("ceramic_tc_copy_button")
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
      DiscCapacitorGraphic(
        stampText = item.eiaCode,
        topDotColor = item.dotColor
      )

      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(16.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("TEMPERATURE COEFFICIENT METRIC", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
          Text(item.eiaCode, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
          Text("Drift: ${item.ppmDrift}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
          Text(item.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f))
        }
      }

      Text("Select Top Dot / Band Color:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        tempCoeffs.forEachIndexed { i, tc ->
          val isSelected = (i == selectedIndex)
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
              .border(2.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(10.dp))
              .clickable { selectedIndex = i }
              .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
              Box(
                modifier = Modifier
                  .size(26.dp)
                  .clip(CircleShape)
                  .background(tc.dotColor)
                  .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
              )
              Column {
                Text(tc.colorName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text(tc.eiaCode, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
            Text(tc.ppmDrift, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary)
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

data class TempCoeff(
  val colorName: String,
  val dotColor: Color,
  val eiaCode: String,
  val ppmDrift: String,
  val description: String
)
