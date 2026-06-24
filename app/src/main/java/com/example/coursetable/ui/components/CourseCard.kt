package com.example.coursetable.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.coursetable.data.model.Course

/**
 * Card displaying a single course in the grid.
 * Shows name, teacher, and classroom with color coding.
 */
@Composable
fun CourseCard(
    course: Course,
    isOngoing: Boolean = false,
    cardWidth: Dp,
    cardHeight: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val elevationState = animateDpAsState(
        targetValue = if (isOngoing) 6.dp else 1.dp,
        label = "cardElevation"
    )
    val elevation = elevationState.value

    Card(
        modifier = modifier
            .width(cardWidth)
            .height(cardHeight)
            .padding(horizontal = 1.dp, vertical = 1.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(course.color).copy(alpha = 0.88f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Course name
            Text(
                text = course.name,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 13.sp
            )

            if (course.teacher.isNotBlank()) {
                Spacer(modifier = Modifier.height(1.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = course.teacher,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 9.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (course.classroom.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = course.classroom,
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 9.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Ongoing indicator
            if (isOngoing) {
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFFFEB3B))
                )
            }
        }
    }
}
