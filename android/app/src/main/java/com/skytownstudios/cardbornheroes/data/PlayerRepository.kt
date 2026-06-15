package com.skytownstudios.cardbornheroes.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.playerDataStore: DataStore<Preferences> by preferencesDataStore("player_state")

class PlayerRepository(private val context: Context) {
    private val key = stringPreferencesKey("state_json")

    val playerFlow: Flow<PlayerState> = context.playerDataStore.data.map { prefs ->
        val json = prefs[key]
        if (json.isNullOrBlank()) PlayerState() else decode(json)
    }

    suspend fun load(): PlayerState = playerFlow.first()

    suspend fun save(state: PlayerState) {
        context.playerDataStore.edit { it[key] = encode(state) }
    }

    private fun encode(s: PlayerState): String = JSONObject().apply {
        put("schemaVersion", SCHEMA_VERSION)
        put("crowns", s.crowns)
        put("sigils", s.sigils)
        put("materials", mapToJson(s.materials))
        put("packInventory", mapToJson(s.packInventory))
        put("heroCounts", mapToJson(s.heroCounts))
        put("gearCounts", mapToJson(s.gearCounts))
        put("hands", handsToJson(s.hands))
        put("activeHandIndex", s.activeHandIndex)
        put("activeCampaignId", s.activeCampaignId)
        put("activeCampaignLevel", s.activeCampaignLevel)
        put("campaignBestLevel", mapToJson(s.campaignBestLevel))
        put("activeFarmId", s.activeFarmId)
        put("lastFarmTickEpochMs", s.lastFarmTickEpochMs)
        put("questProgress", mapToJson(s.questProgress))
        put("questClaimed", setToJson(s.questClaimed))
        put("discoveredHeroes", setToJson(s.discoveredHeroes))
        put("discoveredGear", setToJson(s.discoveredGear))
        put("discoveredEnemies", setToJson(s.discoveredEnemies))
        put("discoveredRecipes", setToJson(s.discoveredRecipes))
        put("stats", statsToJson(s.stats))
        put("farmCrownsRemainder", s.farmCrownsRemainder)
        put("farmMaterialRemainder", s.farmMaterialRemainder)
        put("pendingFarmRewards", pendingFarmToJson(s.pendingFarmRewards))
    }.toString()

    private fun decode(json: String): PlayerState {
        val o = JSONObject(json)
        val version = o.optInt("schemaVersion", 1)
        val hands = if (version >= 2) {
            jsonToHandsV2(o.optJSONArray("hands"))
        } else {
            migrateHandsV1(o.optJSONArray("hands"))
        }
        val rawHeroCounts = jsonToMap(o.optJSONObject("heroCounts"))
        val heroCounts = if (version < 3) HeroInventory.migrateFlatCounts(rawHeroCounts) else rawHeroCounts
        val (activeCampaignId, activeCampaignLevel, campaignBestLevel) = if (version < 4) {
            Triple("whispering_woods", 1, emptyMap<String, Int>())
        } else {
            Triple(
                o.optString("activeCampaignId", "whispering_woods"),
                o.optInt("activeCampaignLevel", 1),
                jsonToMap(o.optJSONObject("campaignBestLevel"))
            )
        }
        return PlayerState(
            crowns = o.optInt("crowns", 2000),
            sigils = o.optInt("sigils", 1000),
            materials = jsonToMap(o.optJSONObject("materials")),
            packInventory = jsonToMap(o.optJSONObject("packInventory")),
            heroCounts = heroCounts,
            gearCounts = jsonToMap(o.optJSONObject("gearCounts")),
            hands = hands,
            activeHandIndex = o.optInt("activeHandIndex", 0),
            activeCampaignId = activeCampaignId,
            activeCampaignLevel = activeCampaignLevel,
            campaignBestLevel = campaignBestLevel,
            activeFarmId = o.optString("activeFarmId", "goblin_hills"),
            lastFarmTickEpochMs = o.optLong("lastFarmTickEpochMs", System.currentTimeMillis()),
            questProgress = jsonToMap(o.optJSONObject("questProgress")),
            questClaimed = jsonToSet(o.optJSONArray("questClaimed")),
            discoveredHeroes = jsonToSet(o.optJSONArray("discoveredHeroes")),
            discoveredGear = jsonToSet(o.optJSONArray("discoveredGear")),
            discoveredEnemies = jsonToSet(o.optJSONArray("discoveredEnemies")),
            discoveredRecipes = jsonToSet(o.optJSONArray("discoveredRecipes")),
            stats = jsonToStats(o.optJSONObject("stats")),
            farmCrownsRemainder = o.optDouble("farmCrownsRemainder", 0.0),
            farmMaterialRemainder = o.optDouble("farmMaterialRemainder", 0.0),
            pendingFarmRewards = jsonToPendingFarm(o.optJSONObject("pendingFarmRewards"))
        )
    }

