package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.FirebaseService
import com.example.model.CategoryItem
import com.example.ui.components.LuxuryButton
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BackgroundDarkSecondary
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.launch

@Composable
fun CategoryManagerDialog(
    categoryToEdit: CategoryItem?,
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    val isNew = categoryToEdit == null
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf(categoryToEdit?.name ?: "") }
    var platform by remember { mutableStateOf(categoryToEdit?.platform ?: "Instagram") }
    var displayOrder by remember { mutableStateOf(categoryToEdit?.displayOrder?.toString() ?: "1") }

    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(20.dp))
                .background(BackgroundDarkSecondary)
                .border(1.dp, Brush.horizontalGradient(listOf(AccentCyan.copy(alpha = 0.5f), Color(0x330072FE))), RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isNew) "Add Category" else "Edit Category",
                        color = TextWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = TextDim)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (errorMessage != null) {
                    Text(errorMessage!!, color = ErrorRed, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name (e.g. Followers, Likes)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = customFieldColors()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = platform,
                    onValueChange = { platform = it },
                    label = { Text("Platform (e.g. Instagram, YouTube)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = customFieldColors()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = displayOrder,
                    onValueChange = { displayOrder = it },
                    label = { Text("Display Order (Number)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = customFieldColors()
                )

                Spacer(modifier = Modifier.height(18.dp))

                LuxuryButton(
                    text = if (isNew) "Create Category" else "Update Category",
                    onClick = {
                        if (name.isBlank()) {
                            errorMessage = "Name cannot be empty"
                            return@LuxuryButton
                        }
                        isSaving = true
                        scope.launch {
                            val cat = CategoryItem(
                                id = categoryToEdit?.id ?: "",
                                name = name.trim(),
                                platform = platform.trim(),
                                displayOrder = displayOrder.toIntOrNull() ?: 1,
                                isActive = true
                            )
                            val res = FirebaseService.saveCategory(cat)
                            isSaving = false
                            if (res.isSuccess) {
                                onSaved()
                            } else {
                                errorMessage = res.exceptionOrNull()?.message
                            }
                        }
                    },
                    isLoading = isSaving,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
