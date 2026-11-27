package com.mixcom.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    onSelectUser: (UserProfile) -> Unit,
    onLogout: () -> Unit
) {
    val supabase = SupabaseClientProvider.client
    val currentUserId = supabase.auth.currentUserOrNull()?.id.orEmpty()
    var users by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            val allUsers = supabase.from("profiles").select().decodeList<UserProfile>()
            users = allUsers.filter { it.id != currentUserId }
        } catch (_: Exception) {}
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mixcom Chat") },
                actions = {
                    TextButton(onClick = {
                        scope.launch {
                            supabase.auth.signOut()
                            onLogout()
                        }
                    }) {
                        Text("Logout")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            items(users) { user ->
                ListItem(
                    headlineContent = { Text(user.displayName) },
                    supportingContent = { Text(user.email) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectUser(user) }
                        .padding(horizontal = 8.dp)
                )
                HorizontalDivider()
            }
        }
    }
}
