package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BatchPerformance
import com.example.data.model.DashboardSummary
import com.example.data.model.InstituteSettings
import com.example.ui.theme.PaidGreen
import kotlin.math.cos
import kotlin.math.sin

/**
 * Ava Agentic 3D Animated Sphere
 * Renders an interactive, iridescent, breathing 3D orb with dynamic light refraction,
 * pulsing aura rings, and rotating orbital energy paths inspired by AmazingUI's "Ava Agentic OS".
 */
@Composable
fun AvaAgenticSphere(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ava_sphere_transition")

    // Breathing pulse scale
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Orbital ring rotation angle
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_angle"
    )

    // Secondary reverse counter-rotation
    val counterAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "counter_angle"
    )

    // Glow aura intensity
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(pulseScale),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
            val radius = (size.toPx() / 2f) * 0.72f

            // 1. Ambient Outer Halo / Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF38BDF8).copy(alpha = glowAlpha * 0.5f),
                        Color(0xFF818CF8).copy(alpha = glowAlpha * 0.25f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius * 1.55f
                ),
                radius = radius * 1.55f,
                center = center
            )

            // 2. Rotating Outer Orbital Ring 1
            rotate(rotationAngle, pivot = center) {
                drawOval(
                    color = Color(0xFF67E8F9).copy(alpha = 0.65f),
                    topLeft = Offset(center.x - radius * 1.25f, center.y - radius * 0.45f),
                    size = androidx.compose.ui.geometry.Size(radius * 2.5f, radius * 0.9f),
                    style = Stroke(width = 1.6.dp.toPx())
                )
            }

            // 3. Counter-Rotating Orbital Ring 2 (Tilted)
            rotate(counterAngle + 45f, pivot = center) {
                drawOval(
                    color = Color(0xFFA855F7).copy(alpha = 0.45f),
                    topLeft = Offset(center.x - radius * 1.15f, center.y - radius * 0.55f),
                    size = androidx.compose.ui.geometry.Size(radius * 2.3f, radius * 1.1f),
                    style = Stroke(width = 1.2.dp.toPx())
                )
            }

            // 4. Core Iridescent 3D Sphere Body
            val lightSpecular = Offset(center.x - radius * 0.32f, center.y - radius * 0.35f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF), // Specular light highlight
                        Color(0xFF7DD3FC), // Electric ice cyan
                        Color(0xFF2563EB), // Deep energetic cobalt
                        Color(0xFF4F46E5), // Violet indigo depth
                        Color(0xFF0F172A)  // Deep shadow rim
                    ),
                    center = lightSpecular,
                    radius = radius * 1.25f
                ),
                radius = radius,
                center = center
            )

            // 5. Specular Gloss Reflection Lens
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.9f),
                        Color.White.copy(alpha = 0.0f)
                    ),
                    center = lightSpecular,
                    radius = radius * 0.45f
                ),
                topLeft = Offset(lightSpecular.x - radius * 0.28f, lightSpecular.y - radius * 0.22f),
                size = androidx.compose.ui.geometry.Size(radius * 0.56f, radius * 0.38f)
            )
        }
    }
}

/**
 * Ava Agentic OS - AI Daily Overview Card
 * Direct realization of the Dribbble concept featuring:
 * - Animated Agentic Sphere
 * - Contextual intelligent daily briefing
 * - 1-Tap actionable automated workflows with spring feedback
 * - Shimmer sweep and electric blue ambient lighting
 */
