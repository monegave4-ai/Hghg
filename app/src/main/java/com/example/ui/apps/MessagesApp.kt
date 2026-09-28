package com.example.ui.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.ContactEntity
import com.example.data.local.entities.MessageEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MessagesApp(
    messages: List<MessageEntity>,
    contacts: List<ContactEntity>,
    onSendMessage: (String, String, String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeConversationPhone by remember { mutableStateOf<String?>(null) }
    var activeConversationName by remember { mutableStateOf<String?>(null) }

    if (activeConversationPhone != null) {
        val convMessages = messages.filter { it.contactPhoneNumber == activeConversationPhone }
        ConversationDetailScreen(
            contactName = activeConversationName ?: activeConversationPhone!!,
            phoneNumber = activeConversationPhone!!,
            messages = convMessages,
            onSendMessage = { text ->
                onSendMessage(activeConversationPhone!!, activeConversationName ?: activeConversationPhone!!, text)
            },
            onBack = { activeConversationPhone = null }
        )
    } else {
        MessagesListScreen(
            messages = messages,
            contacts = contacts,
            onSelectConversation = { phone, name ->
                activeConversationPhone = phone
                activeConversationName = name
            },
            onBack = onBack,
            modifier = modifier
        )
    }
}

@Composable
fun MessagesListScreen(
    messages: List<MessageEntity>,
    contacts: List<ContactEntity>,
    onSelectConversation: (String, String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Group messages by contactPhoneNumber
    val conversations = messages.groupBy { it.contactPhoneNumber }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F121A))
    ) {
        // App Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "الرسائل",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (conversations.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "لا توجد محادثات سابقة",
                    color = Color(0xFF7E8A9E),
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(conversations.keys.toList()) { phone ->
                    val thread = conversations[phone] ?: emptyList()
                    val lastMsg = thread.maxByOrNull { it.timestamp }
                    val matchedContact = contacts.find { it.phoneNumber == phone }
                    val displayName = matchedContact?.name ?: lastMsg?.senderName ?: phone

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSelectConversation(phone, displayName) },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF181C26))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(MessagesBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = displayName.take(1),
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = displayName,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = formatTime(lastMsg?.timestamp ?: System.currentTimeMillis()),
                                        color = Color(0xFF8892A6),
                                        fontSize = 11.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = lastMsg?.text ?: "",
                                    color = Color(0xFFB0B8C8),
                                    fontSize = 13.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConversationDetailScreen(
    contactName: String,
    phoneNumber: String,
    messages: List<MessageEntity>,
    onSendMessage: (String) -> Unit,
    onBack: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0E14))
    ) {
        // Conversation Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF151821))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MessagesBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(text = contactName.take(1), color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = contactName, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(text = phoneNumber, color = Color(0xFF8892A6), fontSize = 11.sp)
            }
        }

        // Messages Bubble Stream
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val isMe = msg.isFromMe
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isMe) 16.dp else 4.dp,
                                    bottomEnd = if (isMe) 4.dp else 16.dp
                                )
                            )
                            .background(
                                if (isMe) MessagesBlue else Color(0xFF222736)
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Column {
                            Text(
                                text = msg.text,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = formatTime(msg.timestamp),
                                color = if (isMe) Color(0xCCFFFFFF) else Color(0xFF8892A6),
                                fontSize = 10.sp,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }
                }
            }
        }

        // Bottom Input Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF151821))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("اكتب رسالة نصية...", color = Color(0xFF7E8A9E), fontSize = 13.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("sms_input_field"),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1E2330),
                    unfocusedContainerColor = Color(0xFF1A1E29),
                    focusedBorderColor = MessagesBlue,
                    unfocusedBorderColor = Color(0xFF2C3242),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                maxLines = 3
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        onSendMessage(inputText)
                        inputText = ""
                    }
                },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MessagesBlue)
                    .testTag("sms_send_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    val fmt = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return fmt.format(Date(millis))
}
