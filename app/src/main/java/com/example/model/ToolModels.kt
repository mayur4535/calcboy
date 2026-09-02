package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.Hardware
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Pattern
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.graphics.vector.ImageVector

enum class ToolCategory(val displayName: String, val description: String) {
  COLOR_CODES(
    displayName = "Colour & Component Codes",
    description = "Color bands, SMD markings, capacitors, diodes, transistors, fuses & IC pinout"
  ),
  CIRCUITS_ANALYSIS(
    displayName = "Circuits & Analysis",
    description = "Ohm's law, LED resistor, voltage divider & 555 timer oscillators"
  )
}

enum class ToolId {
  // 16 Colour & Marking Code Tools
  RESISTOR_COLOR_CODE,
  INDUCTOR_COLOR_CODE,
  SMD_RESISTOR_CODE,
  SMD_INDUCTOR_CODE,
  SMD_CAPACITOR_CODE,
  DIODE_COLOR_CODE,
  CERAMIC_CAPACITOR_CODE,
  CERAMIC_CAP_VOLTAGE,
  CERAMIC_CAP_TEMP_COEFF,
  POLYESTER_CAP_COLOR_CODE,
  FILM_CAPACITOR_CODE,
  TRANSISTOR_COLOR_CODE,
  TANTALUM_CAP_COLOR_CODE,
  MICA_CAPACITOR_COLOR_CODE,
  FUSE_COLOR_CODE,
  IC_COLOR_CODE,

  // Circuits & Analysis Tools
  OHMS_LAW,
  LED_RESISTOR,
  VOLTAGE_DIVIDER,
  TIMER_555
}

data class EngineeringTool(
  val id: ToolId,
  val title: String,
  val category: ToolCategory,
  val formula: String,
  val summary: String,
  val icon: ImageVector,
  val badge: String
)

data class CalcHistoryItem(
  val id: Long = System.currentTimeMillis(),
  val toolTitle: String,
  val summary: String,
  val resultText: String,
  val timestamp: String
)

