package com.skytownstudios.cardbornheroes.data

import android.content.Context
import org.json.JSONObject

class ContentRepository(context: Context) {
    private val assets = context.assets

    val heroes: List<HeroDef> by lazy { loadHeroes() }
    val gear: List<GearDef> by lazy { loadGear() }
    val materials: List<MaterialDef> by lazy { loadMaterials() }
    val packs: List<PackDef> by lazy { loadPacks() }
    val recipes: List<RecipeDef> by lazy { loadRecipes() }
    val quests: List<QuestDef> by lazy { loadQuests() }
    val campaign: CampaignDef by lazy { loadCampaign() }
    val farmAreas: List<FarmArea> by lazy { loadFarmAreas() }

    fun hero(id: String): HeroDef? = heroes.find { it.id == id }
    fun gear(id: String): GearDef? = gear.find { it.id == id }
    fun material(id: String): MaterialDef? = materials.find { it.id == id }
    fun pack(id: String): PackDef? = packs.find { it.id == id }
    fun recipe(id: String): RecipeDef? = recipes.find { it.id == id }
    fun quest(id: String): QuestDef? = quests.find { it.id == id }
    fun farm(id: String): FarmArea? = farmAreas.find { it.id == id }

    fun heroesOfTier(tier: String): List<HeroDef> = heroes.filter { it.tier == tier }
    fun gearOfTier(tier: String): List<GearDef> = gear.filter { it.tier == tier }

    fun currentStage(stageIndex: Int): CampaignStage? =
        campaign.stages.getOrNull(stageIndex)

    private fun loadJson(name: String): JSONObject =
        assets.open("content/$name").bufferedReader().use { JSONObject(it.readText()) }

    private fun loadHeroes(): List<HeroDef> {
        val arr = loadJson("heroes.json").getJSONArray("heroes")
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            val stats = o.getJSONObject("stats")
            HeroDef(
                id = o.getString("id"),
                name = o.getString("name"),
                role = o.getString("role"),
                tier = o.getString("tier"),
                art = o.getString("art"),
                battleArt = o.getString("battleArt"),
                stats = HeroStats(stats.getInt("hp"), stats.getInt("atk"), stats.getInt("def"))
            )
        }
    }

    private fun loadGear(): List<GearDef> {
        val arr = loadJson("gear.json").getJSONArray("gear")
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            val bonus = o.getJSONObject("bonus")
            val roles = o.getJSONArray("compatibleRoles")
            GearDef(
                id = o.getString("id"),
                name = o.getString("name"),
                tier = o.getString("tier"),
                art = o.getString("art"),
                bonus = GearBonus(bonus.getInt("atk"), bonus.getInt("hp"), bonus.getInt("def")),
                compatibleRoles = (0 until roles.length()).map { roles.getString(it) }
            )
        }
    }

    private fun loadMaterials(): List<MaterialDef> {
        val arr = loadJson("materials.json").getJSONArray("materials")
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            MaterialDef(o.getString("id"), o.getString("name"), o.getString("art"))
        }
    }

    private fun loadPacks(): List<PackDef> {
        val arr = loadJson("packs.json").getJSONArray("packs")
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            PackDef(
                id = o.getString("id"),
                name = o.getString("name"),
                packLine = o.getString("packLine"),
                tier = o.getString("tier"),
                currency = o.getString("currency"),
                cost = o.getInt("cost")
            )
        }
    }

    private fun loadRecipes(): List<RecipeDef> {
        val arr = loadJson("recipes.json").getJSONArray("recipes")
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            val mats = o.getJSONArray("materials")
            RecipeDef(
                id = o.getString("id"),
                name = o.getString("name"),
                resultType = o.getString("resultType"),
                resultId = o.getString("resultId"),
                materials = (0 until mats.length()).map { j ->
                    val m = mats.getJSONObject(j)
                    RecipeMaterial(m.getString("id"), m.getInt("qty"))
                }
            )
        }
    }

    private fun loadQuests(): List<QuestDef> {
        val arr = loadJson("quests.json").getJSONArray("quests")
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            QuestDef(
                id = o.getString("id"),
                title = o.getString("title"),
                description = o.getString("description"),
                rewardPackId = o.getString("rewardPackId"),
                progressTarget = o.getInt("progressTarget"),
                type = o.getString("type")
            )
        }
    }

    private fun loadCampaign(): CampaignDef {
        val campaigns = loadJson("campaigns.json").getJSONArray("campaigns")
        val c = campaigns.getJSONObject(0)
        val stages = c.getJSONArray("stages")
        return CampaignDef(
            id = c.getString("id"),
            name = c.getString("name"),
            stages = (0 until stages.length()).map { i ->
                val s = stages.getJSONObject(i)
                val rewards = s.getJSONObject("rewards")
                CampaignStage(
                    id = s.getString("id"),
                    name = s.getString("name"),
                    recommendedPower = s.getInt("recommendedPower"),
                    crownReward = rewards.getInt("crowns")
                )
            }
        )
    }

    private fun loadFarmAreas(): List<FarmArea> {
        val arr = loadJson("campaigns.json").getJSONArray("farmAreas")
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            val secondary = o.optJSONObject("secondaryDrop")?.let { d ->
                FarmDrop(d.getString("type"), d.getString("tier"), d.getDouble("ratePerHour"))
            }
            val rare = o.optJSONObject("rarePackDrop")?.let { d ->
                FarmPackDrop(d.getString("packId"), d.getDouble("ratePerHour"))
            }
            FarmArea(
                id = o.getString("id"),
                name = o.getString("name"),
                description = o.getString("description"),
                baseCrownsPerHour = o.getInt("baseCrownsPerHour"),
                primaryMaterial = o.getString("primaryMaterial"),
                primaryQtyPerHour = o.getDouble("primaryQtyPerHour"),
                secondaryDrop = secondary,
                rarePackDrop = rare
            )
        }
    }
}
