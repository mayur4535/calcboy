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

// -------------------------------------------------------------
// 6. DIODE COLOR CODE (JEDEC 1N-Series & Pro-Electron)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiodeColorCodeScreen(
  onBack: () -> Unit,
  onSaveHistory: (String, String, String) -> Unit
) {
  // Preset or 4-band JEDEC 1N digits: Yellow(4), Brown(1), Yellow(4), Gray(8) -> 1N4148
  var d1Index by remember { mutableIntStateOf(4) } // 4
  var d2Index by remember { mutableIntStateOf(1) } // 1
  var d3Index by remember { mutableIntStateOf(4) } // 4
  var d4Index by remember { mutableIntStateOf(8) } // 8
  var selectedTab by remember { mutableIntStateOf(0) }

  val clipboardManager: ClipboardManager = LocalClipboardManager.current
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  val colorDigits = listOf(
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

  val d1 = colorDigits[d1Index].second.second
  val d2 = colorDigits[d2Index].second.second
  val d3 = colorDigits[d3Index].second.second
  val d4 = colorDigits[d4Index].second.second

  val partNumber = "1N$d1$d2$d3$d4"

  val popularDiodes = listOf(
    "1N4148" to "High-Speed Switching Diode (100V, 200mA, 4ns)",
    "1N4001" to "Standard Rectifier Diode (50V, 1A)",
    "1N4007" to "Standard Rectifier Diode (1000V, 1A)",
    "1N4733" to "Zener Diode 5.1V (1W)",
    "1N4742" to "Zener Diode 12V (1W)"
  )

  val knownDesc = popularDiodes.find { it.first == partNumber }?.second
    ?: "Standard JEDEC 1N-series silicon diode"

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Diode Color Code", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("diode_back_button")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(
            onClick = {
              val text = "Diode Part: $partNumber ($knownDesc)"
              clipboardManager.setText(AnnotatedString(text))
              scope.launch { snackbarHostState.showSnackbar("Copied: $text") }
              onSaveHistory("Diode Color Code", "JEDEC Bands", partNumber)
            },
            modifier = Modifier.testTag("diode_copy_button")
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
      // Glass Diode Canvas Graphic
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
          val bodyH = 40.dp.toPx()
          val left = cx - bodyW / 2f
          val top = cy - bodyH / 2f

          // Metal axial leads
          val leadColor = Color(0xFF94A3B8)
          drawLine(leadColor, Offset(0f, cy), Offset(w, cy), strokeWidth = 5.dp.toPx())

          // Glass transparent envelope (DO-35 orange tint)
          drawRoundRect(
            color = Color(0xFFF97316).copy(alpha = 0.85f),
            topLeft = Offset(left, top),
            size = Size(bodyW, bodyH),
            cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
          )

          // Cathode Band (Silver / Black Stripe on the right or left)
          val cathodeW = 14.dp.toPx()
          drawRect(color = Color(0xFF1E1E1E), topLeft = Offset(left + 16.dp.toPx(), top), size = Size(cathodeW, bodyH))

          // Color bands representing 1N digits
          val bands = listOf(
            colorDigits[d1Index].second.first,
            colorDigits[d2Index].second.first,
            colorDigits[d3Index].second.first,
            colorDigits[d4Index].second.first
          )

          val bandWidth = 10.dp.toPx()
          val startX = left + 45.dp.toPx()
          val spacing = (bodyW - 70.dp.toPx()) / 4f

          bands.forEachIndexed { i, col ->
            drawRect(color = col, topLeft = Offset(startX + i * spacing, top), size = Size(bandWidth, bodyH))
          }

          // Glossy highlight
          drawLine(Color.White.copy(alpha = 0.4f), Offset(left + 8.dp.toPx(), top + 4.dp.toPx()), Offset(left + bodyW - 8.dp.toPx(), top + 4.dp.toPx()), strokeWidth = 2.dp.toPx())
        }
      }

      // Output Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(16.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text("IDENTIFIED DIODE PART NUMBER", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
          Text(partNumber, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
          Text(knownDesc, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary)
          Text("Cathode (K) is marked by the black/silver stripe on terminal end.", style = MaterialTheme.typography.bodySmall)
        }
      }

      // Presets
      Text("Popular Diode Presets:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
      Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf(
          "1N4148" to listOf(4, 1, 4, 8),
          "1N4001" to listOf(4, 0, 0, 1),
          "1N4007" to listOf(4, 0, 0, 7),
          "1N4733" to listOf(4, 7, 3, 3),
          "1N4742" to listOf(4, 7, 4, 2)
        ).forEach { (name, indices) ->
          SuggestionChip(
            onClick = {
              d1Index = indices[0]
              d2Index = indices[1]
              d3Index = indices[2]
              d4Index = indices[3]
            },
            label = { Text(name) }
          )
        }
      }

      val tabs = listOf("1st Digit ($d1)", "2nd Digit ($d2)", "3rd Digit ($d3)", "4th Digit ($d4)")
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
        2 -> d3Index
        else -> d4Index
      }

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        colorDigits.forEachIndexed { i, (name, pair) ->
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
                  2 -> d3Index = i
                  else -> d4Index = i
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
            Text("Digit: $digit", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

// -------------------------------------------------------------
// 12. TRANSISTOR COLOR CODE
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransistorColorCodeScreen(
  onBack: () -> Unit,
  onSaveHistory: (String, String, String) -> Unit
) {
  var selectedFamily by remember { mutableStateOf("JIS 2SC (NPN High Freq)") }
  var hfeGainBand by remember { mutableStateOf("Y (Yellow): hFE 120-240") }

  val clipboardManager: ClipboardManager = LocalClipboardManager.current
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  val jisFamilies = mapOf(
    "JIS 2SA (PNP High Freq)" to "AF/RF PNP Transistor (e.g. 2SA1015). Flat face pinout: E-C-B or E-B-C.",
    "JIS 2SB (PNP Audio Power)" to "Audio frequency power PNP (e.g. 2SB772).",
    "JIS 2SC (NPN High Freq)" to "AF/RF NPN Transistor (e.g. 2SC1815, 2SC945). Standard E-C-B.",
    "JIS 2SD (NPN Audio Power)" to "Audio/Power NPN Transistor (e.g. 2SD882)."
  )

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Transistor Color Code", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("transistor_back_button")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(
            onClick = {
              val text = "Transistor: $selectedFamily, Gain: $hfeGainBand"
              clipboardManager.setText(AnnotatedString(text))
              scope.launch { snackbarHostState.showSnackbar("Copied: $text") }
              onSaveHistory("Transistor Color Code", selectedFamily, hfeGainBand)
            },
            modifier = Modifier.testTag("transistor_copy_button")
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
      // TO-92 Graphic Canvas
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

          // 3 Leads (Emitter, Collector, Base)
          val leadColor = Color(0xFF94A3B8)
          val sp = 20.dp.toPx()
          drawLine(leadColor, Offset(cx - sp, cy + 30.dp.toPx()), Offset(cx - sp, h - 8.dp.toPx()), strokeWidth = 3.dp.toPx())
          drawLine(leadColor, Offset(cx, cy + 30.dp.toPx()), Offset(cx, h - 8.dp.toPx()), strokeWidth = 3.dp.toPx())
          drawLine(leadColor, Offset(cx + sp, cy + 30.dp.toPx()), Offset(cx + sp, h - 8.dp.toPx()), strokeWidth = 3.dp.toPx())

          // TO-92 curved black epoxy body
          drawRoundRect(
            color = Color(0xFF1E293B),
            topLeft = Offset(cx - 32.dp.toPx(), cy - 20.dp.toPx()),
            size = Size(64.dp.toPx(), 48.dp.toPx()),
            cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
          )

          // Color dot on top (gain classification)
          val dotColor = when {
            hfeGainBand.startsWith("O") -> Color(0xFFF97316)
            hfeGainBand.startsWith("Y") -> Color(0xFFEAB308)
            hfeGainBand.startsWith("GR") -> Color(0xFF16A34A)
            else -> Color(0xFF2563EB)
          }
          drawCircle(color = dotColor, radius = 6.dp.toPx(), center = Offset(cx, cy - 8.dp.toPx()))
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
          Text("Pin 1: Emitter | Pin 2: Collector | Pin 3: Base", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 6.dp))
        }
      }

      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(16.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text("TRANSISTOR SPECIFICATIONS", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
          Text(selectedFamily, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
          Text("Gain Rank: $hfeGainBand", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
          Text(jisFamilies[selectedFamily] ?: "", style = MaterialTheme.typography.bodySmall)
        }
      }

      Text("JIS Semiconductor Family:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        jisFamilies.keys.forEach { fam ->
          FilterChip(
            selected = selectedFamily == fam,
            onClick = { selectedFamily = fam },
            label = { Text(fam) },
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer)
          )
        }
      }

      Text("hFE Current Gain Color Rank:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf(
          "O (Orange): hFE 70-140 (Standard Low Gain)",
          "Y (Yellow): hFE 120-240 (Medium Gain)",
          "GR (Green): hFE 200-400 (High Gain)",
          "BL (Blue): hFE 350-700 (Very High Gain)"
        ).forEach { rank ->
          FilterChip(
            selected = hfeGainBand == rank,
            onClick = { hfeGainBand = rank },
            label = { Text(rank) },
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer)
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

// -------------------------------------------------------------
// 16. IC COLOR CODE & PIN 1 IDENTIFIER
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IcColorCodeScreen(
  onBack: () -> Unit,
  onSaveHistory: (String, String, String) -> Unit
) {
  var pinCount by remember { mutableIntStateOf(16) } // 8, 14, 16, 28
  var packageType by remember { mutableStateOf("DIP (Dual In-line)") }

  val clipboardManager: ClipboardManager = LocalClipboardManager.current
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("IC Pinout & Orientation", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("ic_back_button")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(
            onClick = {
              val text = "IC Package: $packageType $pinCount-Pin Orientation Guide"
              clipboardManager.setText(AnnotatedString(text))
              scope.launch { snackbarHostState.showSnackbar("Copied: $text") }
              onSaveHistory("IC Orientation", "$packageType $pinCount-Pin", "Counter-Clockwise Count")
            },
            modifier = Modifier.testTag("ic_copy_button")
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
      // Visual IC Graphic Canvas
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .height(180.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val w = size.width
          val h = size.height
          val cx = w / 2f
          val cy = h / 2f

          val icW = 180.dp.toPx()
          val icH = 90.dp.toPx()
          val left = cx - icW / 2f
          val top = cy - icH / 2f

          val pinsPerSide = pinCount / 2
          val pinSpacing = icW / (pinsPerSide + 1)

          // Metallic Pins Top and Bottom
          val pinColor = Color(0xFFCBD5E1)
          val pinLength = 14.dp.toPx()
          val pinThick = 4.dp.toPx()

          for (i in 1..pinsPerSide) {
            val px = left + i * pinSpacing
            // Top pins
            drawLine(pinColor, Offset(px, top), Offset(px, top - pinLength), strokeWidth = pinThick)
            // Bottom pins
            drawLine(pinColor, Offset(px, top + icH), Offset(px, top + icH + pinLength), strokeWidth = pinThick)
          }

          // IC Body
          drawRoundRect(
            color = Color(0xFF1E293B),
            topLeft = Offset(left, top),
            size = Size(icW, icH),
            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
          )

          // Pin 1 Notch on Left Edge
          drawCircle(
            color = Color(0xFF0F172A),
            radius = 10.dp.toPx(),
            center = Offset(left, cy)
          )

          // Pin 1 Index Dot near bottom-left or top-left
          drawCircle(
            color = Color(0xFF38BDF8), // Bright cyan pin 1 dot
            radius = 5.dp.toPx(),
            center = Offset(left + 16.dp.toPx(), top + icH - 16.dp.toPx())
          )
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("NE555 / 74HC00", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color.White)
            Text("PIN 1 (• CYAN DOT)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
          }
        }
      }

      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(16.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text("PIN COUNTING RULE", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
          Text("Counter-Clockwise (CCW)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
          Text("1. Locate the Notch, Chamfer, or Dot.", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
          Text("2. With the notch facing LEFT, Pin 1 is the bottom-left pin.", style = MaterialTheme.typography.bodySmall)
          Text("3. Count 1 to $pinCount in a counter-clockwise loop around the chip.", style = MaterialTheme.typography.bodySmall)
        }
      }

      Text("Select IC Pin Count:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
      Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf(8, 14, 16, 20, 28).forEach { count ->
          FilterChip(
            selected = pinCount == count,
            onClick = { pinCount = count },
            label = { Text("$count Pins") },
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer)
          )
        }
      }

      Text("Package Style:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
      Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf("DIP (Dual In-line)", "SOIC (SMD)", "TSSOP (Narrow)", "QFP (Quad)").forEach { pkg ->
          FilterChip(
            selected = packageType == pkg,
            onClick = { packageType = pkg },
            label = { Text(pkg) },
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer)
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}
