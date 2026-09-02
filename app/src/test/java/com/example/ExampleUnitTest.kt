package com.example

import com.example.model.ToolCategory
import com.example.model.ToolId
import com.example.model.ToolRegistry
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit test verifying CalcBoy tool registry and calculation configurations.
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun verifyAllSixteenColorCodeToolsExist() {
    val colorCodeTools = ToolRegistry.tools.filter { it.category == ToolCategory.COLOR_CODES }
    assertEquals(16, colorCodeTools.size)
    assertEquals(20, ToolRegistry.tools.size)

    val expectedIds = listOf(
      ToolId.RESISTOR_COLOR_CODE,
      ToolId.INDUCTOR_COLOR_CODE,
      ToolId.SMD_RESISTOR_CODE,
      ToolId.SMD_INDUCTOR_CODE,
      ToolId.SMD_CAPACITOR_CODE,
      ToolId.DIODE_COLOR_CODE,
      ToolId.CERAMIC_CAPACITOR_CODE,
      ToolId.CERAMIC_CAP_VOLTAGE,
      ToolId.CERAMIC_CAP_TEMP_COEFF,
      ToolId.POLYESTER_CAP_COLOR_CODE,
      ToolId.FILM_CAPACITOR_CODE,
      ToolId.TRANSISTOR_COLOR_CODE,
      ToolId.TANTALUM_CAP_COLOR_CODE,
      ToolId.MICA_CAPACITOR_COLOR_CODE,
      ToolId.FUSE_COLOR_CODE,
      ToolId.IC_COLOR_CODE
    )

    expectedIds.forEach { id ->
      assertNotNull("Tool $id must be in registry", ToolRegistry.getTool(id))
    }
  }
}
