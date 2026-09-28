package com.example.ui.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.ui.theme.*

@Composable
fun PhoneDialerApp(
    contacts: List<ContactEntity>,
    onStartCall: (String, String) -> Unit,
    onSendMessage: (String, String) -> Unit,
    onAddContact: (String, String, String) -> Unit,
    onToggleFavorite: (ContactEntity) -> Unit,
    onDeleteContact: (ContactEntity) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Keypad, 1: Recents, 2: Contacts
    var dialedNumber by remember { mutableStateOf("") }
    var contactSearch by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

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
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "الهاتف",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (selectedTab == 2) {
                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.testTag("add_contact_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "Add Contact",
                        tint = SamsungBlueLight
                    )
                }
            }
        }

        // Tabs (Keypad / Recents / Contacts)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DialerTabItem("لوحة الاتصال", selectedTab == 0) { selectedTab = 0 }
            DialerTabItem("الأخيرة", selectedTab == 1) { selectedTab = 1 }
            DialerTabItem("جهات الاتصال", selectedTab == 2) { selectedTab = 2 }
        }

        Divider(color = Color(0xFF222838), modifier = Modifier.padding(top = 8.dp))

        // Tab Content
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> KeypadView(
                    dialedNumber = dialedNumber,
                    onNumberChange = { dialedNumber = it },
                    onCall = {
                        if (dialedNumber.isNotBlank()) {
                            onStartCall(dialedNumber, dialedNumber)
                        }
                    }
                )
                1 -> RecentsViewTab(
                    contacts = contacts,
                    onCall = onStartCall,
                    onMessage = onSendMessage
                )
                2 -> ContactsViewTab(
                    contacts = contacts,
                    searchQuery = contactSearch,
                    onSearchChange = { contactSearch = it },
                    onCall = onStartCall,
                    onMessage = onSendMessage,
                    onToggleFavorite = onToggleFavorite,
                    onDelete = onDeleteContact
                )
            }
        }

        // Add Contact Dialog
        if (showAddDialog) {
            AddContactDialog(
                initialPhone = dialedNumber,
                onDismiss = { showAddDialog = false },
                onSave = { name, phone, email ->
                    onAddContact(name, phone, email)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun DialerTabItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        Text(
            text = title,
            color = if (isSelected) SamsungBlueLight else Color(0xFF8C96A8),
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
        Spacer(modifier = Modifier.height(4.dp))
        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(28.dp)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(SamsungBlueLight)
            )
        }
    }
}

@Composable
fun KeypadView(
    dialedNumber: String,
    onNumberChange: (String) -> Unit,
    onCall: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Dialed Number Display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                text = dialedNumber.ifBlank { " " },
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            if (dialedNumber.isNotBlank()) {
                Text(
                    text = "إضافة إلى جهات الاتصال",
                    color = SamsungBlueLight,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // T9 Keypad Matrix (1-9, *, 0, #)
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val rows = listOf(
                listOf("1" to "", "2" to "ABC", "3" to "DEF"),
                listOf("4" to "GHI", "5" to "JKL", "6" to "MNO"),
                listOf("7" to "PQRS", "8" to "TUV", "9" to "WXYZ"),
                listOf("*" to "", "0" to "+", "#" to "")
            )

            rows.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    row.forEach { (digit, letters) ->
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            KeypadButton(
                                digit = digit,
                                letters = letters,
                                onClick = { onNumberChange(dialedNumber + digit) }
                            )
                        }
                    }
                }
            }
        }

        // Bottom Call and Backspace Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.size(48.dp))

            // Big Green Call Button
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(PhoneGreen)
                    .clickable { onCall() }
                    .testTag("dialer_call_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Call",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }

            // Backspace button
            if (dialedNumber.isNotBlank()) {
                IconButton(
                    onClick = { onNumberChange(dialedNumber.dropLast(1)) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Backspace,
                        contentDescription = "Delete",
                        tint = Color(0xFF9EABB8)
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }
        }
    }
}

@Composable
fun KeypadButton(
    digit: String,
    letters: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(62.dp)
            .clip(CircleShape)
            .background(Color(0xFF1E2330))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = digit,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            if (letters.isNotBlank()) {
                Text(
                    text = letters,
                    color = Color(0xFF8892A6),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun RecentsViewTab(
    contacts: List<ContactEntity>,
    onCall: (String, String) -> Unit,
    onMessage: (String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(contacts) { contact ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF191D28))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(contact.avatarColorHex))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = contact.name.take(1),
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text(
                                text = contact.name,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "مكالمة واردة • ${contact.phoneNumber}",
                                color = Color(0xFF8892A6),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { onMessage(contact.phoneNumber, contact.name) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF262C3D))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubble,
                                contentDescription = "SMS",
                                tint = MessagesBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = { onCall(contact.name, contact.phoneNumber) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PhoneGreen)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Call",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContactsViewTab(
    contacts: List<ContactEntity>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onCall: (String, String) -> Unit,
    onMessage: (String, String) -> Unit,
    onToggleFavorite: (ContactEntity) -> Unit,
    onDelete: (ContactEntity) -> Unit
) {
    val filtered = if (searchQuery.isBlank()) contacts else contacts.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.phoneNumber.contains(searchQuery)
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("بحث في جهات الاتصال...", color = Color(0xFF7E8A9E), fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SamsungBlueLight) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF181C26),
                unfocusedContainerColor = Color(0xFF141720),
                focusedBorderColor = SamsungBlueLight,
                unfocusedBorderColor = Color(0xFF2B3244),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filtered, key = { it.id }) { contact ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF171B26))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(contact.avatarColorHex))),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = contact.name.take(1),
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = contact.name,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (contact.isFavorite) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Favorite",
                                            tint = Color(0xFFFFD54F),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = contact.phoneNumber,
                                    color = Color(0xFF8892A6),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(
                                onClick = { onToggleFavorite(contact) },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = if (contact.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Star",
                                    tint = if (contact.isFavorite) Color(0xFFFFD54F) else Color(0xFF7E8A9E),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = { onMessage(contact.phoneNumber, contact.name) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF242A3B))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChatBubble,
                                    contentDescription = "SMS",
                                    tint = MessagesBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = { onCall(contact.name, contact.phoneNumber) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(PhoneGreen)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Call",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
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
fun AddContactDialog(
    initialPhone: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf(initialPhone) }
    var email by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة جهة اتصال جديدة", color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("الاسم") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_contact_name_field")
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الهاتف") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_contact_phone_field")
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("البريد الإلكتروني (اختياري)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onSave(name, phone, email)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SamsungBlueLight),
                modifier = Modifier.testTag("save_contact_btn")
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = Color.White)
            }
        },
        containerColor = Color(0xFF1E2330)
    )
}
