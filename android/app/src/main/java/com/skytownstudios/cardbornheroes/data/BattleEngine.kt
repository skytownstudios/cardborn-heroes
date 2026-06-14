package com.skytownstudios.cardbornheroes.data

import kotlin.random.Random

object BattleEngine {
    private val enemyNames = listOf("Goblin", "Wolf", "Bandit", "Shade", "Brute")

    fun startBattle(
        content: ContentRepository,
        hand: Hand,
        stage: CampaignStage
    ): BattleState {
        val allies = hand.heroSlots.mapNotNull { slot ->
            if (slot.isEmpty) return@mapNotNull null
            val hero = content.hero(slot.heroId) ?: return@mapNotNull null
            val stats = LoadoutHelper.mergedStats(slot, content) ?: hero.stats
            BattleUnit(
                name = hero.name,
                battleRigId = hero.battleRig,
                mainHandArt = LoadoutHelper.mainHandArt(slot, content),
                offHandArt = LoadoutHelper.offHandArt(slot, content),
                attackStyle = LoadoutHelper.attackStyle(slot, content),
                hp = stats.hp,
                maxHp = stats.hp,
                atk = stats.atk,
                def = stats.def
            )
        }

        val enemyCount = (2 + stage.recommendedPower / 200).coerceIn(2, 5)
        val perEnemyPower = stage.recommendedPower / enemyCount
        val enemies = (0 until enemyCount).map { i ->
            val hp = (perEnemyPower * 2).coerceIn(80, 600)
            val atk = (perEnemyPower / 3).coerceIn(15, 120)
            val def = (perEnemyPower / 8).coerceIn(5, 40)
            BattleUnit(
                name = enemyNames[i % enemyNames.size],
                battleRigId = "knight",
                attackStyle = "melee_1h",
                hp = hp,
                maxHp = hp,
                atk = atk,
                def = def
            )
        }

        return BattleState(
            stageName = stage.name,
            crownReward = stage.crownReward,
            allies = allies,
            enemies = enemies,
            logLine = "Battle begins!"
        )
    }

    /** One attack per call — alternates ally then enemy so animations sync to each striker. */
    fun advance(state: BattleState): BattleState {
        if (state.finished) return state

        val allies = state.allies.toMutableList()
        val enemies = state.enemies.toMutableList()
        val livingAllies = allies.withIndex().filter { it.value.hp > 0 }
        val livingEnemies = enemies.withIndex().filter { it.value.hp > 0 }

        if (livingAllies.isEmpty() || livingEnemies.isEmpty()) {
            return finish(state, allies, enemies, livingEnemies.isEmpty() && livingAllies.isNotEmpty())
        }

        return if (state.nextSide == "ally") {
            allyAttack(state, allies, enemies, livingAllies, livingEnemies)
        } else {
            enemyAttack(state, allies, enemies, livingAllies, livingEnemies)
        }
    }

    private fun allyAttack(
        state: BattleState,
        allies: MutableList<BattleUnit>,
        enemies: MutableList<BattleUnit>,
        livingAllies: List<IndexedValue<BattleUnit>>,
        livingEnemies: List<IndexedValue<BattleUnit>>
    ): BattleState {
        if (livingEnemies.isEmpty()) {
            return finish(state, allies, enemies, victory = true)
        }
        val attacker = livingAllies.maxByOrNull { it.value.atk }!!
        val target = livingEnemies.minByOrNull { it.value.hp }!!
        val dmg = (attacker.value.atk - target.value.def / 2).coerceAtLeast(1)
        val newHp = (target.value.hp - dmg).coerceAtLeast(0)
        enemies[target.index] = target.value.copy(hp = newHp)

        val log = "${attacker.value.name} hits ${target.value.name} for $dmg"
        val enemiesAlive = enemies.any { it.hp > 0 }
        val alliesAlive = allies.any { it.hp > 0 }

        if (!enemiesAlive) {
            return finish(
                state.copy(logLine = log, activeAttackerName = attacker.value.name),
                allies,
                enemies,
                victory = true
            )
        }
        if (!alliesAlive) {
            return finish(
                state.copy(logLine = log, activeAttackerName = attacker.value.name),
                allies,
                enemies,
                victory = false
            )
        }

        return state.copy(
            allies = allies,
            enemies = enemies,
            logLine = log,
            activeAttackerName = attacker.value.name,
            nextSide = "enemy"
        )
    }

    private fun enemyAttack(
        state: BattleState,
        allies: MutableList<BattleUnit>,
        enemies: MutableList<BattleUnit>,
        livingAllies: List<IndexedValue<BattleUnit>>,
        livingEnemies: List<IndexedValue<BattleUnit>>
    ): BattleState {
        if (livingAllies.isEmpty()) {
            return finish(state, allies, enemies, victory = false)
        }
        val attacker = livingEnemies.maxByOrNull { it.value.atk }!!
        val target = livingAllies.minByOrNull { it.value.hp }!!
        val dmg = (attacker.value.atk - target.value.def / 2).coerceAtLeast(1)
        val newHp = (target.value.hp - dmg).coerceAtLeast(0)
        allies[target.index] = target.value.copy(hp = newHp)

        var log = "${attacker.value.name} hits ${target.value.name} for $dmg"
        if (Random.nextFloat() < 0.12f) log = "$log · Clash continues"

        val enemiesAlive = enemies.any { it.hp > 0 }
        val alliesAlive = allies.any { it.hp > 0 }

        if (!alliesAlive || !enemiesAlive) {
            return finish(
                state.copy(logLine = log, activeAttackerName = attacker.value.name),
                allies,
                enemies,
                victory = enemiesAlive.not() && alliesAlive
            )
        }

        return state.copy(
            allies = allies,
            enemies = enemies,
            logLine = log,
            activeAttackerName = attacker.value.name,
            nextSide = "ally"
        )
    }

    private fun finish(
        state: BattleState,
        allies: List<BattleUnit>,
        enemies: List<BattleUnit>,
        victory: Boolean
    ): BattleState = state.copy(
        allies = allies,
        enemies = enemies,
        finished = true,
        victory = victory,
        crownRewardPending = if (victory) state.crownReward else 0,
        logLine = if (victory) "Victory!" else "Defeat…",
        activeAttackerName = null
    )
}
