#!/usr/bin/env node
const fs = require("fs");
const path = require("path");

const ROOT = path.join(__dirname, "..", "content");
const TIER_ORDER = ["basic", "common", "uncommon", "rare", "epic", "mythic", "cardborn"];
const STAR_BASE = 1.5;

const ROLE_TEMPLATES = {
  tank: { hp: 5, atk: 2, def: 3, energy: 0 },
  ranger: { hp: 3, atk: 4, def: 3, energy: 0 },
  caster: { hp: 2, atk: 5, def: 3, energy: 0 },
};

const GEAR_TEMPLATES = {
  sword: { atk: 2, hp: 0, def: 1, energy: 0 },
  shield: { atk: 0, hp: 1, def: 2, energy: 0 },
  bow: { atk: 3, hp: 0, def: 0, energy: 0 },
  staff: { atk: 2, hp: 0, def: 0, energy: 1 },
};

function tierMult(tier) {
  const idx = Math.max(0, TIER_ORDER.indexOf(tier));
  return Math.pow(STAR_BASE, idx * 5);
}

function scaleTemplate(template, mult) {
  const out = {};
  for (const [k, v] of Object.entries(template)) {
    if (k === "energy") out[k] = Math.max(0, Math.round(v * mult));
    else out[k] = v > 0 ? Math.max(1, Math.round(v * mult)) : 0;
  }
  return out;
}

function heroStats(role, tier, unitKind) {
  const base = ROLE_TEMPLATES[role] || ROLE_TEMPLATES.tank;
  let mult = tierMult(tier);
  if (unitKind === "hero" && tier === "basic") mult *= STAR_BASE;
  return scaleTemplate(base, mult);
}

function gearKind(id) {
  for (const kind of Object.keys(GEAR_TEMPLATES)) {
    if (id.includes(kind)) return kind;
  }
  return "sword";
}

function gearBonus(id, tier) {
  return scaleTemplate(GEAR_TEMPLATES[gearKind(id)], tierMult(tier));
}

const heroesPath = path.join(ROOT, "heroes.json");
const gearPath = path.join(ROOT, "gear.json");

const heroesData = JSON.parse(fs.readFileSync(heroesPath, "utf8"));
for (const hero of heroesData.heroes) {
  hero.stats = heroStats(hero.role || "tank", hero.tier || "basic", hero.unitKind || "hero");
}
fs.writeFileSync(heroesPath, JSON.stringify(heroesData, null, 2) + "\n");

const gearData = JSON.parse(fs.readFileSync(gearPath, "utf8"));
for (const item of gearData.gear) {
  item.bonus = gearBonus(item.id, item.tier || "basic");
}
fs.writeFileSync(gearPath, JSON.stringify(gearData, null, 2) + "\n");

console.log("Rescaled heroes.json and gear.json");
