package com.univalle.shopu.presentation.util

import com.univalle.shopu.domain.model.CartItem
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for CartStore operations
 */
class CartStoreTest {

    @Before
    fun setup() {
        // Clear cart before each test
        CartStore.clear()
    }

    @After
    fun tearDown() {
        // Clean up after each test
        CartStore.clear()
    }

    @Test
    fun `adding new item to empty cart works`() {
        // Given
        val item = CartItem(
            id = "1",
            name = "Café",
            price = 2000.0,
            quantity = 1,
            imageUrl = null
        )

        // When
        CartStore.add(item)

        // Then
        assertEquals("Cart should have 1 item", 1, CartStore.items.size)
        assertEquals("Item should be in cart", item.id, CartStore.items[0].id)
    }

    @Test
    fun `adding existing item increases quantity`() {
        // Given
        val item = CartItem(
            id = "1",
            name = "Café",
            price = 2000.0,
            quantity = 1,
            imageUrl = null
        )
        CartStore.add(item)

        // When
        CartStore.add(item)

        // Then
        assertEquals("Cart should still have 1 item", 1, CartStore.items.size)
        assertEquals("Quantity should be 2", 2, CartStore.items[0].quantity)
    }

    @Test
    fun `incrementing item quantity works`() {
        // Given
        val item = CartItem(
            id = "1",
            name = "Café",
            price = 2000.0,
            quantity = 1,
            imageUrl = null
        )
        CartStore.add(item)

        // When
        CartStore.inc("1")

        // Then
        assertEquals("Quantity should be 2", 2, CartStore.items[0].quantity)
    }

    @Test
    fun `decrementing item quantity works`() {
        // Given
        val item = CartItem(
            id = "1",
            name = "Café",
            price = 2000.0,
            quantity = 3,
            imageUrl = null
        )
        CartStore.add(item)

        // When
        CartStore.dec("1")

        // Then
        assertEquals("Quantity should be 2", 2, CartStore.items[0].quantity)
    }

    @Test
    fun `decrementing item with quantity 1 removes it`() {
        // Given
        val item = CartItem(
            id = "1",
            name = "Café",
            price = 2000.0,
            quantity = 1,
            imageUrl = null
        )
        CartStore.add(item)

        // When
        CartStore.dec("1")

        // Then
        assertTrue("Cart should be empty", CartStore.items.isEmpty())
    }

    @Test
    fun `removing item works`() {
        // Given
        val item1 = CartItem("1", "Café", 2000.0, 1, null)
        val item2 = CartItem("2", "Snack", 1500.0, 1, null)
        CartStore.add(item1)
        CartStore.add(item2)

        // When
        CartStore.remove("1")

        // Then
        assertEquals("Cart should have 1 item", 1, CartStore.items.size)
        assertEquals("Remaining item should be item2", "2", CartStore.items[0].id)
    }

    @Test
    fun `total calculation is correct`() {
        // Given
        val item1 = CartItem("1", "Café", 2000.0, 2, null) // subtotal = 4000
        val item2 = CartItem("2", "Snack", 1500.0, 3, null) // subtotal = 4500
        CartStore.add(item1)
        CartStore.add(item2)

        // When
        val total = CartStore.total()

        // Then
        assertEquals("Total should be 8500", 8500.0, total, 0.01)
    }

    @Test
    fun `clearing cart removes all items`() {
        // Given
        CartStore.add(CartItem("1", "Café", 2000.0, 1, null))
        CartStore.add(CartItem("2", "Snack", 1500.0, 1, null))

        // When
        CartStore.clear()

        // Then
        assertTrue("Cart should be empty", CartStore.items.isEmpty())
        assertEquals("Total should be 0", 0.0, CartStore.total(), 0.01)
    }

    @Test
    fun `total of empty cart is zero`() {
        // When
        val total = CartStore.total()

        // Then
        assertEquals("Total should be 0", 0.0, total, 0.01)
    }
}
