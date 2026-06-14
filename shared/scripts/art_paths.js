#!/usr/bin/env node
/** Tier-specific art paths per REBUILD_PLAN §14 */
const TIERS = ["basic", "common", "uncommon", "rare", "epic", "mythic", "cardborn"];

function heroArt(suffix, tier) {
  if (tier === "basic") return `cards/${suffix}.png`;
  return `cards/${suffix}_${tier}.png`;
}

function heroBattleArt(suffix) {
  return `cards/${suffix}_battle.png`;
}

function gearArt(suffix, tier) {
  if (tier === "basic") return `cards/${suffix}.png`;
  return `cards/${suffix}_${tier}.png`;
}

module.exports = { TIERS, heroArt, heroBattleArt, gearArt };
