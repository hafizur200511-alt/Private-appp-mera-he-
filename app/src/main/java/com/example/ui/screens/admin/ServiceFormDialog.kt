package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.CurrencyUtils
import com.example.data.FirebaseService
import com.example.model.ApiProvider
import com.example.model.CategoryItem
import com.example.model.ServiceItem
import com.example.ui.components.LuxuryButton
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BackgroundDarkSecondary
import com.example.ui.theme.BackgroundSurface
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.launch

@Composable
fun ServiceFormDialog(
    serviceToEdit: ServiceItem?,
    categories: List<CategoryItem>,
    providers: List<ApiProvider>,
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    val isNew = serviceToEdit == null
    val scope = rememberCoroutineScope()

    var serviceId by remember { mutableStateOf(serviceToEdit?.serviceId ?: "") }
    var name by remember { mutableStateOf(serviceToEdit?.name ?: "") }
    var platform by remember { mutableStateOf(serviceToEdit?.platform ?: "Instagram") }
    var category by remember { mutableStateOf(serviceToEdit?.category ?: "Followers") }
    var description by remember { mutableStateOf(serviceToEdit?.description ?: "") }
    var minQty by remember { mutableStateOf(serviceToEdit?.minQuantity?.toString() ?: "100") }
    var maxQty by remember { mutableStateOf(serviceToEdit?.maxQuantity?.toString() ?: "10000") }
    var priceInput by remember {
        mutableStateOf(
            if (serviceToEdit != null) {
                String.format("%.2f", serviceToEdit.pricePaise / 100.0)
            } else "40.00"
        )
    }
    var priceUnit by remember { mutableStateOf(serviceToEdit?.priceUnit ?: "PER_1000") }
    var deliveryMethod by remember { mutableStateOf(serviceToEdit?.deliveryMethod ?: "MANUAL_ADMIN") }
    var providerId by remember { mutableStateOf(serviceToEdit?.providerId ?: "") }
    var providerServiceId by remember { mutableStateOf(serviceToEdit?.providerServiceId ?: "") }
    var status by remember { mutableStateOf(serviceToEdit?.status ?: "ACTIVE") }

    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val platformOptions = listOf("Instagram", "YouTube", "Facebook", "Telegram", "WhatsApp", "X / Twitter", "Threads", "Other")
    var platformExpanded by remember { mutableStateOf(false) }

    val categoryOptions = (listOf("Followers", "Likes", "Views", "Comments", "Shares", "Subscribers", "Members", "Reactions", "Watch Time", "Other") + categories.map { it.name }).distinct()
    var categoryExpanded by remember { mutableStateOf(false) }

    val providerOptions = providers
    var providerExpanded by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .background(BackgroundDarkSecondary)
                .border(1.dp, Brush.horizontalGradient(listOf(AccentCyan.copy(alpha = 0.5f), Color(0x330072FE))), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isNew) "+ Add New Service" else "Edit SMM Service",
                            color = TextWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isNew) "Configure service parameters and pricing" else "Service ID: ${serviceToEdit?.serviceId}",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextDim)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ErrorRed.copy(alpha = 0.15f))
                            .border(1.dp, ErrorRed.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Text(text = errorMessage!!, color = ErrorRed, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Platform Dropdown
                Text("A. PLATFORM", color = AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Box {
                    OutlinedTextField(
                        value = platform,
                        onValueChange = { platform = it },
                        readOnly = false,
                        trailingIcon = {
                            IconButton(onClick = { platformExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = AccentCyan)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = customFieldColors()
                    )
                    DropdownMenu(
                        expanded = platformExpanded,
                        onDismissRequest = { platformExpanded = false },
                        modifier = Modifier.background(BackgroundSurface)
                    ) {
                        platformOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt, color = TextWhite) },
                                onClick = {
                                    platform = opt
                                    platformExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Category Dropdown
                Text("B. CATEGORY", color = AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Box {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        trailingIcon = {
                            IconButton(onClick = { categoryExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = AccentCyan)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = customFieldColors()
                    )
                    DropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false },
                        modifier = Modifier.background(BackgroundSurface)
                    ) {
                        categoryOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt, color = TextWhite) },
                                onClick = {
                                    category = opt
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Service Info
                Text("C. SERVICE INFORMATION", color = AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = serviceId,
                    onValueChange = { serviceId = it.trim() },
                    label = { Text("Customer-facing Service ID (e.g. 101, 102)") },
                    enabled = isNew,
                    modifier = Modifier.fillMaxWidth(),
                    colors = customFieldColors()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Service Name (e.g. Instagram Followers HQ Non-Drop)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = customFieldColors()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description & Instructions") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    colors = customFieldColors()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = minQty,
                        onValueChange = { minQty = it },
                        label = { Text("Min Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = customFieldColors()
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = maxQty,
                        onValueChange = { maxQty = it },
                        label = { Text("Max Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = customFieldColors()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Price Configuration
                Text("D. PRICE CONFIGURATION", color = AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = priceInput,
                    onValueChange = { priceInput = it },
                    label = { Text("Selling Price in Rupees (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = customFieldColors()
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = priceUnit == "PER_1000",
                        onClick = { priceUnit = "PER_1000" },
                        colors = RadioButtonDefaults.colors(selectedColor = AccentCyan, unselectedColor = TextDim)
                    )
                    Text("Price per 1000 units", color = TextWhite, fontSize = 13.sp)

                    Spacer(modifier = Modifier.width(16.dp))

                    RadioButton(
                        selected = priceUnit == "FIXED",
                        onClick = { priceUnit = "FIXED" },
                        colors = RadioButtonDefaults.colors(selectedColor = AccentCyan, unselectedColor = TextDim)
                    )
                    Text("Fixed price per order", color = TextWhite, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Delivery Method
                Text("E. DELIVERY METHOD", color = AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = deliveryMethod == "AUTOMATIC_API",
                        onClick = { deliveryMethod = "AUTOMATIC_API" },
                        colors = RadioButtonDefaults.colors(selectedColor = AccentCyan, unselectedColor = TextDim)
                    )
                    Text("Automatic API Delivery", color = TextWhite, fontSize = 13.sp)

                    Spacer(modifier = Modifier.width(16.dp))

                    RadioButton(
                        selected = deliveryMethod == "MANUAL_ADMIN",
                        onClick = { deliveryMethod = "MANUAL_ADMIN" },
                        colors = RadioButtonDefaults.colors(selectedColor = AccentCyan, unselectedColor = TextDim)
                    )
                    Text("Manual Admin Processing", color = TextWhite, fontSize = 13.sp)
                }

                if (deliveryMethod == "AUTOMATIC_API") {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box {
                        val currentProv = providerOptions.find { it.id == providerId }
                        OutlinedTextField(
                            value = currentProv?.name ?: if (providerId.isNotBlank()) providerId else "Select API Provider",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { providerExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = AccentCyan)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = customFieldColors()
                        )
                        DropdownMenu(
                            expanded = providerExpanded,
                            onDismissRequest = { providerExpanded = false },
                            modifier = Modifier.background(BackgroundSurface)
                        ) {
                            if (providerOptions.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No API Providers configured (Add in Providers tab)", color = TextDim) },
                                    onClick = { providerExpanded = false }
                                )
                            }
                            providerOptions.forEach { prov ->
                                DropdownMenuItem(
                                    text = { Text("${prov.name} (${prov.baseUrl})", color = TextWhite) },
                                    onClick = {
                                        providerId = prov.id
                                        providerExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = providerServiceId,
                        onValueChange = { providerServiceId = it.trim() },
                        label = { Text("Provider's Remote Service ID") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = customFieldColors()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Service Status
                Text("F. SERVICE STATUS", color = AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("ACTIVE" to "Active", "INACTIVE" to "Inactive", "UNAVAILABLE" to "Unavailable").forEach { (stKey, stLabel) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = status == stKey,
                                onClick = { status = stKey },
                                colors = RadioButtonDefaults.colors(selectedColor = AccentCyan, unselectedColor = TextDim)
                            )
                            Text(stLabel, color = TextWhite, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (!isNew) {
                        Button(
                            onClick = { showDeleteConfirm = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed.copy(alpha = 0.2f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete", color = ErrorRed, fontSize = 13.sp)
                        }
                    }

                    LuxuryButton(
                        text = if (isNew) "Save Service" else "Update Service",
                        onClick = {
                            val parsedPricePaise = CurrencyUtils.rupeeStringToPaise(priceInput)
                            val parsedMin = minQty.toLongOrNull()
                            val parsedMax = maxQty.toLongOrNull()

                            if (serviceId.isBlank()) {
                                errorMessage = "Service ID cannot be empty"
                                return@LuxuryButton
                            }
                            if (name.isBlank()) {
                                errorMessage = "Service name cannot be empty"
                                return@LuxuryButton
                            }
                            if (parsedMin == null || parsedMin <= 0) {
                                errorMessage = "Minimum quantity must be greater than zero"
                                return@LuxuryButton
                            }
                            if (parsedMax == null || parsedMax < parsedMin) {
                                errorMessage = "Maximum quantity must be >= minimum quantity"
                                return@LuxuryButton
                            }
                            if (parsedPricePaise == null || parsedPricePaise <= 0) {
                                errorMessage = "Please enter a valid price in rupees"
                                return@LuxuryButton
                            }
                            if (deliveryMethod == "AUTOMATIC_API" && providerId.isBlank()) {
                                errorMessage = "Please select an API Provider for Automatic Delivery"
                                return@LuxuryButton
                            }

                            isSaving = true
                            errorMessage = null

                            val itemToSave = ServiceItem(
                                id = serviceToEdit?.id ?: "",
                                serviceId = serviceId,
                                name = name.trim(),
                                platform = platform,
                                category = category,
                                description = description.trim(),
                                minQuantity = parsedMin,
                                maxQuantity = parsedMax,
                                pricePaise = parsedPricePaise,
                                priceUnit = priceUnit,
                                deliveryMethod = deliveryMethod,
                                providerId = providerId,
                                providerServiceId = providerServiceId,
                                status = status
                            )

                            scope.launch {
                                val result = FirebaseService.saveService(itemToSave, isNew)
                                isSaving = false
                                if (result.isSuccess) {
                                    onSaved()
                                } else {
                                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to save service"
                                }
                            }
                        },
                        isLoading = isSaving,
                        modifier = Modifier.weight(2f)
                    )
                }
            }
        }
    }

    if (showDeleteConfirm && serviceToEdit != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Service?", color = TextWhite) },
            text = { Text("Are you sure you want to delete service #${serviceToEdit.serviceId}? If orders exist, it will be safely deactivated.", color = TextMuted) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        scope.launch {
                            FirebaseService.deleteService(serviceToEdit.id)
                            onSaved()
                        }
                    }
                ) {
                    Text("Delete", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = TextDim)
                }
            },
            containerColor = BackgroundSurface
        )
    }
}

@Composable
fun customFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextWhite,
    unfocusedTextColor = TextWhite,
    focusedBorderColor = AccentCyan,
    unfocusedBorderColor = Color(0x3300F5FF),
    focusedLabelColor = AccentCyan,
    unfocusedLabelColor = TextDim,
    cursorColor = AccentCyan
)
