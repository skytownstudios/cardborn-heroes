package com.skytownstudios.cardbornheroes.data

import kotlin.random.Random

object BattleEngine {
    private val enemyNames = listOf("Goblin", "Wolf", "Bandit", "Shade", "Brute")

    fun startBattle(
        content: ContentRepository,
        hand: Hand,
        stage: CampaignStage
    ): BattleState {
        val allies = hand.slots.mapNotNull { slot ->
            if (slot.isEmpty) return@mapNotNull null
            when (slot.type) {
                "hero" -> {
                    val h = content.hero(slot.cardId) ?: return@mapNotNull null
                    BattleUnit(
                        name = h.name,
                        battleRigId = h.battleRig,
                        hp = h.stats.hp,
                        maxHp = h.stats.hp,
                        atk = h.stats.atk,
                        def = h.stats.def
                    )
                }
                "gear" -> {
                    val g = content.gear(slot.cardId) ?: return@mapNotNull null
                    val hp = (g.bonus.hp + g.bonus.def).coerceAtLeast(50)
                    BattleUnit(
                        name = g.name,
                        artAsset = g.art,
                        hp = hp,
                        maxHp = hp,
                        atk = g.bonus.atk,
                        def = g.bonus.def
                    )
                }
                else -> null
            }
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

    fun advance(state: BattleState): BattleState {
        if (state.finished) return state

        var allies = state.allies
        var enemies = state.enemies
        var log = state.logLine

        val livingAllies = allies.withIndex().filter { it.value.hp > 0 }
        val livingEnemies = enemies.withIndex().filter { it.value.hp > 0 }

        if (livingAllies.isEmpty() || livingEnemies.isEmpty()) {
            val victory = livingEnemies.isEmpty() && livingAllies.isNotEmpty()
            return state.copy(
                allies = allies,
                enemies = enemies,
                finished = true,
                victory = victory,
                crownRewardPending = if (victory) state.crownReward else 0,
                logLine = if (victory) "Victory!" else "Defeat…"
            )
        }

        val attacker = livingAllies.maxByOrNull { it.value.atk }!!
        val target = livingEnemies.minByOrNull { it.value.hp }!!
        val dmg = (attacker.value.atk - target.value.def / 2).coerceAtLeast(1)
        val newHp = (target.value.hp - dmg).coerceAtLeast(0)
        enemies = enemies.toMutableList().also { it[target.index] = target.value.copy(hp = newHp) }
        log = "${attacker.value.name} hits ${target.value.name} for $dmg"

        val livingEnemies2 = enemies.withIndex().filter { it.value.hp > 0 }
        val livingAllies2 = allies.withIndex().filter { it.value.hp > 0 }

        if (livingEnemies2.isEmpty()) {
            return state.copy(
                allies = allies,
                enemies = enemies,
                finished = true,
                victory = true,
                crownRewardPending = state.crownReward,
                logLine = "Victory!"
            )
        }
        if (livingAllies2.isEmpty()) {
            return state.copy(
                allies = allies,
                enemies = enemies,
                finished = true,
                victory = false,
                logLine = "Defeat…"
            )
        }

        val eAttacker = livingEnemies2.maxByOrNull { it.value.atk }!!
        val eTarget = livingAllies2.minByOrNull { it.value.hp }!!
        val eDmg = (eAttacker.value.atk - eTarget.value.def / 2).coerceAtLeast(1)
        val eNewHp = (eTarget.value.hp - eDmg).coerceAtLeast(0)
        allies = allies.toMutableList().also { it[eTarget.index] = eTarget.value.copy(hp = eNewHp) }
        log = "${eAttacker.value.name} hits ${eTarget.value.name} for $eDmg"

        val alliesAlive = allies.any { it.hp > 0 }
        val enemiesAlive = enemies.any { it.hp > 0 }

        if (!alliesAlive || !enemiesAlive) {
            val victory = enemiesAlive.not() && alliesAlive
            return state.copy(
                allies = allies,
                enemies = enemies,
                finished = true,
                victory = victory,
                crownRewardPending = if (victory) state.crownReward else 0,
                logLine = if (victory) "Victory!" else "Defeat…"
            )
        }

        if (Random.nextFloat() < 0.15f) {
            log = "$log · Clash continues"
        }

        return state.copy(allies = allies, enemies = enemies, logLine = log)
    }
}
