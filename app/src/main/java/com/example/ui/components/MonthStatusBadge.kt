package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MonthFeeStatus
import com.example.ui.theme.OverdueRed
import com.example.ui.theme.OverdueRedLight
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.PaidGreenLight
import com.example.ui.theme.PendingAmber
import com.example.ui.theme.PendingAmberLight

@Composable
fun MonthStatusBadge(
    status: MonthFeeStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, text, icon) = when (status) {
        MonthFeeStatus.PAID -> Quad(
            PaidGreenLight,
            PaidGreen,
            "PAID",
            Icons.Default.CheckCircle
        )
        MonthFeeStatus.PARTIALLY_PAID -> Quad(
            Color(0xFFDBEAFE),
            Color(0xFF1D4ED8),
            "PARTIAL",
            Icons.Default.HourglassBottom
        )
        MonthFeeStatus.OVERDUE -> Quad(
            OverdueRedLight,
            OverdueRed,
            "OVERDUE",
            Icons.Default.Error
        )
        MonthFeeStatus.PENDING -> Quad(
            PendingAmberLight,
            PendingAmber,
            "PENDING",
            Icons.Default.Schedule
        )
        MonthFeeStatus.EXEMPT -> Quad(
            Color(0xFFF1F5F9),
            Color(0xFF64748B),
            "EXEMPT",
            Icons.Default.Schedule
        )
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = textColor,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

private data class Quad<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
