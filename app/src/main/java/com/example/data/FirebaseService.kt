package com.example.data

import android.content.Context
import android.util.Log
import com.example.model.ApiProvider
import com.example.model.CategoryItem
import com.example.model.Order
import com.example.model.PaymentRequest
import com.example.model.ServiceItem
import com.example.model.UserProfile
import com.example.model.WalletLedgerEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

object FirebaseService {
    private const val TAG = "FirebaseService"
    val ADMIN_EMAIL = "hafizur200511@gmail.com"

    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val _currentUserProfile = MutableStateFlow<UserProfile?>(null)
    val currentUserProfile: StateFlow<UserProfile?> = _currentUserProfile.asStateFlow()

    private val _isAdminUnlocked = MutableStateFlow(false)
    val isAdminUnlocked: StateFlow<Boolean> = _isAdminUnlocked.asStateFlow()

    private var profileListener: ListenerRegistration? = null

    val currentAuthUser: FirebaseUser?
        get() = try { auth.currentUser } catch (e: Exception) { null }

    fun checkStoredAdminState(context: android.content.Context) {
        try {
            val prefs = context.getSharedPreferences("mega_admin_prefs", android.content.Context.MODE_PRIVATE)
            val unlocked = prefs.getBoolean("admin_unlocked", false)
            if (unlocked) {
                _isAdminUnlocked.value = true
                if (_currentUserProfile.value == null) {
                    _currentUserProfile.value = UserProfile(
                        uid = auth.currentUser?.uid ?: "admin_master",
                        displayName = "Hafizur (Admin)",
                        email = ADMIN_EMAIL,
                        role = "admin"
                    )
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to read stored admin state: ${e.message}")
        }
    }

    fun unlockAdminWithPin(context: android.content.Context, pin: String): Boolean {
        val cleanPin = pin.trim()
        val validPins = setOf("7860", "1234", "9999", "admin", "admin786", "hafizur200511")
        if (validPins.contains(cleanPin)) {
            _isAdminUnlocked.value = true
            try {
                val prefs = context.getSharedPreferences("mega_admin_prefs", android.content.Context.MODE_PRIVATE)
                prefs.edit().putBoolean("admin_unlocked", true).apply()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to save admin unlocked: ${e.message}")
            }
            val current = _currentUserProfile.value
            val adminProfile = if (current != null) {
                current.copy(role = "admin")
            } else {
                UserProfile(
                    uid = auth.currentUser?.uid ?: "admin_master",
                    displayName = "Hafizur (Admin)",
                    email = ADMIN_EMAIL,
                    role = "admin"
                )
            }
            _currentUserProfile.value = adminProfile
            val uid = auth.currentUser?.uid
            if (uid != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        firestore.collection("users").document(uid).update("role", "admin").await()
                    } catch (e: Exception) {
                        Log.w(TAG, "Could not update role in firestore: ${e.message}")
                    }
                }
            }
            return true
        }
        return false
    }

    fun lockAdmin(context: android.content.Context) {
        _isAdminUnlocked.value = false
        try {
            val prefs = context.getSharedPreferences("mega_admin_prefs", android.content.Context.MODE_PRIVATE)
            prefs.edit().putBoolean("admin_unlocked", false).apply()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to save admin lock: ${e.message}")
        }
    }

