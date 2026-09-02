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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.util.Locale

// -------------------------------------------------------------
// 10. POLYESTER CAPACITOR COLOR CODE (Mullard "Tropical Fish")
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PolyesterCapColorScreen(
  onBack: () -> Unit,
  onSaveHistory: (String, String, String) -> Unit
) {
  var b1Index by remember { mutableIntStateOf(4) } // Yellow = 4
  var b2Index by remember { mutableIntStateOf(7) } // Violet = 7
  var multIndex by remember { mutableIntStateOf(4) } // Yellow = 10,000 pF -> 470,000 pF = 0.47μF
  var tolIndex by remember { mutableIntStateOf(1) } // White = ±10%
  var voltIndex by remember { mutableIntStateOf(1) } // Red = 250V
  var selectedTab by remember { mutableIntStateOf(0) }

  val clipboardManager: ClipboardManager = LocalClipboardManager.current
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  val digitsList = listOf(
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

  val multList = listOf(
    "Black" to (Color(0xFF1E1E1E) to 1.0),
    "Brown" to (Color(0xFF8B4513) to 10.0),
    "Red" to (Color(0xFFDC2626) to 100.0),
    "Orange" to (Color(0xFFF97316) to 1000.0),
    "Yellow" to (Color(0xFFEAB308) to 10000.0),
    "Green" to (Color(0xFF16A34A) to 100000.0)
  )

  val tolList = listOf(
    "White" to (Color(0xFFF3F4F6) to "±10%"),
    "Black" to (Color(0xFF1E1E1E) to "±20%"),
    "Red" to (Color(0xFFDC2626) to "±2%")
  )

  val voltList = listOf(
    "Brown" to (Color(0xFF8B4513) to "100 V DC"),
    "Red" to (Color(0xFFDC2626) to "250 V DC"),
    "Yellow" to (Color(0xFFEAB308) to "400 V DC"),
    "Blue" to (Color(0xFF2563EB) to "630 V DC")
  )

  val d1 = digitsList[b1Index].second.second
  val d2 = digitsList[b2Index].second.second
  val mult = multList[multIndex].second.second
  val pf = (d1 * 10 + d2) * mult
  val tolStr = tolList[tolIndex].second.second
  val voltStr = voltList[voltIndex].second.second

  fun formatCap(pfVal: Double): String {
    return when {
      pfVal >= 1_000_000 -> String.format(Locale.US, "%.2f μF", pfVal / 1_000_000)
      pfVal >= 1_000 -> String.format(Locale.US, "%.1f nF", pfVal / 1_000)
      else -> String.format(Locale.US, "%.0f pF", pfVal)
    }
  }

  val displayVal = "${formatCap(pf)} $tolStr $voltStr"

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Polyester Cap Color Code", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("polyester_back_button")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(
            onClick = {
              clipboardManager.setText(AnnotatedString(displayVal))
              scope.launch { snackbarHostState.showSnackbar("Copied: $displayVal") }
              onSaveHistory("Polyester Cap Code", "Mullard Tropical Fish", displayVal)
            },
            modifier = Modifier.testTag("polyester_copy_button")
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
      // Iconic Tropical Fish graphic
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
          val cx = w / 2f
          val cy = h / 2f
          val bodyW = 160.dp.toPx()
          val bodyH = 50.dp.toPx()
          val left = cx - bodyW / 2f
          val top = cy - bodyH / 2f

          // Radial leads coming out
          drawLine(Color(0xFF94A3B8), Offset(left + 20.dp.toPx(), top + bodyH), Offset(left + 20.dp.toPx(), h - 8.dp.toPx()), strokeWidth = 3.dp.toPx())
          drawLine(Color(0xFF94A3B8), Offset(left + bodyW - 20.dp.toPx(), top + bodyH), Offset(left + bodyW - 20.dp.toPx(), h - 8.dp.toPx()), strokeWidth = 3.dp.toPx())

          // Tropical fish has 5 continuous stripes across its body
          val bandWidth = bodyW / 5f
          val colors = listOf(
            digitsList[b1Index].second.first,
            digitsList[b2Index].second.first,
            multList[multIndex].second.first,
            tolList[tolIndex].second.first,
            voltList[voltIndex].second.first
          )

          colors.forEachIndexed { i, col ->
            drawRect(
              color = col,
              topLeft = Offset(left + i * bandWidth, top),
              size = Size(bandWidth, bodyH)
            )
          }

          // Border outline with rounded corners
          drawRoundRect(
            color = Color.Black.copy(alpha = 0.25f),
            topLeft = Offset(left, top),
            size = Size(bodyW, bodyH),
            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
          )

          // Glossy curve
          drawLine(Color.White.copy(alpha = 0.35f), Offset(left + 10.dp.toPx(), top + 4.dp.toPx()), Offset(left + bodyW - 10.dp.toPx(), top + 4.dp.toPx()), strokeWidth = 3.dp.toPx())
        }
      }

      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(16.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text("CAPACITOR SPECIFICATION", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
          Text(formatCap(pf), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
          Text("Tolerance: $tolStr | Rating: $voltStr", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
        }
      }

      val tabs = listOf("1st Digit", "2nd Digit", "Multiplier", "Tolerance", "Voltage")
      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.primary,
        modifier = Modifier.clip(RoundedCornerShape(12.dp))
      ) {
        tabs.forEachIndexed { i, title ->
          Tab(
            selected = selectedTab == i,
            onClick = { selectedTab = i },
            text = { Text(title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
          )
        }
      }

      // Color picker for active tab
      when (selectedTab) {
        0, 1 -> {
          val activeIndex = if (selectedTab == 0) b1Index else b2Index
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            digitsList.forEachIndexed { i, (name, pair) ->
              val (color, digit) = pair
              val isSelected = (i == activeIndex)
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                  .border(2.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(10.dp))
                  .clickable { if (selectedTab == 0) b1Index = i else b2Index = i }
                  .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                  Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(color).border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape))
                  Text(name, fontWeight = FontWeight.Medium)
                }
                Text("Digit: $digit", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
              }
            }
          }
        }
        2 -> {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            multList.forEachIndexed { i, (name, pair) ->
              val (color, multVal) = pair
              val isSelected = (i == multIndex)
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                  .border(2.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(10.dp))
                  .clickable { multIndex = i }
                  .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                  Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(color).border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape))
                  Text(name, fontWeight = FontWeight.Medium)
                }
                Text("×${multVal.toLong()} pF", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
              }
            }
          }
        }
        3 -> {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            tolList.forEachIndexed { i, (name, pair) ->
              val (color, tolVal) = pair
              val isSelected = (i == tolIndex)
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                  .border(2.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(10.dp))
                  .clickable { tolIndex = i }
                  .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                  Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(color).border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape))
                  Text(name, fontWeight = FontWeight.Medium)
                }
                Text(tolVal, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
              }
            }
          }
        }
        4 -> {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            voltList.forEachIndexed { i, (name, pair) ->
              val (color, vVal) = pair
              val isSelected = (i == voltIndex)
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                  .border(2.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(10.dp))
                  .clickable { voltIndex = i }
                  .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                  Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(color).border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape))
                  Text(name, fontWeight = FontWeight.Medium)
                }
                Text(vVal, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

// -------------------------------------------------------------
// 11. FILM CAPACITOR CODE (Box / Axial Markings)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilmCapacitorCodeScreen(
  onBack: () -> Unit,
  onSaveHistory: (String, String, String) -> Unit
) {
  var valueCode by remember { mutableStateOf("224") }
  var tolLetter by remember { mutableStateOf("K") }
  var voltageCode by remember { mutableStateOf("400V") }
  var dielectricType by remember { mutableStateOf("MKT") }

  val clipboardManager: ClipboardManager = LocalClipboardManager.current
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  val dielectricDescriptions = mapOf(
    "MKT" to "Metallized Polyester (PET) - General purpose, compact, self-healing.",
    "MKP" to "Metallized Polypropylene (PP) - Very low ESR & loss, high ripple current, pulsed RF circuits.",
    "MKS" to "Subminiature Polyester - High density boards, decoupling.",
    "MKC" to "Metallized Polycarbonate - High temperature stability."
  )

  var pf: Double? = null
  if (valueCode.length == 3 && valueCode.all { it.isDigit() }) {
    val b = valueCode.substring(0, 2).toIntOrNull() ?: 0
    val exp = valueCode[2].digitToIntOrNull() ?: 0
    pf = b * Math.pow(10.0, exp.toDouble())
  }

  val tolString = when (tolLetter) {
    "J" -> "±5%"
    "K" -> "±10%"
    "M" -> "±20%"
    else -> "±10%"
  }

  fun formatFilmCap(pfVal: Double): String {
    return when {
      pfVal >= 1_000_000 -> String.format(Locale.US, "%.3f μF", pfVal / 1_000_000)
      pfVal >= 1_000 -> String.format(Locale.US, "%.1f nF", pfVal / 1000)
      else -> String.format(Locale.US, "%.0f pF", pfVal)
    }
  }

  val displayCap = if (pf != null) formatFilmCap(pf) else "Invalid Code"

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Film Capacitor Code", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("film_back_button")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(
            onClick = {
              val text = "Film Cap: $displayCap $tolString $voltageCode $dielectricType"
              clipboardManager.setText(AnnotatedString(text))
              scope.launch { snackbarHostState.showSnackbar("Copied: $text") }
              onSaveHistory("Film Capacitor Code", "$dielectricType $valueCode$tolLetter $voltageCode", displayCap)
            },
            modifier = Modifier.testTag("film_copy_button")
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
      // Box Capacitor Graphic
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
          val cy = h / 2f - 8.dp.toPx()
          val boxW = 150.dp.toPx()
          val boxH = 65.dp.toPx()
          val left = cx - boxW / 2f
          val top = cy - boxH / 2f

          // Wire pins down into PCB
          val leadColor = Color(0xFF94A3B8)
          drawLine(leadColor, Offset(left + 24.dp.toPx(), top + boxH), Offset(left + 24.dp.toPx(), h - 8.dp.toPx()), strokeWidth = 3.dp.toPx())
          drawLine(leadColor, Offset(left + boxW - 24.dp.toPx(), top + boxH), Offset(left + boxW - 24.dp.toPx(), h - 8.dp.toPx()), strokeWidth = 3.dp.toPx())

          // Box Film body (WIMA red or Epcos blue/grey)
          val boxColor = if (dielectricType == "MKP") Color(0xFF1D4ED8) else Color(0xFFDC2626)
          drawRoundRect(
            color = boxColor,
            topLeft = Offset(left, top),
            size = Size(boxW, boxH),
            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
          )
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              "$valueCode$tolLetter $voltageCode",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.Monospace,
              color = Color.White
            )
            Text(
              dielectricType,
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = Color.White.copy(alpha = 0.9f)
            )
          }
        }
      }

      // Result Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(16.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text("CAPACITOR SPECIFICATIONS", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
          Text(displayCap, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
          Text("Tolerance: $tolString | Voltage: $voltageCode", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
          Text(dielectricDescriptions[dielectricType] ?: "", style = MaterialTheme.typography.bodySmall)
        }
      }

      // Presets
      Text("Capacitance Presets:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
      Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf("104" to "0.1μF", "224" to "0.22μF", "474" to "0.47μF", "105" to "1.0μF", "473" to "47nF", "103" to "10nF").forEach { (code, lbl) ->
          SuggestionChip(onClick = { valueCode = code }, label = { Text("$code ($lbl)") })
        }
      }

      Text("Dielectric Family:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
      Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf("MKT", "MKP", "MKS", "MKC").forEach { type ->
          FilterChip(
            selected = dielectricType == type,
            onClick = { dielectricType = type },
            label = { Text(type) },
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer)
          )
        }
      }

      Text("Rated Voltage:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
      Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf("63V", "100V", "250V", "400V", "630V", "1000V").forEach { v ->
          FilterChip(
            selected = voltageCode == v,
            onClick = { voltageCode = v },
            label = { Text(v) },
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer)
          )
        }
      }

      Text("Tolerance Code:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
      Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf("J" to "±5%", "K" to "±10%", "M" to "±20%").forEach { (code, tol) ->
          FilterChip(
            selected = tolLetter == code,
            onClick = { tolLetter = code },
            label = { Text("$code ($tol)") },
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer)
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}
