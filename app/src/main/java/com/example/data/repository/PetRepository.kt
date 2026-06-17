package com.example.data.repository

import com.example.data.database.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class PetRepository(private val db: AppDatabase) {

    // DAOs
    private val userDao = db.userDao()
    private val loginHistoryDao = db.loginHistoryDao()
    private val activityHistoryDao = db.activityHistoryDao()
    private val productDao = db.productDao()
    private val orderDao = db.orderDao()
    private val wishlistDao = db.wishlistDao()
    private val cartDao = db.cartDao()
    private val reviewDao = db.reviewDao()

    // Users
    suspend fun getUserByEmail(email: String): UserEntity? = userDao.getUserByEmail(email)
    suspend fun getUserById(id: Long): UserEntity? = userDao.getUserById(id)
    fun getAllUsersFlow(): Flow<List<UserEntity>> = userDao.getAllUsersFlow()
    suspend fun insertUser(user: UserEntity): Long = userDao.insertUser(user)
    suspend fun updateUser(user: UserEntity) = userDao.updateUser(user)

    // Login History
    fun getLoginHistoryForUser(userId: Long): Flow<List<LoginHistoryEntity>> = loginHistoryDao.getLoginHistoryForUser(userId)
    fun getAllLoginHistory(): Flow<List<LoginHistoryEntity>> = loginHistoryDao.getAllLoginHistory()
    suspend fun insertLoginHistory(history: LoginHistoryEntity): Long = loginHistoryDao.insertLoginHistory(history)
    suspend fun updateLogoutTime(id: Long, timestamp: Long) = loginHistoryDao.updateLogoutTime(id, timestamp)

    // Activity History
    fun getActivityHistoryForUser(userId: Long): Flow<List<ActivityHistoryEntity>> = activityHistoryDao.getActivityHistoryForUser(userId)
    suspend fun logActivity(userId: Long, actionType: String, description: String) {
        activityHistoryDao.insertActivity(
            ActivityHistoryEntity(
                userId = userId,
                actionType = actionType,
                description = description
            )
        )
    }

    // Products
    fun getAllProducts(): Flow<List<ProductEntity>> = productDao.getAllProducts()
    fun getProductsByCategory(category: String): Flow<List<ProductEntity>> = productDao.getProductsByCategory(category)
    suspend fun getProductById(id: Long): ProductEntity? = productDao.getProductById(id)
    suspend fun insertProduct(product: ProductEntity): Long = productDao.insertProduct(product)
    suspend fun updateProduct(product: ProductEntity) = productDao.updateProduct(product)
    suspend fun deleteProduct(id: Long) = productDao.deleteProduct(id)

    // Orders
    fun getOrdersForUser(userId: Long): Flow<List<OrderEntity>> = orderDao.getOrdersForUser(userId)
    fun getAllOrders(): Flow<List<OrderEntity>> = orderDao.getAllOrders()
    suspend fun insertOrder(order: OrderEntity): Long = orderDao.insertOrder(order)
    suspend fun updateOrder(order: OrderEntity) = orderDao.updateOrder(order)

    // Wishlist
    fun getWishlistForUser(userId: Long): Flow<List<WishlistItemEntity>> = wishlistDao.getWishlistForUser(userId)
    suspend fun existsInWishlist(userId: Long, productId: Long): Boolean = wishlistDao.existsInWishlist(userId, productId)
    suspend fun toggleWishlist(userId: Long, productId: Long) {
        if (existsInWishlist(userId, productId)) {
            wishlistDao.deleteWishlistItem(userId, productId)
            logActivity(userId, "WISHLIST_ADD", "Removed product #$productId from wishlist")
        } else {
            wishlistDao.insertWishlistItem(WishlistItemEntity(userId = userId, productId = productId))
            logActivity(userId, "WISHLIST_ADD", "Added product #$productId to wishlist")
        }
    }

    // Cart
    fun getCartForUser(userId: Long): Flow<List<CartItemEntity>> = cartDao.getCartForUser(userId)
    suspend fun addToCart(userId: Long, productId: Long, quantity: Int = 1) {
        val currentCart = cartDao.getCartForUser(userId).first()
        val existing = currentCart.find { it.productId == productId }
        if (existing != null) {
            existing.quantity += quantity
            cartDao.updateCartItem(existing)
        } else {
            cartDao.insertCartItem(CartItemEntity(userId = userId, productId = productId, quantity = quantity))
        }
        logActivity(userId, "CART_ADD", "Added $quantity of product #$productId to cart")
    }
    suspend fun updateCartQuantity(userId: Long, productId: Long, quantity: Int) {
        val currentCart = cartDao.getCartForUser(userId).first()
        val existing = currentCart.find { it.productId == productId }
        if (existing != null) {
            if (quantity <= 0) {
                cartDao.deleteCartItem(existing.id)
            } else {
                existing.quantity = quantity
                cartDao.updateCartItem(existing)
            }
        }
    }
    suspend fun removeCartItem(userId: Long, productId: Long) {
        cartDao.deleteCartItemByProduct(userId, productId)
    }
    suspend fun clearCart(userId: Long) {
        cartDao.clearCartForUser(userId)
    }

    // Reviews
    fun getReviewsForProduct(productId: Long): Flow<List<ReviewEntity>> = reviewDao.getReviewsForProduct(productId)
    suspend fun insertReview(review: ReviewEntity) {
        reviewDao.insertReview(review)
        
        // Log review activity
        logActivity(review.userId, "VIEW", "Submitted a ${review.rating}-star review for product #${review.productId}")
        
        // Optionally update overall rating for product
        val product = productDao.getProductById(review.productId)
        if (product != null) {
            val allReviews = reviewDao.getReviewsForProduct(review.productId).first()
            val finalRating = if (allReviews.isEmpty()) review.rating else {
                (allReviews.map { it.rating }.sum() + review.rating) / (allReviews.size + 1)
            }
            productDao.updateProduct(product.copy(rating = finalRating))
        }
    }

    // Seeding products on empty checking
    suspend fun seedDatabaseIfEmpty() {
        val existing = productDao.getAllProducts().first()
        if (existing.isNotEmpty()) return

        val sampleProducts = listOf(
            // Dog Products
            ProductEntity(
                name = "Premium Dog Food",
                category = "Dog Products",
                description = "Nutritious, high-protein organic dry pet kibbles formulated for adult dogs of all breeds. Supports shining coats and energetic hearts.",
                price = 45.99,
                discount = 10.0,
                rating = 4.8f,
                stockStatus = "In Stock"
            ),
            ProductEntity(
                name = "Heavy Duty Dog Leash",
                category = "Dog Products",
                description = "A reflective night-safe, dual-handled climbing nylon leash tailored for strong dogs. Heavy rust-proof tactical clasp.",
                price = 18.50,
                discount = 0.0,
                rating = 4.6f,
                stockStatus = "In Stock"
            ),
            ProductEntity(
                name = "Memory Foam Dog Bed",
                category = "Dog Products",
                description = "Orthopedic soft memory foam pet bed with removable, machine-washable plush cover. Ultimate support for dog joints.",
                price = 59.99,
                discount = 15.0,
                rating = 4.9f,
                stockStatus = "In Stock"
            ),
            ProductEntity(
                name = "Natural Rubber Bone Toy",
                category = "Dog Products",
                description = "Virtually indestructible chewing bone toy made of non-toxic natural rubber. Cleans pet teeth and massages gums.",
                price = 12.99,
                discount = 5.0,
                rating = 4.4f,
                stockStatus = "In Stock"
            ),
            ProductEntity(
                name = "Oatmeal Dog Shampoo",
                category = "Dog Products",
                description = "Soothing formula containing natural oatmeal, lavender essence and aloe vera. Perfect for sensitive or itchy dog skin.",
                price = 14.25,
                discount = 0.0,
                rating = 4.5f,
                stockStatus = "Low Stock"
            ),

            // Cat Products
            ProductEntity(
                name = "Premium Salmon Cat Food",
                category = "Cat Products",
                description = "Delectable grain-free dry food formulated with real wild salmon, antioxidants, and life-source vitamins.",
                price = 39.99,
                discount = 12.0,
                rating = 4.7f,
                stockStatus = "In Stock"
            ),
            ProductEntity(
                name = "Sifting Hooded Litter Box",
                category = "Cat Products",
                description = "Odour-absorbing spacious dome litter box with easy snap-locks. Reduces tracking and guards splash messes.",
                price = 34.50,
                discount = 0.0,
                rating = 4.3f,
                stockStatus = "In Stock"
            ),
            ProductEntity(
                name = "Sisal Cat Scratching Post",
                category = "Cat Products",
                description = "Robust vertical tower wrapped in high-quality natural sisal fibers. Promotes healthy scratching habits and active play.",
                price = 28.00,
                discount = 20.0,
                rating = 4.5f,
                stockStatus = "In Stock"
            ),
            ProductEntity(
                name = "Interactive Laser Toy",
                category = "Cat Products",
                description = "Automated 360-degree rotating laser indicator that sparkles cat curiosities, prompting healthy running exercises.",
                price = 16.99,
                discount = 10.0,
                rating = 4.2f,
                stockStatus = "In Stock"
            ),
            ProductEntity(
                name = "Fluffy Marshmallow Cat Bed",
                category = "Cat Products",
                description = "Ultra self-warming polyester plush cat nest. Super cozy design promotes restful sleeping habits.",
                price = 22.99,
                discount = 0.0,
                rating = 4.8f,
                stockStatus = "Out of Stock"
            ),

            // Bird Products
            ProductEntity(
                name = "Palace Dome Bird Cage",
                category = "Bird Products",
                description = "Premium brass-style cage containing wood perches, security slide locks, and pull-out easy-clean base trays.",
                price = 85.00,
                discount = 10.0,
                rating = 4.7f,
                stockStatus = "In Stock"
            ),
            ProductEntity(
                name = "Fortified Organic Bird Seeds",
                category = "Bird Products",
                description = "Nutritious seed compilation featuring millet, canary grass seed and grains enriched with canary prebiotics.",
                price = 9.99,
                discount = 0.0,
                rating = 4.6f,
                stockStatus = "In Stock"
            ),
            ProductEntity(
                name = "Gravity Flow Water Feeder",
                category = "Bird Products",
                description = "Automatic bird feeder with transparent food container, easy slide-cage hooks and non-drip spout.",
                price = 7.50,
                discount = 0.0,
                rating = 4.1f,
                stockStatus = "In Stock"
            ),
            ProductEntity(
                name = "Hanging Wood Bird Swing",
                category = "Bird Products",
                description = "Composed of natural safe wood, bells and beads. Relieves bird cage boredom and exercises tiny claws.",
                price = 6.99,
                discount = 15.0,
                rating = 4.4f,
                stockStatus = "In Stock"
            ),
            ProductEntity(
                name = "Interactive Bird Toy Bundle",
                category = "Bird Products",
                description = "Colorful shatterproof mirrors, ropes and wooden chew structures that stimulate chirping intellects.",
                price = 11.50,
                discount = 0.0,
                rating = 4.5f,
                stockStatus = "Low Stock"
            ),

            // Fish Products
            ProductEntity(
                name = "Tempered Glass Aquarium Tank",
                category = "Fish Products",
                description = "Crystal clear rimless 10-gallon heavy tempered glass tank. Modern aesthetic design for desks or counters.",
                price = 79.99,
                discount = 10.0,
                rating = 4.8f,
                stockStatus = "In Stock"
            ),
            ProductEntity(
                name = "Tropical Fish Flakes",
                category = "Fish Products",
                description = "Specially formulated trace flakes containing natural algae extracts, supporting vivid color enhancements.",
                price = 8.99,
                discount = 0.0,
                rating = 4.7f,
                stockStatus = "In Stock"
            ),
            ProductEntity(
                name = "Silent Multistage Water Filter",
                category = "Fish Products",
                description = "Under-surface submersive cycle filter providing triple oxygenating physical, chemical and bio cleanings.",
                price = 24.99,
                discount = 15.0,
                rating = 4.5f,
                stockStatus = "In Stock"
            ),
            ProductEntity(
                name = "Decorative Exotic Plants Set",
                category = "Fish Products",
                description = "Pack of 6 soft-textured glowing artificial visual safe plants that create stunning tropical visual scenarios.",
                price = 13.50,
                discount = 0.0,
                rating = 4.3f,
                stockStatus = "In Stock"
            ),
            ProductEntity(
                name = "Smart LED Aquarium Light",
                category = "Fish Products",
                description = "Full spectrum dimmable visual light with sunrise/sunset auto timers and moonlight sleep settings.",
                price = 32.00,
                discount = 5.0,
                rating = 4.6f,
                stockStatus = "Low Stock"
            ),

            // Rabbit Products
            ProductEntity(
                name = "Timothy Hay-Based Rabbit Food",
                category = "Rabbit Products",
                description = "Nutritionally balanced high-fiber pellets loaded with meadow grass extracts, supporting healthy bunny digestions.",
                price = 16.50,
                discount = 0.0,
                rating = 4.8f,
                stockStatus = "In Stock"
            ),
            ProductEntity(
                name = "Folding Wood Rabbit House",
                category = "Rabbit Products",
                description = "Cosy natural dried pine wood hideout castle for rabbits or guinea pigs. Chew-safe and cozy security cabins.",
                price = 36.90,
                discount = 10.0,
                rating = 4.6f,
                stockStatus = "In Stock"
            ),
            ProductEntity(
                name = "Non-Drip Glass Water Bottle",
                category = "Rabbit Products",
                description = "Heavy double metal ball-point drinking straw. Easy wire hooks attachment structure to cages.",
                price = 11.20,
                discount = 0.0,
                rating = 4.2f,
                stockStatus = "In Stock"
            ),
            ProductEntity(
                name = "Natural Rabbit Wood Toy Set",
                category = "Rabbit Products",
                description = "10-pack of pine dumbells, grass spheres and bark logs designed for chewing needs of rabbit teeth.",
                price = 15.00,
                discount = 15.0,
                rating = 4.5f,
                stockStatus = "In Stock"
            ),
            ProductEntity(
                name = "Ultra Absorbent Rabbit Bedding",
                category = "Rabbit Products",
                description = "Odor control compressed pine pellet bedding. Biodegradable, highly vacuum absorbent and toxic-free.",
                price = 19.99,
                discount = 0.0,
                rating = 4.7f,
                stockStatus = "In Stock"
            )
        )

        for (p in sampleProducts) {
            productDao.insertProduct(p)
        }
    }
}
