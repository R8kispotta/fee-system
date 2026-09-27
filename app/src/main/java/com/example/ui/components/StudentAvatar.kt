package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

private val AvatarGradients = listOf(
    Pair(Color(0xFF2563EB), Color(0xFF1D4ED8)), // Blue
    Pair(Color(0xFF0D9488), Color(0xFF047857)), // Teal/Emerald
    Pair(Color(0xFF7C3AED), Color(0xFF6D28D9)), // Violet
    Pair(Color(0xFFEA580C), Color(0xFFC2410C)), // Orange
    Pair(Color(0xFFDB2777), Color(0xFFBE185D)), // Pink
    Pair(Color(0xFF4F46E5), Color(0xFF3730A3))  // Indigo
)

@Composable
fun StudentAvatar(
    name: String,
    photoUri: String?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    if (!photoUri.isNullOrEmpty()) {
        AsyncImage(
            model = photoUri,
            contentDescription = "Photo of $name",
            modifier = modifier
                .size(size)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        val initials = remember(name) {
            val parts = name.trim().split(" ")
            if (parts.size >= 2) {
                "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
            } else {
                name.take(2).uppercase()
            }
        }

        val gradient = remember(name) {
            val hash = kotlin.math.abs(name.hashCode())
            AvatarGradients[hash % AvatarGradients.size]
        }

        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(gradient.first, gradient.second))),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.4f).sp
            )
        }
    }
}