@Composable
fun AvaAgenticDailyOverviewCard(
    summary: DashboardSummary,
    settings: InstituteSettings,
    batchPerformances: List<BatchPerformance>,
    onDispatchRemindersClick: () -> Unit,
    onAiInsightsClick: () -> Unit,
    onManageBatchesClick: () -> Unit,
    modifier: Modifier = Modifier,
    initiallyMinimized: Boolean = true
) {
    var isMinimized by remember { mutableStateOf(initiallyMinimized) }
    var isExpanded by remember { mutableStateOf(false) }
    var completedActions by remember { mutableStateOf(setOf<Int>()) }

    val infiniteTransition = rememberInfiniteTransition(label = "ava_card_glow")
    val borderGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "border_glow"
    )

    // Contextual briefing text synthesized from real data
    val dailyBriefing = remember(summary, batchPerformances) {
        val pct = summary.collectionPercentage.toInt()
        val overdue = summary.overdueCount
        val net = summary.netProfitThisMonth.toInt()
        when {
            overdue > 0 -> "Autonomous analysis complete: $overdue student dues are currently overdue. Recommended: dispatch 1-tap WhatsApp notifications. Overall collection pace is at $pct% with ${settings.currencySymbol}$net net surplus."
            pct >= 80 -> "High operational efficiency: $pct% fees collected for ${summary.selectedMonthYear}. Batch attendance and cash flow are synchronized. Net treasury surplus is +${settings.currencySymbol}$net."
            else -> "Daily Overview: Collection pace is currently $pct% (${settings.currencySymbol}${summary.collectedThisMonth.toInt()} collected). ${summary.totalStudents} active students enrolled across ${batchPerformances.size} batches."
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioLowBouncy))
            .testTag("ava_agentic_daily_overview"),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.2.dp,
            Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF38BDF8).copy(alpha = borderGlowAlpha),
                    Color(0xFF818CF8).copy(alpha = borderGlowAlpha * 0.7f),
                    Color(0xFF2563EB).copy(alpha = borderGlowAlpha * 0.9f)
                )
            )
        ),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0B132B), // Deep electric midnight
                            Color(0xFF0F172A), // Slate obsidian
                            Color(0xFF131D38)  // Indigo shadow
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            if (isMinimized) {
                // Compact Minimized Bar (Sleek 38dp single-row pill)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isMinimized = false }
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        AvaAgenticSphere(size = 22.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AVA AGENTIC OS",
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            letterSpacing = 0.8.sp,
                            color = Color(0xFF67E8F9)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• AI Overview Ready",
                            fontSize = 9.5.sp,
                            color = Color(0xFFCBD5E1),
                            maxLines = 1
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0284C7).copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.6f)),
                        modifier = Modifier
                            .testTag("expand_ava_button")
                            .clickable { isMinimized = false }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Expand",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF67E8F9)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = "Expand AVA Agentic OS",
                                tint = Color(0xFF67E8F9),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            } else {
                // Full Expanded Agentic Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    // Top Header: Animated Sphere + Ava Agentic Title + Minimize Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AvaAgenticSphere(size = 36.dp)

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "AVA AGENTIC OS",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp,
                                        letterSpacing = 1.sp,
                                        color = Color(0xFF67E8F9)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    // Live Autonomous Badge
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF0284C7).copy(alpha = 0.25f),
                                        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.45f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = Color(0xFF34D399),
                                                modifier = Modifier.size(5.dp)
                                            ) {}
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "LIVE",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF67E8F9)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = "AI Daily Intelligence Overview",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // Explicit Minimize Button
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0284C7).copy(alpha = 0.3f),
                            border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.6f)),
                            modifier = Modifier
                                .testTag("minimize_ava_button")
                                .clickable { isMinimized = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ExpandLess,
                                    contentDescription = "Minimize AVA Agentic OS",
                                    tint = Color(0xFF67E8F9),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Minimize",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF67E8F9)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Conversational Agentic Intelligence Briefing
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.05f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = dailyBriefing,
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                color = Color(0xFFE2E8F0)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 1-Tap Agentic Action Items (Animated buttons)
                    Text(
                        text = "AGENTIC ACTIONS READY",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Action 1: Dispatch Overdue Alerts
                        AgenticActionButton(
                            title = if (summary.overdueCount > 0) "Alerts (${summary.overdueCount})" else "Fee Alerts",
                            icon = Icons.Default.NotificationsActive,
                            isDone = completedActions.contains(1),
                            accentColor = Color(0xFF10B981),
                            modifier = Modifier.weight(1f),
                            onClick = {
                                completedActions = completedActions + 1
                                onDispatchRemindersClick()
                            }
                        )

                        // Action 2: Gemini Financial Deep Dive
                        AgenticActionButton(
                            title = "AI Analysis",
                            icon = Icons.Default.AutoAwesome,
                            isDone = completedActions.contains(2),
                            accentColor = Color(0xFF38BDF8),
                            modifier = Modifier.weight(1f),
                            onClick = {
                                completedActions = completedActions + 2
                                onAiInsightsClick()
                            }
                        )

                        // Action 3: Review Batches
                        AgenticActionButton(
                            title = "Batches",
                            icon = Icons.Default.School,
                            isDone = completedActions.contains(3),
                            accentColor = Color(0xFFA855F7),
                            modifier = Modifier.weight(1f),
                            onClick = {
                                completedActions = completedActions + 3
                                onManageBatchesClick()
                            }
                        )
                    }

                    // Expandable Details: Telemetry and Health Metrics Toggle
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpanded = !isExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isExpanded) "Hide Institute Telemetry ▲" else "View Institute Telemetry ▼",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF38BDF8)
                        )
                    }

                    AnimatedVisibility(visible = isExpanded) {
                        Column(modifier = Modifier.padding(top = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                TelemetryMiniCard(
                                    label = "Collection Ratio",
                                    value = "${summary.collectionPercentage.toInt()}%",
                                    modifier = Modifier.weight(1f)
                                )
                                TelemetryMiniCard(
                                    label = "Total Inflow",
                                    value = "+${settings.currencySymbol}${summary.collectedThisMonth.toInt()}",
                                    modifier = Modifier.weight(1f)
                                )
                                TelemetryMiniCard(
                                    label = "Net Margin",
                                    value = "${settings.currencySymbol}${summary.netProfitThisMonth.toInt()}",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Explicit Bottom Minimize Button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ava_bottom_minimize_button")
                            .clickable { isMinimized = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExpandLess,
                                contentDescription = "Minimize AVA Agentic OS",
                                tint = Color(0xFF67E8F9),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Minimize AVA Agentic OS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF67E8F9)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AgenticActionButton(
    title: String,
    icon: ImageVector,
    isDone: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isDone) accentColor.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f),
        border = BorderStroke(
            1.dp,
            if (isDone) accentColor.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.15f)
        ),
        modifier = modifier
            .height(34.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isDone) Icons.Default.Check else icon,
                contentDescription = null,
                tint = if (isDone) accentColor else Color.White,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDone) accentColor else Color.White
            )
        }
    }
}

@Composable
private fun TelemetryMiniCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.Black.copy(alpha = 0.25f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(6.dp)) {
            Text(text = label, fontSize = 8.5.sp, color = Color(0xFF94A3B8))
            Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