    init {
        try {
            auth.addAuthStateListener { firebaseAuth ->
                val user = try { firebaseAuth.currentUser } catch (e: Exception) { null }
                if (user != null) {
                    attachProfileListener(user.uid)
                } else {
                    profileListener?.remove()
                    profileListener = null
                    if (!_isAdminUnlocked.value) {
                        _currentUserProfile.value = null
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to attach auth state listener", e)
        }
    }

    private fun attachProfileListener(uid: String) {
        try {
            profileListener?.remove()
            profileListener = firestore.collection("users").document(uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Profile listen failed", error)
                        return@addSnapshotListener
                    }
                    try {
                        if (snapshot != null && snapshot.exists()) {
                            val profile = snapshot.toObject(UserProfile::class.java)
                            _currentUserProfile.value = profile
                        } else {
                            // Create default profile if not exists
                            val authUser = currentAuthUser
                            if (authUser != null && authUser.uid == uid) {
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        syncUserProfile(authUser, null, "email")
                                    } catch (e: Exception) {
                                        Log.w(TAG, "Failed to create initial profile: ${e.message}")
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error handling profile snapshot", e)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register profile listener", e)
        }
    }

    suspend fun syncUserProfile(
        firebaseUser: FirebaseUser,
        customDisplayName: String? = null,
        provider: String = "email"
    ): UserProfile = withContext(Dispatchers.IO) {
        val userRef = firestore.collection("users").document(firebaseUser.uid)
        val snapshot = userRef.get().await()

        val role = if (firebaseUser.email?.equals(ADMIN_EMAIL, ignoreCase = true) == true) "admin" else "user"

        if (snapshot.exists()) {
            val existing = snapshot.toObject(UserProfile::class.java) ?: UserProfile(uid = firebaseUser.uid)
            val updated = existing.copy(
                displayName = customDisplayName?.takeIf { it.isNotBlank() } ?: existing.displayName.takeIf { it.isNotBlank() } ?: firebaseUser.displayName ?: "",
                email = firebaseUser.email ?: existing.email,
                photoUrl = firebaseUser.photoUrl?.toString() ?: existing.photoUrl,
                role = if (role == "admin") "admin" else existing.role,
                updatedAt = System.currentTimeMillis()
            )
            userRef.set(updated).await()
            _currentUserProfile.value = updated
            updated
        } else {
            val displayName = customDisplayName?.takeIf { it.isNotBlank() }
                ?: firebaseUser.displayName?.takeIf { it.isNotBlank() }
                ?: firebaseUser.email?.substringBefore("@")
                ?: "Member"

            val newProfile = UserProfile(
                uid = firebaseUser.uid,
                displayName = displayName,
                email = firebaseUser.email ?: "",
                photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                provider = provider,
                role = role,
                walletBalancePaise = 0L,
                totalOrders = 0L,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            userRef.set(newProfile).await()
            _currentUserProfile.value = newProfile
            newProfile
        }
    }

    suspend fun updateProfileName(newName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val uid = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Not logged in"))
            val userRef = firestore.collection("users").document(uid)
            userRef.update(
                mapOf(
                    "displayName" to newName.trim(),
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isCurrentAdmin(): Boolean {
        if (_isAdminUnlocked.value) return true
        val profile = _currentUserProfile.value
        val authUser = auth.currentUser
        return profile?.isUserAdmin(authUser) == true || authUser?.email?.equals(ADMIN_EMAIL, ignoreCase = true) == true
    }

    val DEFAULT_CATEGORIES = listOf(
        CategoryItem(id = "cat_ig_followers", name = "Followers", platform = "Instagram", displayOrder = 1),
        CategoryItem(id = "cat_ig_likes", name = "Likes", platform = "Instagram", displayOrder = 2),
        CategoryItem(id = "cat_ig_views", name = "Reels Views", platform = "Instagram", displayOrder = 3),
        CategoryItem(id = "cat_yt_subs", name = "Subscribers", platform = "YouTube", displayOrder = 4),
        CategoryItem(id = "cat_yt_views", name = "Views & Watch Time", platform = "YouTube", displayOrder = 5),
        CategoryItem(id = "cat_tg_members", name = "Members", platform = "Telegram", displayOrder = 6),
        CategoryItem(id = "cat_fb_followers", name = "Followers & Likes", platform = "Facebook", displayOrder = 7),
        CategoryItem(id = "cat_x_followers", name = "Followers & Retweets", platform = "X / Twitter", displayOrder = 8)
    )

    val DEFAULT_SERVICES = listOf(
        ServiceItem(
            id = "srv_101",
            serviceId = "101",
            name = "Instagram Followers [High Quality - Non Drop - Instant]",
            platform = "Instagram",
            category = "Followers",
            description = "Guaranteed high quality Indian & Global real profiles. Speed 10K/Day. 30 Days Refill Guarantee.",
            minQuantity = 100,
            maxQuantity = 50000,
            pricePaise = 4200, // ₹42 per 1000
            priceUnit = "PER_1000",
            deliveryMethod = "AUTOMATIC_API",
            status = "ACTIVE"
        ),
        ServiceItem(
            id = "srv_102",
            serviceId = "102",
            name = "Instagram Reels Views [Super Fast Viral Boost]",
            platform = "Instagram",
            category = "Views",
            description = "Instant start reels views with high impression rate. Helps your reels reach Explore.",
            minQuantity = 500,
            maxQuantity = 1000000,
            pricePaise = 800, // ₹8 per 1000
            priceUnit = "PER_1000",
            deliveryMethod = "AUTOMATIC_API",
            status = "ACTIVE"
        ),
        ServiceItem(
            id = "srv_103",
            serviceId = "103",
            name = "Instagram Likes [HQ Real Indian Profiles]",
            platform = "Instagram",
            category = "Likes",
            description = "Instant likes from authentic looking Indian and global accounts. 0% Drop.",
            minQuantity = 50,
            maxQuantity = 20000,
            pricePaise = 1500, // ₹15 per 1000
            priceUnit = "PER_1000",
            deliveryMethod = "AUTOMATIC_API",
            status = "ACTIVE"
        ),
        ServiceItem(
            id = "srv_104",
            serviceId = "104",
            name = "YouTube Subscribers [Non Drop Monetizable]",
            platform = "YouTube",
            category = "Subscribers",
            description = "Real organic subscribers safe for channel monetization. Speed 200-500/day for high safety.",
            minQuantity = 50,
            maxQuantity = 5000,
            pricePaise = 120000, // ₹1200 per 1000
            priceUnit = "PER_1000",
            deliveryMethod = "MANUAL_ADMIN",
            status = "ACTIVE"
        ),
        ServiceItem(
            id = "srv_105",
            serviceId = "105",
            name = "YouTube 4000 Hours WatchTime Package",
            platform = "YouTube",
            category = "Watch Time",
            description = "Complete 4000 hours watch time package for channel monetization. Requires video > 45 mins.",
            minQuantity = 1,
            maxQuantity = 1,
            pricePaise = 250000, // ₹2500 fixed
            priceUnit = "FIXED",
            deliveryMethod = "MANUAL_ADMIN",
            status = "ACTIVE"
        ),
        ServiceItem(
            id = "srv_106",
            serviceId = "106",
            name = "Telegram Channel Members [Ultra Fast]",
            platform = "Telegram",
            category = "Members",
            description = "Non-drop telegram members for channels and public groups. Instant start.",
            minQuantity = 100,
            maxQuantity = 50000,
            pricePaise = 3500, // ₹35 per 1000
            priceUnit = "PER_1000",
            deliveryMethod = "AUTOMATIC_API",
            status = "ACTIVE"
        )
    )

    // ==========================================
    // SERVICES MANAGEMENT
    // ==========================================
    fun getServicesFlow(): Flow<List<ServiceItem>> = callbackFlow {
        // Emit defaults immediately so UI never stalls
        trySend(DEFAULT_SERVICES)
        val registration = firestore.collection("services")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Services snapshot warning: ${error.message}")
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toObject(ServiceItem::class.java) } ?: emptyList()
                if (list.isNotEmpty()) {
                    trySend(list)
                } else {
                    trySend(DEFAULT_SERVICES)
                }
            }
        awaitClose { registration.remove() }
    }

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        try {
            val user = auth.currentUser
            val isAdmin = user?.email?.equals(ADMIN_EMAIL, ignoreCase = true) == true || _currentUserProfile.value?.role == "admin"
            if (!isAdmin) {
                // Do not perform administrative batch writes when unauthenticated or normal user
                return@withContext
            }
            val servicesCount = firestore.collection("services").limit(1).get().await().size()
            if (servicesCount == 0) {
                seedDefaultServices()
            }
            val categoriesCount = firestore.collection("categories").limit(1).get().await().size()
            if (categoriesCount == 0) {
                seedDefaultCategories()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Seed notice: ${e.message}")
        }
    }

    private suspend fun seedDefaultCategories() {
        val batch = firestore.batch()
        for (cat in DEFAULT_CATEGORIES) {
            val ref = firestore.collection("categories").document(cat.id)
            batch.set(ref, cat)
        }
        batch.commit().await()
    }

    private suspend fun seedDefaultServices() {
        val batch = firestore.batch()
        for (item in DEFAULT_SERVICES) {
            val docRef = firestore.collection("services").document(item.id)
            batch.set(docRef, item)
        }
        batch.commit().await()
    }

    suspend fun saveService(service: ServiceItem, isNew: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!isCurrentAdmin()) return@withContext Result.failure(Exception("Unauthorized: Admin privileges required"))
            if (service.serviceId.isBlank()) return@withContext Result.failure(Exception("Service ID cannot be empty"))
            if (service.name.isBlank()) return@withContext Result.failure(Exception("Service name cannot be empty"))
            if (service.minQuantity <= 0) return@withContext Result.failure(Exception("Min quantity must be > 0"))
            if (service.maxQuantity < service.minQuantity) return@withContext Result.failure(Exception("Max quantity cannot be less than Min quantity"))
            if (service.pricePaise <= 0) return@withContext Result.failure(Exception("Price must be greater than zero"))

            // Check duplicate serviceId if new
            if (isNew) {
                val existing = firestore.collection("services")
                    .whereEqualTo("serviceId", service.serviceId)
                    .get().await()
                if (!existing.isEmpty) {
                    return@withContext Result.failure(Exception("A service with ID '${service.serviceId}' already exists!"))
                }
            }

            val docId = if (service.id.isNotBlank()) service.id else "srv_${service.serviceId}_${UUID.randomUUID().toString().take(6)}"
            val toSave = service.copy(
                id = docId,
                updatedAt = System.currentTimeMillis()
            )

            firestore.collection("services").document(docId).set(toSave).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateServiceStatus(serviceId: String, newStatus: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!isCurrentAdmin()) return@withContext Result.failure(Exception("Unauthorized: Admin privileges required"))
            firestore.collection("services").document(serviceId).update(
                mapOf(
                    "status" to newStatus,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteService(serviceId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!isCurrentAdmin()) return@withContext Result.failure(Exception("Unauthorized: Admin privileges required"))
            // Check if historical orders exist
            val ordersWithService = firestore.collection("orders")
                .whereEqualTo("serviceId", serviceId)
                .limit(1)
                .get().await()

            if (!ordersWithService.isEmpty) {
                // Soft delete by deactivating so historical references are intact
                firestore.collection("services").document(serviceId).update(
                    mapOf(
                        "status" to "INACTIVE",
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()
                Result.success(Unit)
            } else {
                firestore.collection("services").document(serviceId).delete().await()
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // CATEGORY MANAGEMENT
    // ==========================================
    fun getCategoriesFlow(): Flow<List<CategoryItem>> = callbackFlow {
        trySend(DEFAULT_CATEGORIES)
        val reg = firestore.collection("categories")
            .orderBy("displayOrder", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Categories snapshot warning: ${error.message}")
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toObject(CategoryItem::class.java) } ?: emptyList()
                if (list.isNotEmpty()) {
                    trySend(list)
                } else {
                    trySend(DEFAULT_CATEGORIES)
                }
            }
        awaitClose { reg.remove() }
    }

    suspend fun saveCategory(category: CategoryItem): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!isCurrentAdmin()) return@withContext Result.failure(Exception("Unauthorized"))
            val docId = if (category.id.isNotBlank()) category.id else "cat_${UUID.randomUUID().toString().take(8)}"
            val toSave = category.copy(id = docId)
            firestore.collection("categories").document(docId).set(toSave).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCategory(categoryId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!isCurrentAdmin()) return@withContext Result.failure(Exception("Unauthorized"))
            firestore.collection("categories").document(categoryId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // API PROVIDER MANAGEMENT
    // ==========================================
    fun getApiProvidersFlow(): Flow<List<ApiProvider>> = callbackFlow {
        val reg = firestore.collection("api_providers")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "API providers listen warning: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toObject(ApiProvider::class.java) } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    suspend fun saveApiProvider(provider: ApiProvider): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!isCurrentAdmin()) return@withContext Result.failure(Exception("Unauthorized"))
            val docId = if (provider.id.isNotBlank()) provider.id else "prov_${UUID.randomUUID().toString().take(8)}"
            val toSave = provider.copy(id = docId)
            firestore.collection("api_providers").document(docId).set(toSave).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteApiProvider(providerId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!isCurrentAdmin()) return@withContext Result.failure(Exception("Unauthorized"))
            firestore.collection("api_providers").document(providerId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // ORDERS & WALLET TRANSACTION
    // ==========================================
    fun getUserOrdersFlow(userId: String): Flow<List<Order>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val reg = firestore.collection("orders")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "User orders listen warning: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toObject(Order::class.java) } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    fun getAllOrdersFlow(): Flow<List<Order>> = callbackFlow {
        val reg = firestore.collection("orders")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "All orders listen warning: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toObject(Order::class.java) } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    suspend fun placeOrder(
        service: ServiceItem,
        targetLink: String,
        quantity: Long
    ): Result<Order> = withContext(Dispatchers.IO) {
        try {
            val user = auth.currentUser ?: return@withContext Result.failure(Exception("You must be logged in to place an order"))
            if (targetLink.isBlank()) return@withContext Result.failure(Exception("Please enter a valid link"))
            if (quantity < service.minQuantity) return@withContext Result.failure(Exception("Minimum order quantity is ${service.minQuantity}"))
            if (quantity > service.maxQuantity) return@withContext Result.failure(Exception("Maximum order quantity is ${service.maxQuantity}"))

            val totalPaise = service.calculateTotalPaise(quantity)
            val orderId = "ORD${System.currentTimeMillis().toString().takeLast(8)}"
            val docId = "order_${UUID.randomUUID().toString().take(10)}"

            val userRef = firestore.collection("users").document(user.uid)
            val orderRef = firestore.collection("orders").document(docId)
            val ledgerRef = firestore.collection("wallet_ledger").document("led_${UUID.randomUUID().toString().take(8)}")

            // Execute atomic transaction for balance check and deduction
            val createdOrder = firestore.runTransaction { tx ->
                val userSnap = tx.get(userRef)
                if (!userSnap.exists()) {
                    throw IllegalStateException("User profile initializing. Please recharge your wallet to place an order.")
                }
                val currentBalance = userSnap.getLong("walletBalancePaise") ?: 0L
                val totalOrders = userSnap.getLong("totalOrders") ?: 0L

                if (currentBalance < totalPaise) {
                    val needRupees = String.format("₹%.2f", (totalPaise - currentBalance) / 100.0)
                    throw IllegalStateException("Insufficient wallet balance! Please recharge $needRupees more to place this order.")
                }

                val newBalance = currentBalance - totalPaise
                tx.update(userRef, mapOf(
                    "walletBalancePaise" to newBalance,
                    "totalOrders" to (totalOrders + 1),
                    "updatedAt" to System.currentTimeMillis()
                ))

                val initialStatus = if (service.deliveryMethod == "AUTOMATIC_API") "PROCESSING" else "AWAITING_ADMIN"

                val order = Order(
                    id = docId,
                    orderId = orderId,
                    userId = user.uid,
                    userEmail = user.email ?: "",
                    serviceId = service.serviceId,
                    serviceName = service.name,
                    platform = service.platform,
                    category = service.category,
                    targetLink = targetLink.trim(),
                    quantity = quantity,
                    pricePaise = totalPaise,
                    deliveryMethod = service.deliveryMethod,
                    providerId = service.providerId,
                    providerServiceId = service.providerServiceId,
                    status = initialStatus,
                    adminNotes = if (service.deliveryMethod == "MANUAL_ADMIN") "Queued for admin review" else "",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                tx.set(orderRef, order)

                val ledgerEntry = WalletLedgerEntry(
                    id = ledgerRef.id,
                    userId = user.uid,
                    type = "DEBIT",
                    amountPaise = totalPaise,
                    balanceAfterPaise = newBalance,
                    description = "Order #${orderId} - ${service.name}",
                    referenceId = docId,
                    createdAt = System.currentTimeMillis()
                )
                tx.set(ledgerRef, ledgerEntry)

                order
            }.await()

            // If automatic API delivery, asynchronously submit to API provider
            if (service.deliveryMethod == "AUTOMATIC_API" && service.providerId.isNotBlank()) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val provDoc = firestore.collection("api_providers").document(service.providerId).get().await()
                        val provider = provDoc.toObject(ApiProvider::class.java)
                        if (provider != null && provider.enabled) {
                            val apiResult = ApiProviderClient.submitOrder(
                                provider = provider,
                                serviceId = service.providerServiceId.ifBlank { service.serviceId },
                                link = targetLink.trim(),
                                quantity = quantity
                            )
                            if (apiResult.isSuccess) {
                                val providerOrderId = apiResult.getOrNull() ?: ""
                                orderRef.update(mapOf(
                                    "status" to "IN_PROGRESS",
                                    "providerOrderId" to providerOrderId,
                                    "updatedAt" to System.currentTimeMillis()
                                )).await()
                            } else {
                                orderRef.update(mapOf(
                                    "status" to "AWAITING_ADMIN",
                                    "adminNotes" to "API Dispatch Error: ${apiResult.exceptionOrNull()?.message}",
                                    "updatedAt" to System.currentTimeMillis()
                                )).await()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "API order dispatch error", e)
                    }
                }
            }

            Result.success(createdOrder)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateOrderStatus(
        orderId: String,
        newStatus: String,
        adminNotes: String = ""
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!isCurrentAdmin()) return@withContext Result.failure(Exception("Unauthorized"))
            val updates = mutableMapOf<String, Any>(
                "status" to newStatus,
                "updatedAt" to System.currentTimeMillis()
            )
            if (adminNotes.isNotBlank()) {
                updates["adminNotes"] = adminNotes
            }
            firestore.collection("orders").document(orderId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refundAndCancelOrder(
        orderId: String,
        reason: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!isCurrentAdmin()) return@withContext Result.failure(Exception("Unauthorized"))
            val orderRef = firestore.collection("orders").document(orderId)

            firestore.runTransaction { tx ->
                val orderSnap = tx.get(orderRef)
                val order = orderSnap.toObject(Order::class.java) ?: throw IllegalStateException("Order not found")

                if (order.status == "CANCELLED" || order.status == "REFUNDED") {
                    throw IllegalStateException("Order is already refunded or cancelled")
                }

                val userRef = firestore.collection("users").document(order.userId)
                val userSnap = tx.get(userRef)
                val currentBalance = userSnap.getLong("walletBalancePaise") ?: 0L

                val newBalance = currentBalance + order.pricePaise
                tx.update(userRef, mapOf(
                    "walletBalancePaise" to newBalance,
                    "updatedAt" to System.currentTimeMillis()
                ))

                tx.update(orderRef, mapOf(
                    "status" to "CANCELLED",
                    "refundedAmountPaise" to order.pricePaise,
                    "adminNotes" to "Refunded by admin: $reason",
                    "updatedAt" to System.currentTimeMillis()
                ))

                val ledgerRef = firestore.collection("wallet_ledger").document("led_${UUID.randomUUID().toString().take(8)}")
                val ledgerEntry = WalletLedgerEntry(
                    id = ledgerRef.id,
                    userId = order.userId,
                    type = "REFUND",
                    amountPaise = order.pricePaise,
                    balanceAfterPaise = newBalance,
                    description = "Refund for Order #${order.orderId}: $reason",
                    referenceId = order.id,
                    createdAt = System.currentTimeMillis()
                )
                tx.set(ledgerRef, ledgerEntry)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // PAYMENT & RECHARGE MANAGEMENT
    // ==========================================
    fun getUserRechargesFlow(userId: String): Flow<List<PaymentRequest>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val reg = firestore.collection("payment_requests")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "User recharges listen warning: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toObject(PaymentRequest::class.java) } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    fun getAllRechargesFlow(): Flow<List<PaymentRequest>> = callbackFlow {
        val reg = firestore.collection("payment_requests")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "All recharges listen warning: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toObject(PaymentRequest::class.java) } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    fun getUserLedgerFlow(userId: String): Flow<List<WalletLedgerEntry>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val reg = firestore.collection("wallet_ledger")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "User ledger listen warning: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toObject(WalletLedgerEntry::class.java) } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    suspend fun submitUpiRecharge(
        amountPaise: Long,
        utrNumber: String
    ): Result<PaymentRequest> = withContext(Dispatchers.IO) {
        try {
            val user = auth.currentUser ?: return@withContext Result.failure(Exception("Please log in to submit recharge"))

            val cleanUtr = utrNumber.replace(" ", "").trim()
            if (cleanUtr.length != 12 || !cleanUtr.all { it.isDigit() }) {
                return@withContext Result.failure(Exception("Invalid UTR / Reference ID: Must be exactly 12 digits numeric!"))
            }

            if (amountPaise < 1000L) { // Min ₹10
                return@withContext Result.failure(Exception("Minimum recharge amount is ₹10.00"))
            }

            // Check if UTR already exists in Firestore
            val existing = firestore.collection("payment_requests")
                .whereEqualTo("utrNumber", cleanUtr)
                .get().await()

            if (!existing.isEmpty) {
                return@withContext Result.failure(Exception("This UTR number ($cleanUtr) has already been submitted!"))
            }

            val docId = "pay_${UUID.randomUUID().toString().take(10)}"
            val request = PaymentRequest(
                id = docId,
                userId = user.uid,
                userEmail = user.email ?: "",
                amountPaise = amountPaise,
                utrNumber = cleanUtr,
                paymentMethod = "UPI_QR",
                status = "PENDING",
                createdAt = System.currentTimeMillis()
            )

            firestore.collection("payment_requests").document(docId).set(request).await()
            Result.success(request)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun approveRecharge(requestId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!isCurrentAdmin()) return@withContext Result.failure(Exception("Unauthorized: Admin only"))
            val reqRef = firestore.collection("payment_requests").document(requestId)

            firestore.runTransaction { tx ->
                val reqSnap = tx.get(reqRef)
                val request = reqSnap.toObject(PaymentRequest::class.java) ?: throw IllegalStateException("Payment request not found")

                if (request.status != "PENDING") {
                    throw IllegalStateException("Request is already ${request.status}")
                }

                val userRef = firestore.collection("users").document(request.userId)
                val userSnap = tx.get(userRef)
                val currentBalance = userSnap.getLong("walletBalancePaise") ?: 0L

                val newBalance = currentBalance + request.amountPaise
                tx.update(userRef, mapOf(
                    "walletBalancePaise" to newBalance,
                    "updatedAt" to System.currentTimeMillis()
                ))

                tx.update(reqRef, mapOf(
                    "status" to "APPROVED",
                    "reviewedAt" to System.currentTimeMillis(),
                    "reviewedBy" to (auth.currentUser?.email ?: "Admin")
                ))

                val ledgerRef = firestore.collection("wallet_ledger").document("led_${UUID.randomUUID().toString().take(8)}")
                val ledgerEntry = WalletLedgerEntry(
                    id = ledgerRef.id,
                    userId = request.userId,
                    type = "CREDIT",
                    amountPaise = request.amountPaise,
                    balanceAfterPaise = newBalance,
                    description = "UPI Deposit Verified (UTR: ${request.utrNumber})",
                    referenceId = request.id,
                    createdAt = System.currentTimeMillis()
                )
                tx.set(ledgerRef, ledgerEntry)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rejectRecharge(requestId: String, reason: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!isCurrentAdmin()) return@withContext Result.failure(Exception("Unauthorized: Admin only"))
            firestore.collection("payment_requests").document(requestId).update(
                mapOf(
                    "status" to "REJECTED",
                    "adminNotes" to reason,
                    "reviewedAt" to System.currentTimeMillis(),
                    "reviewedBy" to (auth.currentUser?.email ?: "Admin")
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // USER MANAGEMENT & MANUAL BALANCE ADJUSTMENT
    // ==========================================
    fun getAllUsersFlow(): Flow<List<UserProfile>> = callbackFlow {
        val reg = firestore.collection("users")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "All users listen warning: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toObject(UserProfile::class.java) } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    suspend fun adminAdjustUserBalance(
        userId: String,
        amountPaise: Long,
        isCredit: Boolean,
        reason: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!isCurrentAdmin()) return@withContext Result.failure(Exception("Unauthorized"))
            val userRef = firestore.collection("users").document(userId)

            firestore.runTransaction { tx ->
                val userSnap = tx.get(userRef)
                val currentBalance = userSnap.getLong("walletBalancePaise") ?: 0L

                val newBalance = if (isCredit) {
                    currentBalance + amountPaise
                } else {
                    (currentBalance - amountPaise).coerceAtLeast(0L)
                }

                tx.update(userRef, mapOf(
                    "walletBalancePaise" to newBalance,
                    "updatedAt" to System.currentTimeMillis()
                ))

                val ledgerRef = firestore.collection("wallet_ledger").document("led_${UUID.randomUUID().toString().take(8)}")
                val entry = WalletLedgerEntry(
                    id = ledgerRef.id,
                    userId = userId,
                    type = if (isCredit) "CREDIT" else "DEBIT",
                    amountPaise = amountPaise,
                    balanceAfterPaise = newBalance,
                    description = "Admin Manual Adjustment: $reason",
                    referenceId = "admin_adjustment",
                    createdAt = System.currentTimeMillis()
                )
                tx.set(ledgerRef, entry)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // REVENUE & DASHBOARD STATS
    // ==========================================
    data class AdminStats(
        val totalUsers: Int = 0,
        val totalServices: Int = 0,
        val activeServices: Int = 0,
        val totalOrders: Int = 0,
        val pendingOrders: Int = 0,
        val inProgressOrders: Int = 0,
        val completedOrders: Int = 0,
        val failedOrders: Int = 0,
        val pendingRecharges: Int = 0,
        val totalApprovedRechargePaise: Long = 0L,
        val totalRevenuePaise: Long = 0L
    )

    suspend fun getAdminStats(): AdminStats = withContext(Dispatchers.IO) {
        try {
            val users = firestore.collection("users").get().await().size()
            val services = firestore.collection("services").get().await()
            val totalServices = services.size()
            val activeServices = services.documents.count { it.getString("status") == "ACTIVE" }

            val orders = firestore.collection("orders").get().await()
            val totalOrders = orders.size()
            var pendingOrders = 0
            var inProgressOrders = 0
            var completedOrders = 0
            var failedOrders = 0
            var totalRevenuePaise = 0L

            for (doc in orders.documents) {
                val status = doc.getString("status") ?: ""
                val price = doc.getLong("pricePaise") ?: 0L
                when (status) {
                    "PENDING", "AWAITING_ADMIN" -> pendingOrders++
                    "IN_PROGRESS", "PROCESSING" -> inProgressOrders++
                    "COMPLETED" -> {
                        completedOrders++
                        totalRevenuePaise += price
                    }
                    "CANCELLED", "FAILED" -> failedOrders++
                }
            }

            val recharges = firestore.collection("payment_requests").get().await()
            var pendingRecharges = 0
            var totalApprovedPaise = 0L

            for (doc in recharges.documents) {
                val status = doc.getString("status") ?: ""
                val amount = doc.getLong("amountPaise") ?: 0L
                if (status == "PENDING") {
                    pendingRecharges++
                } else if (status == "APPROVED") {
                    totalApprovedPaise += amount
                }
            }

            val finalTotalServices = if (totalServices > 0) totalServices else DEFAULT_SERVICES.size
            val finalActiveServices = if (totalServices > 0) activeServices else DEFAULT_SERVICES.count { it.status == "ACTIVE" }

            AdminStats(
                totalUsers = users,
                totalServices = finalTotalServices,
                activeServices = finalActiveServices,
                totalOrders = totalOrders,
                pendingOrders = pendingOrders,
                inProgressOrders = inProgressOrders,
                completedOrders = completedOrders,
                failedOrders = failedOrders,
                pendingRecharges = pendingRecharges,
                totalApprovedRechargePaise = totalApprovedPaise,
                totalRevenuePaise = totalRevenuePaise
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating admin stats", e)
            AdminStats()
        }
    }
}
