package com.univalle.shopu.presentation.util

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for email validation logic
 */
class ValidatorsTest {

    @Test
    fun `valid institutional email with correounivalle domain returns true`() {
        // Given
        val email = "student@correounivalle.edu.co"
        
        // When
        val result = isInstitutionalEmail(email)
        
        // Then
        assertTrue("Should accept correounivalle.edu.co emails", result)
    }

    @Test
    fun `valid institutional email with univalle domain returns true`() {
        // Given
        val email = "professor@univalle.edu.co"
        
        // When
        val result = isInstitutionalEmail(email)
        
        // Then
        assertTrue("Should accept univalle.edu.co emails", result)
    }

    @Test
    fun `valid institutional email with estudiantes domain returns true`() {
        // Given
        val email = "student@estudiantes.univalle.edu.co"
        
        // When
        val result = isInstitutionalEmail(email)
        
        // Then
        assertTrue("Should accept estudiantes.univalle.edu.co emails", result)
    }

    @Test
    fun `email with non-institutional domain returns false`() {
        // Given
        val email = "user@gmail.com"
        
        // When
        val result = isInstitutionalEmail(email)
        
        // Then
        assertFalse("Should reject non-institutional emails", result)
    }

    @Test
    fun `empty email returns false`() {
        // Given
        val email = ""
        
        // When
        val result = isInstitutionalEmail(email)
        
        // Then
        assertFalse("Should reject empty emails", result)
    }

    @Test
    fun `email with whitespace only returns false`() {
        // Given
        val email = "   "
        
        // When
        val result = isInstitutionalEmail(email)
        
        // Then
        assertFalse("Should reject whitespace-only emails", result)
    }

    @Test
    fun `email with correct domain but extra spaces is accepted`() {
        // Given
        val email = "  student@correounivalle.edu.co  "
        
        // When
        val result = isInstitutionalEmail(email)
        
        // Then
        assertTrue("Should trim and accept valid emails", result)
    }

    @Test
    fun `case insensitive domain check works`() {
        // Given
        val email = "Student@CORREOUNIVALLE.EDU.CO"
        
        // When
        val result = isInstitutionalEmail(email)
        
        // Then
        assertTrue("Should accept emails regardless of case", result)
    }
}
