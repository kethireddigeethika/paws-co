package com.example.ui.screens

import com.example.ui.theme.MyApplicationTheme
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.*
import com.example.ui.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: AppViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val activeNotification by viewModel.activeNotification.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.darkThemeMode.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val productsList by viewModel.productList.collectAsStateWithLifecycle()

    var showSidebar by remember { mutableStateOf(false) }

    MyApplicationTheme(darkTheme = isDarkTheme) {
        val colors = MaterialTheme.colorScheme
        
        Scaffold(
            topBar = {
                if (currentScreen != "LOGIN" && currentScreen != "SIGNUP") {
                    CenterAlignedTopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Pets,
                                    contentDescription = "Paws & Co. Logo",
                                    tint = colors.primary,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "Paws & Co.",
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = colors.tertiary,
                                        letterSpacing = (-0.5).sp
                                    )
                                    Text(
                                        "PREMIUM PET BOUTIQUE",
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 8.sp,
                                        color = colors.primary,
                                        letterSpacing = 1.5.sp
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { showSidebar = true }, modifier = Modifier.testTag("menu_button")) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        },
                        actions = {
                            // Dark Mode Toggle
                            IconButton(onClick = { viewModel.toggleDarkMode() }, modifier = Modifier.testTag("dark_mode_toggle")) {
                                Icon(
                                    imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "Theme Toggle",
                                    tint = colors.primary
                                )
                            }
                            // Cart Action
                            IconButton(onClick = { viewModel.navigateTo("CART") }, modifier = Modifier.testTag("top_bar_cart")) {
                                BadgedBox(
                                    badge = {
                                        if (cartItems.isNotEmpty()) {
                                            val count = cartItems.sumOf { it.quantity }
                                            Badge { Text(count.toString()) }
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", tint = colors.primary)
                                }
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = colors.background
                        )
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.background)
                    .padding(innerPadding)
            ) {
                // Crossfade screen swapping for fluid experience
                Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                    when (screen) {
                        "LOGIN" -> LoginScreen(viewModel)
                        "SIGNUP" -> SignupScreen(viewModel)
                        "HOME" -> HomeScreen(viewModel)
                        "CATALOG" -> CatalogScreen(viewModel)
                        "PRODUCT_DETAILS" -> ProductDetailsScreen(viewModel)
                        "CART" -> CartScreen(viewModel)
                        "WISHLIST" -> WishlistScreen(viewModel)
                        "CHECKOUT" -> CheckoutScreen(viewModel)
                        "DASHBOARD" -> DashboardScreen(viewModel)
                        "ADMIN_DASHBOARD" -> AdminDashboardScreen(viewModel)
                    }
                }

                // Custom notification bubble
                activeNotification?.let { msg ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
                            .shadow(8.dp, RoundedCornerShape(12.dp))
                            .background(colors.tertiary, RoundedCornerShape(12.dp))
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                            .testTag("app_notification")
                    ) {
                        Text(
                            msg,
                            color = colors.onTertiary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }

                // Sliding custom drawer (Glassmorphism navigation)
                if (showSidebar) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { showSidebar = false }
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(0.75f)
                                .align(Alignment.TopStart)
                                .shadow(16.dp)
                                .testTag("sidebar_drawer"),
                            color = colors.background,
                            shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(24.dp)
                            ) {
                                // Close Drawer
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Pets, contentDescription = null, tint = colors.primary)
                                        Spacer(Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                "Paws & Co.",
                                                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 20.sp,
                                                color = colors.tertiary,
                                                letterSpacing = (-0.5).sp
                                            )
                                            Text(
                                                "PREMIUM PET BOUTIQUE",
                                                fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 8.sp,
                                                color = colors.primary,
                                                letterSpacing = 1.2.sp
                                            )
                                        }
                                    }
                                    IconButton(onClick = { showSidebar = false }) {
                                        Icon(Icons.Default.Close, contentDescription = "Close Menu")
                                    }
                                }

                                Spacer(Modifier.height(24.dp))

                                // Profile Capsule in Menu
                                currentUser?.let { user ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 24.dp),
                                        colors = CardDefaults.cardColors(containerColor = colors.secondary.copy(alpha = 0.4f)),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .background(colors.primary, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    user.fullName.take(1).uppercase(),
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 20.sp
                                                )
                                            }
                                            Spacer(Modifier.width(12.dp))
                                            Column {
                                                Text(user.fullName, fontWeight = FontWeight.Bold, color = colors.tertiary)
                                                Text(user.email, fontSize = 12.sp, color = colors.onBackground.copy(alpha = 0.7f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                        }
                                    }
                                }

                                Divider(color = colors.onBackground.copy(alpha = 0.1f), modifier = Modifier.padding(bottom = 16.dp))

                                // Menu Links
                                NavigationMenuItem(
                                    label = "Home Screen",
                                    icon = Icons.Default.Home,
                                    selected = currentScreen == "HOME",
                                    onClick = { viewModel.navigateTo("HOME"); showSidebar = false },
                                    tag = "nav_home"
                                )
                                NavigationMenuItem(
                                    label = "Shop Catalog",
                                    icon = Icons.Default.Storefront,
                                    selected = currentScreen == "CATALOG",
                                    onClick = { viewModel.navigateTo("CATALOG"); showSidebar = false },
                                    tag = "nav_catalog"
                                )
                                NavigationMenuItem(
                                    label = "Your Shopping Cart",
                                    icon = Icons.Default.ShoppingCart,
                                    selected = currentScreen == "CART",
                                    onClick = { viewModel.navigateTo("CART"); showSidebar = false },
                                    tag = "nav_cart"
                                )
                                NavigationMenuItem(
                                    label = "Wishlist Favorites",
                                    icon = Icons.Default.Favorite,
                                    selected = currentScreen == "WISHLIST",
                                    onClick = { viewModel.navigateTo("WISHLIST"); showSidebar = false },
                                    tag = "nav_wishlist"
                                )
                                NavigationMenuItem(
                                    label = "Customer Profile",
                                    icon = Icons.Default.Person,
                                    selected = currentScreen == "DASHBOARD",
                                    onClick = { viewModel.navigateTo("DASHBOARD"); showSidebar = false },
                                    tag = "nav_dashboard"
                                )

                                Spacer(modifier = Modifier.height(16.dp))
                                Divider(color = colors.onBackground.copy(alpha = 0.1f), modifier = Modifier.padding(bottom = 16.dp))

                                // Admin Section In Menu
                                if (currentUser?.id == -999L) {
                                    NavigationMenuItem(
                                        label = "Admin Dashboard",
                                        icon = Icons.Default.AdminPanelSettings,
                                        selected = currentScreen == "ADMIN_DASHBOARD",
                                        onClick = { viewModel.navigateTo("ADMIN_DASHBOARD"); showSidebar = false },
                                        tag = "nav_admin"
                                    )
                                } else {
                                    NavigationMenuItem(
                                        label = "Admin Terminal Portal",
                                        icon = Icons.Default.Lock,
                                        selected = false,
                                        onClick = { viewModel.navigateTo("LOGIN"); showSidebar = false },
                                        tag = "nav_admin_login"
                                    )
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                // Out Button
                                if (currentUser != null) {
                                    Button(
                                        onClick = { viewModel.logoutUser(); showSidebar = false },
                                        colors = ButtonDefaults.buttonColors(containerColor = colors.error),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 16.dp)
                                            .testTag("logout_button")
                                    ) {
                                        Icon(Icons.Default.ExitToApp, contentDescription = null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Log Out", fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Button(
                                        onClick = { viewModel.navigateTo("LOGIN"); showSidebar = false },
                                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 16.dp)
                                    ) {
                                        Text("Sign In", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NavigationMenuItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) colors.primary.copy(alpha = 0.15f) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) colors.primary else colors.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(16.dp))
        Text(
            label,
            color = if (selected) colors.primary else colors.onBackground,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 15.sp
        )
    }
}

