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
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.data.ApiProviderClient
import com.example.data.FirebaseService
import com.example.model.ApiProvider
import com.example.ui.components.LuxuryButton
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BackgroundDarkSecondary
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.launch

@Composable
fun ApiProviderDialog(
    providerToEdit: ApiProvider?,
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    val isNew = providerToEdit == null
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf(providerToEdit?.name ?: "") }
    var baseUrl by remember { mutableStateOf(providerToEdit?.baseUrl ?: "https://") }
    var apiKey by remember { mutableStateOf(providerToEdit?.apiKey ?: "") }
    var isEnabled by remember { mutableStateOf(providerToEdit?.enabled ?: true) }

    var isSaving by remember { mutableStateOf(false) }
    var isTesting by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<String?>(null) }
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
                    Column {
                        Text(
                            text = if (isNew) "Add SMM Provider" else "Edit Provider",
                            color = TextWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Standard SMM v2 API integration",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = TextDim)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (errorMessage != null) {
                    Text(errorMessage!!, color = ErrorRed, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                }

                if (testResult != null) {
                    Text(testResult!!, color = if (testResult!!.startsWith("Connection Success")) EmeraldGreen else ErrorRed, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Provider Name (e.g. SMM Rapid, JustAnotherPanel)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = customFieldColors()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    label = { Text("API Endpoint URL (e.g. https://provider.com/api/v2)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = customFieldColors()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key / Token") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = customFieldColors()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        isTesting = true
                        testResult = null
                        scope.launch {
                            val temp = ApiProvider(name = name, baseUrl = baseUrl.trim(), apiKey = apiKey.trim())
                            val res = ApiProviderClient.testConnection(temp)
                            isTesting = false
                            testResult = if (res.isSuccess) res.getOrNull() else res.exceptionOrNull()?.message
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = AccentCyan)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isTesting) "Testing Connection..." else "Test Connection", color = AccentCyan)
                }

                Spacer(modifier = Modifier.height(14.dp))

                LuxuryButton(
                    text = if (isNew) "Save Provider" else "Update Provider",
                    onClick = {
                        if (name.isBlank() || baseUrl.isBlank() || apiKey.isBlank()) {
                            errorMessage = "All fields are required"
                            return@LuxuryButton
                        }
                        isSaving = true
                        scope.launch {
                            val prov = ApiProvider(
                                id = providerToEdit?.id ?: "",
                                name = name.trim(),
                                baseUrl = baseUrl.trim(),
                                apiKey = apiKey.trim(),
                                enabled = isEnabled
                            )
                            val res = FirebaseService.saveApiProvider(prov)
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
