package com.mixcom.chat

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(onAuthSuccess: () -> Unit) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isSignUp) "Create Account" else "Mixcom Chat Login",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(Modifier.height(16.dp))

        if (isSignUp) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Display Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                scope.launch {
                    try {
                        val supabase = SupabaseClientProvider.client
                        if (isSignUp) {
                            supabase.auth.signUpWith(Email) {
                                this.email = email.trim()
                                this.password = password
                            }
                            val userId = supabase.auth.currentUserOrNull()?.id
                            if (userId != null) {
                                supabase.from("profiles").insert(
                                    UserProfile(
                                        id = userId,
                                        email = email.trim(),
                                        displayName = name.ifBlank { email.substringBefore("@") }
                                    )
                                )
                                onAuthSuccess()
                            } else {
                                statusText = "Check email to confirm or try Login"
                            }
                        } else {
                            supabase.auth.signInWith(Email) {
                                this.email = email.trim()
                                this.password = password
                            }
                            onAuthSuccess()
                        }
                    } catch (e: Exception) {
                        statusText = e.message ?: "Auth failed"
                    }
                }
            }
        ) {
            Text(if (isSignUp) "Sign Up" else "Login")
        }

        TextButton(onClick = { isSignUp = !isSignUp }) {
            Text(if (isSignUp) "Already have an account? Login" else "New user? Create Account")
        }

        if (statusText.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(statusText, color = MaterialTheme.colorScheme.error)
        }
    }
}