// -------------------------------------------------------------
// VECTOR PET GRAPHICS DRAWING CODE
// Renders adorable custom geometric vectors using Compose Canvas
// -------------------------------------------------------------
@Composable
fun PetIllustrationCanvas(
    category: String,
    modifier: Modifier = Modifier
) {
    val lightBeige = Color(0xFFE9DCC9)
    val warmBrown = Color(0xFF8B6B4A)
    val darkBrown = Color(0xFF5A3E2B)
    val eyeColor = Color(0xFF2D2D2D)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        drawCircle(
            color = lightBeige.copy(alpha = 0.3f),
            radius = w * 0.45f,
            center = Offset(cx, cy)
        )

        when (category) {
            "Dog Products" -> {
                // Main floppy ears
                drawOval(
                    color = warmBrown,
                    topLeft = Offset(cx - w * 0.35f, cy - h * 0.22f),
                    size = Size(w * 0.22f, h * 0.42f)
                )
                drawOval(
                    color = warmBrown,
                    topLeft = Offset(cx + w * 0.13f, cy - h * 0.22f),
                    size = Size(w * 0.22f, h * 0.42f)
                )
                // Head base
                drawCircle(
                    color = lightBeige,
                    radius = w * 0.25f,
                    center = Offset(cx, cy)
                )
                // Eyes
                drawCircle(
                    color = eyeColor,
                    radius = w * 0.035f,
                    center = Offset(cx - w * 0.09f, cy - h * 0.04f)
                )
                drawCircle(
                    color = eyeColor,
                    radius = w * 0.035f,
                    center = Offset(cx + w * 0.09f, cy - h * 0.04f)
                )
                // Snout
                drawOval(
                    color = Color.White,
                    topLeft = Offset(cx - w * 0.09f, cy),
                    size = Size(w * 0.18f, h * 0.13f)
                )
                // Nose
                drawCircle(
                    color = darkBrown,
                    radius = w * 0.04f,
                    center = Offset(cx, cy + h * 0.02f)
                )
                // Tongue or smile
                drawArc(
                    color = Color(0xFFFF8A80),
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(cx - w * 0.04f, cy + h * 0.06f),
                    size = Size(w * 0.08f, h * 0.06f)
                )
            }
            "Cat Products" -> {
                // Cat triangular ears
                val leftEar = androidx.compose.ui.graphics.Path().apply {
                    moveTo(cx - w * 0.22f, cy - h * 0.12f)
                    lineTo(cx - w * 0.26f, cy - h * 0.36f)
                    lineTo(cx - w * 0.06f, cy - h * 0.20f)
                    close()
                }
                val rightEar = androidx.compose.ui.graphics.Path().apply {
                    moveTo(cx + w * 0.22f, cy - h * 0.12f)
                    lineTo(cx + w * 0.26f, cy - h * 0.36f)
                    lineTo(cx + w * 0.06f, cy - h * 0.20f)
                    close()
                }
                drawPath(leftEar, color = darkBrown)
                drawPath(rightEar, color = darkBrown)

                // Inner ears
                val leftEarInner = androidx.compose.ui.graphics.Path().apply {
                    moveTo(cx - w * 0.19f, cy - h * 0.15f)
                    lineTo(cx - w * 0.22f, cy - h * 0.31f)
                    lineTo(cx - w * 0.09f, cy - h * 0.20f)
                    close()
                }
                drawPath(leftEarInner, color = Color(0xFFFFCDD2))

                // Cat Face
                drawCircle(
                    color = warmBrown,
                    radius = w * 0.23f,
                    center = Offset(cx, cy)
                )

                // Eyes
                drawCircle(
                    color = Color(0xFFCCFF90), // Green cat eyes
                    radius = w * 0.045f,
                    center = Offset(cx - w * 0.08f, cy - h * 0.03f)
                )
                drawCircle(
                    color = Color(0xFFCCFF90),
                    radius = w * 0.045f,
                    center = Offset(cx + w * 0.08f, cy - h * 0.03f)
                )
                // Pupils
                drawOval(
                    color = eyeColor,
                    topLeft = Offset(cx - w * 0.09f, cy - h * 0.05f),
                    size = Size(w * 0.018f, h * 0.05f)
                )
                drawOval(
                    color = eyeColor,
                    topLeft = Offset(cx + w * 0.072f, cy - h * 0.05f),
                    size = Size(w * 0.018f, h * 0.05f)
                )
                // Nose
                val nose = androidx.compose.ui.graphics.Path().apply {
                    moveTo(cx - w * 0.03f, cy + h * 0.04f)
                    lineTo(cx + w * 0.03f, cy + h * 0.04f)
                    lineTo(cx, cy + h * 0.07f)
                    close()
                }
                drawPath(nose, color = Color(0xFFFF8A80))

                // Whiskers
                drawLine(eyeColor, Offset(cx - w * 0.18f, cy + h * 0.06f), Offset(cx - w * 0.35f, cy + h * 0.04f), strokeWidth = 3f)
                drawLine(eyeColor, Offset(cx - w * 0.18f, cy + h * 0.08f), Offset(cx - w * 0.37f, cy + h * 0.09f), strokeWidth = 3f)
                drawLine(eyeColor, Offset(cx + w * 0.18f, cy + h * 0.06f), Offset(cx + w * 0.35f, cy + h * 0.04f), strokeWidth = 3f)
                drawLine(eyeColor, Offset(cx + w * 0.18f, cy + h * 0.08f), Offset(cx + w * 0.37f, cy + h * 0.09f), strokeWidth = 3f)
            }
            "Bird Products" -> {
                // Bird body circle
                drawCircle(
                    color = Color(0xFFFFEB3B), // Yellow bird
                    radius = w * 0.22f,
                    center = Offset(cx, cy + h * 0.05f)
                )
                // Head
                drawCircle(
                    color = Color(0xFFFFEB3B),
                    radius = w * 0.15f,
                    center = Offset(cx, cy - h * 0.12f)
                )
                // Eye
                drawCircle(
                    color = eyeColor,
                    radius = w * 0.025f,
                    center = Offset(cx + w * 0.06f, cy - h * 0.15f)
                )
                // Beak
                val beak = androidx.compose.ui.graphics.Path().apply {
                    moveTo(cx + w * 0.12f, cy - h * 0.16f)
                    lineTo(cx + w * 0.25f, cy - h * 0.11f)
                    lineTo(cx + w * 0.10f, cy - h * 0.08f)
                    close()
                }
                drawPath(beak, color = Color(0xFFFF9800)) // Orange beak

                // Wing
                drawOval(
                    color = Color(0xFFFBC02D),
                    topLeft = Offset(cx - w * 0.15f, cy + h * 0.01f),
                    size = Size(w * 0.22f, h * 0.13f)
                )
            }
            "Fish Products" -> {
                // Fish body oval
                drawOval(
                    color = Color(0xFFFF9800), // Orange goldfish
                    topLeft = Offset(cx - w * 0.23f, cy - h * 0.15f),
                    size = Size(w * 0.42f, h * 0.28f)
                )
                // Tail fin
                val tail = androidx.compose.ui.graphics.Path().apply {
                    moveTo(cx - w * 0.20f, cy)
                    lineTo(cx - w * 0.38f, cy - h * 0.18f)
                    lineTo(cx - w * 0.32f, cy)
                    lineTo(cx - w * 0.38f, cy + h * 0.18f)
                    close()
                }
                drawPath(tail, color = Color(0xFFFF5722))

                // Eye
                drawCircle(
                    color = Color.White,
                    radius = w * 0.045f,
                    center = Offset(cx + w * 0.10f, cy - h * 0.04f)
                )
                drawCircle(
                    color = eyeColor,
                    radius = w * 0.025f,
                    center = Offset(cx + w * 0.11f, cy - h * 0.04f)
                )
                // Bubbles
                drawCircle(Color(0xFF80DEEA).copy(alpha = 0.5f), w * 0.025f, Offset(cx + w * 0.26f, cy - h * 0.15f))
                drawCircle(Color(0xFF80DEEA).copy(alpha = 0.5f), w * 0.015f, Offset(cx + w * 0.32f, cy - h * 0.25f))
            }
            "Rabbit Products" -> {
                // Long Ears
                drawOval(
                    color = Color.White,
                    topLeft = Offset(cx - w * 0.15f, cy - h * 0.38f),
                    size = Size(w * 0.11f, h * 0.32f)
                )
                drawOval(
                    color = Color.White,
                    topLeft = Offset(cx + w * 0.04f, cy - h * 0.38f),
                    size = Size(w * 0.11f, h * 0.32f)
                )
                // Inner ears
                drawOval(
                    color = Color(0xFFFFCDD2),
                    topLeft = Offset(cx - w * 0.12f, cy - h * 0.33f),
                    size = Size(w * 0.06f, h * 0.24f)
                )
                drawOval(
                    color = Color(0xFFFFCDD2),
                    topLeft = Offset(cx + w * 0.06f, cy - h * 0.33f),
                    size = Size(w * 0.06f, h * 0.24f)
                )

                // Face
                drawCircle(
                    color = Color(0xFFF5F5F5),
                    radius = w * 0.24f,
                    center = Offset(cx, cy + h * 0.05f)
                )
                // Eyes
                drawCircle(
                    color = Color(0xFFE91E63), // Pink rabbit eyes
                    radius = w * 0.03f,
                    center = Offset(cx - w * 0.09f, cy)
                )
                drawCircle(
                    color = Color(0xFFE91E63),
                    radius = w * 0.03f,
                    center = Offset(cx + w * 0.09f, cy)
                )
                // Nose
                drawCircle(
                    color = Color(0xFFFF8A80),
                    radius = w * 0.025f,
                    center = Offset(cx, cy + h * 0.05f)
                )
            }
            else -> {
                // Accessories / generic
                drawCircle(
                    color = warmBrown,
                    radius = w * 0.18f,
                    center = Offset(cx, cy)
                )
                // Bone shape
                drawRect(
                    color = lightBeige,
                    topLeft = Offset(cx - w * 0.22f, cy - h * 0.04f),
                    size = Size(w * 0.44f, h * 0.08f)
                )
                drawCircle(lightBeige, w * 0.07f, Offset(cx - w * 0.22f, cy - h * 0.04f))
                drawCircle(lightBeige, w * 0.07f, Offset(cx - w * 0.22f, cy + h * 0.04f))
                drawCircle(lightBeige, w * 0.07f, Offset(cx + w * 0.22f, cy - h * 0.04f))
                drawCircle(lightBeige, w * 0.07f, Offset(cx + w * 0.22f, cy + h * 0.04f))
            }
        }
    }
}

