package com.skytownstudios.cardbornheroes.data

import android.content.Context
import org.json.JSONObject

class ContentRepository(context: Context) {
    private val assets = context.assets

    val rigManifest: RigManifest by lazy { RigManifest.load(context) }

    val heroes: List<HeroDef> by lazy { loadHeroes() }
    val enemies: List<EnemyDef> by lazy { loadEnemies() }
    val gear: List<GearDef> by lazy { loadGear() }
    val materials: List<MaterialDef> by lazy { loadMaterials() }
    val packs: List<PackDef> by lazy { loadPacks() }
    val recipes: List<RecipeDef> by lazy { loadRecipes() }
    val quests: List<QuestDef> by lazy { loadQuests() }
    val campaignZones: List<CampaignZone> by lazy { loadCampaignZones() }
    val farmAreas: List<FarmArea> by lazy { loadFarmAreas() }

    fun hero(id: String): HeroDef? = heroes.find { it.id == id }
    fun enemy(id: String): EnemyDef? = enemies.find { it.id == id }
    fun gear(id: String): GearDef? = gear.find { it.id == id }
    fun material(id: String): MaterialDef? = materials.find { it.id == id }
    fun pack(id: String): PackDef? = packs.find { it.id == id }
    fun recipe(id: String): RecipeDef? = recipes.find { it.id == id }
    fun quest(id: String): QuestDef? = quests.find { it.id == id }
    fun farm(id: String): FarmArea? = farmAreas.find { it.id == id }
    fun campaignZone(id: String): CampaignZone? = campaignZones.find { it.id == id }

    fun campaignRun(campaignId: String, level: Int): CampaignRun? =
        campaignZone(campaignId)?.let { CampaignScaler.resolve(it, level) }

