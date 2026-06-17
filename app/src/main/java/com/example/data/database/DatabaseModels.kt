package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val fullName: String,
    val email: String,
    val mobileNumber: String,
    val passwordHashed: String,
    val registrationDate: Long = System.currentTimeMillis(),
    val lastLogin: Long = System.currentTimeMillis()
)

@Entity(tableName = "login_history")
data class LoginHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userId: Long,
    val loginTimestamp: Long = System.currentTimeMillis(),
    val logoutTimestamp: Long? = null,
    val deviceInfo: String,
    val browserInfo: String,
    val ipAddress: String
)

@Entity(tableName = "activity_history")
data class ActivityHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userId: Long,
    val actionType: String, // "VIEW", "CART_ADD", "ORDER_PLACE", "WISHLIST_ADD", "SEARCH"
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val category: String, // "Dog", "Cat", "Bird", "Fish", "Rabbit", "Accessories"
    val description: String,
    val price: Double,
    val discount: Double = 0.0, // e.g. 10.0 for 10%
    val rating: Float = 5.0f,
    val stockStatus: String = "In Stock", // "In Stock", "Low Stock", "Out of Stock"
    val isCustomAdded: Boolean = false
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userId: Long,
    val productsJson: String, // Serialized list of purchased items (CartItem/Product info)
    val orderDate: Long = System.currentTimeMillis(),
    val totalPrice: Double,
    val status: String = "Pending", // "Pending", "Processing", "Shipped", "Delivered"
    val fullName: String,
    val address: String,
    val city: String,
    val state: String,
    val pinCode: String,
    val phoneNumber: String,
    val paymentMethod: String
)

@Entity(tableName = "wishlist")
data class WishlistItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userId: Long,
    val productId: Long
)

@Entity(tableName = "cart")
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userId: Long,
    val productId: Long,
    var quantity: Int
)

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val productId: Long,
    val userId: Long,
    val userName: String,
    val comment: String,
    val rating: Float,
    val timestamp: Long = System.currentTimeMillis()
)