// -------------------------------------------------------------
// LOGIN SCREEN MODULE
// -------------------------------------------------------------
@Composable
fun LoginScreen(viewModel: AppViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    var adminPin by remember { mutableStateOf("") }
    var showAdminLogin by remember { mutableStateOf(false) }

    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Heart-centered brand visual
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(colors.primary.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Pets,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text("Pet Paws", fontWeight = FontWeight.Bold, fontSize = 28.sp, color = colors.tertiary)
            Text("Your Premium Pet Products Store", fontSize = 14.sp, color = colors.onBackground.copy(alpha = 0.7f))

            Spacer(Modifier.height(32.dp))

            if (!showAdminLogin) {
                // Email Field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("email_input"),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Spacer(Modifier.height(12.dp))

                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Account Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Password Toggle"
                            )
                        }
                    },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("password_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(8.dp))

                // Remember Me
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it })
                        Text("Remember Me", fontSize = 13.sp, color = colors.onBackground.copy(alpha = 0.8f))
                    }
                    TextButton(onClick = { viewModel.showToast("Verification link dispatched if account is valid.") }) {
                        Text("Forgot Password?", fontSize = 13.sp, color = colors.primary, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Primary Button
                Button(
                    onClick = {
                        viewModel.loginUser(email, password, rememberMe) {}
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_login_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) {
                    Text("Secure Login", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(Modifier.height(16.dp))

                // Sign Up Switcher
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("New pet parent? ", fontSize = 14.sp)
                    TextButton(onClick = { viewModel.navigateTo("SIGNUP") }, modifier = Modifier.testTag("goto_signup")) {
                        Text("Create Account", fontWeight = FontWeight.Bold, color = colors.primary)
                    }
                }
            } else {
                // Admin pin section
                Text("Admin Console Login", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = colors.tertiary)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = adminPin,
                    onValueChange = { adminPin = it },
                    label = { Text("Enter Admin PIN (mock code: admin123)") },
                    leadingIcon = { Icon(Icons.Default.Security, contentDescription = null) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_pin_input"),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.loginAdmin(adminPin) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.tertiary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Access Dashboard", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(24.dp))

            // Admin Toggle Link
            TextButton(onClick = { showAdminLogin = !showAdminLogin }) {
                Text(
                    if (showAdminLogin) "Return to Customer Sign In" else "Admin Terminal Console Portal Mode",
                    color = colors.tertiary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// -------------------------------------------------------------
// SIGNUP SCREEN MODULE
// -------------------------------------------------------------
@Composable
fun SignupScreen(viewModel: AppViewModel) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Pets,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier
                    .size(64.dp)
                    .padding(bottom = 8.dp)
            )

            Text("Create Account", fontWeight = FontWeight.Bold, fontSize = 26.sp, color = colors.tertiary)
            Text("Join our lovable pet parent communities", fontSize = 13.sp, color = colors.onBackground.copy(alpha = 0.6f))

            Spacer(Modifier.height(24.dp))

            // Inputs
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signup_name"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signup_email"),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = mobile,
                onValueChange = { mobile = it },
                label = { Text("Mobile Number") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signup_mobile"),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Choose Password (6+ chars)") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signup_password"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm password") },
                leadingIcon = { Icon(Icons.Default.LockClock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signup_confirm_password"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.signupUser(name, email, mobile, password, confirmPassword) {
                        viewModel.navigateTo("LOGIN")
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_signup")
            ) {
                Text("Join Store", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Already a member? ")
                TextButton(onClick = { viewModel.navigateTo("LOGIN") }) {
                    Text("Sign In Here", fontWeight = FontWeight.Bold, color = colors.primary)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// HOME SCREEN MODULE
// -------------------------------------------------------------
@Composable
fun HomeScreen(viewModel: AppViewModel) {
    val colors = MaterialTheme.colorScheme
    val products by viewModel.productList.collectAsStateWithLifecycle()

    val categories = listOf(
        "Dog Products" to Icons.Default.Pets,
        "Cat Products" to Icons.Default.Pets,
        "Bird Products" to Icons.Default.Pets,
        "Fish Products" to Icons.Default.Pets,
        "Rabbit Products" to Icons.Default.Pets,
        "Pet Accessories" to Icons.Default.Stars
    )

    // Rotative banner messages
    val bannerMessages = listOf(
        "Spic & Span! 15% OFF On Comfort Beds & Shampoos" to "Coupon Code: PAWS20",
        "Happy Kitty! Free Nibbles On Salmon Combos Today" to "Treat your Cats",
        "Fresh Aquatic Glass Tanks! Explore Aquarium Filters" to "Decorate Underwater"
    )
    var activeBannerIndex by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(4000)
            activeBannerIndex = (activeBannerIndex + 1) % bannerMessages.size
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen_content"),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Welcoming Headline
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Hello Pet Lover, 🐾",
                        fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                        fontSize = 12.sp,
                        color = colors.primary,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    Text(
                        "Discover Paws & Co.",
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.tertiary,
                        letterSpacing = (-0.5).sp
                    )
                }
            }
        }

        // Hero Sliding Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(colors.primary, colors.secondary)
                        )
                    )
                    .testTag("hero_banner")
            ) {
                // Background pattern shapes
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .offset(x = 240.dp, y = (-20).dp)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            bannerMessages[activeBannerIndex].first,
                            color = colors.tertiary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            bannerMessages[activeBannerIndex].second,
                            color = colors.tertiary.copy(alpha = 0.8f),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Button(
                        onClick = { viewModel.navigateTo("CATALOG") },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.tertiary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Shop Now", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.onTertiary)
                    }
                }
            }
        }

        // Categories quick grid items
        item {
            Text(
                "Popular Categories",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = colors.tertiary,
                modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(categories) { (catName, icon) ->
                    Card(
                        modifier = Modifier
                            .width(110.dp)
                            .clickable {
                                viewModel.activeCategory.value = catName
                                viewModel.navigateTo("CATALOG")
                            }
                            .testTag("cat_card_$catName"),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            PetIllustrationCanvas(
                                category = catName,
                                modifier = Modifier
                                    .size(54.dp)
                                    .padding(bottom = 6.dp)
                            )
                            Text(
                                catName.replace(" Products", ""),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.tertiary,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Featured Products
        item {
            Text(
                "Featured Products",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = colors.tertiary,
                modifier = Modifier.padding(top = 28.dp, bottom = 12.dp)
            )
        }

        val featuredList = products.filter { it.price > 20.0 }.take(4)
        if (featuredList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colors.primary)
                }
            }
        } else {
            items(featuredList) { prod ->
                ProductCardListItem(prod, viewModel)
            }
        }
    }
}