    fun heroesOfTier(tier: String): List<HeroDef> = heroes.filter { it.tier == tier }
    fun heroesOfUnitKind(unitKind: String): List<HeroDef> = heroes.filter { it.unitKind == unitKind }
    fun gearOfTier(tier: String): List<GearDef> = gear.filter { it.tier == tier }

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
                unitKind = o.optString("unitKind", "hero"),
                art = o.getString("art"),
                battleRig = o.optString("battleRig", "knight"),
                stats = HeroStats(
                    stats.getInt("hp"),
                    stats.getInt("atk"),
                    stats.getInt("def"),
                    stats.optInt("energy", 0)
                )
            )
        }
    }

    private fun loadEnemies(): List<EnemyDef> {
        val arr = loadJson("enemies.json").getJSONArray("enemies")
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            val stats = o.getJSONObject("stats")
            val weak = o.getJSONArray("weakTo")
            val strong = o.getJSONArray("strongAgainst")
            EnemyDef(
                id = o.getString("id"),
                name = o.getString("name"),
                role = o.getString("role"),
                art = o.getString("art"),
                battleRig = o.optString("battleRig", "knight"),
                stats = HeroStats(
                    stats.getInt("hp"),
                    stats.getInt("atk"),
                    stats.getInt("def"),
                    stats.optInt("energy", 0)
                ),
                weakTo = (0 until weak.length()).map { weak.getString(it) },
                strongAgainst = (0 until strong.length()).map { strong.getString(it) },
                counterTip = o.getString("counterTip")
            )
        }
    }

    private fun loadGear(): List<GearDef> {
        val arr = loadJson("gear.json").getJSONArray("gear")
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            val bonus = o.getJSONObject("bonus")
            val roles = o.getJSONArray("compatibleRoles")
            val id = o.getString("id")
            val art = o.getString("art")
            val hands = if (o.has("hands")) o.getInt("hands") else defaultHands(id)
            val hand = if (o.has("hand")) o.getString("hand") else defaultHand(id)
            val battleArt = if (o.has("battleArt")) o.getString("battleArt") else art
            GearDef(
                id = id,
                name = o.getString("name"),
                tier = o.getString("tier"),
                art = art,
                battleArt = battleArt,
                hands = hands,
                hand = hand,
                bonus = GearBonus(
                    bonus.getInt("atk"),
                    bonus.getInt("hp"),
                    bonus.getInt("def"),
                    bonus.optInt("energy", 0)
                ),
                compatibleRoles = (0 until roles.length()).map { roles.getString(it) }
            )
        }
    }

    private fun defaultHands(id: String): Int = when {
        id.contains("staff") -> 2
        else -> 1
    }

    private fun defaultHand(id: String): String = when {
        id.contains("shield") -> "off"
        id.contains("staff") -> "main"
        else -> "main"
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
            val heroInputs = o.optJSONArray("heroInputs")?.let { inputs ->
                (0 until inputs.length()).map { j ->
                    val h = inputs.getJSONObject(j)
                    RecipeHeroInput(h.getString("heroId"), h.optInt("stars", 0), h.getInt("qty"))
                }
            } ?: emptyList()
            RecipeDef(
                id = o.getString("id"),
                name = o.getString("name"),
                resultType = o.getString("resultType"),
                resultId = o.getString("resultId"),
                materials = (0 until mats.length()).map { j ->
                    val m = mats.getJSONObject(j)
                    RecipeMaterial(m.getString("id"), m.getInt("qty"))
                },
                recipeType = o.optString("recipeType", "craft"),
                heroInputs = heroInputs,
                resultStars = o.optInt("resultStars", 0)
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

    private fun parseCampaignZone(o: org.json.JSONObject): CampaignZone {
        val rewards = o.getJSONObject("rewards")
        val crowns = rewards.getJSONObject("crowns")
        val enemyArr = o.getJSONArray("enemies")
        val matArr = rewards.optJSONArray("materials")
        val materialRewards = if (matArr != null) {
            (0 until matArr.length()).map { j ->
                val m = matArr.getJSONObject(j)
                CampaignMaterialReward(
                    id = m.getString("id"),
                    min = m.getInt("min"),
                    max = m.getInt("max"),
                    weight = m.optDouble("weight", 1.0)
                )
            }
        } else emptyList()
        return CampaignZone(
            id = o.getString("id"),
            name = o.getString("name"),
            description = o.getString("description"),
            mapArt = o.optString("mapArt", "maps/whispering_woods.png"),
            icon = o.optString("icon", "ui/icon_bag.png"),
            baseRecommendedPower = o.getInt("baseRecommendedPower"),
            enemyIds = (0 until enemyArr.length()).map { enemyArr.getString(it) },
            crownRewardMin = crowns.getInt("min"),
            crownRewardMax = crowns.getInt("max"),
            materialRewards = materialRewards
        )
    }

    private fun loadCampaignZones(): List<CampaignZone> {
        val arr = loadJson("campaigns.json").getJSONArray("campaigns")
        return (0 until arr.length()).map { i -> parseCampaignZone(arr.getJSONObject(i)) }
    }

    private fun loadFarmAreas(): List<FarmArea> {
        val arr = loadJson("campaigns.json").getJSONArray("farmAreas")
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            val secondary = o.optJSONObject("secondaryDrop")?.let { d ->
                FarmDrop(d.getString("type"), d.getString("tier"), d.getDouble("ratePerHour"))
            }
            val packDrops = buildList {
                val arr = o.optJSONArray("packDrops")
                if (arr != null) {
                    for (j in 0 until arr.length()) {
                        val d = arr.getJSONObject(j)
                        add(FarmPackDrop(d.getString("packId"), d.getDouble("ratePerHour")))
                    }
                } else {
                    o.optJSONObject("rarePackDrop")?.let { d ->
                        add(FarmPackDrop(d.getString("packId"), d.getDouble("ratePerHour")))
                    }
                }
            }
            FarmArea(
                id = o.getString("id"),
                name = o.getString("name"),
                description = o.getString("description"),
                baseCrownsPerHour = o.getInt("baseCrownsPerHour"),
                primaryMaterial = o.getString("primaryMaterial"),
                primaryQtyPerHour = o.getDouble("primaryQtyPerHour"),
                secondaryDrop = secondary,
                packDrops = packDrops
            )
        }
    }
}
