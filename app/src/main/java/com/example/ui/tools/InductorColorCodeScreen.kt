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
import androidx.compose.material.icons.filled.Refresh
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

data class InductorColor(
  val name: String,
  val color: Color,
  val digit: Int?,
  val multiplier: Double?,
  val tolerance: Double?
)

val InductorColorTable = listOf(
  InductorColor("Black", Color(0xFF1E1E1E), 0, 1.0, 20.0),
  InductorColor("Brown", Color(0xFF8B4513), 1, 10.0, 1.0),
  InductorColor("Red", Color(0xFFDC2626), 2, 100.0, 2.0),
  InductorColor("Orange", Color(0xFFF97316), 3, 1000.0, 3.0),
  InductorColor("Yellow", Color(0xFFEAB308), 4, 10000.0, 4.0),
  InductorColor("Green", Color(0xFF16A34A), 5, null, null),
  InductorColor("Blue", Color(0xFF2563EB), 6, null, null),
  InductorColor("Violet", Color(0xFF9333EA), 7, null, null),
  InductorColor("Gray", Color(0xFF6B7280), 8, null, null),
  InductorColor("White", Color(0xFFF3F4F6), 9, null, null),
  InductorColor("Gold", Color(0xFFD97706), null, 0.1, 5.0),
  InductorColor("Silver", Color(0xFF9CA3AF), null, 0.01, 10.0)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InductorColorCodeScreen(
  onBack: () -> Unit,
  onSaveHistory: (String, String, String) -> Unit
) {
  // Mode: 3-band, 4-band, 5-band (Military Standard)
  var bandMode by remember { mutableIntStateOf(4) } // 3, 4, or 5

  var band1Index by remember { mutableIntStateOf(1) } // Brown = 1
  var band2Index by remember { mutableIntStateOf(0) } // Black = 0
  var multiplierIndex by remember { mutableIntStateOf(1) } // Brown = 10 -> 100 uH
  var toleranceIndex by remember { mutableIntStateOf(10) } // Gold = 5%
  var selectedTab by remember { mutableIntStateOf(0) }

  val clipboardManager: ClipboardManager = LocalClipboardManager.current
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  val d1 = InductorColorTable[band1Index].digit ?: 0
  val d2 = InductorColorTable[band2Index].digit ?: 0
  val mult = InductorColorTable[multiplierIndex].multiplier ?: 1.0
  val tol = if (bandMode == 3) 20.0 else (InductorColorTable[toleranceIndex].tolerance ?: 5.0)

  val inductanceMicroHenries = (d1 * 10 + d2) * mult

  fun formatInductance(uh: Double): String {
    return when {
      uh >= 1_000_000 -> String.format(Locale.US, "%.3f H", uh / 1_000_000)
      uh >= 1_000 -> String.format(Locale.US, "%.2f mH", uh / 1_000)
      uh < 1.0 -> String.format(Locale.US, "%.2f nH", uh * 1000)
      else -> String.format(Locale.US, "%.2f μH", uh)
    }
  }

  val formattedValue = formatInductance(inductanceMicroHenries)
  val tolStr = "±$tol%"

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Inductor Color Code", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("inductor_back_button")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(
            onClick = {
              band1Index = 1
              band2Index = 0
              multiplierIndex = 1
              toleranceIndex = 10
            },
            modifier = Modifier.testTag("inductor_reset_button")
          ) {
            Icon(Icons.Default.Refresh, contentDescription = "Reset")
          }
          IconButton(
            onClick = {
              val text = "Inductance: $formattedValue $tolStr ($bandMode-Band)"
              clipboardManager.setText(AnnotatedString(text))
              scope.launch { snackbarHostState.showSnackbar("Copied: $text") }
              onSaveHistory("Inductor Color Code", "$bandMode-Band Inductor", "$formattedValue $tolStr")
            },
            modifier = Modifier.testTag("inductor_copy_button")
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
      // Band Selector Mode
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf(3 to "3-Band (20%)", 4 to "4-Band (Standard)", 5 to "5-Band (Military MIL)").forEach { (mode, label) ->
          FilterChip(
            selected = bandMode == mode,
            onClick = {
              bandMode = mode
              if (selectedTab >= (if (mode == 3) 3 else 4)) selectedTab = 0
            },
            label = { Text(label) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
            )
          )
        }
      }

      // Visual Inductor Canvas
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
          val cy = h / 2f

          // Metallic leads
          drawLine(Color(0xFF94A3B8), Offset(0f, cy), Offset(w, cy), strokeWidth = 6.dp.toPx())

          // Molded inductor body (typically teal/cyan or olive green)
          val bodyW = w * 0.65f
          val bodyH = 50.dp.toPx()
          val bodyLeft = (w - bodyW) / 2f
          val bodyTop = cy - bodyH / 2f

          drawRoundRect(
            color = Color(0xFF0D9488), // Teal Inductor body
            topLeft = Offset(bodyLeft, bodyTop),
            size = Size(bodyW, bodyH),
            cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
          )

          // Color bands
          val bandWidth = 12.dp.toPx()
          val bands = when (bandMode) {
            3 -> listOf(
              InductorColorTable[band1Index].color,
              InductorColorTable[band2Index].color,
              InductorColorTable[multiplierIndex].color
            )
            5 -> listOf(
              Color(0xFF9CA3AF), // Double width military silver band
              InductorColorTable[band1Index].color,
              InductorColorTable[band2Index].color,
              InductorColorTable[multiplierIndex].color,
              InductorColorTable[toleranceIndex].color
            )
            else -> listOf(
              InductorColorTable[band1Index].color,
              InductorColorTable[band2Index].color,
              InductorColorTable[multiplierIndex].color,
              InductorColorTable[toleranceIndex].color
            )
          }

          val spacing = (bodyW - 40.dp.toPx()) / (bands.size - 1).coerceAtLeast(1)
          bands.forEachIndexed { i, color ->
            val isMilSilver = (bandMode == 5 && i == 0)
            val bw = if (isMilSilver) bandWidth * 1.8f else bandWidth
            val bx = bodyLeft + 18.dp.toPx() + i * spacing
            drawRect(color = color, topLeft = Offset(bx, bodyTop), size = Size(bw, bodyH))
          }

          // Glossy highlight
          drawLine(
            Color.White.copy(alpha = 0.35f),
            Offset(bodyLeft + 10.dp.toPx(), bodyTop + 5.dp.toPx()),
            Offset(bodyLeft + bodyW - 10.dp.toPx(), bodyTop + 5.dp.toPx()),
            strokeWidth = 3.dp.toPx()
          )
        }
      }

      // Output Display Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(16.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "INDUCTANCE VALUE (L)",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
          )
          Spacer(modifier = Modifier.height(4.dp))
          Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = formattedValue,
              style = MaterialTheme.typography.headlineLarge,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
              text = tolStr,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.secondary,
              modifier = Modifier.padding(bottom = 4.dp)
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Raw: ${String.format(Locale.US, "%.2f", inductanceMicroHenries)} μH (Base: ${d1 * 10 + d2} × $mult)",
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
          )
        }
      }

      // Tabs for bands
      val tabs = when (bandMode) {
        3 -> listOf("1st Band", "2nd Band", "Multiplier")
        5 -> listOf("1st Digit", "2nd Digit", "Multiplier", "Tolerance")
        else -> listOf("1st Band", "2nd Band", "Multiplier", "Tolerance")
      }
      val currentTabClamped = selectedTab.coerceIn(0, tabs.size - 1)

      TabRow(
        selectedTabIndex = currentTabClamped,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.primary,
        modifier = Modifier.clip(RoundedCornerShape(12.dp))
      ) {
        tabs.forEachIndexed { index, title ->
          Tab(
            selected = currentTabClamped == index,
            onClick = { selectedTab = index },
            text = { Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
          )
        }
      }

      val currentActiveColorIndex = when (currentTabClamped) {
        0 -> band1Index
        1 -> band2Index
        2 -> multiplierIndex
        else -> toleranceIndex
      }

      Text(
        text = "Select Color for ${tabs[currentTabClamped]}:",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold
      )

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        InductorColorTable.forEachIndexed { index, item ->
          val isValid = when (currentTabClamped) {
            0 -> item.digit != null && index != 0
            1 -> item.digit != null
            2 -> item.multiplier != null
            else -> item.tolerance != null
          }

          if (isValid) {
            val isSelected = (index == currentActiveColorIndex)
            val borderColor by animateColorAsState(
              targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
              label = "indColorBorder"
            )

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(
                  if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                  else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
                .border(2.dp, borderColor, RoundedCornerShape(10.dp))
                .clickable {
                  when (currentTabClamped) {
                    0 -> band1Index = index
                    1 -> band2Index = index
                    2 -> multiplierIndex = index
                    else -> toleranceIndex = index
                  }
                }
                .padding(horizontal = 12.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(item.color)
                    .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                )
                Text(
                  text = item.name,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  style = MaterialTheme.typography.bodyMedium
                )
              }

              val detailText = when (currentTabClamped) {
                0, 1 -> "Value: ${item.digit}"
                2 -> "×${item.multiplier}"
                else -> "±${item.tolerance}%"
              }

              Text(
                text = detailText,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.primary
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}