    private fun pendingFarmToJson(p: PendingFarmRewards): JSONObject = JSONObject().apply {
        put("crowns", p.crowns)
        put("materials", mapToJson(p.materials))
        put("heroes", mapToJson(p.heroes))
        put("gear", mapToJson(p.gear))
        put("packs", mapToJson(p.packs))
    }

    private fun jsonToPendingFarm(o: JSONObject?): PendingFarmRewards {
        if (o == null) return PendingFarmRewards()
        return PendingFarmRewards(
            crowns = o.optInt("crowns"),
            materials = jsonToMap(o.optJSONObject("materials")),
            heroes = jsonToMap(o.optJSONObject("heroes")),
            gear = jsonToMap(o.optJSONObject("gear")),
            packs = jsonToMap(o.optJSONObject("packs"))
        )
    }

    private fun migrateHandsV1(a: JSONArray?): List<Hand> {
        if (a == null) return defaultStarterHands()
        return (0 until a.length()).map { i ->
            val slotsArr = a.getJSONArray(i)
            val heroSlots = MutableList(5) { HeroLoadout() }
            var lastHeroIndex = -1
            for (j in 0 until slotsArr.length().coerceAtMost(5)) {
                val s = slotsArr.getJSONObject(j)
                val type = s.optString("type", "")
                val cardId = s.optString("cardId", "")
                if (cardId.isEmpty()) continue
                when (type) {
                    "hero" -> {
                        val idx = heroSlots.indexOfFirst { it.isEmpty }
                        if (idx >= 0) {
                            heroSlots[idx] = HeroLoadout(heroId = cardId)
                            lastHeroIndex = idx
                        }
                    }
                    "gear" -> {
                        if (lastHeroIndex < 0) continue
                        val loadout = heroSlots[lastHeroIndex]
                        heroSlots[lastHeroIndex] = when {
                            loadout.mainHandGearId.isEmpty() -> loadout.copy(mainHandGearId = cardId)
                            loadout.offHandGearId.isEmpty() -> loadout.copy(offHandGearId = cardId)
                            else -> loadout
                        }
                    }
                }
            }
            Hand(heroSlots)
        }.let { list ->
            if (list.size >= 3) list else list + List(3 - list.size) { Hand() }
        }
    }

    private fun handsToJson(hands: List<Hand>): JSONArray = JSONArray().apply {
        hands.forEach { hand ->
            put(JSONArray().apply {
                hand.heroSlots.forEach { slot ->
                    put(JSONObject().apply {
                        put("heroId", slot.heroId)
                        put("heroStars", slot.heroStars)
                        put("mainHandGearId", slot.mainHandGearId)
                        put("offHandGearId", slot.offHandGearId)
                    })
                }
            })
        }
    }

    private fun jsonToHandsV2(a: JSONArray?): List<Hand> {
        if (a == null) return defaultStarterHands()
        return (0 until a.length()).map { i ->
            val slotsArr = a.getJSONArray(i)
            val heroSlots = (0 until 5).map { j ->
                if (j < slotsArr.length()) {
                    val s = slotsArr.getJSONObject(j)
                    HeroLoadout(
                        heroId = s.optString("heroId", ""),
                        heroStars = s.optInt("heroStars", 0),
                        mainHandGearId = s.optString("mainHandGearId", ""),
                        offHandGearId = s.optString("offHandGearId", "")
                    )
                } else HeroLoadout()
            }
            Hand(heroSlots)
        }.let { list ->
            if (list.size >= 3) list else list + List(3 - list.size) { Hand() }
        }
    }

    private fun mapToJson(m: Map<String, Int>): JSONObject =
        JSONObject().apply { m.forEach { (k, v) -> put(k, v) } }

    private fun jsonToMap(o: JSONObject?): Map<String, Int> {
        if (o == null) return emptyMap()
        return buildMap {
            o.keys().forEach { k -> put(k, o.getInt(k)) }
        }
    }

    private fun setToJson(s: Set<String>): JSONArray = JSONArray(s.toList())

    private fun jsonToSet(a: JSONArray?): Set<String> {
        if (a == null) return emptySet()
        return buildSet {
            for (i in 0 until a.length()) add(a.getString(i))
        }
    }

    private fun statsToJson(s: PlayerLifetimeStats): JSONObject = JSONObject().apply {
        put("wins", s.wins)
        put("losses", s.losses)
        put("crownsEarned", s.crownsEarned)
        put("packsOpened", s.packsOpened)
        put("craftsDone", s.craftsDone)
        put("questsClaimed", s.questsClaimed)
    }

    private fun jsonToStats(o: JSONObject?): PlayerLifetimeStats {
        if (o == null) return PlayerLifetimeStats()
        return PlayerLifetimeStats(
            wins = o.optInt("wins"),
            losses = o.optInt("losses"),
            crownsEarned = o.optInt("crownsEarned"),
            packsOpened = o.optInt("packsOpened"),
            craftsDone = o.optInt("craftsDone"),
            questsClaimed = o.optInt("questsClaimed")
        )
    }

    companion object {
        const val SCHEMA_VERSION = 4
    }
}
