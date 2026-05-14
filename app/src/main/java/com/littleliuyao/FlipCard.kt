package com.littleliuyao

import android.animation.ValueAnimator
import android.graphics.BitmapFactory
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.IOException

@Composable
fun FlipCard(
    pulledCard: PulledCard,
    revealed: Boolean,
    onReveal: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val reduceMotion = !ValueAnimator.areAnimatorsEnabled()
    val rotation by animateFloatAsState(
        targetValue = if (revealed) 180f else 0f,
        animationSpec = tween(durationMillis = if (reduceMotion) 0 else 520),
        label = "cardFlip",
    )
    val showFront = rotation > 90f || (reduceMotion && revealed)

    Box(
        modifier = modifier
            .then(if (compact) Modifier.aspectRatio(1f) else Modifier.width(218.dp).aspectRatio(0.68f))
            .graphicsLayer {
                cameraDistance = 10f * density
                rotationY = if (reduceMotion) 0f else rotation
            }
            .clickable(enabled = !revealed, onClick = onReveal),
    ) {
        if (showFront) {
            CardFace(
                pulledCard = pulledCard,
                front = true,
                compact = compact,
                modifier = Modifier.graphicsLayer {
                    if (!reduceMotion) rotationY = 180f
                },
            )
        } else {
            CardFace(pulledCard = pulledCard, front = false, compact = compact)
        }
    }
}

@Composable
fun CardFace(
    pulledCard: PulledCard,
    front: Boolean,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = if (front) {
                        listOf(Color(0xFF33271F), Color(0xFF15100D))
                    } else {
                        listOf(Color(0xFF201915), Color(0xFF100C0A))
                    },
                ),
            )
            .border(1.dp, Gold.copy(alpha = if (front) 0.58f else 0.36f), shape)
            .padding(if (compact) 10.dp else 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (front) {
            CardFront(pulledCard, compact)
        } else {
            CardBack(pulledCard, compact)
        }
    }
}

@Composable
private fun CardBack(pulledCard: PulledCard, compact: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(cardIndex(pulledCard.position), color = Gold, style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(if (compact) 10.dp else 18.dp))
        Text(
            text = pulledCard.deck.deckName,
            color = Parchment,
            fontFamily = FontFamily.Serif,
            fontSize = if (compact) 30.sp else 34.sp,
            lineHeight = if (compact) 34.sp else 38.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = if (compact) "Tap to reveal" else "Waiting",
            color = MutedParchment,
            style = if (compact) MaterialTheme.typography.labelLarge else MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        if (!compact) {
            Spacer(Modifier.height(4.dp))
            Text("Ready to reveal", color = MutedParchment, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun CardFront(pulledCard: PulledCard, compact: Boolean) {
    val image = rememberCardImage(pulledCard)
    Box(modifier = Modifier.fillMaxSize()) {
        if (image != null && !compact) {
            Image(
                bitmap = image,
                contentDescription = pulledCard.card.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(168.dp)
                    .clip(RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Crop,
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(cardIndex(pulledCard.position), color = Gold, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(if (compact) 8.dp else 18.dp))
            Text(
                text = pulledCard.card.name,
                color = Parchment,
                fontFamily = FontFamily.Serif,
                fontSize = if (compact) 38.sp else 52.sp,
                lineHeight = if (compact) 42.sp else 56.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(if (compact) 8.dp else 16.dp))
            Text(
                text = pulledCard.deck.englishLabel,
                color = Jade,
                style = if (compact) MaterialTheme.typography.labelLarge else MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(if (compact) 2.dp else 4.dp))
            Text(
                text = pulledCard.deck.deckName,
                color = MutedParchment,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun rememberCardImage(pulledCard: PulledCard): androidx.compose.ui.graphics.ImageBitmap? {
    val assets = LocalContext.current.assets
    return produceState(
        initialValue = null as androidx.compose.ui.graphics.ImageBitmap?,
        key1 = pulledCard.deck.assetFolder,
        key2 = pulledCard.card.id,
    ) {
        value = try {
            assets.open("${pulledCard.deck.assetFolder}/${pulledCard.card.id}.jpg")
                .use { BitmapFactory.decodeStream(it) }
                ?.asImageBitmap()
        } catch (_: IOException) {
            null
        }
    }.value
}

private fun cardIndex(position: Int): String = position.toString().padStart(2, '0')
