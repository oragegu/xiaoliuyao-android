package com.littleliuyao

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun PullScreen(
    repository: PullRepository = remember { PullRepository() },
) {
    var intention by remember { mutableStateOf("") }
    var privatePull by remember { mutableStateOf(true) }
    var pulledCards by remember { mutableStateOf<List<PulledCard>>(emptyList()) }
    var message by remember { mutableStateOf<String?>(null) }
    var pulling by remember { mutableStateOf(false) }
    val revealed = remember { mutableStateListOf<Boolean>() }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink)
            .drawBehind {
                drawCircle(
                    color = Jade.copy(alpha = 0.08f),
                    radius = size.width * 0.52f,
                    center = Offset(size.width * 0.14f, size.height * 0.16f),
                )
                drawCircle(
                    color = Rose.copy(alpha = 0.07f),
                    radius = size.width * 0.44f,
                    center = Offset(size.width * 0.95f, size.height * 0.42f),
                )
                drawCircle(
                    color = Gold.copy(alpha = 0.06f),
                    radius = size.width * 0.34f,
                    center = Offset(size.width * 0.48f, size.height * 0.98f),
                )
            },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            Text(
                text = "Little LiuYao",
                color = Gold,
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Pull a three-card signal.",
                color = Parchment,
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Set a quiet intention, draw from the three decks, and read the pattern that arrives.",
                color = MutedParchment,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(18.dp))

            IntentionPanel(
                intention = intention,
                onIntentionChange = { intention = it },
                privatePull = privatePull,
                onPrivatePullChange = { privatePull = it },
            )

            Spacer(Modifier.height(14.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Gold,
                    contentColor = Ink,
                ),
                enabled = !pulling,
                onClick = {
                    pulling = true
                    scope.launch {
                        when (val result = repository.pullCards(intention, privatePull)) {
                            is PullResult.Success -> {
                                pulledCards = result.cards
                                message = result.message
                            }

                            is PullResult.Failure -> {
                                pulledCards = result.fallbackCards
                                message = result.message
                            }
                        }
                        revealed.clear()
                        repeat(pulledCards.size) { revealed.add(false) }
                        pulling = false
                    }
                },
            ) {
                Text(if (pulling) "Pulling..." else "Pull 3 Cards")
            }

            message?.let {
                Spacer(Modifier.height(12.dp))
                CalmMessage(it)
            }

            if (pulledCards.isNotEmpty()) {
                Spacer(Modifier.height(18.dp))
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val cardGap = 12.dp
                    val cardSize = (maxWidth - cardGap) / 2
                    Column(verticalArrangement = Arrangement.spacedBy(cardGap)) {
                        pulledCards.chunked(2).forEach { rowCards ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(cardGap),
                            ) {
                                rowCards.forEach { pulledCard ->
                                    val index = pulledCard.position - 1
                                    FlipCard(
                                        pulledCard = pulledCard,
                                        revealed = revealed.getOrNull(index) == true,
                                        onReveal = {
                                            if (index in revealed.indices) {
                                                revealed[index] = true
                                            }
                                        },
                                        compact = true,
                                        modifier = Modifier.size(cardSize),
                                    )
                                }
                                if (rowCards.size == 1) {
                                    Spacer(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IntentionPanel(
    intention: String,
    onIntentionChange: (String) -> Unit,
    privatePull: Boolean,
    onPrivatePullChange: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Charcoal.copy(alpha = 0.96f), Soot.copy(alpha = 0.94f)),
                ),
                shape = RoundedCornerShape(8.dp),
            )
            .padding(14.dp),
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = intention,
            onValueChange = onIntentionChange,
            placeholder = { Text("Ask, notice, or simply breathe.") },
            minLines = 2,
            maxLines = 3,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Parchment,
                unfocusedTextColor = Parchment,
                focusedBorderColor = Gold,
                unfocusedBorderColor = MutedParchment.copy(alpha = 0.35f),
                cursorColor = Gold,
                focusedPlaceholderColor = MutedParchment,
                unfocusedPlaceholderColor = MutedParchment,
                focusedContainerColor = Ink.copy(alpha = 0.42f),
                unfocusedContainerColor = Ink.copy(alpha = 0.42f),
            ),
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Private Pull", color = Parchment, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Local randomness, nothing stored.",
                    color = MutedParchment,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Spacer(Modifier.width(12.dp))
            Switch(
                checked = privatePull,
                onCheckedChange = onPrivatePullChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Ink,
                    checkedTrackColor = Jade,
                    uncheckedThumbColor = Parchment,
                    uncheckedTrackColor = Color(0xFF5C4B3E),
                ),
            )
        }
    }
}

@Composable
private fun CalmMessage(text: String) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .background(Gold.copy(alpha = 0.13f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        text = text,
        color = Parchment,
        style = MaterialTheme.typography.bodyMedium,
    )
}
