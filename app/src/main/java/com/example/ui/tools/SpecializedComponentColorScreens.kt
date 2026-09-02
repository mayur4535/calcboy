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
// 13. TANTALUM CAPACITOR COLOR CODE
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TantalumCapColorScreen(
  onBack: () -> Unit,
  onSaveHistory: (String, String, String) -> Unit
) {
  var d1Index by remember { mutableIntStateOf(4) } // Yellow = 4
  var d2Index by remember { mutableIntStateOf(7) } // Violet = 7
  var multIndex by remember { mutableIntStateOf(5) } // Green = 10^5 pF = 0.1μF -> 4.7μF
  var voltIndex by remember { mutableIntStateOf(2) } // Black = 10V
  var selectedTab by remember { mutableIntStateOf(0) }

  val clipboardManager: ClipboardManager = LocalClipboardManager.current
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  val colors = listOf(
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

  val voltRatings = listOf(
    "Yellow" to (Color(0xFFEAB308) to "6.3 V"),
    "Black" to (Color(0xFF1E1E1E) to "10 V"),
    "Green" to (Color(0xFF16A34A) to "16 V"),
    "White" to (Color(0xFFF3F4F6) to "25 V"),
    "Gray" to (Color(0xFF6B7280) to "50 V"),
    "Pink" to (Color(0xFFEC4899) to "35 V")
  )

  val d1 = colors[d1Index].second.second
  val d2 = colors[d2Index].second.second
  val mult = Math.pow(10.0, multIndex.toDouble())
  val totalPf = (d1 * 10 + d2) * mult
  val volt = voltRatings[voltIndex].second.second
  val voltCol = voltRatings[voltIndex].second.first

  fun formatTantalum(pf: Double): String {
    val uf = pf / 1_000_000.0
    return if (uf >= 1.0) String.format(Locale.US, "%.1f μF", uf) else String.format(Locale.US, "%.2f μF (%.0f nF)", uf, pf / 1000)
  }

  val displayVal = "${formatTantalum(totalPf)} @ $volt"

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Tantalum Cap Color Code", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("tantalum_back_button")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(
            onClick = {
              clipboardManager.setText(AnnotatedString(displayVal))
              scope.launch { snackbarHostState.showSnackbar("Copied: $displayVal") }
              onSaveHistory("Tantalum Cap Code", "Dipped Bead", displayVal)
            },
            modifier = Modifier.testTag("tantalum_copy_button")
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
      // Bead Tantalum Canvas
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

          // Radial leads
          val leadColor = Color(0xFF94A3B8)
          drawLine(leadColor, Offset(cx - 14.dp.toPx(), cy + 24.dp.toPx()), Offset(cx - 14.dp.toPx(), h - 8.dp.toPx()), strokeWidth = 3.dp.toPx())
          drawLine(leadColor, Offset(cx + 14.dp.toPx(), cy + 24.dp.toPx()), Offset(cx + 14.dp.toPx(), h - 8.dp.toPx()), strokeWidth = 3.dp.toPx())

          // Teardrop Bead body (Orange / Ochre base)
          drawCircle(color = Color(0xFFF97316), radius = 32.dp.toPx(), center = Offset(cx, cy))

          // Top color spot (Digit 1)
          drawCircle(color = colors[d1Index].second.first, radius = 10.dp.toPx(), center = Offset(cx, cy - 20.dp.toPx()))

          // Shoulder color spot (Digit 2)
          drawCircle(color = colors[d2Index].second.first, radius = 9.dp.toPx(), center = Offset(cx + 16.dp.toPx(), cy - 6.dp.toPx()))

          // Center spot (Multiplier)
          drawCircle(color = colors[multIndex].second.first, radius = 9.dp.toPx(), center = Offset(cx, cy))

          // Voltage dot (bottom)
          drawCircle(color = voltCol, radius = 8.dp.toPx(), center = Offset(cx, cy + 18.dp.toPx()))
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
          Text("+ (Anode Lead on Left)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 6.dp))
        }
      }

      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(16.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text("TANTALUM SPECIFICATION", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
          Text(formatTantalum(totalPf), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
          Text("Rated Working Voltage: $volt", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
        }
      }

      val tabs = listOf("Top (1st Digit)", "Shoulder (2nd)", "Multiplier Dot", "Voltage Spot")
      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.primary,
        modifier = Modifier.clip(RoundedCornerShape(12.dp))
      ) {
        tabs.forEachIndexed { i, title ->
          Tab(selected = selectedTab == i, onClick = { selectedTab = i }, text = { Text(title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) })
        }
      }

      if (selectedTab < 3) {
        val activeIndex = when (selectedTab) {
          0 -> d1Index
          1 -> d2Index
          else -> multIndex
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          colors.forEachIndexed { i, (name, pair) ->
            val (color, digit) = pair
            val isSelected = (i == activeIndex)
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .border(2.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(10.dp))
                .clickable {
                  when (selectedTab) {
                    0 -> d1Index = i
                    1 -> d2Index = i
                    else -> multIndex = i
                  }
                }
                .padding(10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(color).border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape))
                Text(name, fontWeight = FontWeight.Medium)
              }
              Text(if (selectedTab == 2) "×10^$digit pF" else "Digit: $digit", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
          }
        }
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          voltRatings.forEachIndexed { i, (name, pair) ->
            val (color, vStr) = pair
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
              Text(vStr, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

// -------------------------------------------------------------
// 14. MICA CAPACITOR COLOR CODE (MIL-C-5 / EIA 6-Dot)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MicaCapacitorColorScreen(
  onBack: () -> Unit,
  onSaveHistory: (String, String, String) -> Unit
) {
  var d1Index by remember { mutableIntStateOf(1) } // Brown = 1
  var d2Index by remember { mutableIntStateOf(0) } // Black = 0
  var multIndex by remember { mutableIntStateOf(2) } // Red = 100 -> 1000 pF (1nF)
  var tolIndex by remember { mutableIntStateOf(2) } // Gold = ±5%
  var charIndex by remember { mutableIntStateOf(3) } // D class
  var selectedTab by remember { mutableIntStateOf(0) }

  val clipboardManager: ClipboardManager = LocalClipboardManager.current
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  val colors = listOf(
    "Black" to (Color(0xFF1E1E1E) to 0),
    "Brown" to (Color(0xFF8B4513) to 1),
    "Red" to (Color(0xFFDC2626) to 2),
    "Orange" to (Color(0xFFF97316) to 3),
    "Yellow" to (Color(0xFFEAB308) to 4),
    "Green" to (Color(0xFF16A34A) to 5),
    "Blue" to (Color(0xFF2563EB) to 6),
    "Violet" to (Color(0xFF9333EA) to 7),
    "Gold" to (Color(0xFFD97706) to -1),
    "Silver" to (Color(0xFF9CA3AF) to -2)
  )

  val tols = listOf(
    "Brown" to "±1%",
    "Red" to "±2%",
    "Gold" to "±5%",
    "Silver" to "±10%",
    "Black" to "±20%"
  )

  val characteristics = listOf(
    "Black (A)" to "±1000 ppm/°C",
    "Brown (B)" to "±500 ppm/°C",
    "Red (C)" to "±200 ppm/°C",
    "Orange (D)" to "±100 ppm/°C",
    "Yellow (E)" to "-20 to +100 ppm/°C",
    "Green (F)" to "0 to +70 ppm/°C (Precision)"
  )

  val d1 = colors[d1Index].second.second
  val d2 = colors[d2Index].second.second
  val mult = Math.pow(10.0, multIndex.toDouble())
  val totalPf = (d1 * 10 + d2) * mult
  val tolStr = tols[tolIndex].second
  val charStr = characteristics[charIndex]

  fun formatMica(pf: Double): String {
    return when {
      pf >= 1_000_000 -> String.format(Locale.US, "%.3f μF", pf / 1_000_000)
      pf >= 1_000 -> String.format(Locale.US, "%.2f nF", pf / 1000)
      else -> String.format(Locale.US, "%.0f pF", pf)
    }
  }

  val displayVal = "${formatMica(totalPf)} $tolStr ($charStr)"

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Mica Capacitor 6-Dot Code", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("mica_back_button")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(
            onClick = {
              clipboardManager.setText(AnnotatedString(displayVal))
              scope.launch { snackbarHostState.showSnackbar("Copied: $displayVal") }
              onSaveHistory("Mica Capacitor Code", "MIL-C-5 6-Dot", displayVal)
            },
            modifier = Modifier.testTag("mica_copy_button")
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
      // 6-Dot Mica Canvas
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
          val cy = h / 2f
          val bodyW = 160.dp.toPx()
          val bodyH = 75.dp.toPx()
          val left = cx - bodyW / 2f
          val top = cy - bodyH / 2f

          // Molded Mica Bakelite Dark Body
          drawRoundRect(
            color = Color(0xFF451A03), // Deep brown phenolic resin
            topLeft = Offset(left, top),
            size = Size(bodyW, bodyH),
            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
          )

          // 6 Dots in 2 rows of 3
          // Top row: Dot 1 (White = EIA), Dot 2 (1st digit), Dot 3 (2nd digit)
          // Bottom row: Dot 6 (Char), Dot 5 (Tolerance), Dot 4 (Multiplier)
          val col1 = left + 30.dp.toPx()
          val col2 = left + 80.dp.toPx()
          val col3 = left + 130.dp.toPx()
          val row1 = top + 22.dp.toPx()
          val row2 = top + 52.dp.toPx()
          val r = 7.dp.toPx()

          drawCircle(Color.White, r, Offset(col1, row1)) // Dot 1: EIA White
          drawCircle(colors[d1Index].second.first, r, Offset(col2, row1)) // Dot 2: 1st digit
          drawCircle(colors[d2Index].second.first, r, Offset(col3, row1)) // Dot 3: 2nd digit

          drawCircle(Color(0xFFD97706), r, Offset(col1, row2)) // Dot 6: Characteristic
          drawCircle(Color(0xFFEAB308), r, Offset(col2, row2)) // Dot 5: Tolerance
          drawCircle(colors[multIndex].second.first, r, Offset(col3, row2)) // Dot 4: Multiplier
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
          Text("Top: [EIA White] [1st Digit] [2nd Digit]  |  Bottom: [Char] [Tol] [Mult]", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f), modifier = Modifier.padding(bottom = 4.dp))
        }
      }

      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(16.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text("CAPACITANCE VALUE", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
          Text(formatMica(totalPf), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
          Text("Tolerance: $tolStr | Stability: $charStr", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
        }
      }

      val tabs = listOf("1st Digit ($d1)", "2nd Digit ($d2)", "Multiplier")
      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.primary,
        modifier = Modifier.clip(RoundedCornerShape(12.dp))
      ) {
        tabs.forEachIndexed { i, title ->
          Tab(selected = selectedTab == i, onClick = { selectedTab = i }, text = { Text(title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) })
        }
      }

      val activeIndex = when (selectedTab) {
        0 -> d1Index
        1 -> d2Index
        else -> multIndex
      }

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        colors.take(10).forEachIndexed { i, (name, pair) ->
          val (color, digit) = pair
          val isSelected = (i == activeIndex)
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
              .border(2.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(10.dp))
              .clickable {
                when (selectedTab) {
                  0 -> d1Index = i
                  1 -> d2Index = i
                  else -> multIndex = i
                }
              }
              .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(color).border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape))
              Text(name, fontWeight = FontWeight.Medium)
            }
            Text("Value: $digit", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

// -------------------------------------------------------------
// 15. FUSE COLOR CODE (Automotive Blade & Mini)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuseColorCodeScreen(
  onBack: () -> Unit,
  onSaveHistory: (String, String, String) -> Unit
) {
  var selectedIndex by remember { mutableIntStateOf(6) } // Red 10A standard default

  val clipboardManager: ClipboardManager = LocalClipboardManager.current
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  val fuseList = listOf(
    FuseInfo("1A", "Black", Color(0xFF1E1E1E), "32V DC", "Micro low-current instrumentation"),
    FuseInfo("2A", "Gray", Color(0xFF6B7280), "32V DC", "Small sensors & logic controls"),
    FuseInfo("3A", "Violet", Color(0xFF9333EA), "32V DC", "ECU memory & dash electronics"),
    FuseInfo("4A", "Pink", Color(0xFFEC4899), "32V DC", "Specialized automotive modules"),
    FuseInfo("5A", "Tan", Color(0xFFD97706), "32V DC", "Airbags, instrument cluster, radio illumination"),
    FuseInfo("7.5A", "Brown", Color(0xFF78350F), "32V DC", "OBD-II, steering angle sensor, immobilizer"),
    FuseInfo("10A", "Red", Color(0xFFDC2626), "32V DC", "Headlights, tail lamps, turn signals, audio"),
    FuseInfo("15A", "Blue", Color(0xFF2563EB), "32V DC", "Brake lights, fuel pump, horn, cigarette lighter"),
    FuseInfo("20A", "Yellow", Color(0xFFEAB308), "32V DC", "Wiper motor, power windows, seat heater"),
    FuseInfo("25A", "Clear / White", Color(0xFFF1F5F9), "32V DC", "Cooling fan, sunroof, amplifier"),
    FuseInfo("30A", "Green", Color(0xFF16A34A), "32V DC", "Air conditioner compressor, blower motor, ABS"),
    FuseInfo("35A", "Blue-Green", Color(0xFF0D9488), "32V DC", "High-current auxiliary circuits"),
    FuseInfo("40A", "Amber / Orange", Color(0xFFEA580C), "32V DC", "Electric power steering, radiator fans")
  )

  val current = fuseList[selectedIndex]

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Fuse Color Code", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("fuse_back_button")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(
            onClick = {
              val text = "Fuse: ${current.rating} (${current.colorName}) - ${current.useCase}"
              clipboardManager.setText(AnnotatedString(text))
              scope.launch { snackbarHostState.showSnackbar("Copied: $text") }
              onSaveHistory("Fuse Color Code", "${current.rating} (${current.colorName})", current.useCase)
            },
            modifier = Modifier.testTag("fuse_copy_button")
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
      // Automotive Blade Fuse Graphic
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
          val cy = h / 2f - 10.dp.toPx()

          val bodyW = 100.dp.toPx()
          val bodyH = 65.dp.toPx()
          val left = cx - bodyW / 2f
          val top = cy - bodyH / 2f

          // Two silver metal blade prongs sticking out of the bottom
          val prongColor = Color(0xFFCBD5E1)
          val prongW = 14.dp.toPx()
          val prongL = 30.dp.toPx()
          drawRect(prongColor, Offset(left + 16.dp.toPx(), top + bodyH), Size(prongW, prongL))
          drawRect(prongColor, Offset(left + bodyW - 16.dp.toPx() - prongW, top + bodyH), Size(prongW, prongL))

          // Plastic colored fuse body
          drawRoundRect(
            color = current.color,
            topLeft = Offset(left, top),
            size = Size(bodyW, bodyH),
            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
          )

          // Clear center test window
          drawRoundRect(
            color = Color.White.copy(alpha = 0.35f),
            topLeft = Offset(cx - 20.dp.toPx(), cy - 14.dp.toPx()),
            size = Size(40.dp.toPx(), 28.dp.toPx()),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
          )
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Text(
            current.rating,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            color = if (current.colorName.contains("White") || current.colorName.contains("Yellow")) Color.Black else Color.White,
            modifier = Modifier.padding(bottom = 20.dp)
          )
        }
      }

      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(16.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text("CURRENT RATING", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
          Text("${current.rating} (${current.colorName})", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
          Text("Voltage: ${current.voltage} | Typical App: ${current.useCase}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary)
        }
      }

      Text("Select Automotive Blade Fuse Color:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        fuseList.forEachIndexed { i, f ->
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
                  .size(24.dp)
                  .clip(RoundedCornerShape(4.dp))
                  .background(f.color)
                  .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
              )
              Column {
                Text(f.colorName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text(f.useCase, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
            Text(f.rating, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary)
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

data class FuseInfo(
  val rating: String,
  val colorName: String,
  val color: Color,
  val voltage: String,
  val useCase: String
)
