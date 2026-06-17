package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): UserEntity?

    @Query("SELECT * FROM users ORDER BY registrationDate DESC")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)
}

@Dao
interface LoginHistoryDao {
    @Query("SELECT * FROM login_history WHERE userId = :userId ORDER BY loginTimestamp DESC")
    fun getLoginHistoryForUser(userId: Long): Flow<List<LoginHistoryEntity>>

    @Query("SELECT * FROM login_history ORDER BY loginTimestamp DESC")
    fun getAllLoginHistory(): Flow<List<LoginHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoginHistory(history: LoginHistoryEntity): Long
    
    @Query("UPDATE login_history SET logoutTimestamp = :logoutTimestamp WHERE id = :id")
    suspend fun updateLogoutTime(id: Long, logoutTimestamp: Long)
}

@Dao
interface ActivityHistoryDao {
    @Query("SELECT * FROM activity_history WHERE userId = :userId ORDER BY timestamp DESC")
    fun getActivityHistoryForUser(userId: Long): Flow<List<ActivityHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: ActivityHistoryEntity)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY id ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE category = :category ORDER BY id ASC")
    fun getProductsByCategory(category: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProduct(id: Long)

    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY orderDate DESC")
    fun getOrdersForUser(userId: Long): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders ORDER BY orderDate DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long
    
    @Update
    suspend fun updateOrder(order: OrderEntity)
}

@Dao
interface WishlistDao {
    @Query("SELECT * FROM wishlist WHERE userId = :userId")
    fun getWishlistForUser(userId: Long): Flow<List<WishlistItemEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM wishlist WHERE userId = :userId AND productId = :productId LIMIT 1)")
    suspend fun existsInWishlist(userId: Long, productId: Long): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWishlistItem(item: WishlistItemEntity)

    @Query("DELETE FROM wishlist WHERE userId = :userId AND productId = :productId")
    suspend fun deleteWishlistItem(userId: Long, productId: Long)
}

@Dao
interface CartDao {
    @Query("SELECT * FROM cart WHERE userId = :userId")
    fun getCartForUser(userId: Long): Flow<List<CartItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItemEntity)

    @Update
    suspend fun updateCartItem(item: CartItemEntity)

    @Query("DELETE FROM cart WHERE id = :id")
    suspend fun deleteCartItem(id: Long)

    @Query("DELETE FROM cart WHERE userId = :userId AND productId = :productId")
    suspend fun deleteCartItemByProduct(userId: Long, productId: Long)

    @Query("DELETE FROM cart WHERE userId = :userId")
    suspend fun clearCartForUser(userId: Long)
}

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE productId = :productId ORDER BY timestamp DESC")
    fun getReviewsForProduct(productId: Long): Flow<List<ReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity)
}
