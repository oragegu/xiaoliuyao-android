package com.littleliuyao

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class AppView(
    val label: String,
    val mark: String,
) {
    Divinate("测", "◇"),
    Knowledge("识", "□"),
    Records("记", "≡"),
}

@Composable
fun PullScreen(
    repository: PullRepository = remember { PullRepository() },
) {
    val context = LocalContext.current
    val savedPullRepository = remember(context) { SavedPullRepository(context) }
    val records = remember { mutableStateListOf<SavedPull>() }
    var activeView by remember { mutableStateOf(AppView.Divinate) }
    var intention by remember { mutableStateOf("") }
    var privatePull by remember { mutableStateOf(true) }
    var pulledCards by remember { mutableStateOf<List<PulledCard>>(emptyList()) }
    var message by remember { mutableStateOf<String?>(null) }
    var pulling by remember { mutableStateOf(false) }
    var savedCurrentPullId by remember { mutableStateOf<Long?>(null) }
    val revealed = remember { mutableStateListOf<Boolean>() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(savedPullRepository) {
        records.clear()
        records.addAll(savedPullRepository.load())
    }

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
            },
    ) {
        when (activeView) {
            AppView.Divinate -> DivinateView(
                intention = intention,
                onIntentionChange = { intention = it },
                privatePull = privatePull,
                onPrivatePullChange = { privatePull = it },
                pulledCards = pulledCards,
                message = message,
                pulling = pulling,
                saved = savedCurrentPullId != null,
                revealed = revealed,
                onPull = {
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
                        savedCurrentPullId = null
                        pulling = false
                    }
                },
                onSave = {
                    if (pulledCards.isNotEmpty()) {
                        val savedPull = savedPullRepository.save(intention, pulledCards)
                        records.add(0, savedPull)
                        savedCurrentPullId = savedPull.id
                        message = "Saved to records."
                    }
                },
            )

            AppView.Knowledge -> KnowledgeView()
            AppView.Records -> RecordsView(
                records = records,
                onDelete = { record ->
                    savedPullRepository.delete(record.id)
                    records.remove(record)
                    if (savedCurrentPullId == record.id) {
                        savedCurrentPullId = null
                    }
                },
                onEdit = { record, updatedIntention ->
                    savedPullRepository.updateIntention(record.id, updatedIntention)?.let { updatedRecord ->
                        val index = records.indexOfFirst { it.id == record.id }
                        if (index != -1) {
                            records[index] = updatedRecord
                        }
                        if (savedCurrentPullId == record.id) {
                            intention = updatedRecord.intention
                        }
                    }
                },
                onOpen = { record ->
                    intention = record.intention
                    pulledCards = record.cards
                    revealed.clear()
                    repeat(record.cards.size) { revealed.add(true) }
                    savedCurrentPullId = record.id
                    message = "Loaded from records."
                    activeView = AppView.Divinate
                },
            )
        }

        BottomTabs(
            activeView = activeView,
            onSelect = { activeView = it },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun DivinateView(
    intention: String,
    onIntentionChange: (String) -> Unit,
    privatePull: Boolean,
    onPrivatePullChange: (Boolean) -> Unit,
    pulledCards: List<PulledCard>,
    message: String?,
    pulling: Boolean,
    saved: Boolean,
    revealed: MutableList<Boolean>,
    onPull: () -> Unit,
    onSave: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp)
            .padding(bottom = 140.dp),
    ) {
        Text(
            text = "小六爻",
            color = Parchment,
            style = MaterialTheme.typography.displaySmall,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "轻轻起一课，再决定下一步。",
            color = MutedParchment,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(18.dp))

        IntentionPanel(
            intention = intention,
            onIntentionChange = onIntentionChange,
            privatePull = privatePull,
            onPrivatePullChange = onPrivatePullChange,
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
            onClick = onPull,
        ) {
            Text(if (pulling) "起课中..." else "测")
        }

        message?.let {
            Spacer(Modifier.height(12.dp))
            CalmMessage(it)
        }

        if (pulledCards.isNotEmpty()) {
            Spacer(Modifier.height(18.dp))
            ResultPanel(
                pulledCards = pulledCards,
                revealed = revealed,
                saved = saved,
                onSave = onSave,
            )
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
            .border(1.dp, Gold.copy(alpha = 0.22f), RoundedCornerShape(8.dp))
            .padding(14.dp),
    ) {
        Text("问一件眼前事", color = Parchment, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = intention,
            onValueChange = onIntentionChange,
            placeholder = { Text("例如：这次面试会顺利吗？") },
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
                    text = "Local randomness. Save only when you tap Save.",
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
private fun ResultPanel(
    pulledCards: List<PulledCard>,
    revealed: MutableList<Boolean>,
    saved: Boolean,
    onSave: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Charcoal.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
            .border(1.dp, Gold.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pulledCards.joinToString(" · ") { it.card.name },
                    color = Rose,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = pulledCards.joinToString(" · ") { it.deck.deckName },
                    color = MutedParchment,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Spacer(Modifier.width(12.dp))
            OutlinedButton(
                enabled = !saved,
                shape = RoundedCornerShape(8.dp),
                onClick = onSave,
            ) {
                Text(if (saved) "已存" else "保存")
            }
        }

        Spacer(Modifier.height(14.dp))
        Text(
            text = "解读占位：这里可以写整体趋势、适合行动或等待的提示。之后你可以把每张牌的真实解释替换进来。",
            color = Parchment,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(14.dp))
        val cardGap = 12.dp
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
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f),
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

@Composable
private fun KnowledgeView() {
    var selectedDeckId by remember { mutableStateOf(LiuYaoDecks.all.first().id) }
    val selectedDeck = LiuYaoDecks.deckById(selectedDeckId) ?: LiuYaoDecks.all.first()
    var expandedCardKey by remember(selectedDeckId) { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 18.dp)
            .padding(bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text(
                text = "六宫速览",
                color = Parchment,
                style = MaterialTheme.typography.displaySmall,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "把眼前事落到不同状态里。这里先放占位解释，方便之后逐条替换。",
                color = MutedParchment,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        item {
            KnowledgeDeckTabs(
                decks = LiuYaoDecks.all,
                selectedDeckId = selectedDeckId,
                onDeckSelect = { selectedDeckId = it },
            )
        }

        item {
            Text(
                text = "${selectedDeck.deckName} · ${selectedDeck.englishLabel}",
                color = Gold,
                style = MaterialTheme.typography.titleMedium,
            )
        }

        items(selectedDeck.cards, key = { "${selectedDeck.id}-${it.id}" }) { card ->
            val cardKey = "${selectedDeck.id}-${card.id}"
            KnowledgeCard(
                deck = selectedDeck,
                card = card,
                expanded = expandedCardKey == cardKey,
                onToggle = {
                    expandedCardKey = if (expandedCardKey == cardKey) null else cardKey
                },
            )
        }
    }
}

@Composable
private fun KnowledgeDeckTabs(
    decks: List<CardDeck>,
    selectedDeckId: String,
    onDeckSelect: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Charcoal.copy(alpha = 0.74f), RoundedCornerShape(8.dp))
            .border(1.dp, Gold.copy(alpha = 0.18f), RoundedCornerShape(8.dp))
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        decks.forEach { deck ->
            val selected = deck.id == selectedDeckId
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        color = if (selected) Gold.copy(alpha = 0.16f) else Color.Transparent,
                        shape = RoundedCornerShape(8.dp),
                    )
                    .border(
                        width = 1.dp,
                        color = if (selected) Gold.copy(alpha = 0.42f) else Color.Transparent,
                        shape = RoundedCornerShape(8.dp),
                    )
                    .clickable { onDeckSelect(deck.id) }
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = deck.deckName,
                    color = if (selected) Rose else Parchment,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${deck.cards.size}张",
                    color = MutedParchment,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun KnowledgeCard(
    deck: CardDeck,
    card: LiuYaoCard,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Charcoal.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
            .border(1.dp, Gold.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .clickable(onClick = onToggle)
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = deck.englishLabel,
                    color = Jade,
                    style = MaterialTheme.typography.labelLarge,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = card.name,
                    color = Rose,
                    fontFamily = FontFamily.Serif,
                    style = MaterialTheme.typography.headlineMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = if (expanded) "收起" else "展开",
                color = Gold,
                style = MaterialTheme.typography.labelLarge,
            )
        }

        if (expanded) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "${card.name} 的解释占位。可以写它代表的状态、节奏、适合的行动，以及在感情、事业、学业、身心里的提示。",
                color = Parchment,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "口诀占位：这里之后放简短记忆句。",
                color = MutedParchment,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun RecordsView(
    records: List<SavedPull>,
    onDelete: (SavedPull) -> Unit,
    onEdit: (SavedPull, String) -> Unit,
    onOpen: (SavedPull) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 18.dp)
            .padding(bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text(
                text = "记",
                color = Parchment,
                style = MaterialTheme.typography.displaySmall,
            )
        }

        if (records.isEmpty()) {
            item {
                EmptyRecords()
            }
        } else {
            items(records, key = { it.id }) { record ->
                RecordCard(
                    record = record,
                    onDelete = { onDelete(record) },
                    onEdit = { onEdit(record, it) },
                    onOpen = { onOpen(record) },
                )
            }
        }
    }
}

@Composable
private fun EmptyRecords() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Charcoal.copy(alpha = 0.86f), RoundedCornerShape(8.dp))
            .border(1.dp, Gold.copy(alpha = 0.18f), RoundedCornerShape(8.dp))
            .padding(18.dp),
    ) {
        Text("还没有保存的课。", color = Parchment, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "测完之后点保存，就会出现在这里。",
            color = MutedParchment,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun RecordCard(
    record: SavedPull,
    onDelete: () -> Unit,
    onEdit: (String) -> Unit,
    onOpen: () -> Unit,
) {
    var editing by remember(record.id) { mutableStateOf(false) }
    var editedIntention by remember(record.id, record.intention) { mutableStateOf(record.intention) }
    val cardModifier = Modifier
        .fillMaxWidth()
        .background(Charcoal.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
        .border(1.dp, Gold.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
        .then(if (editing) Modifier else Modifier.clickable(onClick = onOpen))
        .padding(18.dp)

    Column(
        modifier = cardModifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.cards.firstOrNull()?.card?.name ?: "未起课",
                    color = Rose,
                    style = MaterialTheme.typography.headlineMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = formatPullDate(record.createdAt),
                    color = MutedParchment,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    shape = RoundedCornerShape(8.dp),
                    onClick = {
                        editedIntention = record.intention
                        editing = true
                    },
                ) {
                    Text("改")
                }
                OutlinedButton(
                    shape = RoundedCornerShape(8.dp),
                    onClick = onDelete,
                ) {
                    Text("删")
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        if (editing) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = editedIntention,
                onValueChange = { editedIntention = it },
                placeholder = { Text("未写问题") },
                minLines = 2,
                maxLines = 4,
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
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Gold,
                        contentColor = Ink,
                    ),
                    onClick = {
                        onEdit(editedIntention)
                        editing = false
                    },
                ) {
                    Text("保存")
                }
                OutlinedButton(
                    shape = RoundedCornerShape(8.dp),
                    onClick = {
                        editedIntention = record.intention
                        editing = false
                    },
                ) {
                    Text("取消")
                }
            }
        } else {
            Text(
                text = record.intention.ifBlank { "未写问题" },
                color = Parchment,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = "解读占位：这里之后显示保存时的详细解释摘要。",
            color = Parchment,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(14.dp))
        RecordChip(
            text = record.cards.joinToString(" · ") { it.card.name },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun RecordChip(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier
            .background(Gold.copy(alpha = 0.14f), RoundedCornerShape(8.dp))
            .border(1.dp, Gold.copy(alpha = 0.44f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        text = text,
        color = Parchment,
        style = MaterialTheme.typography.labelLarge,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun BottomTabs(
    activeView: AppView,
    onSelect: (AppView) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Charcoal.copy(alpha = 0.98f))
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        AppView.entries.forEach { view ->
            val selected = view == activeView
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelect(view) }
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    modifier = Modifier
                        .background(
                            color = if (selected) Gold.copy(alpha = 0.16f) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp),
                        )
                        .padding(horizontal = 22.dp, vertical = 8.dp),
                    text = view.mark,
                    color = if (selected) Rose else MutedParchment,
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = view.label,
                    color = if (selected) Rose else MutedParchment,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
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

private fun formatPullDate(timestamp: Long): String {
    return SimpleDateFormat("yyyy年M月d日 HH:mm", Locale.CHINA).format(Date(timestamp))
}
