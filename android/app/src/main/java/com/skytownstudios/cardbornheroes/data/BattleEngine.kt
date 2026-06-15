package com.skytownstudios.cardbornheroes.data

import kotlin.random.Random

object BattleEngine {
    fun startBattle(
        content: ContentRepository,
        hand: Hand,
        run: CampaignRun
    ): BattleState {
        val allies = hand.heroSlots.mapNotNull { slot ->
            if (slot.isEmpty) return@mapNotNull null
            val hero = content.hero(slot.heroId) ?: return@mapNotNull null
            val stats = LoadoutHelper.mergedStats(slot, content) ?: hero.stats
            BattleUnit(
                id = slot.heroId,
                name = hero.name,
                battleRigId = hero.battleRig,
                mainHandArt = LoadoutHelper.mainHandArt(slot, content),
                offHandArt = LoadoutHelper.offHandArt(slot, content),
                attackStyle = LoadoutHelper.attackStyle(slot, content),
                role = hero.role,
                hp = stats.hp,
                maxHp = stats.hp,
                atk = stats.atk,
                def = stats.def,
                energy = stats.energy
            )
        }

        val pool = run.enemyIds.mapNotNull { content.enemy(it) }
        val statMult = run.enemyStatMultiplier
        val enemies = if (pool.isEmpty()) {
            listOf(fallbackEnemy(run.recommendedPower))
        } else {
            val count = (2 + run.recommendedPower / 12).coerceIn(2, 5)
            (0 until count).map { i ->
                val def = pool[i % pool.size]
                toBattleUnit(def, statMult)
            }
        }

        return BattleState(
            stageName = run.displayName,
            campaignId = run.campaignId,
            campaignLevel = run.level,
            crownRewardMin = run.crownRewardMin,
            crownRewardMax = run.crownRewardMax,
            materialRewards = run.materialRewards,
            allies = allies,
            enemies = enemies,
            logLine = "Battle begins!"
        )
    }

    fun enemyIdsInBattle(state: BattleState): Set<String> =
        state.enemies.mapNotNull { it.id.takeIf { id -> id.isNotEmpty() } }.toSet()

    private fun toBattleUnit(def: EnemyDef, statMult: Float = 1f): BattleUnit {
        val s = def.stats
        val hp = (s.hp * statMult).toInt().coerceAtLeast(1)
        val atk = (s.atk * statMult).toInt().coerceAtLeast(1)
        val defStat = (s.def * statMult).toInt().coerceAtLeast(0)
        val energy = (s.energy * statMult).toInt().coerceAtLeast(0)
        return BattleUnit(
            id = def.id,
            name = def.name,
            artAsset = def.art,
            battleRigId = def.battleRig,
            attackStyle = when (def.role) {
                "ranger" -> "ranged"
                "caster" -> "caster"
                else -> "melee_1h"
            },
            role = def.role,
            hp = hp,
            maxHp = hp,
            atk = atk,
            def = defStat,
            energy = energy
        )
    }

    private fun fallbackEnemy(power: Int): BattleUnit {
        val hp = (power / 2).coerceAtLeast(3)
        val atk = (power / 4).coerceAtLeast(2)
        val def = (power / 5).coerceAtLeast(1)
        return BattleUnit(
            id = "enemy_goblin",
            name = "Goblin",
            battleRigId = "knight",
            attackStyle = "melee_1h",
            role = "ranger",
            hp = hp,
            maxHp = hp,
            atk = atk,
            def = def
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
        val dmg = computeDamage(attacker.value, target.value)
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
        val dmg = computeDamage(attacker.value, target.value)
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

    private fun computeDamage(attacker: BattleUnit, target: BattleUnit): Int {
        var dmg = (attacker.atk - target.def / 2).coerceAtLeast(1)
        if (attacker.role in roleAdvantage(target.role)) dmg = (dmg * 1.25).toInt().coerceAtLeast(1)
        if (target.role in roleAdvantage(attacker.role)) dmg = (dmg * 0.85).toInt().coerceAtLeast(1)
        return dmg
    }

    private fun roleAdvantage(role: String): List<String> = when (role) {
        "tank" -> listOf("caster")
        "ranger" -> listOf("tank")
        "caster" -> listOf("ranger")
        else -> emptyList()
    }

    private fun finish(
        state: BattleState,
        allies: List<BattleUnit>,
        enemies: List<BattleUnit>,
        victory: Boolean
    ): BattleState {
        val crowns = if (victory) {
            CampaignRewardRoll.rollCrowns(state.crownRewardMin, state.crownRewardMax)
        } else 0
        val materials = if (victory) {
            CampaignRewardRoll.rollMaterials(state.materialRewards)
        } else emptyMap()
        return state.copy(
            allies = allies,
            enemies = enemies,
            finished = true,
            victory = victory,
            crownRewardPending = crowns,
            materialRewardPending = materials,
            logLine = if (victory) "Victory!" else "Defeat…",
            activeAttackerName = null
        )
    }
}