// -------------------------------------------------------------
// PRODUCT CARD COMPONENT
// -------------------------------------------------------------
@Composable
fun ProductCardListItem(
    product: ProductEntity,
    viewModel: AppViewModel
) {
    val colors = MaterialTheme.colorScheme
    var isHovered by remember { mutableStateOf(false) }
    
    // Scale transition on card click or press
    val scaleFactor by animateFloatAsState(if (isHovered) 1.03f else 1.0f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .scale(scaleFactor)
            .clickable { viewModel.selectProduct(product) }
            .testTag("product_card_${product.id}"),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isHovered) 4.dp else 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Programmatic dynamic vector pet drawing
            PetIllustrationCanvas(
                category = product.category,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.background)
            )

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(colors.primary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            product.category.replace(" Products", ""),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                    }
                    if (product.discount > 0) {
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(colors.error.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "-${product.discount.toInt()}% OFF",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.error
                            )
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = colors.tertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    product.description,
                    fontSize = 12.sp,
                    color = colors.onBackground.copy(alpha = 0.65f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(vertical = 2.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val finalPrice = product.price * (1.0 - (product.discount / 100.0))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "₹${"%.2f".format(finalPrice)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = colors.primary
                        )
                        if (product.discount > 0) {
                            Text(
                                " ₹${"%.2f".format(product.price)}",
                                color = colors.onBackground.copy(alpha = 0.4f),
                                fontSize = 12.sp,
                                style = androidx.compose.ui.text.TextStyle(
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                ),
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            " ${product.rating}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = colors.onBackground.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            IconButton(
                onClick = { viewModel.toggleWishlist(product) },
                modifier = Modifier.testTag("wish_toggle_${product.id}")
            ) {
                val isFav = viewModel.isWishlisted(product.id)
                Icon(
                    imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFav) colors.error else colors.onBackground.copy(alpha = 0.4f)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// FILTERABLE CATALOG SCREEN
// -------------------------------------------------------------
@Composable
fun CatalogScreen(viewModel: AppViewModel) {
    val colors = MaterialTheme.colorScheme
    val filteredProds by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val rawCategory by viewModel.activeCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val priceLimit by viewModel.priceFilterMax.collectAsStateWithLifecycle()
    val inStockOnly by viewModel.inStockOnlyFilter.collectAsStateWithLifecycle()
    val ratingMin by viewModel.ratingFilterMin.collectAsStateWithLifecycle()
    val activeSortOpt by viewModel.catalogSortOption.collectAsStateWithLifecycle()

    var showFiltersSheet by remember { mutableStateOf(false) }

    val categoriesList = listOf("All", "Dog Products", "Cat Products", "Bird Products", "Fish Products", "Rabbit Products")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("catalog_screen_layout")
    ) {
        // Search & Filter header trigger
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                label = { Text("Search products...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("catalog_search_input"),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.width(12.dp))
            IconButton(
                onClick = { showFiltersSheet = !showFiltersSheet },
                modifier = Modifier
                    .background(colors.primary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .size(52.dp)
                    .testTag("open_filters_sheet")
            ) {
                Icon(Icons.Default.FilterList, contentDescription = "Open Filters", tint = colors.primary)
            }
        }

        // Horizontal Category Pill Strips
        LazyRow(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categoriesList) { cat ->
                val selected = rawCategory == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(if (selected) colors.primary else colors.secondary.copy(alpha = 0.4f))
                        .clickable { viewModel.activeCategory.value = cat }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("pill_$cat")
                ) {
                    Text(
                        cat,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (selected) Color.White else colors.tertiary
                    )
                }
            }
        }

        // Filter expand panel (Dynamic layout slider)
        AnimatedVisibility(visible = showFiltersSheet) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Refine Catalog Search", fontWeight = FontWeight.Bold, color = colors.tertiary)
                    
                    Spacer(Modifier.height(10.dp))

                    // Price Slider Limit
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Max Budget Price", fontSize = 12.sp)
                            Text("₹${priceLimit.toInt()}", fontWeight = FontWeight.Bold, color = colors.primary)
                        }
                        Slider(
                            value = priceLimit.toFloat(),
                            onValueChange = { viewModel.priceFilterMax.value = it.toDouble() },
                            valueRange = 10f..120f
                        )
                    }

                    // Minimum Rating selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Min Customer Rating", fontSize = 12.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.ratingFilterMin.value = 0f }) { Icon(Icons.Default.Refresh, "Clear Rating", modifier = Modifier.size(16.dp)) }
                            Text("${ratingMin.toInt()}★ +", fontWeight = FontWeight.Bold, color = colors.primary)
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (i in 1..5) {
                            val active = ratingMin.toInt() == i
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (active) colors.primary else colors.secondary.copy(alpha = 0.3f))
                                    .clickable { viewModel.ratingFilterMin.value = i.toFloat() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("$i★", color = if (active) Color.White else colors.tertiary, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // Stock toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Hide Out of Stock items", fontSize = 12.sp)
                        Switch(checked = inStockOnly, onCheckedChange = { viewModel.inStockOnlyFilter.value = it })
                    }

                    Spacer(Modifier.height(10.dp))

                    // Sorters Options list
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sort Options", fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("PriceLowToHigh" to "Price Low", "PriceHighToLow" to "Price High").forEach { (term, label) ->
                                val active = activeSortOpt == term
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (active) colors.primary else colors.secondary.copy(alpha = 0.4f))
                                        .clickable { viewModel.catalogSortOption.value = if (active) "Default" else term }
                                        .padding(8.dp)
                                ) {
                                    Text(label, fontSize = 11.sp, color = if (active) Color.White else colors.tertiary)
                                }
                            }
                        }
                    }
                }
            }
        }

        // List Grid items
        if (filteredProds.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Inbox, contentDescription = null, tint = colors.primary.copy(alpha = 0.4f), modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("No Matching Pet Products", fontWeight = FontWeight.Bold, color = colors.onBackground.copy(alpha = 0.6f))
                    Text("Try relaxing filters or changing keywords.", fontSize = 12.sp, color = colors.onBackground.copy(alpha = 0.4f))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(filteredProds) { prod ->
                    ProductCardListItem(product = prod, viewModel = viewModel)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// PRODUCT DETAILS PAGE SCREEN
// -------------------------------------------------------------
@Composable
fun ProductDetailsScreen(viewModel: AppViewModel) {
    val colors = MaterialTheme.colorScheme
    val product by viewModel.selectedProduct.collectAsStateWithLifecycle()
    val reviews by viewModel.activeProductReviews.collectAsStateWithLifecycle()

    var buyQty by remember { mutableStateOf(1) }
    var reviewText by remember { mutableStateOf("") }
    var reviewRating by remember { mutableStateOf(5) }
    var isImageZoomed by remember { mutableStateOf(false) }

    val zoomScale by animateFloatAsState(if (isImageZoomed) 1.5f else 1.0f)

    if (product == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val prod = product!!
    val finalUnitPrice = prod.price * (1.0 - (prod.discount / 100.0))

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("details_screen_${prod.id}"),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Back toolbar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateTo("CATALOG") }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Go Back")
                }
                IconButton(onClick = { viewModel.toggleWishlist(prod) }) {
                    val fav = viewModel.isWishlisted(prod.id)
                    Icon(
                        imageVector = if (fav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (fav) colors.error else colors.onBackground.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // Image & Zoom interactive Cell
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.surface)
                        .clickable { isImageZoomed = !isImageZoomed }
                        .testTag("product_image_zoom"),
                    contentAlignment = Alignment.Center
                ) {
                    PetIllustrationCanvas(
                        category = prod.category,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .scale(zoomScale)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    if (isImageZoomed) "🔍 Tap image to Zoom Out" else "🔍 Tap image to Zoom In",
                    fontSize = 11.sp,
                    color = colors.onBackground.copy(alpha = 0.5f)
                )
            }
        }

        // Product Titles & Specifications
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    prod.category,
                    color = colors.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    prod.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = colors.tertiary
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rating stars
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300))
                        Text(
                            " ${prod.rating} (Based on customer logs)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    // Stock Tag
                    val stockColor = when (prod.stockStatus) {
                        "In Stock" -> colors.primary
                        "Low Stock" -> Color(0xFFFF9800)
                        else -> colors.error
                    }
                    Box(
                        modifier = Modifier
                            .background(stockColor.copy(alpha = 0.15f), RoundedCornerShape(100.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(prod.stockStatus, color = stockColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Divider(color = colors.onBackground.copy(alpha = 0.1f))

                // Price tag
                Row(
                    modifier = Modifier.padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "₹${"%.2f".format(finalUnitPrice)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = colors.primary
                    )
                    if (prod.discount > 0) {
                        Text(
                            "₹${"%.2f".format(prod.price)}",
                            style = androidx.compose.ui.text.TextStyle(
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                            ),
                            color = colors.onBackground.copy(alpha = 0.4f),
                            fontSize = 16.sp,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                }

                // Description Title
                Text("Product Overview", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colors.tertiary)
                Text(
                    prod.description,
                    color = colors.onBackground.copy(alpha = 0.7f),
                    lineHeight = 20.sp,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        // Quantity Selector and Cart Button
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Qty:", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = { if (buyQty > 1) buyQty-- }) {
                        Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Minus")
                    }
                    Text("$buyQty", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 4.dp))
                    IconButton(onClick = { buyQty++ }) {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = "Plus")
                    }
                }

                Button(
                    onClick = {
                        viewModel.addToCart(prod, buyQty)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                        .height(48.dp)
                        .testTag("add_to_cart_detail_btn")
                ) {
                    Text("Add To Cart", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Reviews section
        item {
            Spacer(Modifier.height(16.dp))
            Text("Verified Pet Parent Reviews", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = colors.tertiary)
        }

        // Add Product Review Box
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                colors = CardDefaults.cardColors(containerColor = colors.secondary.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Submit Customer Product Feedback", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    
                    // Star triggers
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (star in 1..5) {
                            val active = reviewRating >= star
                            IconButton(onClick = { reviewRating = star }, modifier = Modifier.size(32.dp)) {
                                Icon(
                                    imageVector = if (active) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = null,
                                    tint = if (active) Color(0xFFFFB300) else colors.onBackground.copy(alpha = 0.3f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = reviewText,
                        onValueChange = { reviewText = it },
                        placeholder = { Text("Write about nutrition digestions, durability etc...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("review_input_field"),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 3
                    )

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = {
                            viewModel.submitReview(prod.id, reviewText, reviewRating.toFloat())
                            reviewText = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.tertiary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.End)
                            .testTag("submit_review_btn")
                    ) {
                        Text("Post Review", fontSize = 12.sp)
                    }
                }
            }
        }

        // Reviews Feed
        if (reviews.isEmpty()) {
            item {
                Text(
                    "Be the first pet owner to write feedback about this product!",
                    color = colors.onBackground.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        } else {
            items(reviews) { rev ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(rev.userName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Row {
                                for (p in 1..5) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (p <= rev.rating) Color(0xFFFFB300) else colors.onBackground.copy(alpha = 0.15f),
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            rev.comment,
                            fontSize = 12.sp,
                            color = colors.onBackground.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SHOPPING CART SCREEN
// -------------------------------------------------------------
@Composable
fun CartScreen(viewModel: AppViewModel) {
    val colors = MaterialTheme.colorScheme
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val productsList by viewModel.productList.collectAsStateWithLifecycle()
    val activeCoupon by viewModel.appliedCoupon.collectAsStateWithLifecycle()

    var couponField by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("cart_layout")
    ) {
        Text("Your Pet Shopping Cart", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = colors.tertiary)

        Spacer(Modifier.height(12.dp))

        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = colors.primary.copy(alpha = 0.3f), modifier = Modifier.size(80.dp).rotate((-10).toFloat()))
                    Spacer(Modifier.height(16.dp))
                    Text("Your cart is looking empty!", fontWeight = FontWeight.Bold, color = colors.tertiary)
                    Text("Browse our catalog for delightful pet assets.", fontSize = 13.sp, color = colors.onBackground.copy(alpha = 0.5f))
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { viewModel.navigateTo("CATALOG") }) {
                        Text("Explore Catalog Store")
                    }
                }
            }
        } else {
            // Calculated values
            var subtotal = 0.0
            val itemRows = mutableListOf<Triple<CartItemEntity, ProductEntity, Double>>()

            for (item in cartItems) {
                val prod = productsList.find { it.id == item.productId }
                if (prod != null) {
                    val finalPrice = prod.price * (1.0 - (prod.discount / 100.0))
                    subtotal += finalPrice * item.quantity
                    itemRows.add(Triple(item, prod, finalPrice))
                }
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(itemRows) { (cItem, prod, finalPrice) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PetIllustrationCanvas(prod.category, modifier = Modifier.size(54.dp))
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = colors.tertiary)
                                Text("₹${"%.2f".format(finalPrice)} each", fontSize = 12.sp, color = colors.primary)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { viewModel.updateCartQty(cItem.productId, cItem.quantity - 1) }) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(18.dp))
                                }
                                Text("${cItem.quantity}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                IconButton(onClick = { viewModel.updateCartQty(cItem.productId, cItem.quantity + 1) }) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { viewModel.removeCartItem(cItem.productId) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = colors.error, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            // Coupon Field
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = couponField,
                    onValueChange = { couponField = it },
                    placeholder = { Text("Coupon Code (PAWS20)") },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("coupon_input"),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.width(12.dp))
                Button(
                    onClick = {
                        if (couponField == "PAWS20") {
                            viewModel.appliedCoupon.value = "PAWS20"
                            viewModel.showToast("Coupon Applied! 20% discount saved.")
                        } else {
                            viewModel.showToast("Invalid promo coupon code.")
                        }
                    },
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("Apply")
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            // Billing Invoice details
            val taxAmount = subtotal * 0.05
            val discountAmount = if (activeCoupon == "PAWS20") (subtotal + taxAmount) * 0.2 else 0.0
            val totalFinal = subtotal + taxAmount - discountAmount

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Basket Subtotal", fontSize = 13.sp)
                    Text("₹${"%.2f".format(subtotal)}")
                }
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("M3 Flat Tax Tier (5%)", fontSize = 13.sp)
                    Text("₹${"%.2f".format(taxAmount)}")
                }
                if (discountAmount > 0) {
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Active Coupon Discount (20% Off)", fontSize = 13.sp, color = Color(0xFF4CAF50))
                        Text("-₹${"%.2f".format(discountAmount)}", color = Color(0xFF4CAF50))
                    }
                }
                Divider(Modifier.padding(vertical = 6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Price Amount", fontWeight = FontWeight.Bold, color = colors.tertiary)
                    Text("₹${"%.2f".format(totalFinal)}", fontWeight = FontWeight.Bold, color = colors.primary, fontSize = 18.sp)
                }
            }

            Button(
                onClick = { viewModel.navigateTo("CHECKOUT") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("cart_checkout_btn")
            ) {
                Text("Proceed To Checkout Secured", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// -------------------------------------------------------------
// CHECKOUT SCREEN PAGE
// -------------------------------------------------------------
@Composable
fun CheckoutScreen(viewModel: AppViewModel) {
    val colors = MaterialTheme.colorScheme
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val productsList by viewModel.productList.collectAsStateWithLifecycle()

    var shippingName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var pinCode by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf("UPI Portal") }

    LaunchedEffect(Unit) {
        val user = viewModel.currentUser.value
        if (user != null) {
            shippingName = user.fullName
            phone = user.mobileNumber
        }
    }

    var billingAmount = 0.0
    for (item in cartItems) {
        val p = productsList.find { it.id == item.productId }
        if (p != null) {
            billingAmount += p.price * (1.0 - (p.discount / 100.0)) * item.quantity
        }
    }
    val finalBill = (billingAmount * 1.05) * (if (viewModel.appliedCoupon.value == "PAWS20") 0.8 else 1.0)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("checkout_page_scroll"),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            IconButton(onClick = { viewModel.navigateTo("CART") }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("Shipping & Payment Checkout", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = colors.tertiary, modifier = Modifier.padding(bottom = 16.dp))
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Final Invoice:", fontWeight = FontWeight.Bold)
                    Text("₹${"%.2f".format(finalBill)}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = colors.primary)
                }
            }
        }

        // Address Form fields
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Shipping Information Details", fontWeight = FontWeight.Bold, color = colors.tertiary)
                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = shippingName,
                        onValueChange = { shippingName = it },
                        label = { Text("Recipient Full Name") },
                        modifier = Modifier.fillMaxWidth().testTag("checkout_field_name"),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Street Address Details") },
                        modifier = Modifier.fillMaxWidth().testTag("checkout_field_address"),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Row {
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("City") },
                            modifier = Modifier.weight(1f).testTag("checkout_field_city"),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        OutlinedTextField(
                            value = state,
                            onValueChange = { state = it },
                            label = { Text("State") },
                            modifier = Modifier.weight(1f).testTag("checkout_field_state"),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row {
                        OutlinedTextField(
                            value = pinCode,
                            onValueChange = { pinCode = it },
                            label = { Text("PIN Postal Code") },
                            modifier = Modifier.weight(1f).testTag("checkout_field_pin"),
                            shape = RoundedCornerShape(10.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(Modifier.width(8.dp))
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Mobile Contact No") },
                            modifier = Modifier.weight(1f).testTag("checkout_field_phone"),
                            shape = RoundedCornerShape(10.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                    }
                }
            }
        }

        // Payment Portals Select
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Payment Dispatch Method", fontWeight = FontWeight.Bold, color = colors.tertiary, modifier = Modifier.padding(bottom = 10.dp))

                    val portals = listOf("UPI Portal", "Credit/Debit Card", "Net Banking Hub", "Cash on Delivery")
                    portals.forEach { p ->
                        val active = selectedPaymentMethod == p
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (active) colors.primary.copy(alpha = 0.1f) else Color.Transparent)
                                .clickable { selectedPaymentMethod = p }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = active, onClick = { selectedPaymentMethod = p })
                            Spacer(Modifier.width(10.dp))
                            Text(p, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }
        }

        // Order Complete Button
        item {
            Button(
                onClick = {
                    viewModel.placeOrder(
                        shippingName, address, city, state, pinCode, phone, selectedPaymentMethod
                    ) {
                        viewModel.navigateTo("DASHBOARD")
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_checkout_btn")
            ) {
                Text("Dispatch Order Secured", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// -------------------------------------------------------------
// USER SETTINGS DASHBOARD
// -------------------------------------------------------------
@Composable
fun DashboardScreen(viewModel: AppViewModel) {
    val colors = MaterialTheme.colorScheme
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val orders by viewModel.userOrders.collectAsStateWithLifecycle()
    val historyLog by viewModel.userLoginHistory.collectAsStateWithLifecycle()
    val actionList by viewModel.userActivityHistory.collectAsStateWithLifecycle()

    var editingProfileName by remember { mutableStateOf("") }
    var editingProfileContact by remember { mutableStateOf("") }
    
    var oldPassField by remember { mutableStateOf("") }
    var newPassField by remember { mutableStateOf("") }

    LaunchedEffect(user) {
        user?.let {
            editingProfileName = it.fullName
            editingProfileContact = it.mobileNumber
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_root"),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Welcome Header
        item {
            Text("Settings Dashboard Panel", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = colors.tertiary, modifier = Modifier.padding(bottom = 12.dp))
        }

        // Profile Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Modify Personal Details Profile", fontWeight = FontWeight.Bold, color = colors.tertiary, fontSize = 16.sp)
                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = editingProfileName,
                        onValueChange = { editingProfileName = it },
                        label = { Text("Display Full Name") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_name"),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editingProfileContact,
                        onValueChange = { editingProfileContact = it },
                        label = { Text("Contact mobile number") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_mobile"),
                        shape = RoundedCornerShape(10.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.updateUserProfile(editingProfileName, editingProfileContact) },
                        modifier = Modifier.align(Alignment.End).testTag("save_profile_btn")
                    ) {
                        Text("Save Profile Changes", fontSize = 12.sp)
                    }
                }
            }
        }

        // Password Change Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Authorize Password Changes", fontWeight = FontWeight.Bold, color = colors.tertiary, fontSize = 16.sp)
                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = oldPassField,
                        onValueChange = { oldPassField = it },
                        label = { Text("Old Current password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().testTag("profile_old_pass"),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPassField,
                        onValueChange = { newPassField = it },
                        label = { Text("New Secure Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().testTag("profile_new_pass"),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = colors.tertiary),
                        onClick = {
                            viewModel.changeUserPassword(oldPassField, newPassField)
                            oldPassField = ""
                            newPassField = ""
                        },
                        modifier = Modifier.align(Alignment.End).testTag("change_password_btn")
                    ) {
                        Text("Update secure authorization", fontSize = 12.sp)
                    }
                }
            }
        }

        // Order history segment
        item {
            Text("Order History", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = colors.tertiary, modifier = Modifier.padding(vertical = 8.dp))
        }

        if (orders.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No orders recorded yet. Let's make your pet happy! 🐾")
                    }
                }
            }
        } else {
            items(orders) { o ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("ID Order: #${o.id}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            val statusColor = when (o.status) {
                                "Delivered" -> colors.primary
                                "Shipped" -> Color(0xFF4CAF50)
                                else -> Color(0xFFFF9800)
                            }
                            Text(o.status, color = statusColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text("Products Ordered: ${o.productsJson}", fontSize = 12.sp, color = colors.onBackground.copy(alpha = 0.75f))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            Text(sdf.format(Date(o.orderDate)), fontSize = 11.sp, color = colors.onBackground.copy(alpha = 0.5f))
                            Text("Total Billing: ₹${"%.2f".format(o.totalPrice)}", fontWeight = FontWeight.Bold, color = colors.primary)
                        }
                    }
                }
            }
        }

        // Login log records
        item {
            Text("Audited Session Login History", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = colors.tertiary, modifier = Modifier.padding(vertical = 12.dp))
        }

        items(historyLog.take(5)) { log ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(log.deviceInfo, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("IP: ${log.ipAddress} | ${log.browserInfo}", fontSize = 11.sp, color = colors.onBackground.copy(alpha = 0.6f))
                    }
                    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    Text(sdf.format(Date(log.loginTimestamp)), fontSize = 11.sp, color = colors.onBackground.copy(alpha = 0.6f))
                }
            }
        }

        // User Tracker Activity Logs
        item {
            Text("Activity History Timeline Tracking", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = colors.tertiary, modifier = Modifier.padding(vertical = 12.dp))
        }

        items(actionList.take(6)) { act ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(colors.primary, CircleShape)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(act.description, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    Text(sdf.format(Date(act.timestamp)), fontSize = 10.sp, color = colors.onBackground.copy(alpha = 0.5f))
                }
            }
        }
    }
}

// -------------------------------------------------------------
// WISHLIST SCREEN MODULE
// -------------------------------------------------------------
@Composable
fun WishlistScreen(viewModel: AppViewModel) {
    val colors = MaterialTheme.colorScheme
    val wishlist by viewModel.wishlistItems.collectAsStateWithLifecycle()
    val availableProducts by viewModel.productList.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("wishlist_root")
    ) {
        Text("Your Wishlist Favorites", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = colors.tertiary)
        Spacer(Modifier.height(12.dp))

        val wishlistedProducts = availableProducts.filter { p -> wishlist.any { it.productId == p.id } }

        if (wishlistedProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = colors.primary.copy(alpha = 0.3f), modifier = Modifier.size(84.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No favorite items yet!", fontWeight = FontWeight.Bold, color = colors.tertiary)
                    Text("Tap the hearts on product cards to append here.", fontSize = 13.sp, color = colors.onBackground.copy(alpha = 0.5f))
                }
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(wishlistedProducts) { prod ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PetIllustrationCanvas(prod.category, modifier = Modifier.size(54.dp))
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("₹${prod.price}", color = colors.primary, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    viewModel.addToCart(prod, 1)
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                            ) {
                                Text("Move to Cart", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.width(8.dp))
                            IconButton(onClick = { viewModel.toggleWishlist(prod) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = colors.error, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SEPARATE ADMIN TERMINAL DASHBOARD SCREEN
// -------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminDashboardScreen(viewModel: AppViewModel) {
    val colors = MaterialTheme.colorScheme
    val users by viewModel.allUsersList.collectAsStateWithLifecycle()
    val orders by viewModel.allOrdersList.collectAsStateWithLifecycle()
    val products by viewModel.productList.collectAsStateWithLifecycle()
    val loginAudits by viewModel.allLoginHistoryList.collectAsStateWithLifecycle()

    // Analytics computation
    val totalRevenue = orders.sumOf { it.totalPrice }
    val totalUserCount = users.filter { it.id != -999L }.size // Skip admin mock
    val totalOrderCount = orders.size

    var adminActiveTab by remember { mutableStateOf("METRICS") } // METRICS, PRODUCTS, AUDIT_LOGS

    // New product insertion state
    var editModeProdId by remember { mutableStateOf<Long?>(null) }
    var prodName by remember { mutableStateOf("") }
    var prodCategory by remember { mutableStateOf("Dog Products") }
    var prodDesc by remember { mutableStateOf("") }
    var prodPrice by remember { mutableStateOf("") }
    var prodDiscount by remember { mutableStateOf("") }
    var prodStock by remember { mutableStateOf("In Stock") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_dashboard_root"),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text("Admin Terminal Command Panel", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = colors.tertiary)
            Spacer(Modifier.height(12.dp))

            // Tab bar switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                listOf("METRICS" to "Key Metrics", "PRODUCTS" to "Manage Products", "AUDIT_LOGS" to "Login Log Audits").forEach { (tab, label) ->
                    val active = adminActiveTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (active) colors.primary else colors.secondary.copy(alpha = 0.3f))
                            .clickable { adminActiveTab = tab }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, color = if (active) Color.White else colors.tertiary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        when (adminActiveTab) {
            "METRICS" -> {
                item {
                    // Metrics Grid Display
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Total Income
                        Card(modifier = Modifier.weight(1f).minimumInteractiveComponentSize(), colors = CardDefaults.cardColors(containerColor = colors.surface)) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Total Revenue", fontSize = 12.sp, color = colors.onBackground.copy(alpha = 0.6f))
                                Text("₹${"%.2f".format(totalRevenue)}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = colors.primary)
                            }
                        }
                        // Total Orders
                        Card(modifier = Modifier.weight(1f).minimumInteractiveComponentSize(), colors = CardDefaults.cardColors(containerColor = colors.surface)) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Total Orders Dispatch", fontSize = 12.sp, color = colors.onBackground.copy(alpha = 0.6f))
                                Text("$totalOrderCount Shipments", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = colors.tertiary)
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = colors.surface)) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Total Registered Pet Parents", fontSize = 12.sp, color = colors.onBackground.copy(alpha = 0.6f))
                            Text("$totalUserCount Active Accounts", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = colors.primary)
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    Text("Customer Shipment Orders", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colors.tertiary, modifier = Modifier.padding(vertical = 8.dp))
                }

                if (orders.isEmpty()) {
                    item {
                        Text("No orders placed yet.", fontSize = 12.sp, color = colors.onBackground.copy(alpha = 0.5f))
                    }
                } else {
                    items(orders) { o ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Order #${o.id}", fontWeight = FontWeight.Bold)
                                    Text("Status: ${o.status}", fontWeight = FontWeight.Bold, color = colors.primary)
                                }
                                Text("Client: ${o.fullName} | Delivery Address: ${o.address}", fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Amt: ₹${"%.2f".format(o.totalPrice)}", fontWeight = FontWeight.Bold)
                                    Row {
                                        if (o.status != "Delivered") {
                                            TextButton(onClick = { viewModel.adminUpdateOrderStatus(o, "Delivered") }) {
                                                Text("Mark Delivered", fontSize = 11.sp, color = Color(0xFF4CAF50))
                                            }
                                        }
                                        if (o.status != "Shipped" && o.status != "Delivered") {
                                            TextButton(onClick = { viewModel.adminUpdateOrderStatus(o, "Shipped") }) {
                                                Text("Dispatch Shipped", fontSize = 11.sp, color = colors.primary)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "PRODUCTS" -> {
                // Form box to Add or Edit
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.secondary.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                if (editModeProdId != null) "Edit Pet Product SKU" else "Create New Product SKU Entry",
                                fontWeight = FontWeight.Bold,
                                color = colors.tertiary
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = prodName,
                                onValueChange = { prodName = it },
                                label = { Text("Product Display Name") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(Modifier.height(6.dp))
                            OutlinedTextField(
                                value = prodDesc,
                                onValueChange = { prodDesc = it },
                                label = { Text("Product Marketing Overview") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(Modifier.height(6.dp))
                            Row {
                                OutlinedTextField(
                                    value = prodPrice,
                                    onValueChange = { prodPrice = it },
                                    label = { Text("Price (₹)") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                Spacer(Modifier.width(8.dp))
                                OutlinedTextField(
                                    value = prodDiscount,
                                    onValueChange = { prodDiscount = it },
                                    label = { Text("Discount (%)") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            
                            // Category Select Strips
                            Text("Category:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                                listOf("Dog Products", "Cat Products", "Bird Products", "Fish Products", "Rabbit Products", "Pet Accessories").forEach { cat ->
                                    val active = prodCategory == cat
                                    Box(
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (active) colors.primary else colors.secondary.copy(alpha = 0.3f))
                                            .clickable { prodCategory = cat }
                                            .padding(8.dp)
                                    ) {
                                        Text(cat, fontSize = 10.sp, color = if (active) Color.White else colors.tertiary)
                                    }
                                }
                            }

                            Row {
                                Button(
                                    onClick = {
                                        val p = prodPrice.toDoubleOrNull() ?: 0.0
                                        val d = prodDiscount.toDoubleOrNull() ?: 0.0
                                        val eid = editModeProdId
                                        if (eid != null) {
                                            viewModel.adminEditProduct(eid, prodName, prodCategory, prodDesc, p, d, prodStock)
                                        } else {
                                            viewModel.adminAddProduct(prodName, prodCategory, prodDesc, p, d, prodStock)
                                        }

                                        // Clear
                                        editModeProdId = null
                                        prodName = ""
                                        prodDesc = ""
                                        prodPrice = ""
                                        prodDiscount = ""
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(if (editModeProdId != null) "Update SKU" else "Insert SKU Entry")
                                }
                                if (editModeProdId != null) {
                                    Spacer(Modifier.width(8.dp))
                                    TextButton(onClick = {
                                        editModeProdId = null
                                        prodName = ""
                                        prodDesc = ""
                                        prodPrice = ""
                                        prodDiscount = ""
                                    }) {
                                        Text("Cancel")
                                    }
                                }
                            }
                        }
                    }
                }

                items(products) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${item.category} | ₹${item.price} (${item.discount}% off)", fontSize = 12.sp, color = colors.onBackground.copy(alpha = 0.5f))
                            }
                            Row {
                                IconButton(onClick = {
                                    editModeProdId = item.id
                                    prodName = item.name
                                    prodDesc = item.description
                                    prodPrice = item.price.toString()
                                    prodDiscount = item.discount.toString()
                                    prodCategory = item.category
                                    prodStock = item.stockStatus
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit sku", tint = colors.primary)
                                }
                                IconButton(onClick = { viewModel.adminDeleteProduct(item.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete sku", tint = colors.error)
                                }
                            }
                        }
                    }
                }
            }

            "AUDIT_LOGS" -> {
                // Table login audits
                items(loginAudits) { log ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                            Text("Timestamp: " + sdf.format(Date(log.loginTimestamp)), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Device OS: ${log.deviceInfo} (${log.browserInfo})", fontSize = 11.sp)
                            Text("Incoming IP Address: ${log.ipAddress}", fontSize = 11.sp, color = colors.primary)
                            Text("User Entity reference: #${log.userId}", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
