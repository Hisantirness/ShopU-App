package com.univalle.shopu.presentation.util

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for currency formatting utility
 */
class FormatUtilsTest {

    @Test
    fun `formatting positive amount works`() {
        // Given
        val amount = 5000.0

        // When
        val result = formatCurrencyCOP(amount)

        // Then
        assertTrue("Should contain formatted amount", result.contains("5"))
        assertTrue("Should contain currency symbol", result.contains("$"))
    }

    @Test
    fun `formatting zero amount works`() {
        // Given
        val amount = 0.0

        // When
        val result = formatCurrencyCOP(amount)

        // Then
        assertTrue("Should format zero", result.contains("0"))
    }

    @Test
    fun `formatting large amount works`() {
        // Given
        val amount = 1000000.0

        // When
        val result = formatCurrencyCOP(amount)

        // Then
        assertTrue("Should contain formatted amount", result.contains("1"))
        assertTrue("Should contain currency symbol", result.contains("$"))
    }

    @Test
    fun `formatting decimal amount works`() {
        // Given
        val amount = 1500.50

        // When
        val result = formatCurrencyCOP(amount)

        // Then
        assertTrue("Should format decimals", result.isNotEmpty())
        assertTrue("Should contain currency symbol", result.contains("$"))
    }
}