object ToolRegistry {
  val tools = listOf(
    // 16 Tools for Category: Colour & Component Codes
    EngineeringTool(
      id = ToolId.RESISTOR_COLOR_CODE,
      title = "Resistor Color Codes 3-4-5-6 Band",
      category = ToolCategory.COLOR_CODES,
      formula = "R = (Digits) × 10ⁿ ± Tol% (ppm/K)",
      summary = "Decode 3, 4, 5, and 6-band axial resistors with multiplier, tolerance, and temperature coefficient.",
      icon = Icons.Default.Hardware,
      badge = "3-6 Band"
    ),
    EngineeringTool(
      id = ToolId.INDUCTOR_COLOR_CODE,
      title = "Inductor Color Code 3-4-5 Band",
      category = ToolCategory.COLOR_CODES,
      formula = "L = (Digits) × 10ⁿ μH ± Tol%",
      summary = "Calculate inductance in microhenries (μH) and tolerance for 3, 4, and 5-band axial inductors & military standard.",
      icon = Icons.Default.Tune,
      badge = "3-5 Band"
    ),
    EngineeringTool(
      id = ToolId.SMD_RESISTOR_CODE,
      title = "SMD Resistor Codes",
      category = ToolCategory.COLOR_CODES,
      formula = "3-digit, 4-digit, EIA-96, R-notation",
      summary = "Decode standard 3-digit, 4-digit precision, and EIA-96 1% surface-mount resistor markings.",
      icon = Icons.Default.Numbers,
      badge = "EIA-96 & SMD"
    ),
    EngineeringTool(
      id = ToolId.SMD_INDUCTOR_CODE,
      title = "SMD Inductor Code",
      category = ToolCategory.COLOR_CODES,
      formula = "L = Digits × 10ⁿ nH/μH",
      summary = "Decode surface-mount chip inductor codes in nH and μH with R decimal notations.",
      icon = Icons.Default.Pattern,
      badge = "Chip Inductor"
    ),
    EngineeringTool(
      id = ToolId.SMD_CAPACITOR_CODE,
      title = "SMD Capacitor Code",
      category = ToolCategory.COLOR_CODES,
      formula = "C = Digits × 10ⁿ pF | EIA-198",
      summary = "Decode MLCC chip capacitor 3-digit and EIA-198 two-character alphanumeric markings with voltage rating.",
      icon = Icons.Default.Layers,
      badge = "MLCC & SMD"
    ),
    EngineeringTool(
      id = ToolId.DIODE_COLOR_CODE,
      title = "Diode Color Code",
      category = ToolCategory.COLOR_CODES,
      formula = "JEDEC 1N-series & Pro-Electron",
      summary = "Identify diode part numbers from colored glass bands (e.g. 1N4148) and cathode polarity band.",
      icon = Icons.Default.ElectricBolt,
      badge = "JEDEC & Pro-El"
    ),
    EngineeringTool(
      id = ToolId.CERAMIC_CAPACITOR_CODE,
      title = "Ceramic Capacitor Code",
      category = ToolCategory.COLOR_CODES,
      formula = "C = Digits × 10ⁿ pF ± Letter Tol%",
      summary = "Decode standard 3-digit ceramic disc capacitor stamps (e.g. 104, 223) with J/K/M tolerance letters.",
      icon = Icons.Default.Layers,
      badge = "3-Digit Disc"
    ),
    EngineeringTool(
      id = ToolId.CERAMIC_CAP_VOLTAGE,
      title = "Ceramic Capacitor with Voltage",
      category = ToolCategory.COLOR_CODES,
      formula = "Capacitance + Voltage Rating Band",
      summary = "Decode ceramic disc color dots/stripes indicating capacitance, tolerance, and maximum working voltage.",
      icon = Icons.Default.ElectricMeter,
      badge = "Color & Voltage"
    ),
    EngineeringTool(
      id = ToolId.CERAMIC_CAP_TEMP_COEFF,
      title = "Ceramic Cap Temp Coefficient",
      category = ToolCategory.COLOR_CODES,
      formula = "EIA Class 1: C0G/NP0, N080, N150, N750, SL",
      summary = "Decode the top color dot or band specifying temperature coefficient stability in ppm/°C.",
      icon = Icons.Default.Speed,
      badge = "Temp Drift"
    ),
    EngineeringTool(
      id = ToolId.POLYESTER_CAP_COLOR_CODE,
      title = "Polyester Capacitor Color Code",
      category = ToolCategory.COLOR_CODES,
      formula = "Mullard 5-Band: Val + Tol + Voltage",
      summary = "Decode vintage 'Tropical Fish' 5-band polyester/Mylar capacitors for value, tolerance, and DC voltage.",
      icon = Icons.Default.Pattern,
      badge = "5-Band Tropical"
    ),
    EngineeringTool(
      id = ToolId.FILM_CAPACITOR_CODE,
      title = "Film Capacitor Code",
      category = ToolCategory.COLOR_CODES,
      formula = "Dielectric: MKT, MKP, MKS + Value + Tol",
      summary = "Decode box & axial film capacitors with dielectric type (metallized polyester/polypropylene) and ratings.",
      icon = Icons.Default.Layers,
      badge = "Box & Axial"
    ),
    EngineeringTool(
      id = ToolId.TRANSISTOR_COLOR_CODE,
      title = "Transistor Color Code",
      category = ToolCategory.COLOR_CODES,
      formula = "Pro-Electron & JIS 2SA/2SC Dot Codes",
      summary = "Decode color-coded bands and dot markers on transistors and pinout arrangement (E-B-C).",
      icon = Icons.Default.SettingsSuggest,
      badge = "Semiconductor"
    ),
    EngineeringTool(
      id = ToolId.TANTALUM_CAP_COLOR_CODE,
      title = "Tantalum Capacitor Color Code",
      category = ToolCategory.COLOR_CODES,
      formula = "Top + Shoulder + Spot + Voltage Dot",
      summary = "Decode dipped/bead polarized tantalum capacitor colored dots: capacitance, voltage rating, and polarity.",
      icon = Icons.Default.Tune,
      badge = "Bead Tantalum"
    ),
    EngineeringTool(
      id = ToolId.MICA_CAPACITOR_COLOR_CODE,
      title = "Mica Capacitor Color Code",
      category = ToolCategory.COLOR_CODES,
      formula = "MIL-C-5 / EIA 6-Dot Standard System",
      summary = "Decode 6-dot precision silver mica capacitors: identification, value, multiplier, tolerance, and characteristic.",
      icon = Icons.Default.Numbers,
      badge = "MIL-C-5 6-Dot"
    ),
    EngineeringTool(
      id = ToolId.FUSE_COLOR_CODE,
      title = "Fuse Color Code",
      category = ToolCategory.COLOR_CODES,
      formula = "Automotive Blade & Micro Cartridge",
      summary = "Instantly identify current ratings and voltage for automotive blade (ATO/ATC/Mini) and cartridge fuses.",
      icon = Icons.Default.Security,
      badge = "Blade & Mini"
    ),
    EngineeringTool(
      id = ToolId.IC_COLOR_CODE,
      title = "IC Color Code & Pinout",
      category = ToolCategory.COLOR_CODES,
      formula = "Pin 1 Notch/Dot + Counter-Clockwise Count",
      summary = "Pin 1 identification methods (notch, dot, chamfer), package orientations, and laser marking codes.",
      icon = Icons.Default.Memory,
      badge = "Pin 1 & ICs"
    ),

    // Circuits & Analysis Tools
    EngineeringTool(
      id = ToolId.OHMS_LAW,
      title = "Ohm's Law & Power",
      category = ToolCategory.CIRCUITS_ANALYSIS,
      formula = "V = I × R  |  P = V × I",
      summary = "Calculate Voltage, Current, Resistance, or Power by providing any two known parameters.",
      icon = Icons.Default.Speed,
      badge = "Dual-Input Solver"
    ),
    EngineeringTool(
      id = ToolId.LED_RESISTOR,
      title = "LED Series Resistor",
      category = ToolCategory.CIRCUITS_ANALYSIS,
      formula = "R = (Vs - Vf) / If",
      summary = "Find current-limiting series resistor, E24 standard commercial value, and safe wattage rating for LEDs.",
      icon = Icons.Default.Lightbulb,
      badge = "E24 Series"
    ),
    EngineeringTool(
      id = ToolId.VOLTAGE_DIVIDER,
      title = "Voltage Divider",
      category = ToolCategory.CIRCUITS_ANALYSIS,
      formula = "Vout = Vin × (R2 / (R1 + R2))",
      summary = "Compute output voltage, attenuation ratio, load dissipation, and voltage drops across resistive dividers.",
      icon = Icons.Default.ElectricMeter,
      badge = "Ratio & Attenuation"
    ),
    EngineeringTool(
      id = ToolId.TIMER_555,
      title = "555 Timer Astable",
      category = ToolCategory.CIRCUITS_ANALYSIS,
      formula = "f = 1.44 / ((R1 + 2·R2) × C)",
      summary = "Calculate oscillation frequency, time period, high/low pulse duration, and duty cycle for 555 timers.",
      icon = Icons.Default.Timer,
      badge = "Oscillator & PWM"
    )
  )
}
