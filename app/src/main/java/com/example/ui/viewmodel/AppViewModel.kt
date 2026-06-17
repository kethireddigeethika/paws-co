package com.example.ui.viewmodel

import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.*
import com.example.data.repository.PetRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.security.MessageDigest

class AppViewModel(private val repository: PetRepository) : ViewModel() {

    // Central Screen Navigation
    private val _currentScreen = MutableStateFlow("LOGIN") // LOGIN, SIGNUP, HOME, CATALOG, PRODUCT_DETAILS, CART, WISHLIST, CHECKOUT, DASHBOARD, ADMIN_DASHBOARD
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Shared selected entities
    private val _selectedProduct = MutableStateFlow<ProductEntity?>(null)
    val selectedProduct: StateFlow<ProductEntity?> = _selectedProduct.asStateFlow()

    // Preferences / Dark Theme
    private val _darkThemeMode = MutableStateFlow(false)
    val darkThemeMode: StateFlow<Boolean> = _darkThemeMode.asStateFlow()

    // Toast/Notification state
    private val _activeNotification = MutableStateFlow<String?>(null)
    val activeNotification: StateFlow<String?> = _activeNotification.asStateFlow()

    // User Authentication State
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private var currentLoginHistoryId: Long? = null

    // Shopping Cart & Wishlist & Orders (Reactive Flows)
    val productList: StateFlow<List<ProductEntity>> = repository.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsersList: StateFlow<List<UserEntity>> = repository.getAllUsersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrdersList: StateFlow<List<OrderEntity>> = repository.getAllOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLoginHistoryList: StateFlow<List<LoginHistoryEntity>> = repository.getAllLoginHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI filters for CATALOG
    val activeCategory = MutableStateFlow("All")
    val searchQuery = MutableStateFlow("")
    val priceFilterMax = MutableStateFlow(100.0)
    val ratingFilterMin = MutableStateFlow(0f)
    val inStockOnlyFilter = MutableStateFlow(false)
    val catalogSortOption = MutableStateFlow("Default") // Default, PriceLowToHigh, PriceHighToLow, Rating

