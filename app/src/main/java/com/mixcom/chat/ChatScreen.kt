package com.mixcom.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    peerUser: UserProfile,
    onBack: () -> Unit
) {
    val supabase = SupabaseClientProvider.client
    val currentUserId = supabase.auth.currentUserOrNull()?.id.orEmpty()
    val scope = rememberCoroutineScope()
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var text by remember { mutableStateOf("") }

    suspend fun loadMessages() {
        messages = supabase.from("messages")
            .select {
                filter {
                    or {
                        and {
                            eq("sender_id", currentUserId)
                            eq("receiver_id", peerUser.id)
                        }
                        and {
                            eq("sender_id", peerUser.id)
                            eq("receiver_id", currentUserId)
                        }
                    }
                }
                order("created_at", Order.ASCENDING)
            }
            .decodeList()
    }

    LaunchedEffect(peerUser.id) {
        loadMessages()
        val channel = supabase.channel("room-${peerUser.id}")
        val changeFlow = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "messages"
        }
        channel.subscribe()
        changeFlow.collect {
            loadMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(peerUser.displayName) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { msg ->
                    val isMine = msg.senderId == currentUserId
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = if (isMine) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Surface(
                            color = if (isMine) Color(0xFFDCF8C6) else Color(0xFFEEEEEE),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = msg.content,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Message...") }
                )
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    val clean = text.trim()
                    if (clean.isNotEmpty()) {
                        text = ""
                        scope.launch {
                            supabase.from("messages").insert(
                                ChatMessage(
                                    senderId = currentUserId,
                                    receiverId = peerUser.id,
                                    content = clean
                                )
                            )
                            loadMessages()
                        }
                    }
                }) {
                    Text("Send")
                }
            }
        }
    }
}
