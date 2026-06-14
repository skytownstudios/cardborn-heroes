package com.skytownstudios.cardbornheroes.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.skytownstudios.cardbornheroes.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {
    val content = ContentRepository(application)
    private val playerRepo = PlayerRepository(application)

    var player by mutableStateOf(PlayerState())
        private set

    var battle by mutableStateOf<BattleState?>(null)
        private set

    var showVault by mutableStateOf(false)
    var showProfile by mutableStateOf(false)
    var showQuests by mutableStateOf(false)

    var packRevealCards by mutableStateOf<List<PackRevealCard>?>(null)
        private set
    var packRevealIndex by mutableIntStateOf(0)
    var openingPackId by mutableStateOf<String?>(null)

    var equipPickerHandIndex by mutableIntStateOf(-1)
    var equipPickerSlotIndex by mutableIntStateOf(-1)

    val activeHand: Hand get() = player.hands.getOrElse(player.activeHandIndex) { Hand() }
    val activeHandPower: Int get() = HandPower.calculate(activeHand, content)

    init {
        viewModelScope.launch {
            player = playerRepo.load()
            applyFarmTicks()
            playerRepo.save(player)
            while (true) {
                delay(FarmEngine.TICK_MS)
                applyFarmTicks()
                playerRepo.save(player)
            }
        }
    }

    private fun updatePlayer(block: (PlayerState) -> PlayerState) {
        player = block(player)
        viewModelScope.launch { playerRepo.save(player) }
    }

    private fun applyFarmTicks() {
        val result = FarmEngine.processTicks(player, content) ?: return
        player = applyTickResult(player, result)
    }

    private fun applyTickResult(s: PlayerState, result: FarmEngine.TickResult): PlayerState {
        var next = s.copy(
            crowns = s.crowns + result.crownsGained,
            lastFarmTickEpochMs = result.newLastTickMs,
            farmCrownsRemainder = result.crownsRemainder,
            farmMaterialRemainder = result.materialRemainder,
            stats = s.stats.copy(
                crownsEarned = s.stats.crownsEarned + result.crownsGained
            )
        )
        next = addMaterials(next, result.materialGains)
        next = addHeroes(next, result.heroDrops)
        next = addGear(next, result.gearDrops)
        next = addPacks(next, result.packDrops)
        return next
    }

    fun beginBattle() {
        val stage = content.currentStage(player.campaignStageIndex) ?: return
        if (activeHand.slots.none { !it.isEmpty }) return
        battle = BattleEngine.startBattle(content, activeHand, stage)
    }

    fun advanceBattle() {
        val b = battle ?: return
        battle = BattleEngine.advance(b)
    }

    fun closeBattle(victory: Boolean) {
        val reward = battle?.crownRewardPending ?: 0
        if (victory) {
            updatePlayer { s ->
                var next = s.copy(
                    crowns = s.crowns + reward,
                    campaignStageIndex = (s.campaignStageIndex + 1).coerceAtMost(
                        content.campaign.stages.lastIndex
                    ),
                    stats = s.stats.copy(
                        wins = s.stats.wins + 1,
                        crownsEarned = s.stats.crownsEarned + reward
                    )
                )
                next = incrementQuest(next, "campaign_wins")
                next = checkHandBuiltQuest(next)
                next
            }
        } else {
            updatePlayer { s ->
                s.copy(stats = s.stats.copy(losses = s.stats.losses + 1))
            }
        }
        battle = null
    }

    fun selectFarm(farmId: String) {
        updatePlayer { it.copy(activeFarmId = farmId) }
    }

    fun setActiveHand(index: Int) {
        if (index in 0..2) updatePlayer { it.copy(activeHandIndex = index) }
    }

    fun equipCard(handIndex: Int, slotIndex: Int, type: String, cardId: String) {
        if (handIndex !in 0..2 || slotIndex !in 0..4) return
        if (!canEquip(type, cardId, handIndex, slotIndex)) return
        updatePlayer { s ->
            val hands = s.hands.toMutableList()
            val hand = hands[handIndex]
            val slots = hand.slots.toMutableList()
            val prev = slots[slotIndex]
            if (!prev.isEmpty) slots[slotIndex] = HandSlot()
            slots[slotIndex] = HandSlot(type, cardId)
            hands[handIndex] = Hand(slots)
            var next = s.copy(hands = hands)
            next = checkHandBuiltQuest(next)
            next
        }
        equipPickerHandIndex = -1
        equipPickerSlotIndex = -1
    }

    fun unequipSlot(handIndex: Int, slotIndex: Int) {
        if (handIndex !in 0..2 || slotIndex !in 0..4) return
        updatePlayer { s ->
            val hands = s.hands.toMutableList()
            val slots = hands[handIndex].slots.toMutableList()
            slots[slotIndex] = HandSlot()
            hands[handIndex] = Hand(slots)
            s.copy(hands = hands)
        }
    }

    fun canEquip(type: String, cardId: String, handIndex: Int, slotIndex: Int): Boolean {
        val available = availableCount(type, cardId, handIndex, slotIndex)
        return available > 0
    }

    fun availableCount(type: String, cardId: String, excludeHand: Int = -1, excludeSlot: Int = -1): Int {
        val owned = when (type) {
            "hero" -> player.heroCounts[cardId] ?: 0
            "gear" -> player.gearCounts[cardId] ?: 0
            else -> 0
        }
        val inHands = player.hands.withIndex().sumOf { (hi, hand) ->
            hand.slots.withIndex().count { (si, slot) ->
                if (hi == excludeHand && si == excludeSlot) false
                else slot.type == type && slot.cardId == cardId
            }
        }
        return owned - inHands
    }

    fun buyPack(packId: String): Boolean {
        val pack = content.pack(packId) ?: return false
        val canBuy = when (pack.currency) {
            "crowns" -> player.crowns >= pack.cost
            "sigils" -> player.sigils >= pack.cost
            else -> false
        }
        if (!canBuy) return false
        updatePlayer { s ->
            val packs = s.packInventory.toMutableMap()
            packs[packId] = (packs[packId] ?: 0) + 1
            when (pack.currency) {
                "crowns" -> s.copy(crowns = s.crowns - pack.cost, packInventory = packs)
                "sigils" -> s.copy(sigils = s.sigils - pack.cost, packInventory = packs)
                else -> s
            }
        }
        return true
    }

    fun startOpenPack(packId: String): Boolean {
        val count = player.packInventory[packId] ?: 0
        if (count <= 0) return false
        val pack = content.pack(packId) ?: return false
        val cards = PackGenerator.generate(content, pack)
        packRevealCards = cards
        packRevealIndex = 0
        openingPackId = packId
        updatePlayer { s ->
            val packs = s.packInventory.toMutableMap()
            val left = (packs[packId] ?: 1) - 1
            if (left <= 0) packs.remove(packId) else packs[packId] = left
            s.copy(packInventory = packs)
        }
        return true
    }

    fun revealNextCard() {
        val cards = packRevealCards ?: return
        if (packRevealIndex < cards.lastIndex) {
            packRevealIndex++
        } else {
            applyPackRewards(cards)
            packRevealCards = null
            packRevealIndex = 0
            openingPackId = null
        }
    }

    fun skipToGuaranteed() {
        val cards = packRevealCards ?: return
        packRevealIndex = cards.lastIndex
    }

    fun finishPackReveal() {
        val cards = packRevealCards ?: return
        applyPackRewards(cards)
        packRevealCards = null
        packRevealIndex = 0
        openingPackId = null
    }

    private fun applyPackRewards(cards: List<PackRevealCard>) {
        updatePlayer { s ->
            var next = s.copy(
                stats = s.stats.copy(packsOpened = s.stats.packsOpened + 1)
            )
            for (card in cards) {
                when (card.type) {
                    "hero" -> next = addHeroes(next, mapOf(card.id to 1))
                    "gear" -> next = addGear(next, mapOf(card.id to 1))
                    "material" -> next = addMaterials(next, mapOf(card.id to card.qty))
                }
            }
            next = incrementQuest(next, "packs_opened")
            next
        }
    }

    fun craftRecipe(recipeId: String): Boolean {
        val recipe = content.recipe(recipeId) ?: return false
        if (!canCraft(recipe)) return false
        updatePlayer { s ->
            var mats = s.materials.toMutableMap()
            recipe.materials.forEach { m ->
                mats[m.id] = (mats[m.id] ?: 0) - m.qty
            }
            mats = mats.filterValues { it > 0 }.toMutableMap()
            var next = s.copy(
                materials = mats,
                discoveredRecipes = s.discoveredRecipes + recipeId,
                stats = s.stats.copy(craftsDone = s.stats.craftsDone + 1)
            )
            next = when (recipe.resultType) {
                "hero" -> addHeroes(next, mapOf(recipe.resultId to 1))
                "gear" -> addGear(next, mapOf(recipe.resultId to 1))
                else -> next
            }
            next
        }
        return true
    }

    fun canCraft(recipe: RecipeDef): Boolean =
        recipe.materials.all { m -> (player.materials[m.id] ?: 0) >= m.qty }

    fun claimQuest(questId: String): Boolean {
        val quest = content.quest(questId) ?: return false
        if (questId in player.questClaimed) return false
        val progress = questProgress(quest)
        if (progress < quest.progressTarget) return false
        updatePlayer { s ->
            val packs = s.packInventory.toMutableMap()
            packs[quest.rewardPackId] = (packs[quest.rewardPackId] ?: 0) + 1
            s.copy(
                packInventory = packs,
                questClaimed = s.questClaimed + questId,
                stats = s.stats.copy(questsClaimed = s.stats.questsClaimed + 1)
            )
        }
        return true
    }

    fun questProgress(quest: QuestDef): Int = when (quest.type) {
        "hands_built" -> if (handIsBuilt(activeHand)) 1 else 0
        else -> player.questProgress[quest.id] ?: 0
    }

    fun effectiveCrownsPerHour(): Int {
        val farm = content.farm(player.activeFarmId) ?: return 0
        return (farm.baseCrownsPerHour * HandPower.farmMultiplier(activeHandPower)).toInt()
    }

    private fun handIsBuilt(hand: Hand): Boolean {
        val hasHero = hand.slots.any { it.type == "hero" && !it.isEmpty }
        val hasGear = hand.slots.any { it.type == "gear" && !it.isEmpty }
        return hasHero && hasGear
    }

    private fun checkHandBuiltQuest(s: PlayerState): PlayerState {
        if (!handIsBuilt(s.hands.getOrElse(s.activeHandIndex) { Hand() })) return s
        val progress = s.questProgress.toMutableMap()
        progress["quest_build_hand"] = 1
        return s.copy(questProgress = progress)
    }

    private fun incrementQuest(s: PlayerState, type: String): PlayerState {
        val progress = s.questProgress.toMutableMap()
        content.quests.filter { it.type == type }.forEach { q ->
            progress[q.id] = (progress[q.id] ?: 0) + 1
        }
        return s.copy(questProgress = progress)
    }

    private fun addMaterials(s: PlayerState, gains: Map<String, Int>): PlayerState {
        if (gains.isEmpty()) return s
        val mats = s.materials.toMutableMap()
        gains.forEach { (id, qty) -> mats[id] = (mats[id] ?: 0) + qty }
        return s.copy(materials = mats)
    }

    private fun addHeroes(s: PlayerState, gains: Map<String, Int>): PlayerState {
        if (gains.isEmpty()) return s
        val heroes = s.heroCounts.toMutableMap()
        var discovered = s.discoveredHeroes
        gains.forEach { (id, qty) ->
            heroes[id] = (heroes[id] ?: 0) + qty
            discovered = discovered + id
        }
        return s.copy(heroCounts = heroes, discoveredHeroes = discovered)
    }

    private fun addGear(s: PlayerState, gains: Map<String, Int>): PlayerState {
        if (gains.isEmpty()) return s
        val gear = s.gearCounts.toMutableMap()
        var discovered = s.discoveredGear
        gains.forEach { (id, qty) ->
            gear[id] = (gear[id] ?: 0) + qty
            discovered = discovered + id
        }
        return s.copy(gearCounts = gear, discoveredGear = discovered)
    }

    private fun addPacks(s: PlayerState, gains: Map<String, Int>): PlayerState {
        if (gains.isEmpty()) return s
        val packs = s.packInventory.toMutableMap()
        gains.forEach { (id, qty) -> packs[id] = (packs[id] ?: 0) + qty }
        return s.copy(packInventory = packs)
    }
}