    // Dynamic Filtered Products
    val filteredProducts: StateFlow<List<ProductEntity>> = combine(
        productList,
        activeCategory,
        searchQuery,
        priceFilterMax,
        ratingFilterMin,
        inStockOnlyFilter,
        catalogSortOption
    ) { flowsArray ->
        @Suppress("UNCHECKED_CAST")
        val products = flowsArray[0] as List<ProductEntity>
        val category = flowsArray[1] as String
        val query = flowsArray[2] as String
        val priceMax = flowsArray[3] as Double
        val ratingMin = flowsArray[4] as Float
        val inStock = flowsArray[5] as Boolean
        val sortOpt = flowsArray[6] as String

        var list = products

        // Category filter
        if (category != "All") {
            list = list.filter { it.category == category }
        }

        // Search query filter
        if (query.isNotBlank()) {
            list = list.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true)
            }
        }

        // Price range filter
        list = list.filter { it.price * (1 - (it.discount / 100.0)) <= priceMax }

        // Rating filter
        list = list.filter { it.rating >= ratingMin }

        // Stock filter
        if (inStock) {
            list = list.filter { it.stockStatus == "In Stock" || it.stockStatus == "Low Stock" }
        }

        // Sorting
        when (sortOpt) {
            "PriceLowToHigh" -> list.sortedBy { it.price * (1 - (it.discount / 100.0)) }
            "PriceHighToLow" -> list.sortedByDescending { it.price * (1 - (it.discount / 100.0)) }
            "Rating" -> list.sortedByDescending { it.rating }
            else -> list
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active User Specific flows
    private val _cartItems = MutableStateFlow<List<CartItemEntity>>(emptyList())
    val cartItems: StateFlow<List<CartItemEntity>> = _cartItems.asStateFlow()

    private val _wishlistItems = MutableStateFlow<List<WishlistItemEntity>>(emptyList())
    val wishlistItems: StateFlow<List<WishlistItemEntity>> = _wishlistItems.asStateFlow()

    private val _userOrders = MutableStateFlow<List<OrderEntity>>(emptyList())
    val userOrders: StateFlow<List<OrderEntity>> = _userOrders.asStateFlow()

    private val _userLoginHistory = MutableStateFlow<List<LoginHistoryEntity>>(emptyList())
    val userLoginHistory: StateFlow<List<LoginHistoryEntity>> = _userLoginHistory.asStateFlow()

    private val _userActivityHistory = MutableStateFlow<List<ActivityHistoryEntity>>(emptyList())
    val userActivityHistory: StateFlow<List<ActivityHistoryEntity>> = _userActivityHistory.asStateFlow()

    // Active Product Review Flow
    private val _activeProductReviews = MutableStateFlow<List<ReviewEntity>>(emptyList())
    val activeProductReviews: StateFlow<List<ReviewEntity>> = _activeProductReviews.asStateFlow()

    // Coupon tracking
    val appliedCoupon = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
        }
    }

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    fun toggleDarkMode() {
        _darkThemeMode.value = !_darkThemeMode.value
    }

    fun showToast(msg: String) {
        _activeNotification.value = msg
        viewModelScope.launch {
            kotlinx.coroutines.delay(2500)
            if (_activeNotification.value == msg) {
                _activeNotification.value = null
            }
        }
    }

    // Select Product and track View activity
    fun selectProduct(product: ProductEntity) {
        _selectedProduct.value = product
        _currentScreen.value = "PRODUCT_DETAILS"
        
        val user = _currentUser.value
        if (user != null) {
            viewModelScope.launch {
                repository.logActivity(user.id, "VIEW", "Viewed product: ${product.name}")
                // Refresh reviews
                repository.getReviewsForProduct(product.id).collect {
                    _activeProductReviews.value = it
                }
            }
        }
    }

    // SHA-256 encryption helper
    private fun hashPassword(password: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(password.toByteArray(Charsets.UTF_8))
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            password // Fallback to raw if exception
        }
    }

    // AUTH SYSTEM: SIGNUP
    fun signupUser(fullName: String, email: String, mobile: String, pass: String, confirmPass: String, onSuccess: () -> Unit) {
        if (fullName.isBlank() || email.isBlank() || mobile.isBlank() || pass.isBlank()) {
            showToast("Please fill all signup fields.")
            return
        }
        if (pass != confirmPass) {
            showToast("Passwords do not match.")
            return
        }
        if (pass.length < 6) {
            showToast("Password must be at least 6 characters.")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Invalid email address format.")
            return
        }

        viewModelScope.launch {
            val existing = repository.getUserByEmail(email)
            if (existing != null) {
                showToast("Email address already registered.")
                return@launch
            }

            val newUser = UserEntity(
                fullName = fullName,
                email = email,
                mobileNumber = mobile,
                passwordHashed = hashPassword(pass)
            )

            val id = repository.insertUser(newUser)
            showToast("Account created successfully! Please Login.")
            onSuccess()
        }
    }

    // AUTH SYSTEM: LOGIN
    fun loginUser(email: String, pass: String, rememberMe: Boolean, onSuccess: () -> Unit) {
        if (email.isBlank() || pass.isBlank()) {
            showToast("Please enter your email and password.")
            return
        }

        viewModelScope.launch {
            val user = repository.getUserByEmail(email)
            if (user == null || user.passwordHashed != hashPassword(pass)) {
                showToast("Invalid email or password.")
                return@launch
            }

            // Login verified!
            _currentUser.value = user
            
            // Record login history
            val device = "Android " + Build.VERSION.RELEASE
            val browser = "Native Applet Shell"
            val ip = "127.0.0.1"
            
            val logId = repository.insertLoginHistory(
                LoginHistoryEntity(
                    userId = user.id,
                    deviceInfo = device,
                    browserInfo = browser,
                    ipAddress = ip
                )
            )
            currentLoginHistoryId = logId

            repository.logActivity(user.id, "LOGIN", "Successfully logged in to Pet Paws")
            
            // Update last login date
            repository.updateUser(user.copy(lastLogin = System.currentTimeMillis()))

            // Hook up user reactive flows
            launch { observeUserSpecificData(user.id) }

            showToast("Welcome back, ${user.fullName}!")
            _currentScreen.value = "HOME"
            onSuccess()
        }
    }

    // Admin Quick Login (Bypass or Admin flow)
    fun loginAdmin(pass: String) {
        if (pass == "admin123") {
            // Seed a mock admin user
            val adminUser = UserEntity(
                id = -999L,
                fullName = "Administrator",
                email = "admin@petpaws.com",
                mobileNumber = "9999999999",
                passwordHashed = "hashed"
            )
            _currentUser.value = adminUser
            showToast("Logged in as Administrator")
            _currentScreen.value = "ADMIN_DASHBOARD"
        } else {
            showToast("Incorrect Admin PIN")
        }
    }

    fun logoutUser() {
        val user = _currentUser.value
        val logId = currentLoginHistoryId
        if (user != null) {
            viewModelScope.launch {
                repository.logActivity(user.id, "LOGIN", "Logged out from application")
                if (logId != null) {
                    repository.updateLogoutTime(logId, System.currentTimeMillis())
                }
                _currentUser.value = null
                currentLoginHistoryId = null
                _currentScreen.value = "LOGIN"
                showToast("Logged out successfully.")
            }
        } else {
            _currentScreen.value = "LOGIN"
        }
    }

    private suspend fun observeUserSpecificData(userId: Long) {
        repository.getCartForUser(userId).collect { _cartItems.value = it }
    }

    init {
        // Automatically respond to currentUser changes and collect related flows in background
        viewModelScope.launch {
            _currentUser.collectLatest { user ->
                if (user != null && user.id != -999L) {
                    launch { repository.getCartForUser(user.id).collect { _cartItems.value = it } }
                    launch { repository.getWishlistForUser(user.id).collect { _wishlistItems.value = it } }
                    launch { repository.getOrdersForUser(user.id).collect { _userOrders.value = it } }
                    launch { repository.getLoginHistoryForUser(user.id).collect { _userLoginHistory.value = it } }
                    launch { repository.getActivityHistoryForUser(user.id).collect { _userActivityHistory.value = it } }
                } else {
                    _cartItems.value = emptyList()
                    _wishlistItems.value = emptyList()
                    _userOrders.value = emptyList()
                    _userLoginHistory.value = emptyList()
                    _userActivityHistory.value = emptyList()
                }
            }
        }
    }

    // CART ACTIONS
    fun addToCart(product: ProductEntity, qty: Int = 1) {
        val user = _currentUser.value
        if (user == null) {
            showToast("Please log in to add items to cart.")
            _currentScreen.value = "LOGIN"
            return
        }
        viewModelScope.launch {
            repository.addToCart(user.id, product.id, qty)
            showToast("Added ${product.name} to Cart")
        }
    }

    fun updateCartQty(productId: Long, qty: Int) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.updateCartQuantity(user.id, productId, qty)
        }
    }

    fun removeCartItem(productId: Long) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.removeCartItem(user.id, productId)
            showToast("Item removed from Cart")
        }
    }

    // WISHLIST ACTIONS
    fun toggleWishlist(product: ProductEntity) {
        val user = _currentUser.value
        if (user == null) {
            showToast("Please log in to manage your wishlist.")
            _currentScreen.value = "LOGIN"
            return
        }
        viewModelScope.launch {
            repository.toggleWishlist(user.id, product.id)
            // Re-fetch wishlist for confirmation
            val isNowIn = repository.existsInWishlist(user.id, product.id)
            if (isNowIn) {
                showToast("Added ${product.name} to Wishlist")
            } else {
                showToast("Removed ${product.name} from Wishlist")
            }
        }
    }

    fun isWishlisted(productId: Long): Boolean {
        return _wishlistItems.value.any { it.productId == productId }
    }

    // SUBMIT REVIEW
    fun submitReview(productId: Long, comment: String, rating: Float) {
        val user = _currentUser.value
        if (user == null) {
            showToast("Please log in to submit a review.")
            return
        }
        if (comment.isBlank()) {
            showToast("Please enter a comment.")
            return
        }
        viewModelScope.launch {
            val rev = ReviewEntity(
                productId = productId,
                userId = user.id,
                userName = user.fullName,
                comment = comment,
                rating = rating
            )
            repository.insertReview(rev)
            showToast("Thank you for your review!")
        }
    }

    // CHECKOUT / PLACE ORDER
    fun placeOrder(
        fullName: String,
        address: String,
        city: String,
        state: String,
        pinCode: String,
        phone: String,
        paymentMethod: String,
        onSuccess: () -> Unit
    ) {
        val user = _currentUser.value ?: return
        val currentCart = _cartItems.value
        if (currentCart.isEmpty()) {
            showToast("Your cart is empty.")
            return
        }
        if (fullName.isBlank() || address.isBlank() || city.isBlank() || state.isBlank() || pinCode.isBlank() || phone.isBlank()) {
            showToast("Please fill all shipping information.")
            return
        }

        viewModelScope.launch {
            // Build CSV-like list of products: "[ProductId:Quantity;Price]"
            val itemsDetails = mutableListOf<String>()
            var total = 0.0
            
            for (c in currentCart) {
                val prod = repository.getProductById(c.productId)
                if (prod != null) {
                    val finalPrice = prod.price * (1.0 - (prod.discount / 100.0))
                    itemsDetails.add("${prod.name} (x${c.quantity}) - ₹${"%.2f".format(finalPrice)}")
                    total += finalPrice * c.quantity
                }
            }

            // Calculations
            val tax = total * 0.05
            var finalTotal = total + tax
            if (appliedCoupon.value == "PAWS20") {
                finalTotal *= 0.8
            }

            val productsString = itemsDetails.joinToString(" | ")

            val order = OrderEntity(
                userId = user.id,
                productsJson = productsString,
                totalPrice = finalTotal,
                fullName = fullName,
                address = address,
                city = city,
                state = state,
                pinCode = pinCode,
                phoneNumber = phone,
                paymentMethod = paymentMethod
            )

            repository.insertOrder(order)
            repository.clearCart(user.id)
            repository.logActivity(user.id, "ORDER_PLACE", "Placed Order for ₹${"%.2f".format(finalTotal)} via $paymentMethod")
            
            appliedCoupon.value = null
            showToast("Order Placed Successfully!")
            onSuccess()
        }
    }

    // ADMIN CONTROLS
    fun adminDeleteProduct(id: Long) {
        viewModelScope.launch {
            repository.deleteProduct(id)
            showToast("Product deleted successfully.")
        }
    }

    fun adminAddProduct(name: String, cat: String, desc: String, price: Double, discount: Double, stock: String) {
        if (name.isBlank() || desc.isBlank() || price <= 0) {
            showToast("Please enter valid product details.")
            return
        }
        viewModelScope.launch {
            val p = ProductEntity(
                name = name,
                category = cat,
                description = desc,
                price = price,
                discount = discount,
                stockStatus = stock,
                isCustomAdded = true
            )
            repository.insertProduct(p)
            showToast("Product added successfully.")
        }
    }

    fun adminEditProduct(id: Long, name: String, cat: String, desc: String, price: Double, discount: Double, stock: String) {
        if (name.isBlank() || desc.isBlank() || price <= 0) {
            showToast("Please enter valid product details.")
            return
        }
        viewModelScope.launch {
            val p = ProductEntity(
                id = id,
                name = name,
                category = cat,
                description = desc,
                price = price,
                discount = discount,
                stockStatus = stock,
                isCustomAdded = true
            )
            repository.insertProduct(p) // replaces
            showToast("Product updated successfully.")
        }
    }
    
    fun adminUpdateOrderStatus(order: OrderEntity, nextStatus: String) {
        viewModelScope.launch {
            repository.updateOrder(order.copy(status = nextStatus))
            showToast("Order status updated to $nextStatus")
        }
    }

    // PROFILE CONTROLS
    fun updateUserProfile(fullName: String, mobileNumber: String) {
        val user = _currentUser.value ?: return
        if (fullName.isBlank() || mobileNumber.isBlank()) {
            showToast("Please enter valid profile details.")
            return
        }
        viewModelScope.launch {
            val updated = user.copy(fullName = fullName, mobileNumber = mobileNumber)
            repository.updateUser(updated)
            _currentUser.value = updated
            repository.logActivity(user.id, "PROFILE_CHANGE", "Updated profile profile fields")
            showToast("Profile updated successfully")
        }
    }

    fun changeUserPassword(oldPass: String, newPass: String) {
        val user = _currentUser.value ?: return
        if (oldPass.isBlank() || newPass.isBlank()) {
            showToast("Fields cannot be empty")
            return
        }
        if (newPass.length < 6) {
            showToast("New password must be at least 6 characters")
            return
        }
        viewModelScope.launch {
            if (user.passwordHashed != hashPassword(oldPass)) {
                showToast("Current password is incorrect.")
                return@launch
            }
            val updated = user.copy(passwordHashed = hashPassword(newPass))
            repository.updateUser(updated)
            _currentUser.value = updated
            repository.logActivity(user.id, "PROFILE_CHANGE", "Changed account password")
            showToast("Password updated successfully.")
        }
    }
}

// ViewModel Factory
class AppViewModelFactory(private val repository: PetRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
