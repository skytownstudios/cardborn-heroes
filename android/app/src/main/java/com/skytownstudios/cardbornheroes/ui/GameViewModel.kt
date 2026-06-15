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
    private val prefsRepo = AppPreferencesRepository(application)

    var player by mutableStateOf(PlayerState())
        private set

    private var _darkMode by mutableStateOf(true)
    val darkMode: Boolean get() = _darkMode

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
    var equipPickerTarget by mutableStateOf("hero") // hero | main | off

    var slotDetailHandIndex by mutableIntStateOf(-1)
    var slotDetailSlotIndex by mutableIntStateOf(-1)
    var slotDetailTarget by mutableStateOf("hero") // hero | main | off

    var showFarmClaimModal by mutableStateOf(false)
    var lastClaimedFarmRewards by mutableStateOf(PendingFarmRewards())
        private set

    val activeHand: Hand get() = player.hands.getOrElse(player.activeHandIndex) { Hand() }
    val activeHandPower: Int get() = HandPower.calculate(activeHand, content)

    init {
        viewModelScope.launch {
            _darkMode = prefsRepo.loadDarkMode()
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

    fun setDarkMode(enabled: Boolean) {
        _darkMode = enabled
        viewModelScope.launch { prefsRepo.setDarkMode(enabled) }
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
        val pending = s.pendingFarmRewards
        val nextPending = pending.copy(
            crowns = pending.crowns + result.crownsGained,
            materials = PendingFarmRewards.mergeCounts(pending.materials, result.materialGains),
            heroes = PendingFarmRewards.mergeCounts(pending.heroes, result.heroDrops),
            gear = PendingFarmRewards.mergeCounts(pending.gear, result.gearDrops),
            packs = PendingFarmRewards.mergeCounts(pending.packs, result.packDrops)
        )
        return s.copy(
            lastFarmTickEpochMs = result.newLastTickMs,
            farmCrownsRemainder = result.crownsRemainder,
            farmMaterialRemainder = result.materialRemainder,
            pendingFarmRewards = nextPending
        )
    }

    fun claimFarmRewards() {
        val pending = player.pendingFarmRewards
        if (pending.isEmpty) return
        lastClaimedFarmRewards = pending
        updatePlayer { s ->
            var next = s.copy(pendingFarmRewards = PendingFarmRewards())
            if (pending.crowns > 0) {
                next = next.copy(
                    crowns = next.crowns + pending.crowns,
                    stats = next.stats.copy(
                        crownsEarned = next.stats.crownsEarned + pending.crowns
                    )
                )
            }
            next = addMaterials(next, pending.materials)
            next = addHeroes(next, pending.heroes)
            next = addGear(next, pending.gear)
            next = addPacks(next, pending.packs)
            next
        }
        showFarmClaimModal = true
    }

    fun dismissFarmClaimModal() {
        showFarmClaimModal = false
        lastClaimedFarmRewards = PendingFarmRewards()
    }

    fun selectedCampaignRun(): CampaignRun? =
        content.campaignRun(player.activeCampaignId, player.activeCampaignLevel)

    fun maxPlayableLevel(campaignId: String): Int {
        val best = player.campaignBestLevel[campaignId] ?: 0
        return CampaignScaler.maxPlayableLevel(best)
    }

    fun selectCampaign(campaignId: String) {
        if (content.campaignZone(campaignId) == null) return
        updatePlayer { s ->
            val maxLvl = CampaignScaler.maxPlayableLevel(s.campaignBestLevel[campaignId] ?: 0)
            s.copy(
                activeCampaignId = campaignId,
                activeCampaignLevel = 1.coerceIn(1, maxLvl)
            )
        }
    }

    fun setCampaignLevel(level: Int) {
        val maxLvl = maxPlayableLevel(player.activeCampaignId)
        val clamped = level.coerceIn(1, maxLvl)
        if (clamped == player.activeCampaignLevel) return
        updatePlayer { it.copy(activeCampaignLevel = clamped) }
    }

    fun beginBattle() {
        val run = selectedCampaignRun() ?: return
        if (activeHand.heroSlots.none { !it.isEmpty }) return
        val state = BattleEngine.startBattle(content, activeHand, run)
        battle = state
        val enemyIds = BattleEngine.enemyIdsInBattle(state)
        if (enemyIds.isNotEmpty()) {
            updatePlayer { it.copy(discoveredEnemies = it.discoveredEnemies + enemyIds) }
        }
    }

    fun advanceBattle() {
        val b = battle ?: return
        battle = BattleEngine.advance(b)
    }

    fun closeBattle(victory: Boolean) {
        val b = battle
        val crownReward = b?.crownRewardPending ?: 0
        val materialReward = b?.materialRewardPending ?: emptyMap()
        if (victory) {
            val campaignId = b?.campaignId?.takeIf { it.isNotEmpty() } ?: player.activeCampaignId
            val level = b?.campaignLevel ?: player.activeCampaignLevel
            updatePlayer { s ->
                val prevBest = s.campaignBestLevel[campaignId] ?: 0
                val newBest = if (level > prevBest) {
                    s.campaignBestLevel.toMutableMap().apply { put(campaignId, level) }
                } else s.campaignBestLevel
                var next = s.copy(
                    crowns = s.crowns + crownReward,
                    campaignBestLevel = newBest,
                    stats = s.stats.copy(
                        wins = s.stats.wins + 1,
                        crownsEarned = s.stats.crownsEarned + crownReward
                    )
                )
                next = addMaterials(next, materialReward)
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

    fun openHeroPicker(handIndex: Int, slotIndex: Int) {
        equipPickerHandIndex = handIndex
        equipPickerSlotIndex = slotIndex
        equipPickerTarget = "hero"
    }

    fun openMainHandPicker(handIndex: Int, slotIndex: Int) {
        equipPickerHandIndex = handIndex
        equipPickerSlotIndex = slotIndex
        equipPickerTarget = "main"
    }

    fun openOffHandPicker(handIndex: Int, slotIndex: Int) {
        equipPickerHandIndex = handIndex
        equipPickerSlotIndex = slotIndex
        equipPickerTarget = "off"
    }

    fun closeEquipPicker() {
        equipPickerHandIndex = -1
        equipPickerSlotIndex = -1
    }

    fun openSlotDetail(handIndex: Int, slotIndex: Int, target: String) {
        slotDetailHandIndex = handIndex
        slotDetailSlotIndex = slotIndex
        slotDetailTarget = target
    }

    fun closeSlotDetail() {
        slotDetailHandIndex = -1
        slotDetailSlotIndex = -1
    }

    fun equipHero(handIndex: Int, slotIndex: Int, heroId: String, heroStars: Int = 0) {
        if (handIndex !in 0..2 || slotIndex !in 0..4) return
        if (availableHeroCount(heroId, heroStars, handIndex, slotIndex) <= 0) return
        updatePlayer { s ->
            val hands = s.hands.toMutableList()
            val slots = hands[handIndex].heroSlots.toMutableList()
            slots[slotIndex] = HeroLoadout(heroId = heroId, heroStars = heroStars)
            hands[handIndex] = Hand(slots)
            checkHandBuiltQuest(s.copy(hands = hands))
        }
        closeEquipPicker()
    }

    fun equipMainHand(handIndex: Int, slotIndex: Int, gearId: String) {
        if (handIndex !in 0..2 || slotIndex !in 0..4) return
        val loadout = player.hands.getOrNull(handIndex)?.heroSlots?.getOrNull(slotIndex) ?: return
        if (loadout.isEmpty) return
        val hero = content.hero(loadout.heroId) ?: return
        val gear = content.gear(gearId) ?: return
        if (!LoadoutHelper.canEquipMainHand(gear, hero, loadout, content)) return
        if (availableGearCount(gearId, handIndex, slotIndex, "main") <= 0) return
        updatePlayer { s ->
            val hands = s.hands.toMutableList()
            val slots = hands[handIndex].heroSlots.toMutableList()
            var updated = loadout.copy(mainHandGearId = gearId)
            if (gear.hands >= 2) updated = updated.copy(offHandGearId = "")
            slots[slotIndex] = updated
            hands[handIndex] = Hand(slots)
            checkHandBuiltQuest(s.copy(hands = hands))
        }
        closeEquipPicker()
    }

    fun equipOffHand(handIndex: Int, slotIndex: Int, gearId: String) {
        if (handIndex !in 0..2 || slotIndex !in 0..4) return
        val loadout = player.hands.getOrNull(handIndex)?.heroSlots?.getOrNull(slotIndex) ?: return
        if (loadout.isEmpty) return
        val hero = content.hero(loadout.heroId) ?: return
        val gear = content.gear(gearId) ?: return
        if (!LoadoutHelper.canEquipOffHand(gear, hero, loadout, content)) return
        if (availableGearCount(gearId, handIndex, slotIndex, "off") <= 0) return
        updatePlayer { s ->
            val hands = s.hands.toMutableList()
            val slots = hands[handIndex].heroSlots.toMutableList()
            slots[slotIndex] = loadout.copy(offHandGearId = gearId)
            hands[handIndex] = Hand(slots)
            checkHandBuiltQuest(s.copy(hands = hands))
        }
        closeEquipPicker()
    }

    fun clearHeroSlot(handIndex: Int, slotIndex: Int) {
        if (handIndex !in 0..2 || slotIndex !in 0..4) return
        updatePlayer { s ->
            val hands = s.hands.toMutableList()
            val slots = hands[handIndex].heroSlots.toMutableList()
            slots[slotIndex] = HeroLoadout()
            hands[handIndex] = Hand(slots)
            s.copy(hands = hands)
        }
    }

    fun clearMainHand(handIndex: Int, slotIndex: Int) {
        if (handIndex !in 0..2 || slotIndex !in 0..4) return
        updatePlayer { s ->
            val hands = s.hands.toMutableList()
            val slots = hands[handIndex].heroSlots.toMutableList()
            val loadout = slots[slotIndex]
            slots[slotIndex] = loadout.copy(mainHandGearId = "")
            hands[handIndex] = Hand(slots)
            s.copy(hands = hands)
        }
    }

    fun clearOffHand(handIndex: Int, slotIndex: Int) {
        if (handIndex !in 0..2 || slotIndex !in 0..4) return
        updatePlayer { s ->
            val hands = s.hands.toMutableList()
            val slots = hands[handIndex].heroSlots.toMutableList()
            val loadout = slots[slotIndex]
            slots[slotIndex] = loadout.copy(offHandGearId = "")
            hands[handIndex] = Hand(slots)
            s.copy(hands = hands)
        }
    }

    fun availableHeroCount(
        heroId: String,
        stars: Int = 0,
        excludeHand: Int = -1,
        excludeSlot: Int = -1
    ): Int {
        val owned = HeroInventory.count(player.heroCounts, heroId, stars)
        val equipped = player.hands.withIndex().sumOf { (hi, hand) ->
            hand.heroSlots.withIndex().count { (si, slot) ->
                if (hi == excludeHand && si == excludeSlot) false
                else slot.heroId == heroId && slot.heroStars == stars
            }
        }
        return owned - equipped
    }

    fun heroStacksForPicker(excludeHand: Int, excludeSlot: Int): List<HeroStack> =
        HeroInventory.allStacks(player.heroCounts).mapNotNull { stack ->
            val avail = availableHeroCount(stack.heroId, stack.stars, excludeHand, excludeSlot)
            if (avail > 0) stack.copy(count = avail) else null
        }

    fun fusionCandidates(): List<HeroStack> =
        HeroInventory.allStacks(player.heroCounts).filter { stack ->
            stack.stars < HeroInventory.MAX_STARS &&
                availableHeroCount(stack.heroId, stack.stars) >= 3
        }

    fun canFuseHero(heroId: String, stars: Int): Boolean =
        stars < HeroInventory.MAX_STARS && availableHeroCount(heroId, stars) >= 3

    fun fuseHeroStack(heroId: String, stars: Int): Boolean {
        if (!canFuseHero(heroId, stars)) return false
        updatePlayer { s ->
            var heroes = HeroInventory.remove(s.heroCounts, heroId, stars, 3)
            heroes = HeroInventory.add(heroes, heroId, stars + 1, 1)
            s.copy(
                heroCounts = heroes,
                stats = s.stats.copy(craftsDone = s.stats.craftsDone + 1)
            )
        }
        return true
    }

    fun availableGearCount(
        gearId: String,
        excludeHand: Int = -1,
        excludeSlot: Int = -1,
        excludeWhich: String? = null
    ): Int {
        val owned = player.gearCounts[gearId] ?: 0
        val equipped = player.hands.withIndex().sumOf { (hi, hand) ->
            hand.heroSlots.withIndex().sumOf { (si, slot) ->
                var n = 0
                if (slot.mainHandGearId == gearId) {
                    if (!(hi == excludeHand && si == excludeSlot && excludeWhich == "main")) n++
                }
                if (slot.offHandGearId == gearId) {
                    if (!(hi == excludeHand && si == excludeSlot && excludeWhich == "off")) n++
                }
                n
            }
        }
        return owned - equipped
    }

    fun canEquipGearOnSlot(gearId: String, handIndex: Int, slotIndex: Int, which: String): Boolean {
        val loadout = player.hands.getOrNull(handIndex)?.heroSlots?.getOrNull(slotIndex) ?: return false
        if (loadout.isEmpty) return false
        val hero = content.hero(loadout.heroId) ?: return false
        val gear = content.gear(gearId) ?: return false
        if (availableGearCount(gearId, handIndex, slotIndex, which) <= 0) return false
        return when (which) {
            "main" -> LoadoutHelper.canEquipMainHand(gear, hero, loadout, content)
            "off" -> LoadoutHelper.canEquipOffHand(gear, hero, loadout, content)
            else -> false
        }
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

    fun skipToRareSlot() {
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
            var heroes = s.heroCounts
            recipe.heroInputs.forEach { input ->
                heroes = HeroInventory.remove(heroes, input.heroId, input.stars, input.qty)
            }
            var next = s.copy(
                materials = mats,
                heroCounts = heroes,
                discoveredRecipes = s.discoveredRecipes + recipeId,
                stats = s.stats.copy(craftsDone = s.stats.craftsDone + 1)
            )
            next = when (recipe.resultType) {
                "hero" -> addHeroes(
                    next,
                    mapOf(HeroInventory.stackKey(recipe.resultId, recipe.resultStars) to 1)
                )
                "gear" -> addGear(next, mapOf(recipe.resultId to 1))
                else -> next
            }
            next
        }
        return true
    }

    fun canCraft(recipe: RecipeDef): Boolean {
        if (!recipe.materials.all { m -> (player.materials[m.id] ?: 0) >= m.qty }) return false
        return recipe.heroInputs.all { input ->
            availableHeroCount(input.heroId, input.stars) >= input.qty
        }
    }

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

    fun farmHandMultiplier(): Float = HandPower.farmMultiplier(activeHandPower)

    fun effectiveMaterialPerHour(farm: FarmArea): String {
        val mult = farmHandMultiplier().toDouble()
        val rate = if (farm.id == "cardborn_vault") 1.0 * mult else farm.primaryQtyPerHour * mult
        return formatFarmHourlyRate(rate)
    }

    fun effectiveSecondaryDrop(farm: FarmArea): Pair<String, String>? {
        val drop = farm.secondaryDrop ?: return null
        val rate = drop.ratePerHour * farmHandMultiplier()
        val tierLabel = drop.tier.replaceFirstChar { it.uppercase() }
        val label = when (drop.type) {
            "hero" -> "Hero ($tierLabel)"
            "gear" -> "Gear ($tierLabel)"
            else -> "Bonus"
        }
        return label to formatFarmChancePercent(rate)
    }

    fun effectivePackDrops(farm: FarmArea): List<FarmPackRateRow> =
        farm.packDrops.mapNotNull { drop ->
            val pack = content.pack(drop.packId) ?: return@mapNotNull null
            val rate = drop.ratePerHour * farmHandMultiplier()
            FarmPackRateRow(
                label = "${pack.tier.replaceFirstChar { it.uppercase() }} pack",
                rate = formatFarmChancePercent(rate),
                icon = "ui/pack_ascendant_${pack.tier}.png",
                packName = pack.name
            )
        }

    private fun formatFarmChancePercent(hourlyRate: Double): String {
        if (hourlyRate <= 0) return "0%/hr"
        val pct = hourlyRate * 100.0
        return when {
            pct >= 10 -> String.format("%.0f%%/hr", pct)
            pct >= 1 -> String.format("%.1f%%/hr", pct)
            else -> String.format("%.2f%%/hr", pct)
        }
    }

    private fun formatFarmHourlyRate(rate: Double): String {
        if (rate <= 0) return "0/hr"
        return when {
            rate >= 100 -> "${rate.toInt()}/hr"
            rate >= 1 -> String.format("%.1f/hr", rate)
            rate >= 0.01 -> String.format("%.1f/hr", rate)
            else -> String.format("%.2f/hr", rate)
        }
    }

    private fun handIsBuilt(hand: Hand): Boolean = LoadoutHelper.handIsBuilt(hand)

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
        var heroes = s.heroCounts
        var discovered = s.discoveredHeroes
        gains.forEach { (key, qty) ->
            val (id, stars) = HeroInventory.parseStackKey(key)
            heroes = HeroInventory.add(heroes, id, stars, qty)
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
