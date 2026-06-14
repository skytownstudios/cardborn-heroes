#!/usr/bin/env node
/** Generate tiered heroes/gear + Ascendant pack definitions. */
const fs = require("fs");
const path = require("path");

const ROOT = path.join(__dirname, "..");
const CONTENT = path.join(ROOT, "content");

const PACK_TIERS = ["basic", "common", "uncommon", "rare", "epic", "mythic"];
const DROP_TIERS = [...PACK_TIERS, "cardborn"];
const MULT = { basic: 1.0, common: 1.15, uncommon: 1.32, rare: 1.52, epic: 1.75, mythic: 2.0, cardborn: 2.35 };
const TIER_LABEL = { basic: "Basic", common: "Common", uncommon: "Uncommon", rare: "Rare", epic: "Epic", mythic: "Mythic", cardborn: "Cardborn" };

const HERO_BASE = [
  { suffix: "knight", name: "Knight", role: "tank", art: "cards/knight.png", battleArt: "cards/knight_battle.png", stats: { hp: 1200, atk: 85, def: 60 }, description: "Frontline shield bearer." },
  { suffix: "archer", name: "Archer", role: "ranger", art: "cards/archer.png", battleArt: "cards/archer_battle.png", stats: { hp: 750, atk: 110, def: 35 }, description: "Ranged striker." },
  { suffix: "mage", name: "Mage", role: "caster", art: "cards/mage.png", battleArt: "cards/mage_battle.png", stats: { hp: 680, atk: 130, def: 25 }, description: "Arcane damage dealer." },
];
const GEAR_BASE = [
  { suffix: "sword", name: "Sword", slot: "weapon", art: "cards/sword.png", bonus: { atk: 25, def: 10, hp: 0 }, compatibleRoles: ["tank", "ranger"], description: "A sturdy blade." },
  { suffix: "bow", name: "Bow", slot: "weapon", art: "cards/bow.png", bonus: { atk: 35, def: 0, hp: 50 }, compatibleRoles: ["ranger"], description: "Standard ranged weapon." },
  { suffix: "staff", name: "Staff", slot: "weapon", art: "cards/staff.png", bonus: { atk: 40, def: 0, hp: 30 }, compatibleRoles: ["caster"], description: "Focuses arcane power." },
];

function scaleStats(stats, mult) {
  const out = {};
  for (const [k, v] of Object.entries(stats)) out[k] = Math.max(1, Math.floor(v * mult));
  return out;
}
function cardId(prefix, suffix, tier) {
  return tier === "basic" ? `${prefix}_${suffix}` : `${prefix}_${suffix}_${tier}`;
}

const heroes = [];
const gear = [];
for (const tier of DROP_TIERS) {
  const label = TIER_LABEL[tier];
  const mult = MULT[tier];
  for (const base of HERO_BASE) {
    heroes.push({
      id: cardId("hero", base.suffix, tier),
      name: `${label} ${base.name}`,
      role: base.role,
      tier,
      rarity: tier,
      art: base.art,
      battleArt: base.battleArt,
      stats: scaleStats(base.stats, mult),
      description: `${label}-tier ${base.description}`,
    });
  }
  for (const base of GEAR_BASE) {
    const bonus = scaleStats(base.bonus, mult);
    gear.push({
      id: cardId("gear", base.suffix, tier),
      name: `${label} ${base.name}`,
      slot: base.slot,
      tier,
      rarity: tier,
      art: base.art,
      bonus: { atk: bonus.atk, hp: bonus.hp, def: bonus.def || 0 },
      compatibleRoles: base.compatibleRoles,
      description: `${label}-tier ${base.description}`,
    });
  }
}

const crownCosts = [500, 1200, 2500, 0, 0, 0];
const sigilCosts = [0, 0, 0, 150, 350, 750];
const packs = PACK_TIERS.map((tier, i) => {
  const crowns = crownCosts[i];
  const sigils = sigilCosts[i];
  return {
    id: `ascendant_${tier}`,
    name: `${TIER_LABEL[tier]} Ascendant Pack`,
    packLine: "ascendant",
    tier,
    description: `Ascendant pack — 8 mixed cards/materials (${tier} or lower), 1 guaranteed ${tier}+ slot (hero or gear).`,
    currency: crowns === 0 ? "sigils" : "crowns",
    cost: crowns === 0 ? sigils : crowns,
  };
});

fs.mkdirSync(CONTENT, { recursive: true });
fs.writeFileSync(path.join(CONTENT, "heroes.json"), JSON.stringify({ heroes }, null, 2) + "\n");
fs.writeFileSync(path.join(CONTENT, "gear.json"), JSON.stringify({ gear }, null, 2) + "\n");
fs.writeFileSync(path.join(CONTENT, "packs.json"), JSON.stringify({ packs }, null, 2) + "\n");
console.log(`Wrote ${heroes.length} heroes, ${gear.length} gear, ${packs.length} packs`);
