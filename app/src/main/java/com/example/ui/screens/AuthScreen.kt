package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.data.FirebaseService
import com.example.ui.components.AnimatedGlowingBackground
import com.example.ui.components.GlassCard
import com.example.ui.components.LuxuryButton
import com.example.ui.screens.admin.customFieldColors
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BackgroundDarkSecondary
import com.example.ui.theme.BackgroundSurface
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.InstagramGradient
import com.example.ui.theme.LuxuryGold
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isSignUp by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showForgotDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var resetSentMessage by remember { mutableStateOf<String?>(null) }

    var showGoogleConfigDialog by remember { mutableStateOf(false) }

    AnimatedGlowingBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Brand Icon & Name
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(AccentCyan.copy(alpha = 0.3f), PrimaryBlue.copy(alpha = 0.4f))
                            )
                        )
                        .border(1.5.dp, AccentCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "M",
                        color = AccentCyan,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "MEGA SMM INDIA",
                    color = TextWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "Premier Indian Social Media Marketing Panel",
                    color = TextMuted,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = if (isSignUp) "Create Account" else "Welcome Back",
                            color = TextWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        if (errorMessage != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ErrorRed.copy(alpha = 0.15f))
                                    .border(1.dp, ErrorRed.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Text(errorMessage!!, color = ErrorRed, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        if (isSignUp) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Your Full Name") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = AccentCyan) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = customFieldColors()
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it.trim() },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = AccentCyan) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            colors = customFieldColors()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AccentCyan) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = TextDim
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth(),
                            colors = customFieldColors()
                        )

                        if (!isSignUp) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                Text(
                                    text = "Forgot Password?",
                                    color = AccentCyan,
                                    fontSize = 12.sp,
                                    modifier = Modifier.clickable {
                                        resetEmail = email
                                        showForgotDialog = true
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        LuxuryButton(
                            text = if (isSignUp) "Sign Up" else "Sign In",
                            onClick = {
                                if (email.isBlank() || password.isBlank()) {
                                    errorMessage = "Please enter email and password"
                                    return@LuxuryButton
                                }
                                if (isSignUp && name.isBlank()) {
                                    errorMessage = "Please enter your name"
                                    return@LuxuryButton
                                }

                                isLoading = true
                                errorMessage = null

                                scope.launch {
                                    try {
                                        if (isSignUp) {
                                            val authResult = FirebaseService.auth.createUserWithEmailAndPassword(email, password).await()
                                            val user = authResult.user
                                            if (user != null) {
                                                FirebaseService.syncUserProfile(user, customDisplayName = name.trim(), provider = "email")
                                                onLoginSuccess()
                                            }
                                        } else {
                                            val authResult = FirebaseService.auth.signInWithEmailAndPassword(email, password).await()
                                            val user = authResult.user
                                            if (user != null) {
                                                FirebaseService.syncUserProfile(user, provider = "email")
                                                onLoginSuccess()
                                            }
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = e.localizedMessage ?: "Authentication failed"
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },
                            isLoading = isLoading,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0x33CBD5E1)))
                            Text("  OR  ", color = TextDim, fontSize = 11.sp)
                            Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0x33CBD5E1)))
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Google Sign In Button
                        Button(
                            onClick = {
                                performGoogleSignIn(
                                    context = context,
                                    scope = scope,
                                    onLoading = { isLoading = it },
                                    onError = { errorMessage = it },
                                    onSuccess = onLoginSuccess
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BackgroundSurface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .border(1.dp, Brush.horizontalGradient(listOf(Color(0x44FFFFFF), Color(0x2200F5FF))), RoundedCornerShape(14.dp))
                        ) {
                            Text("Continue with Google", color = TextWhite, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 1-Tap Quick Admin Access for virtual box / emulator
                        Button(
                            onClick = {
                                isLoading = true
                                errorMessage = null
                                scope.launch {
                                    val adminEmail = "hafizur200511@gmail.com"
                                    val adminPass = "MegaSmm@2026"
                                    try {
                                        val res = FirebaseService.auth.signInWithEmailAndPassword(adminEmail, adminPass).await()
                                        val user = res.user
                                        if (user != null) {
                                            FirebaseService.syncUserProfile(user, customDisplayName = "Hafizur Rahman", provider = "email")
                                            onLoginSuccess()
                                        }
                                    } catch (e: Exception) {
                                        try {
                                            val res = FirebaseService.auth.createUserWithEmailAndPassword(adminEmail, adminPass).await()
                                            val user = res.user
                                            if (user != null) {
                                                FirebaseService.syncUserProfile(user, customDisplayName = "Hafizur Rahman", provider = "email")
                                                onLoginSuccess()
                                            }
                                        } catch (createEx: Exception) {
                                            email = adminEmail
                                            errorMessage = "Account exists. Please enter your password above."
                                        }
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold.copy(alpha = 0.18f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .border(1.dp, LuxuryGold.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        ) {
                            Text("👑 1-Tap Admin Login (hafizur200511@gmail.com)", color = LuxuryGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (isSignUp) "Already have an account? " else "Don't have an account? ",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (isSignUp) "Sign In" else "Sign Up",
                                color = AccentCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.clickable {
                                    isSignUp = !isSignUp
                                    errorMessage = null
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Official Instagram Support Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val igIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/megasmmindia"))
                        context.startActivity(igIntent)
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(InstagramGradient)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = TextWhite, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "24/7 Official Support: @megasmmindia",
                            color = TextWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }

    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            title = { Text("Reset Password", color = TextWhite) },
            text = {
                Column {
                    Text("Enter your email to receive a password reset link.", color = TextMuted, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = customFieldColors()
                    )
                    if (resetSentMessage != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(resetSentMessage!!, color = EmeraldGreen, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (resetEmail.isNotBlank()) {
                            scope.launch {
                                try {
                                    FirebaseService.auth.sendPasswordResetEmail(resetEmail.trim()).await()
                                    resetSentMessage = "Password reset email sent! Please check your inbox."
                                } catch (e: Exception) {
                                    resetSentMessage = "Error: ${e.localizedMessage}"
                                }
                            }
                        }
                    }
                ) {
                    Text("Send Link", color = AccentCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotDialog = false }) {
                    Text("Close", color = TextDim)
                }
            },
            containerColor = BackgroundSurface
        )
    }

    var manualClientId by remember { mutableStateOf("") }

    if (showGoogleConfigDialog) {
        AlertDialog(
            onDismissRequest = { showGoogleConfigDialog = false },
            title = { Text("Connect 1-Tap Google Sign-In", color = TextWhite) },
            text = {
                Column {
                    Text(
                        "Google Sign-In requires your Firebase Web Client ID (from Firebase Console -> Authentication -> Google -> Web SDK configuration).",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = manualClientId,
                        onValueChange = { manualClientId = it },
                        label = { Text("Paste Web Client ID") },
                        placeholder = { Text("e.g. 600165504488-xxxx.apps.googleusercontent.com") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = customFieldColors(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("App Package: Com.google.MegaSmmIndia", color = AccentCyan, fontSize = 11.sp)
                    Text("SHA-1: F8:18:33:00:55:B6:8C:5A:EE:97:37:04:88:38:B9:8A:76:E3:2D:C1", color = TextDim, fontSize = 10.sp)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val cleanId = manualClientId.trim()
                        if (cleanId.isNotBlank()) {
                            val prefs = context.getSharedPreferences("mega_auth_prefs", Context.MODE_PRIVATE)
                            prefs.edit().putString("google_web_client_id", cleanId).apply()
                            showGoogleConfigDialog = false
                            performGoogleSignIn(
                                context = context,
                                scope = scope,
                                onLoading = { isLoading = it },
                                onError = { errorMessage = it },
                                onSuccess = onLoginSuccess
                            )
                        } else {
                            showGoogleConfigDialog = false
                        }
                    }
                ) {
                    Text(if (manualClientId.isNotBlank()) "Save & Sign In" else "Close", color = AccentCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoogleConfigDialog = false }) {
                    Text("Use Email / Password", color = TextDim)
                }
            },
            containerColor = BackgroundSurface
        )
    }
}

private fun performGoogleSignIn(
    context: Context,
    scope: kotlinx.coroutines.CoroutineScope,
    onLoading: (Boolean) -> Unit,
    onError: (String) -> Unit,
    onSuccess: () -> Unit
) {
    val activity = context as? Activity
    val officialClientId = "600165504488-gusn6b3vd20h6rie5q21l4l79an4r8ks.apps.googleusercontent.com"

    onLoading(true)
    scope.launch {
        var credentialSuccess = false
        try {
            val credentialManager = CredentialManager.create(context)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(officialClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = FirebaseService.auth.signInWithCredential(authCredential).await()
                val user = authResult.user
                if (user != null) {
                    credentialSuccess = true
                    FirebaseService.syncUserProfile(user, provider = "google")
                    onLoading(false)
                    onSuccess()
                    return@launch
                }
            }
        } catch (e: GetCredentialCancellationException) {
            onLoading(false)
            return@launch
        } catch (e: Exception) {
            android.util.Log.w("AuthScreen", "CredentialManager: ${e.message}, falling back to Web OAuth")
        }

        if (!credentialSuccess && activity != null) {
            try {
                val provider = com.google.firebase.auth.OAuthProvider.newBuilder("google.com")
                provider.addCustomParameter("prompt", "select_account")
                FirebaseService.auth.startActivityForSignInWithProvider(activity, provider.build())
                    .addOnSuccessListener { authResult ->
                        val user = authResult.user
                        if (user != null) {
                            scope.launch {
                                FirebaseService.syncUserProfile(user, provider = "google")
                                onLoading(false)
                                onSuccess()
                            }
                        } else {
                            onLoading(false)
                        }
                    }
                    .addOnFailureListener { ex ->
                        onLoading(false)
                        onError("Google Login: ${ex.localizedMessage ?: "Sign-in cancelled or failed."}")
                    }
            } catch (ex: Exception) {
                onLoading(false)
                onError("Google Login: ${ex.localizedMessage}")
            }
        } else if (!credentialSuccess) {
            onLoading(false)
            onError("Please use 1-Tap Admin Login or Email / Password on this virtual emulator.")
        }
    }
}
