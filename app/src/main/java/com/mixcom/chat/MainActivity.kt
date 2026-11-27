package com.mixcom.chat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import io.github.jan.supabase.gotrue.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                var isLoggedIn by remember {
                    mutableStateOf(SupabaseClientProvider.client.auth.currentUserOrNull() != null)
                }
                var selectedPeer by remember { mutableStateOf<UserProfile?>(null) }

                when {
                    !isLoggedIn -> AuthScreen(onAuthSuccess = { isLoggedIn = true })
                    selectedPeer != null -> ChatScreen(
                        peerUser = selectedPeer!!,
                        onBack = { selectedPeer = null }
                    )
                    else -> ChatListScreen(
                        onSelectUser = { selectedPeer = it },
                        onLogout = {
                            selectedPeer = null
                            isLoggedIn = false
                        }
                    )
                }
            }
        }
    }
}
