package com.example.ui.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WeatherData
import com.example.ui.theme.AuraCyan

@Composable
fun WeatherApp(
    weather: WeatherData,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
                )
            )
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
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "الطقس (Weather)",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Main City & Temp Card
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = weather.city, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "${weather.temp}°", color = Color.White, fontSize = 72.sp, fontWeight = FontWeight.Light)
                    Text(text = weather.condition, color = AuraCyan, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "العظمى: ${weather.high}° • الصغرى: ${weather.low}°", color = Color(0xFFE2E8F0), fontSize = 13.sp)
                }
            }

            // Hourly Forecast
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x4D000000))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("التوقعات بالساعة", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(weather.hourly) { hour ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(hour.time, color = Color(0xFFCBD5E1), fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Icon(Icons.Default.WbSunny, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("${hour.temp}°", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // 7-Day Forecast
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x4D000000))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("توقعات الـ 7 أيام القادمة", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        weather.daily.forEach { day ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(day.day, color = Color.White, fontSize = 13.sp, modifier = Modifier.width(70.dp))
                                Icon(Icons.Default.WbSunny, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(20.dp))
                                Text(day.condition, color = Color(0xFFCBD5E1), fontSize = 12.sp)
                                Text("${day.maxTemp}° / ${day.minTemp}°", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Weather Details (Humidity, Wind, UV)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0x4D000000))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("الرطوبة", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            Text("${weather.humidity}%", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0x4D000000))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("الرياح", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            Text("${weather.windKmh} كم/س", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0x4D000000))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("مؤشر UV", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            Text(weather.uvIndex, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
