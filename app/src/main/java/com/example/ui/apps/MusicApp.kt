package com.example.ui.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MusicTrack
import com.example.ui.theme.*

@Composable
fun MusicApp(
    playlist: List<MusicTrack>,
    currentTrackIndex: Int,
    isPlaying: Boolean,
    positionSeconds: Int,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onSelectTrack: (Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTrack = playlist.getOrElse(currentTrackIndex) { playlist.first() }
    val progress = (positionSeconds.toFloat() / currentTrack.durationSeconds.toFloat()).coerceIn(0f, 1f)

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
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Samsung Music",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Album Art Box with animated gradient glow
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.linearGradient(currentTrack.coverGradient)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(72.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = currentTrack.title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${currentTrack.artist} • ${currentTrack.album}",
                color = Color(0xFF8E99A8),
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Slider
            Slider(
                value = progress,
                onValueChange = {},
                colors = SliderDefaults.colors(thumbColor = MusicPink, activeTrackColor = MusicPink)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = String.format("%02d:%02d", positionSeconds / 60, positionSeconds % 60),
                    color = Color(0xFF8892A6),
                    fontSize = 11.sp
                )
                Text(
                    text = String.format("%02d:%02d", currentTrack.durationSeconds / 60, currentTrack.durationSeconds % 60),
                    color = Color(0xFF8892A6),
                    fontSize = 11.sp
                )
            }

            // Playback Controls (Prev, Play/Pause, Next)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrev, modifier = Modifier.size(46.dp)) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Prev", tint = Color.White, modifier = Modifier.size(32.dp))
                }

                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(MusicPink)
                        .clickable { onTogglePlay() }
                        .testTag("music_play_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                IconButton(onClick = onNext, modifier = Modifier.size(46.dp)) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(32.dp))
                }
            }
        }

        Divider(color = Color(0xFF222838), modifier = Modifier.padding(horizontal = 16.dp))

        // Playlist List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            itemsIndexed(playlist) { index, track ->
                val isSelected = index == currentTrackIndex
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectTrack(index) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF221F2D) else Color(0xFF161922)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Brush.linearGradient(track.coverGradient)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                            Column {
                                Text(
                                    text = track.title,
                                    color = if (isSelected) MusicPink else Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(text = track.artist, color = Color(0xFF8892A6), fontSize = 11.sp)
                            }
                        }

                        Text(
                            text = String.format("%02d:%02d", track.durationSeconds / 60, track.durationSeconds % 60),
                            color = Color(0xFF8892A6),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
