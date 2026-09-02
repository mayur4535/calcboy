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

data class ResistorColor(
  val name: String,
  val color: Color,
  val digit: Int?,
  val multiplier: Double?,
  val tolerance: Double?,
  val tempCoeffPpm: Int? = null,
  val textColor: Color = Color.White
)

val ResistorColorTable = listOf(
  ResistorColor("Black", Color(0xFF1E1E1E), 0, 1.0, null, 250, Color.White),
  ResistorColor("Brown", Color(0xFF8B4513), 1, 10.0, 1.0, 100, Color.White),
  ResistorColor("Red", Color(0xFFDC2626), 2, 100.0, 2.0, 50, Color.White),
  ResistorColor("Orange", Color(0xFFF97316), 3, 1000.0, null, 15, Color.White),
  ResistorColor("Yellow", Color(0xFFEAB308), 4, 10000.0, null, 25, Color.Black),
  ResistorColor("Green", Color(0xFF16A34A), 5, 100000.0, 0.5, 20, Color.White),
  ResistorColor("Blue", Color(0xFF2563EB), 6, 1000000.0, 0.25, 10, Color.White),
  ResistorColor("Violet", Color(0xFF9333EA), 7, 10000000.0, 0.1, 5, Color.White),
  ResistorColor("Gray", Color(0xFF6B7280), 8, 100000000.0, 0.05, 1, Color.White),
  ResistorColor("White", Color(0xFFF3F4F6), 9, 1000000000.0, null, null, Color.Black),
  ResistorColor("Gold", Color(0xFFD97706), null, 0.1, 5.0, null, Color.White),
  ResistorColor("Silver", Color(0xFF9CA3AF), null, 0.01, 10.0, null, Color.Black)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResistorColorCodeScreen(
  onBack: () -> Unit,
  onSaveHistory: (String, String, String) -> Unit
) {
  // Band mode: 3, 4, 5, or 6 bands
  var bandCount by remember { mutableIntStateOf(4) }

  // Default values: Yellow (4), Violet (7), Red (x100) -> 4.7k, Gold (5%)
  var band1Index by remember { mutableIntStateOf(4) } // Yellow = 4
  var band2Index by remember { mutableIntStateOf(7) } // Violet = 7
  var band3Index by remember { mutableIntStateOf(0) } // Black = 0 (for 5/6-band)
  var multiplierIndex by remember { mutableIntStateOf(2) } // Red = 100
  var toleranceIndex by remember { mutableIntStateOf(10) } // Gold = 5%
  var tempCoeffIndex by remember { mutableIntStateOf(1) } // Brown = 100 ppm/K (for 6-band)

  var selectedTab by remember { mutableIntStateOf(0) }

  val clipboardManager: ClipboardManager = LocalClipboardManager.current
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  val hasBand3 = (bandCount >= 5)
  val hasTolerance = (bandCount >= 4)
  val hasPpm = (bandCount == 6)

  // Calculate Value
  val d1 = ResistorColorTable[band1Index].digit ?: 0
  val d2 = ResistorColorTable[band2Index].digit ?: 0
  val d3 = if (hasBand3) (ResistorColorTable[band3Index].digit ?: 0) else 0

  val baseDigits = if (hasBand3) (d1 * 100 + d2 * 10 + d3) else (d1 * 10 + d2)
  val multiplier = ResistorColorTable[multiplierIndex].multiplier ?: 1.0
  val tolerance = if (!hasTolerance) 20.0 else (ResistorColorTable[toleranceIndex].tolerance ?: 5.0)
  val ppm = if (hasPpm) ResistorColorTable[tempCoeffIndex].tempCoeffPpm ?: 100 else null

  val resistanceOhms = baseDigits * multiplier
  val minResistance = resistanceOhms * (1 - tolerance / 100.0)
  val maxResistance = resistanceOhms * (1 + tolerance / 100.0)

  fun formatOhms(ohms: Double): String {
    return when {
      ohms >= 1_000_000 -> String.format(Locale.US, "%.2f MΩ", ohms / 1_000_000)
      ohms >= 1_000 -> String.format(Locale.US, "%.2f kΩ", ohms / 1_000)
      else -> String.format(Locale.US, "%.2f Ω", ohms)
    }
  }

  val formattedValue = formatOhms(resistanceOhms)
  val toleranceStr = "±$tolerance%"
  val ppmStr = if (ppm != null) " ($ppm ppm/K)" else ""

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Resistor Color Code", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("resistor_back_button")
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(
            onClick = {
              band1Index = 1
              band2Index = 0
              band3Index = 0
              multiplierIndex = 3
              toleranceIndex = 10
              tempCoeffIndex = 1
            },
            modifier = Modifier.testTag("resistor_reset_button")
          ) {
            Icon(Icons.Default.Refresh, contentDescription = "Reset to 10k")
          }
          IconButton(
            onClick = {
              val text = "$formattedValue $toleranceStr$ppmStr (Range: ${formatOhms(minResistance)} - ${formatOhms(maxResistance)})"
              clipboardManager.setText(AnnotatedString(text))
              scope.launch {
                snackbarHostState.showSnackbar("Copied: $text")
              }
              onSaveHistory("Resistor Color Code", "$bandCount-Band Resistor", "$formattedValue $toleranceStr$ppmStr")
            },
            modifier = Modifier.testTag("resistor_copy_button")
          ) {
            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Result")
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
      // Band Count Mode Selector
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf(
          3 to "3-Band (20%)",
          4 to "4-Band (Standard)",
          5 to "5-Band (Precision)",
          6 to "6-Band (+PPM/K)"
        ).forEach { (count, label) ->
          FilterChip(
            selected = bandCount == count,
            onClick = {
              bandCount = count
              selectedTab = 0
            },
            label = { Text(label) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
          )
        }
      }

      // Visual Resistor Graphic Canvas
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .height(130.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
      ) {
        val bandColors = when (bandCount) {
          3 -> listOf(
            ResistorColorTable[band1Index].color,
            ResistorColorTable[band2Index].color,
            ResistorColorTable[multiplierIndex].color
          )
          4 -> listOf(
            ResistorColorTable[band1Index].color,
            ResistorColorTable[band2Index].color,
            ResistorColorTable[multiplierIndex].color,
            ResistorColorTable[toleranceIndex].color
          )
          5 -> listOf(
            ResistorColorTable[band1Index].color,
            ResistorColorTable[band2Index].color,
            ResistorColorTable[band3Index].color,
            ResistorColorTable[multiplierIndex].color,
            ResistorColorTable[toleranceIndex].color
          )
          else -> listOf(
            ResistorColorTable[band1Index].color,
            ResistorColorTable[band2Index].color,
            ResistorColorTable[band3Index].color,
            ResistorColorTable[multiplierIndex].color,
            ResistorColorTable[toleranceIndex].color,
            ResistorColorTable[tempCoeffIndex].color
          )
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
          val canvasW = size.width
          val canvasH = size.height
          val centerY = canvasH / 2f

          // Draw metallic axial leads
          val leadColor = Color(0xFFCBD5E1)
          drawLine(
            color = leadColor,
            start = Offset(0f, centerY),
            end = Offset(canvasW, centerY),
            strokeWidth = 8.dp.toPx()
          )

          // Draw Resistor Ceramic Body
          val bodyWidth = canvasW * 0.65f
          val bodyHeight = 56.dp.toPx()
          val bodyLeft = (canvasW - bodyWidth) / 2f
          val bodyTop = centerY - bodyHeight / 2f
          val bodyColor = if (bandCount >= 5) Color(0xFF60A5FA) else Color(0xFFD6C4A5)

          drawRoundRect(
            color = bodyColor,
            topLeft = Offset(bodyLeft, bodyTop),
            size = Size(bodyWidth, bodyHeight),
            cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
          )

          // Draw Bands
          val bandWidth = 13.dp.toPx()
          val totalBands = bandColors.size
          for (i in 0 until totalBands) {
            val bandX = if (i == totalBands - 1 && bandCount in 4..5) {
              // Tolerance separated
              bodyLeft + bodyWidth - 28.dp.toPx()
            } else {
              bodyLeft + 18.dp.toPx() + i * ((bodyWidth - 56.dp.toPx()) / (totalBands - 1).coerceAtLeast(1))
            }

            drawRect(
              color = bandColors[i],
              topLeft = Offset(bandX, bodyTop),
              size = Size(bandWidth, bodyHeight)
            )
          }

          // Subtle glossy highlight
          drawLine(
            color = Color.White.copy(alpha = 0.35f),
            start = Offset(bodyLeft + 10.dp.toPx(), bodyTop + 6.dp.toPx()),
            end = Offset(bodyLeft + bodyWidth - 10.dp.toPx(), bodyTop + 6.dp.toPx()),
            strokeWidth = 3.dp.toPx()
          )
        }
      }

      // Output Display Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          Text(
            text = "TOTAL RESISTANCE ($bandCount-BAND)",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
            letterSpacing = 1.sp
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
              text = "$toleranceStr$ppmStr",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.secondary,
              modifier = Modifier.padding(bottom = 4.dp)
            )
          }

          Spacer(modifier = Modifier.height(10.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(
                text = "Min Tolerance Value",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
              )
              Text(
                text = formatOhms(minResistance),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
            }
            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = "Max Tolerance Value",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
              )
              Text(
                text = formatOhms(maxResistance),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
            }
          }
        }
      }

      // Band Selection Tabs
      val tabs = when (bandCount) {
        3 -> listOf("1st Band", "2nd Band", "Multiplier")
        4 -> listOf("1st Band", "2nd Band", "Multiplier", "Tolerance")
        5 -> listOf("1st Band", "2nd Band", "3rd Band", "Multiplier", "Tolerance")
        else -> listOf("1st Band", "2nd Band", "3rd Band", "Multiplier", "Tolerance", "Temp Coeff (PPM)")
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
            text = { Text(title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
          )
        }
      }

      // Color Palette Picker for the currently selected band
      val currentActiveColorIndex = when (bandCount) {
        3 -> when (currentTabClamped) {
          0 -> band1Index
          1 -> band2Index
          else -> multiplierIndex
        }
        4 -> when (currentTabClamped) {
          0 -> band1Index
          1 -> band2Index
          2 -> multiplierIndex
          else -> toleranceIndex
        }
        5 -> when (currentTabClamped) {
          0 -> band1Index
          1 -> band2Index
          2 -> band3Index
          3 -> multiplierIndex
          else -> toleranceIndex
        }
        else -> when (currentTabClamped) {
          0 -> band1Index
          1 -> band2Index
          2 -> band3Index
          3 -> multiplierIndex
          4 -> toleranceIndex
          else -> tempCoeffIndex
        }
      }

      Text(
        text = "Select Color for ${tabs[currentTabClamped]}:",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold
      )

      Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        ResistorColorTable.forEachIndexed { index, item ->
          val isValidForCurrentTab = when (bandCount) {
            3 -> when (currentTabClamped) {
              0 -> item.digit != null && index != 0
              1 -> item.digit != null
              else -> item.multiplier != null
            }
            4 -> when (currentTabClamped) {
              0 -> item.digit != null && index != 0
              1 -> item.digit != null
              2 -> item.multiplier != null
              else -> item.tolerance != null
            }
            5 -> when (currentTabClamped) {
              0 -> item.digit != null && index != 0
              1, 2 -> item.digit != null
              3 -> item.multiplier != null
              else -> item.tolerance != null
            }
            else -> when (currentTabClamped) {
              0 -> item.digit != null && index != 0
              1, 2 -> item.digit != null
              3 -> item.multiplier != null
              4 -> item.tolerance != null
              else -> item.tempCoeffPpm != null
            }
          }

          if (isValidForCurrentTab) {
            val isSelected = (index == currentActiveColorIndex)
            val borderColor by animateColorAsState(
              targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
              label = "colorBorder"
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
                  when (bandCount) {
                    3 -> when (currentTabClamped) {
                      0 -> band1Index = index
                      1 -> band2Index = index
                      else -> multiplierIndex = index
                    }
                    4 -> when (currentTabClamped) {
                      0 -> band1Index = index
                      1 -> band2Index = index
                      2 -> multiplierIndex = index
                      else -> toleranceIndex = index
                    }
                    5 -> when (currentTabClamped) {
                      0 -> band1Index = index
                      1 -> band2Index = index
                      2 -> band3Index = index
                      3 -> multiplierIndex = index
                      else -> toleranceIndex = index
                    }
                    else -> when (currentTabClamped) {
                      0 -> band1Index = index
                      1 -> band2Index = index
                      2 -> band3Index = index
                      3 -> multiplierIndex = index
                      4 -> toleranceIndex = index
                      else -> tempCoeffIndex = index
                    }
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

              val detailText = when (bandCount) {
                3 -> when (currentTabClamped) {
                  0, 1 -> "Value: ${item.digit}"
                  else -> "×${item.multiplier}"
                }
                4 -> when (currentTabClamped) {
                  0, 1 -> "Value: ${item.digit}"
                  2 -> "×${item.multiplier}"
                  else -> "±${item.tolerance}%"
                }
                5 -> when (currentTabClamped) {
                  0, 1, 2 -> "Value: ${item.digit}"
                  3 -> "×${item.multiplier}"
                  else -> "±${item.tolerance}%"
                }
                else -> when (currentTabClamped) {
                  0, 1, 2 -> "Value: ${item.digit}"
                  3 -> "×${item.multiplier}"
                  4 -> "±${item.tolerance}%"
                  else -> "${item.tempCoeffPpm} ppm/K"
                }
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
